package com.example.netequip.exception.deviceport;

/**
 * Исключение выбрасывается при попытке создать некорректное подключение между портами
 */
public class InvalidPortConnectionException extends RuntimeException {

    public InvalidPortConnectionException(String message) {
        super(message);
    }
}