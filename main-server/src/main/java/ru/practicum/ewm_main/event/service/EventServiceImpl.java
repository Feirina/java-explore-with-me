package ru.practicum.ewm_main.event.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm_main.category.repository.CategoryRepository;
import ru.practicum.ewm_main.client.EventClient;
import ru.practicum.ewm_main.event.EventMapper;
import ru.practicum.ewm_main.event.dto.*;
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
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final EventClient eventClient;
    private final ParticipationRepository participationRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        List<ShortEventDto> events = eventRepository.searchEvents(text, categoryIds, paid, PUBLISHED,
                PageRequest.of(from / size, size))
                .stream()
                .filter(event -> rangeStart != null ?
                        event.getEventDate().isAfter(LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER)) :
                        event.getEventDate().isAfter(LocalDateTime.now())
                                && rangeEnd != null ? event.getEventDate().isBefore(LocalDateTime.parse(rangeEnd,
                                DATE_TIME_FORMATTER)) :
                                event.getEventDate().isBefore(LocalDateTime.MAX))
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

    @Transactional
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
        LocalDateTime date = LocalDateTime.parse(eventDto.getEventDate(),
                DATE_TIME_FORMATTER);
        if (eventDto.getEventDate() != null) {
            if (date.isBefore(LocalDateTime.now().minusHours(2))) {
                throw new BadRequestException("date event is too late");
            }
            event.setEventDate(date);
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

    @Transactional
    @Override
    public EventDto createEvent(Long userId, NewEventDto eventDto) {
        User user = checkAndGetUser(userId);
        Event event = toEvent(eventDto);
        if (event.getEventDate().isBefore(LocalDateTime.now().minusHours(2))) {
            throw new BadRequestException("date event is too late");
        }
        Location location = locationRepository.save(toLocation(eventDto.getLocation()));
        event.setCategory(categoryRepository.findById(eventDto.getCategory())
                .orElseThrow(() -> new NotFoundException("category not found")));
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

    @Transactional
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
    public List<EventDto> getEventsByAdmin(List<Long> userIds, List<String> states, List<Long> categoryIds,
                                           String rangeStart, String rangeEnd, int from, int size) {
        List<State> stateList = states
                .stream()
                .map(State::valueOf)
                .collect(Collectors.toList());
        return eventRepository.searchEventsByAdmin(userIds, stateList, categoryIds, PageRequest.of(from / size, size))
                .stream()
                .filter(event -> rangeStart != null ?
                        event.getEventDate().isAfter(LocalDateTime.parse(rangeStart, DATE_TIME_FORMATTER)) :
                        event.getEventDate().isAfter(LocalDateTime.now())
                        && rangeEnd != null ? event.getEventDate().isBefore(LocalDateTime.parse(rangeEnd,
                                DATE_TIME_FORMATTER)) : event.getEventDate().isBefore(LocalDateTime.MAX))
                .map(EventMapper::toEventDto)
                .map(this::setViewsAndConfirmedRequests)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public EventDto updateEventByAdmin(Long eventId, AdminUpdateEventDto eventDto) {
        Event event = checkAndGetEvent(eventId);
        Optional.ofNullable(eventDto.getAnnotation()).ifPresent(event::setAnnotation);
        if (eventDto.getCategory() != null) {
            event.setCategory(categoryRepository.findById(eventDto.getCategory())
                    .orElseThrow(() -> new NotFoundException("category not found")));
        }
        Optional.ofNullable(eventDto.getDescription()).ifPresent(event::setDescription);
        if (eventDto.getEventDate() != null) {
            event.setEventDate(LocalDateTime.parse(eventDto.getEventDate(), DATE_TIME_FORMATTER));
        }
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

    @Transactional
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

    @Transactional
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