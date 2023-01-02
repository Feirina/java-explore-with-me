package ru.practicum.ewm_main.event;

import ru.practicum.ewm_main.event.dto.EventDto;
import ru.practicum.ewm_main.event.dto.ShortEventDto;
import ru.practicum.ewm_main.event.model.Event;

import static ru.practicum.ewm_main.category.CategoryMapper.toCategory;
import static ru.practicum.ewm_main.category.CategoryMapper.toCategoryDto;
import static ru.practicum.ewm_main.event.LocationMapper.toLocationDto;
import static ru.practicum.ewm_main.user.UserMapper.toShortUserDto;

public class EventMapper {
    public static EventDto toEventDto(Event event) {
        return EventDto
                .builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(toCategoryDto(event.getCategory()))
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(toShortUserDto(event.getInitiator()))
                .location(toLocationDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(event.getState())
                .title(event.getTitle())
                .build();
    }

    public static Event toEvent(EventDto eventDto) {
        return Event
                .builder()
                .id(eventDto.getId())
                .annotation(eventDto.getAnnotation())
                .category(toCategory(eventDto.getCategory()))
                .createdOn(eventDto.getCreatedOn())
                .description(eventDto.getDescription())
                .eventDate(eventDto.getEventDate())
                .paid(eventDto.getPaid())
                .participantLimit(eventDto.getParticipantLimit())
                .publishedOn(eventDto.getPublishedOn())
                .requestModeration(eventDto.getRequestModeration())
                .state(eventDto.getState())
                .title(eventDto.getTitle())
                .build();
    }

    public static ShortEventDto toShortEventDto(Event event) {
        return ShortEventDto
                .builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(toCategoryDto(event.getCategory()))
                .eventDate(event.getEventDate())
                .initiator(toShortUserDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .build();
    }

    public static Event toEvent(ShortEventDto shortEventDto) {
        return Event
                .builder()
                .id(shortEventDto.getId())
                .annotation(shortEventDto.getAnnotation())
                .category(toCategory(shortEventDto.getCategory()))
                .eventDate(shortEventDto.getEventDate())
                .paid(shortEventDto.getPaid())
                .title(shortEventDto.getTitle())
                .build();
    }
}
