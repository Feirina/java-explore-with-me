package ru.practicum.ewm_main.comment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm_main.comment.dto.CommentDto;
import ru.practicum.ewm_main.comment.service.CommentService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/comments")
@Validated
public class UserCommentController {
    private final CommentService commentService;

    public UserCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{eventId}")
    public CommentDto createComment(@Valid @RequestBody CommentDto commentDto,
                                    @PathVariable Long userId,
                                    @PathVariable Long eventId) {
        log.info("create comment by user {}", userId);
        return commentService.createComment(commentDto, userId, eventId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateComment(@PathVariable Long commentId,
                                    @PathVariable Long userId,
                                    @Valid @RequestBody CommentDto commentDto) {
        log.info("update comment {}", commentId);
        return commentService.updateComment(commentId, userId, commentDto);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        log.info("delete comment {}", commentId);
        commentService.deleteComment(commentId, userId);
    }

    @GetMapping
    public List<CommentDto> getAllCommentsByUser(@PathVariable Long userId,
                                                 @RequestParam int from,
                                                 @RequestParam int size) {
        log.info("get all user {} comments", userId);
        return commentService.getAllCommentsByUser(userId, from, size);
    }
}
