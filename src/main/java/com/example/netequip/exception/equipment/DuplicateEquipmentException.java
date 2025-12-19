package com.example.netequip.exception.equipment;

/**
 * Исключение выбрасывается при попытке создать оборудование с существующим уникальным значением
 */
public class DuplicateEquipmentException extends RuntimeException {

    public DuplicateEquipmentException(String field, String value) {
        super("Оборудование с " + field + " '" + value + "' уже существует");
    }
}
