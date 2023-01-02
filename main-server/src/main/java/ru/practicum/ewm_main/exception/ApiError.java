package ru.practicum.ewm_main.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class ApiError {
    @NotBlank
    private List<Error> errors;

    @NotEmpty
    private String message;

    private String reason;

    private String status;

    private LocalDateTime timestamp;
}
