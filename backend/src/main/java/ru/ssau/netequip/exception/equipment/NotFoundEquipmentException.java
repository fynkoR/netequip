package ru.ssau.netequip.exception.equipment;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundEquipmentException extends RuntimeException{
    public NotFoundEquipmentException(Long id){
        super("Оборудование с данным id: " + id +  " не найдено");
    }
}
