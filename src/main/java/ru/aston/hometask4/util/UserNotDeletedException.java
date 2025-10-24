package ru.aston.hometask4.util;


public class UserNotDeletedException extends RuntimeException {
    public UserNotDeletedException(String message) {
        super(message);
    }
}
