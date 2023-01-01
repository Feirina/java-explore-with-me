package ru.practicum.ewm_main.event.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm_main.category.model.Category;
import ru.practicum.ewm_main.event.model.State;
import ru.practicum.ewm_main.user.dto.ShortUserDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EventDto {
    private Long id;

    private String annotation;

    private Category category;

    private LocalDateTime createdOn;

    private String description;

    private LocalDateTime eventDate;

    private ShortUserDto initiator;

    private LocationDto location;

    private Boolean paid;

    private Integer participantLimit;

    private LocalDateTime publishedOn;

    private Boolean requestModeration;

    private State state;

    private String title;

    private Integer confirmedRequests;

    private Integer views;
}
