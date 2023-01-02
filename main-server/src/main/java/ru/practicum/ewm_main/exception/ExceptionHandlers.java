package ru.practicum.ewm_main.exception;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class ExceptionHandlers {
    @ExceptionHandler(NotFoundException.class)
    public ApiError handleNotFoundExceptions(RuntimeException e) {
        return ApiError.builder()
                .status(String.valueOf(HttpStatus.NOT_FOUND))
                .reason("object not found")
                .message(e.getLocalizedMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler(BadRequestException.class)
    public ApiError handleBadRequestExceptions(RuntimeException e) {
        return ApiError.builder()
                .status(String.valueOf(HttpStatus.FORBIDDEN))
                .reason("bad request")
                .message(e.getLocalizedMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    /*@ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConstraintExceptions(DataIntegrityViolationException e) {
        return ApiError.builder()
                .status(String.valueOf(HttpStatus.CONFLICT))
                .message(e.getLocalizedMessage())
                .timestamp(LocalDateTime.now())
                .reason("constraint exception")
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleThrowableExceptions(final Throwable e) {
        return ApiError.builder()
                .message(e.getLocalizedMessage())
                .reason("Throwable exception")
                .status(String.valueOf(HttpStatus.BAD_REQUEST))
                .timestamp(LocalDateTime.now())
                .build();
    }*/
}
