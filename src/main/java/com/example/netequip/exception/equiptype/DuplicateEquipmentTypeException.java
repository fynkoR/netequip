package com.example.netequip.exception.equiptype;

public class DuplicateEquipmentTypeException extends RuntimeException {

    public DuplicateEquipmentTypeException(String typeName) {
        super("Тип оборудования с названием '" + typeName + "' уже существует");
    }
}
