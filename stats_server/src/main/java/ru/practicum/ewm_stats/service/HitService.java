package ru.practicum.ewm_stats.service;

import ru.practicum.ewm_stats.dto.EndpointHit;
import ru.practicum.ewm_stats.dto.ViewStats;

import java.util.List;

public interface HitService {
    void saveHit(EndpointHit endpointHit);

    List<ViewStats> getStats(String start, String end, List<String> uris, Boolean uniq);
}
