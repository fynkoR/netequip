package com.example.netequip.exception.equiptype;

/**
 * Исключение выбрасывается когда тип оборудования не найден в базе данных
 */
public class EquipmentTypeNotFoundException extends RuntimeException {

    public EquipmentTypeNotFoundException(Long id) {
        super("Тип оборудования с ID " + id + " не найден");
    }

    public EquipmentTypeNotFoundException(String message) {
        super(message);
    }

    public EquipmentTypeNotFoundException(String manufacturer, String model) {
        super("Тип оборудования производителя " + manufacturer + " модели " + model + " не найден");
    }
}
