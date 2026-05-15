package ru.ssau.netequip.exception.equipmentType;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundEquipmentTypeException extends RuntimeException {
    public NotFoundEquipmentTypeException(Long id) {
        super("Сотрудник с данным id: " + id + " не найден");
    }
}
