package ru.practicum.ewm_stats.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm_stats.HitMapper;
import ru.practicum.ewm_stats.dto.EndpointHit;
import ru.practicum.ewm_stats.dto.ViewStats;
import ru.practicum.ewm_stats.repository.HitRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.ewm_stats.HitMapper.toHit;

@Service
@Transactional(readOnly = true)
public class HitServiceImpl implements HitService {
    private final HitRepository hitRepository;

    public HitServiceImpl(HitRepository hitRepository) {
        this.hitRepository = hitRepository;
    }

    @Transactional
    @Override
    public void saveHit(EndpointHit endpointHit) {
        hitRepository.save(toHit(endpointHit));
    }

    @Override
    public List<ViewStats> getStats(String start, String end, List<String> uris, Boolean uniq) {
        LocalDateTime startDate = LocalDateTime.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime endDate = LocalDateTime.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<ViewStats> hits;
        if (Boolean.TRUE.equals(uniq)) {
            hits = hitRepository.findDistinct(startDate, endDate)
                    .stream()
                    .peek(viewStats -> viewStats.setHits(countViewsByUri(viewStats.getUri())))
                    .collect(Collectors.toList());
        } else {
            hits = hitRepository.findAllByTimestampBetween(startDate, endDate)
                    .stream()
                    .map(HitMapper::toViewStats)
                    .peek(viewStats -> viewStats.setHits(countViewsByUri(viewStats.getUri())))
                    .collect(Collectors.toList());
        }
        return uris == null ? hits : hits.stream()
                .map(viewStats -> filterByUris(viewStats, uris))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Long countViewsByUri(String uri) {
        return hitRepository.countByUri(uri);
    }

    private ViewStats filterByUris(ViewStats v, List<String> uris) {
        if (uris.contains(v.getUri())) {
            return v;
        } else {
            return null;
        }
    }
}
