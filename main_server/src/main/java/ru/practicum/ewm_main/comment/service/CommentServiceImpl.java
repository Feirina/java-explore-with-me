package ru.practicum.ewm_main.comment.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm_main.comment.CommentMapper;
import ru.practicum.ewm_main.comment.dto.CommentDto;
import ru.practicum.ewm_main.comment.model.Comment;
import ru.practicum.ewm_main.comment.repository.CommentRepository;
import ru.practicum.ewm_main.event.model.Event;
import ru.practicum.ewm_main.event.repository.EventRepository;
import ru.practicum.ewm_main.exception.BadRequestException;
import ru.practicum.ewm_main.exception.NotFoundException;
import ru.practicum.ewm_main.user.model.User;
import ru.practicum.ewm_main.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.ewm_main.comment.CommentMapper.toComment;
import static ru.practicum.ewm_main.comment.CommentMapper.toCommentDto;
import static ru.practicum.ewm_main.comment.model.CommentState.*;

@Service
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public CommentServiceImpl(CommentRepository commentRepository, UserRepository userRepository, EventRepository eventRepository) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public CommentDto createComment(CommentDto commentDto, Long userId, Long eventId) {
        User user = checkAndGetUser(userId);
        Event event = checkAndGetEvent(eventId);
        Comment comment = toComment(commentDto);
        comment.setUser(user);
        comment.setEvent(event);
        return toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long commentId, Long userId, CommentDto commentDto) {
        Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new BadRequestException("only author can change comment"));
        comment.setText(commentDto.getText());
        comment.setState(NEW);
        return toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new BadRequestException("only author can delete comment"));
        commentRepository.delete(comment);
    }

    @Override
    public List<CommentDto> getAllCommentsForEvent(Long eventId, int from, int size) {
        Event event = checkAndGetEvent(eventId);
        return commentRepository.findAllByEventAndState(event, APPROVED, PageRequest.of(from / size, size))
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getAllCommentsByUser(Long userId, int from, int size) {
        User user = checkAndGetUser(userId);
        return commentRepository.findAllByUser(user, PageRequest.of(from / size, size))
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto approveComment(Long commentId) {
        Comment comment = checkAndGetComment(commentId);
        comment.setState(APPROVED);
        return toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto rejectComment(Long commentId) {
        Comment comment = checkAndGetComment(commentId);
        comment.setState(REJECTED);
        return toCommentDto(commentRepository.save(comment));
    }

    private User checkAndGetUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user with id = " + id + " not found"));
    }

    private Event checkAndGetEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("event with id = " + id + " not found"));
    }

    private Comment checkAndGetComment(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("comment with id = " + id + " not found"));
    }
}
