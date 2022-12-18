package ru.practicum.ewm_main.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm_main.event.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
}
