package ru.practicum.ewm_main.event.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.ewm_main.category.repository.CategoryRepository;
import ru.practicum.ewm_main.client.EventClient;
import ru.practicum.ewm_main.event.EventMapper;
import ru.practicum.ewm_main.event.dto.AdminUpdateEventDto;
import ru.practicum.ewm_main.event.dto.EventDto;
import ru.practicum.ewm_main.event.dto.ShortEventDto;
import ru.practicum.ewm_main.event.dto.UserUpdateEventDto;
import ru.practicum.ewm_main.event.model.Event;
import ru.practicum.ewm_main.event.model.Location;
import ru.practicum.ewm_main.event.model.State;
import ru.practicum.ewm_main.event.repository.EventRepository;
import ru.practicum.ewm_main.event.repository.LocationRepository;
import ru.practicum.ewm_main.exception.BadRequestException;
import ru.practicum.ewm_main.exception.NotFoundException;
import ru.practicum.ewm_main.participation.repository.ParticipationRepository;
import ru.practicum.ewm_main.user.model.User;
import ru.practicum.ewm_main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.ewm_main.event.EventMapper.toEvent;
import static ru.practicum.ewm_main.event.EventMapper.toEventDto;
import static ru.practicum.ewm_main.event.LocationMapper.toLocation;
import static ru.practicum.ewm_main.event.model.State.*;
import static ru.practicum.ewm_main.participation.model.StatusRequest.CONFIRMED;

