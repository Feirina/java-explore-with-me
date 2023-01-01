package ru.practicum.ewm_main.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm_main.event.model.Event;
import ru.practicum.ewm_main.event.model.State;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    @Query("SELECT e FROM Event AS e " +
            "WHERE ((:users) IS NULL OR e.initiator.id IN :users) " +
            "AND ((:states) IS NULL OR e.state IN :states) " +
            "AND ((:categoryIds) IS NULL OR e.category.id IN :categoryIds) " +
            "AND (e.eventDate >= :rangeStart) " +
            "AND (CAST(:rangeEnd AS date) >= e.eventDate)")
    Page<Event> searchEventsByAdmin(List<Long> users, List<State> states,
                             List<Long> categoryIds, LocalDateTime rangeStart,
                             LocalDateTime rangeEnd, Pageable pageable);

    @Query("SELECT e FROM Event AS e " +
            "WHERE (lower(e.annotation) like lower(concat('%', :text, '%')) " +
            "OR lower(e.description) like lower(concat('%', :text, '%'))) " +
            "AND ((:categoryIds) IS NULL OR e.category.id IN :categoryIds) " +
            "AND e.paid = :paid " +
            "AND e.eventDate between :rangeStart and :rangeEnd " +
            "AND e.state IN :state")
    Page<Event> searchEvents(String text, List<Long> categoryIds, Boolean paid, LocalDateTime rangeStart,
                             LocalDateTime rangeEnd, State state, Pageable pageable);
}
