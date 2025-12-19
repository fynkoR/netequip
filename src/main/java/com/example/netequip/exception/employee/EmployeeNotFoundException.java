package com.example.netequip.exception.employee;

/**
 * Исключение выбрасывается когда сотрудник не найден в базе данных
 */
public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(Long id) {
        super("Сотрудник с ID " + id + " не найден");
    }

    public EmployeeNotFoundException(String email) {
        super("Сотрудник с email '" + email + "' не найден");
    }
}
