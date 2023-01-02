package ru.practicum.ewm_main.event.dto;

import lombok.*;
import ru.practicum.ewm_main.category.dto.CategoryDto;
import ru.practicum.ewm_main.category.model.Category;
import ru.practicum.ewm_main.user.dto.ShortUserDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortEventDto {
    private Long id;

    private String annotation;

    private CategoryDto category;

    private String eventDate;

    private ShortUserDto initiator;

    private Boolean paid;

    private String title;

    private Integer confirmedRequests;

    private Integer views;
}
