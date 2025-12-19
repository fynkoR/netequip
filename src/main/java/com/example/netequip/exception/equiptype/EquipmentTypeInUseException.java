package com.example.netequip.exception.equiptype;

/**
 * Исключение выбрасывается при попытке удалить тип оборудования, который используется
 */
public class EquipmentTypeInUseException extends RuntimeException {

    public EquipmentTypeInUseException(Long id) {
        super("Невозможно удалить тип оборудования с ID " + id + ", так как он используется существующим оборудованием");
    }

    public EquipmentTypeInUseException(String message) {
        super(message);
    }
}
