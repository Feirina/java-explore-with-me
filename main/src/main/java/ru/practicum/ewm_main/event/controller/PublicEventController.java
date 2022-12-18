package ru.practicum.ewm_main.event.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm_main.event.dto.EventDto;
import ru.practicum.ewm_main.event.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
public class PublicEventController {
    private final EventService eventService;

    public PublicEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<EventDto> getEvents(@RequestParam String text,
                                    @RequestParam List<Long> categoryIds,
                                    @RequestParam Boolean paid,
                                    @RequestParam String rangeStart,
                                    @RequestParam String rangeEnd,
                                    @RequestParam (defaultValue = "false") Boolean onlyAvailable,
                                    @RequestParam String sort,
                                    @RequestParam (defaultValue = "0") int from,
                                    @RequestParam (defaultValue = "10") int size) {
        log.info("get events by param: text = {}, categoryIds = {}, paid = {}, rangeStart = {}, rangeEnd = {}, " +
                "onlyAvailable = {}, sort = {}, from = {}, size = {}", text, categoryIds, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size);
        return eventService.getEvents(text, categoryIds, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);
    }

    @GetMapping("/{id}")
    public EventDto getEvent(@PathVariable Long id) {
        log.info("get event with id {}", id);
        return eventService.getEvent(id);
    }
}
