package com.example.netequip.exception.deviceport;

/**
 * Исключение выбрасывается когда порт устройства не найден в базе данных
 */
public class DevicePortNotFoundException extends RuntimeException {

    public DevicePortNotFoundException(Long id) {
        super("Порт устройства с ID " + id + " не найден");
    }

    public DevicePortNotFoundException(Long equipmentId, Integer portNumber) {
        super("Порт №" + portNumber + " устройства с ID " + equipmentId + " не найден");
    }
}
