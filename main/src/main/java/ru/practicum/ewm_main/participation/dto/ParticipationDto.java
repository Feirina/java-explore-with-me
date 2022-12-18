package ru.practicum.ewm_main.participation.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm_main.participation.model.StatusRequest;

import java.time.LocalDateTime;

@Getter
@Setter
public class ParticipationDto {
    private Long id;

    private LocalDateTime created;

    private Long event;

    private Long requester;

    private StatusRequest status;
}
