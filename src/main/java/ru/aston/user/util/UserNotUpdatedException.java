package ru.aston.user.util;


public class UserNotUpdatedException extends RuntimeException {
    public UserNotUpdatedException(String message) {
        super(message);
    }
}
