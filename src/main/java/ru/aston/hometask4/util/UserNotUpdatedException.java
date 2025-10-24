package ru.aston.hometask4.util;


public class UserNotUpdatedException extends RuntimeException {
    public UserNotUpdatedException(String message) {
        super(message);
    }
}
