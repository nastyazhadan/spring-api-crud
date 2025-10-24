package ru.aston.hometask4.util;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;


public class UserErrorResponse {
    @Getter @Setter
    private String message;
    @Getter @Setter
    private LocalDateTime timestamp;
    @Getter  @Setter
    private HttpStatus status;

    public UserErrorResponse(String message, HttpStatus status) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.status = status;
    }

    public static String getErrorMessage(BindingResult bindingResult) {
        return bindingResult.getFieldErrors().stream()
                .map(e -> e.getField() + " - " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
    }
}
