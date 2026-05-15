package ru.ssau.netequip.exception.employee;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)

public class NotFoundEmployeeException extends RuntimeException {
    public NotFoundEmployeeException(Long id) {
        super("Сотрудник с данным id " + id + " не найден");
    }
}
