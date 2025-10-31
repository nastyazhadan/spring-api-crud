package ru.aston.user.util;


public class UserNotDeletedException extends RuntimeException {
    public UserNotDeletedException(String message) {
        super(message);
    }
}
