package ru.ssau.netequip.exception.equipment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEquipmentNameException extends RuntimeException {
    public DuplicateEquipmentNameException(String field, String value) {
        super("Оборудование с " + field + ": " + value + " уже существует");
    }
}
