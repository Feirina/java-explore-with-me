package ru.practicum.ewm_main.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
public class UserDto {
    private Long id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String email;
}
