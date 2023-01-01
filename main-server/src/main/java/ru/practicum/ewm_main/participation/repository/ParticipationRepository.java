package ru.practicum.ewm_main.participation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm_main.participation.model.Participation;
import ru.practicum.ewm_main.participation.model.StatusRequest;
import ru.practicum.ewm_main.user.model.User;

import java.util.List;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {
    List<Participation> findAllByRequester(User user);

    Participation findByEventIdAndRequesterId(Long eventId, Long userId);

    int countParticipationByEventIdAndStatus(Long eventId, StatusRequest status);

    List<Participation> findAllByEventId(Long eventId);
}
