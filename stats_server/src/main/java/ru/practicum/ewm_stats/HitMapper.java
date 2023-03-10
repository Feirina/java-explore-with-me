package ru.practicum.ewm_stats;

import ru.practicum.ewm_stats.dto.EndpointHit;
import ru.practicum.ewm_stats.dto.ViewStats;
import ru.practicum.ewm_stats.model.Hit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HitMapper {
    public static Hit toHit(EndpointHit endpointHit) {
        return Hit
                .builder()
                .id(endpointHit.getId())
                .app(endpointHit.getApp())
                .ip(endpointHit.getIp())
                .uri(endpointHit.getUri())
                .timestamp(LocalDateTime.parse(endpointHit.getTimestamp(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    public static ViewStats toViewStats(Hit hit) {
        return ViewStats
                .builder()
                .app(hit.getApp())
                .uri(hit.getUri())
                .build();
    }
}
