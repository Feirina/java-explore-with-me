package ru.practicum.ewm_main.event.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm_main.category.model.Category;
import ru.practicum.ewm_main.user.dto.ShortUserDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ShortEventDto {
    private Long id;

    private String annotation;

    private Category category;

    private LocalDateTime eventDate;

    private ShortUserDto initiator;

    private Boolean paid;

    private String title;

    private Integer confirmedRequests;

    private Integer views;
}
