package ru.practicum.ewm_stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm_stats.dto.ViewStats;
import ru.practicum.ewm_stats.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {
    @Query("SELECT new ru.practicum.ewm_stats.dto.ViewStats(h.app, h.uri, count(distinct h.ip)) " +
            "FROM Hit h WHERE h.timestamp between ?1 AND ?2")
    List<ViewStats> findDistinct(LocalDateTime start, LocalDateTime end);

    List<Hit> findAllByTimestampBetween(LocalDateTime start, LocalDateTime end);

    Integer countByUri(String uri);
}
