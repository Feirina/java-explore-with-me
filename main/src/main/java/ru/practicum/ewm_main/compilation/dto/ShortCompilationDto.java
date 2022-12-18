package ru.practicum.ewm_main.compilation.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ShortCompilationDto {
    private String title;

    private Boolean pinned;

    private List<Long> eventsIds;
}
