package ru.ssau.netequip.exception.equipmentType;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEquipmentTypeNameException extends RuntimeException {
    public DuplicateEquipmentTypeNameException(String typeName) {
        super("Тип оборудования с именем: " + typeName + " уже существует.");
    }
}
