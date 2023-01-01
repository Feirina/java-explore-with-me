package ru.practicum.ewm_main.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ShortUserDto {
    private Long id;

    private String name;
}
