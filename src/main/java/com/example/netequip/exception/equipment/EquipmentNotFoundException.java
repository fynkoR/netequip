package com.example.netequip.exception.equipment;

/**
 * Исключение выбрасывается когда оборудование не найдено в базе данных
 */
public class EquipmentNotFoundException extends RuntimeException {

    public EquipmentNotFoundException(Long id) {
        super("Оборудование с ID " + id + " не найдено");
    }

    public EquipmentNotFoundException(String message) {
        super(message);
    }
}
