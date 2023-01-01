package ru.practicum.ewm_main.event.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LocationDto {
    private float lat;

    private float lon;
}
