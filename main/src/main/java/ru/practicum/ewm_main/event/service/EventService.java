package ru.practicum.ewm_main.event.service;

import ru.practicum.ewm_main.event.dto.EventDto;
import ru.practicum.ewm_main.participation.dto.ParticipationDto;

import java.util.List;

public interface EventService {
    List<EventDto> getEvents(String text, List<Long> categoryIds, Boolean paid, String rangeStart, String rangeEnd,
                             Boolean onlyAvailable, String sort, int from, int size);

    EventDto getEvent(Long id);

    List<EventDto> getUserEvents(Long userId, int from, int size);

    EventDto updateEvent(Long userId, EventDto eventDto);

    EventDto createEvent(Long userId, EventDto eventDto);

    EventDto getEventByUser(Long eventId, Long userId);

    EventDto cancelEventByUser(Long eventId, Long userId);

    List<ParticipationDto> getParticipationRequests(Long eventId, Long userId);

    ParticipationDto confirmParticipationRequest(Long eventId, Long userId, Long reqId);

    ParticipationDto rejectParticipationRequest(Long eventId, Long userId, Long reqId);

    List<EventDto> getEventsByAdmin(List<Long> userIds, List<String> states, List<Long> categoryIds,
                                    String rangeStart, String rangeEnd, int from, int size);

    EventDto updateEventByAdmin(Long eventId, EventDto eventDto);

    EventDto publishEvent(Long eventId);

    EventDto rejectEvent(Long eventId);
}
