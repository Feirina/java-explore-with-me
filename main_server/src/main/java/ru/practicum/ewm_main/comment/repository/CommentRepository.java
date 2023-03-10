package ru.practicum.ewm_main.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm_main.comment.model.Comment;
import ru.practicum.ewm_main.comment.model.CommentState;
import ru.practicum.ewm_main.event.model.Event;
import ru.practicum.ewm_main.user.model.User;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAllByEventAndState(Event event, CommentState state, Pageable pageable);

    Page<Comment> findAllByUser(User user, Pageable pageable);

    Optional<Comment> findByIdAndUserId(Long id, Long userId);
}
