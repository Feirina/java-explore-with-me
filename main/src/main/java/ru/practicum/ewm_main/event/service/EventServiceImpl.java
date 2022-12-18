package ru.practicum.ewm_main.event.service;

import org.springframework.stereotype.Service;
import ru.practicum.ewm_main.event.dto.EventDto;
import ru.practicum.ewm_main.event.repository.EventRepository;
import ru.practicum.ewm_main.participation.dto.ParticipationDto;

import java.util.List;

@Service
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;

    public EventServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public List<EventDto> getEvents(String text, List<Long> categoryIds, Boolean paid, String rangeStart,
                                    String rangeEnd, Boolean onlyAvailable, String sort, int from, int size) {
        return null;
    }

    @Override
    public EventDto getEvent(Long id) {
        return null;
    }

    @Override
    public List<EventDto> getUserEvents(Long userId, int from, int size) {
        return null;
    }

    @Override
    public EventDto updateEvent(Long userId, EventDto eventDto) {
        return null;
    }

    @Override
    public EventDto createEvent(Long userId, EventDto eventDto) {
        return null;
    }

    @Override
    public EventDto getEventByUser(Long eventId, Long userId) {
        return null;
    }

    @Override
    public EventDto cancelEventByUser(Long eventId, Long userId) {
        return null;
    }

    @Override
    public List<ParticipationDto> getParticipationRequests(Long eventId, Long userId) {
        return null;
    }

    @Override
    public ParticipationDto confirmParticipationRequest(Long eventId, Long userId, Long reqId) {
        return null;
    }

    @Override
    public ParticipationDto rejectParticipationRequest(Long eventId, Long userId, Long reqId) {
        return null;
    }

    @Override
    public List<EventDto> getEventsByAdmin(List<Long> userIds, List<String> states, List<Long> categoryIds,
                                           String rangeStart, String rangeEnd, int from, int size) {
        return null;
    }

    @Override
    public EventDto updateEventByAdmin(Long eventId, EventDto eventDto) {
        return null;
    }

    @Override
    public EventDto publishEvent(Long eventId) {
        return null;
    }

    @Override
    public EventDto rejectEvent(Long eventId) {
        return null;
    }
}