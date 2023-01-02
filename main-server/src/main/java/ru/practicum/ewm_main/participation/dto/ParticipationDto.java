package ru.practicum.ewm_main.participation.dto;

import lombok.*;
import ru.practicum.ewm_main.participation.model.StatusRequest;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationDto {
    private Long id;

    private LocalDateTime created;

    private Long event;

    private Long requester;

    private StatusRequest status;
}
