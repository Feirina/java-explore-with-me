package ru.practicum.ewm_main.event.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserUpdateEventDto {
    Long eventId;

    String annotation;

    Long category;

    String description;

    LocalDateTime eventDate;

    Boolean paid;

    Integer participantLimit;

    String title;
}
