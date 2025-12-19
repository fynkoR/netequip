package com.example.netequip.exception.deviceport;

/**
 * Исключение выбрасывается при попытке создать порт с уже существующим номером на устройстве
 */
public class DuplicateDevicePortException extends RuntimeException {

    public DuplicateDevicePortException(Long equipmentId, Integer portNumber) {
        super("Порт №" + portNumber + " уже существует на устройстве с ID " + equipmentId);
    }
}