package com.example.netequip.exception.employee;

/**
 * Исключение выбрасывается при попытке создать сотрудника с уже существующим email
 */
public class DuplicateEmployeeEmailException extends RuntimeException {

    public DuplicateEmployeeEmailException(String email) {
        super("Сотрудник с email '" + email + "' уже существует");
    }
}