@Service
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventClient eventClient;
    private final ParticipationRepository participationRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public EventServiceImpl(EventRepository eventRepository, EventClient eventClient, ParticipationRepository participationRepository, CategoryRepository categoryRepository, LocationRepository locationRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.eventClient = eventClient;
        this.participationRepository = participationRepository;
        this.categoryRepository = categoryRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ShortEventDto> getEvents(String text, List<Long> categoryIds, Boolean paid, String rangeStart,
                                         String rangeEnd, Boolean onlyAvailable, String sort, int from, int size) {
        LocalDateTime start;
        if (rangeStart == null) {
            start = LocalDateTime.now();
        } else {
            start = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        LocalDateTime end;
        if (rangeEnd == null) {
            end = LocalDateTime.MAX;
        } else {
            end = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        List<ShortEventDto> events = eventRepository.searchEvents(text, categoryIds, paid, start, end, PUBLISHED,
                PageRequest.of(from / size, size))
                .stream()
                .map(EventMapper::toShortEventDto)
                .map(this::setViewsAndConfirmedRequests)
                .collect(Collectors.toList());
        if (Boolean.TRUE.equals(onlyAvailable)) {
            events = events.stream().filter(shortEventDto ->
                shortEventDto.getConfirmedRequests() < eventRepository
                        .findById(shortEventDto.getId()).get().getParticipantLimit() ||
                        eventRepository.findById(shortEventDto.getId()).get().getParticipantLimit() == 0
            ).collect(Collectors.toList());
        }
        if (sort != null) {
            switch (sort) {
                case "EVENT_DATE":
                    events = events
                            .stream()
                            .sorted(Comparator.comparing(ShortEventDto::getEventDate))
                            .collect(Collectors.toList());
                    break;
                case "VIEWS":
                    events = events
                            .stream()
                            .sorted(Comparator.comparing(ShortEventDto::getViews))
                            .collect(Collectors.toList());
                    break;
                default:
                    throw new BadRequestException("can be sorted only by views or event date");
            }
        }
        return events;
    }

    @Override
    public EventDto getEvent(Long id) {
        Event event = checkAndGetEvent(id);
        if (!event.getState().equals(PUBLISHED)) {
            throw new BadRequestException("event must be published");
        }
        return setViewsAndConfirmedRequests(toEventDto(event));
    }

    @Override
    public List<ShortEventDto> getUserEvents(Long userId, int from, int size) {
        User user = checkAndGetUser(userId);
        return eventRepository.findAllByInitiatorId(userId, PageRequest.of(from / size, size))
                .stream()
                .map(EventMapper::toShortEventDto)
                .map(this::setViewsAndConfirmedRequests)
                .collect(Collectors.toList());
    }

    @Override
    public EventDto updateEvent(Long userId, UserUpdateEventDto eventDto) {
        Event event = checkAndGetEvent(eventDto.getEventId());
        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("only initiator can update event");
        }
        if (event.getState().equals(PUBLISHED)) {
            throw new BadRequestException("published event cant be update");
        }
        Optional.ofNullable(eventDto.getAnnotation()).ifPresent(event::setAnnotation);
        if (eventDto.getCategory() != null) {
            event.setCategory(categoryRepository.findById(eventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("category not found")));
        }
        Optional.ofNullable(eventDto.getDescription()).ifPresent(event::setDescription);
        if (eventDto.getEventDate() != null) {
            if (eventDto.getEventDate().isBefore(LocalDateTime.now().minusHours(2))) {
                throw new BadRequestException("date event is too late");
            }
            event.setEventDate(eventDto.getEventDate());
        }
        Optional.ofNullable(eventDto.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(eventDto.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(eventDto.getTitle()).ifPresent(event::setTitle);
        if (event.getState().equals(CANCELED)) {
            event.setState(PENDING);
        }
        EventDto returnEventDto = toEventDto(eventRepository.save(event));
        return setViewsAndConfirmedRequests(returnEventDto);
    }

    @Override
    public EventDto createEvent(Long userId, EventDto eventDto) {
        User user = checkAndGetUser(userId);
        if (eventDto.getEventDate().isBefore(LocalDateTime.now().minusHours(2))) {
            throw new BadRequestException("date event is too late");
        }
        Location location = locationRepository.save(toLocation(eventDto.getLocation()));
        Event event = toEvent(eventDto);
        event.setLocation(location);
        event.setInitiator(user);
        return toEventDto(eventRepository.save(event));
    }

    @Override
    public EventDto getEventByUser(Long eventId, Long userId) {
        Event event = checkAndGetEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("only initiator can get fullEventDto");
        }
        return setViewsAndConfirmedRequests(toEventDto(event));
    }

    @Override
    public EventDto cancelEventByUser(Long eventId, Long userId) {
        Event event = checkAndGetEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("only initiator of event can change it");
        }
        if (!event.getState().equals(PENDING)) {
            throw new BadRequestException("only pending event can be canceled");
        }
        event.setState(CANCELED);
        EventDto eventDto = toEventDto(eventRepository.save(event));
        return setViewsAndConfirmedRequests(eventDto);
    }

    @Override
    public List<EventDto> getEventsByAdmin(List<Long> userIds, List<State> states, List<Long> categoryIds,
                                           String rangeStart, String rangeEnd, int from, int size) {
        LocalDateTime start;
        if (rangeStart == null) {
            start = LocalDateTime.now();
        } else {
            start = LocalDateTime.parse(rangeStart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        LocalDateTime end;
        if (rangeEnd == null) {
            end = LocalDateTime.MAX;
        } else {
            end = LocalDateTime.parse(rangeEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        return eventRepository.searchEventsByAdmin(userIds, states, categoryIds, start, end,
                PageRequest.of(from / size, size))
                .stream()
                .map(EventMapper::toEventDto)
                .map(this::setViewsAndConfirmedRequests)
                .collect(Collectors.toList());
    }

    @Override
    public EventDto updateEventByAdmin(Long eventId, AdminUpdateEventDto eventDto) {
        Event event = checkAndGetEvent(eventId);
        Optional.ofNullable(eventDto.getAnnotation()).ifPresent(event::setAnnotation);
        if (eventDto.getCategory() != null) {
            event.setCategory(categoryRepository.findById(eventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("category not found")));
        }
        Optional.ofNullable(eventDto.getDescription()).ifPresent(event::setDescription);
        Optional.ofNullable(eventDto.getEventDate()).ifPresent(event::setEventDate);
        if (eventDto.getLocation() != null) {
            Location location = locationRepository.save(toLocation(eventDto.getLocation()));
            event.setLocation(location);
        }
        Optional.ofNullable(eventDto.getPaid()).ifPresent(event::setPaid);
        Optional.ofNullable(eventDto.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        Optional.ofNullable(eventDto.getRequestModeration()).ifPresent(event::setRequestModeration);
        Optional.ofNullable(eventDto.getTitle()).ifPresent(event::setTitle);
        EventDto returnEventDto = toEventDto(eventRepository.save(event));
        return setViewsAndConfirmedRequests(returnEventDto);
    }

    @Override
    public EventDto publishEvent(Long eventId) {
        Event event = checkAndGetEvent(eventId);
        if (event.getEventDate().isBefore(LocalDateTime.now().minusHours(1))) {
            throw new BadRequestException("event must start min after one hour of now");
        }
        if (!event.getState().equals(PENDING)) {
            throw new BadRequestException("state of event must be PENDING");
        }
        event.setState(PUBLISHED);
        EventDto eventDto = toEventDto(eventRepository.save(event));
        return setViewsAndConfirmedRequests(eventDto);
    }

    @Override
    public EventDto rejectEvent(Long eventId) {
        Event event = checkAndGetEvent(eventId);
        event.setState(CANCELED);
        EventDto eventDto = toEventDto(eventRepository.save(event));
        return setViewsAndConfirmedRequests(eventDto);
    }

    private Integer getViews(Long eventId) {
        return (Integer) eventClient.getViews("/events/" + eventId);
    }

    private EventDto setViewsAndConfirmedRequests(EventDto eventDto) {
        eventDto.setViews(getViews(eventDto.getId()));
        eventDto.setConfirmedRequests(participationRepository.countParticipationByEventIdAndStatus(eventDto.getId(),
                CONFIRMED));
        return eventDto;
    }

    private ShortEventDto setViewsAndConfirmedRequests(ShortEventDto eventDto) {
        eventDto.setViews(getViews(eventDto.getId()));
        eventDto.setConfirmedRequests(participationRepository.countParticipationByEventIdAndStatus(eventDto.getId(),
                CONFIRMED));
        return eventDto;
    }

    private Event checkAndGetEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("event with id = " + id + " not found"));
    }

    private User checkAndGetUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user with id = " + id + " not found"));
    }
}