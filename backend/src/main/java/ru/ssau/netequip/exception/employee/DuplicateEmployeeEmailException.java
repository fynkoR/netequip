package ru.ssau.netequip.exception.employee;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)

public class DuplicateEmployeeEmailException extends RuntimeException {
    public DuplicateEmployeeEmailException(String email) {
        super("Сотрудник с данным email: " + email + " уже существует.");
    }
}
