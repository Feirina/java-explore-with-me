package ru.practicum.ewm_main.compilation.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortCompilationDto {
    private String title;

    private Boolean pinned;

    private List<Long> events;
}
