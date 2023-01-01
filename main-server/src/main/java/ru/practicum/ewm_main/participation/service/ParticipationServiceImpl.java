package ru.practicum.ewm_main.participation.service;

import org.springframework.stereotype.Service;
import ru.practicum.ewm_main.event.model.Event;
import ru.practicum.ewm_main.event.repository.EventRepository;
import ru.practicum.ewm_main.exception.BadRequestException;
import ru.practicum.ewm_main.exception.NotFoundException;
import ru.practicum.ewm_main.participation.ParticipationMapper;
import ru.practicum.ewm_main.participation.dto.ParticipationDto;
import ru.practicum.ewm_main.participation.model.Participation;
import ru.practicum.ewm_main.participation.repository.ParticipationRepository;
import ru.practicum.ewm_main.user.model.User;
import ru.practicum.ewm_main.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.ewm_main.event.model.State.PUBLISHED;
import static ru.practicum.ewm_main.participation.ParticipationMapper.toParticipationDto;
import static ru.practicum.ewm_main.participation.model.StatusRequest.*;

@Service
public class ParticipationServiceImpl implements ParticipationService {
    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public ParticipationServiceImpl(ParticipationRepository participationRepository, UserRepository userRepository, EventRepository eventRepository) {
        this.participationRepository = participationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public List<ParticipationDto> getParticipationRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("user with id = " + userId + " not found"));
        return participationRepository.findAllByRequester(user)
                .stream()
                .map(ParticipationMapper :: toParticipationDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationDto createParticipationRequest(Long userId, Long eventId) {
        if (participationRepository.findByEventIdAndRequesterId(eventId, userId) != null) {
            throw new BadRequestException("participation request already exist");
        }
        Participation participation = Participation
                .builder()
                .created(LocalDateTime.now())
                .requester(userRepository.findById(userId)
                        .orElseThrow(() -> new NotFoundException("user with id = " + userId + " not found")))
                .event(eventRepository.findById(eventId)
                        .orElseThrow(() -> new NotFoundException("event with id = " + eventId + " not found")))
                .status(CONFIRMED)
                .build();
        if (userId.equals(participation.getEvent().getInitiator().getId())) {
            throw new BadRequestException("requester can`t be initiator of event");
        }
        if (!participation.getEvent().getState().equals(PUBLISHED)) {
            throw new BadRequestException("event not published");
        }
        if (participation.getEvent().getParticipantLimit() <= participationRepository
                .countParticipationByEventIdAndStatus(eventId, CONFIRMED)) {
            throw new BadRequestException("the limit of requests for participation has been exhausted");
        }
        if (Boolean.TRUE.equals(participation.getEvent().getRequestModeration())) {
            participation.setStatus(PENDING);
        }
        return toParticipationDto(participationRepository.save(participation));
    }

    @Override
    public ParticipationDto cancelParticipationRequest(Long userId, Long reqId) {
        Participation participation = participationRepository.findById(reqId)
                .orElseThrow(() -> new NotFoundException("participation request with id = " + reqId + " not found"));
        if (Objects.equals(userId, participation.getRequester().getId())) {
            participation.setStatus(CANCELED);
        } else {
            throw new BadRequestException("only owner can cancel participation request");
        }
        return toParticipationDto(participationRepository.save(participation));
    }

    @Override
    public List<ParticipationDto> getParticipationRequests(Long eventId, Long userId) {
        Event event = checkAndGetEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("only initiator of event can reject participation request to this event");
        }
        return participationRepository.findAllByEventId(eventId)
                .stream()
                .map(ParticipationMapper :: toParticipationDto)
                .collect(Collectors.toList());
    }

    @Override
    public ParticipationDto confirmParticipationRequest(Long eventId, Long userId, Long reqId) {
        Event event = checkAndGetEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("only initiator of event can reject participation request to this event");
        }
        Participation participation = participationRepository.findById(reqId)
                .orElseThrow(() -> new NotFoundException("participation request with id = " + reqId + " not found"));
        if (!participation.getStatus().equals(PENDING)) {
            throw new BadRequestException("only participation request with status pending can be approval");
        }
        if (event.getParticipantLimit() >= participationRepository.countParticipationByEventIdAndStatus(eventId, CONFIRMED)) {
            participation.setStatus(REJECTED);
        } else {
            participation.setStatus(CONFIRMED);
        }
        return toParticipationDto(participationRepository.save(participation));
    }

    @Override
    public ParticipationDto rejectParticipationRequest(Long eventId, Long userId, Long reqId) {
        Event event = checkAndGetEvent(eventId);
        if (!event.getInitiator().getId().equals(userId)) {
            throw new BadRequestException("only initiator of event can reject request to this event");
        }
        Participation participation = participationRepository.findById(reqId)
                .orElseThrow(() -> new NotFoundException("participation request with id = " + reqId + " not found"));
        participation.setStatus(REJECTED);
        return toParticipationDto(participationRepository.save(participation));
    }

    private Event checkAndGetEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("event with id = " + id + " not found"));
    }
}
