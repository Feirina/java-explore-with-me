package ru.practicum.ewm_stats.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm_stats.dto.EndpointHit;
import ru.practicum.ewm_stats.dto.ViewStats;
import ru.practicum.ewm_stats.service.HitService;

import java.util.List;

@Slf4j
@RestController
public class HitController {
    private final HitService hitService;

    public HitController(HitService hitService) {
        this.hitService = hitService;
    }

    @GetMapping("stats")
    public List<ViewStats> getStats(@RequestParam String start,
                                    @RequestParam String end,
                                    @RequestParam List<String> uris,
                                    @RequestParam Boolean uniq) {
        log.info("get statistic for uris {}", uris);
        return hitService.getStats(start, end, uris, uniq);
    }

    @PostMapping("/hit")
    public void saveHit(@RequestBody EndpointHit endpointHit) {
        log.info("save hit for uri {}", endpointHit.getUri());
        hitService.saveHit(endpointHit);
    }

    @GetMapping("/hit")
    public Long getViews(@RequestParam String uri) {
        return hitService.countViewsByUri(uri);
    }
}
