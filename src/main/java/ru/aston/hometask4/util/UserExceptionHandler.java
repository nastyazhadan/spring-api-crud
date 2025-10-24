package ru.aston.hometask4.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    private ResponseEntity<UserErrorResponse> handleNotFound(UserNotFoundException e) {
        log.warn("User not found: {}", e.getMessage());

        return buildResponse(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserNotCreatedException.class)
    private ResponseEntity<UserErrorResponse> handleNotUpdated(UserNotCreatedException e) {
        log.warn("User not created: {}", e.getMessage());

        return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotUpdatedException.class)
    private ResponseEntity<UserErrorResponse> handleNotUpdated(UserNotUpdatedException e) {
        log.warn("User not updated: {}", e.getMessage());

        return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotDeletedException.class)
    private ResponseEntity<UserErrorResponse> handleNotDeleted(UserNotDeletedException e) {
        log.warn("User not deleted: {}", e.getMessage());

        return buildResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<UserErrorResponse> handleDatabaseError(DataAccessException e) {
        log.error("Database error occurred", e);

        return buildResponse("A server error occurred. Please try again later.",
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<UserErrorResponse> handleException(Exception e) {
        log.error("Unexpected error", e);

        return buildResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<UserErrorResponse> buildResponse(String message, HttpStatus status) {
        UserErrorResponse response = new UserErrorResponse(message, status);

        return new ResponseEntity<>(response, status);
    }
}
