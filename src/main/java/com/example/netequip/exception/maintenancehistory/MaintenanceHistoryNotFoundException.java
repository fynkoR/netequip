package com.example.netequip.exception.maintenancehistory;

/**
 * Исключение выбрасывается когда запись об обслуживании не найдена в базе данных
 */
public class MaintenanceHistoryNotFoundException extends RuntimeException {

    public MaintenanceHistoryNotFoundException(Long id) {
        super("Запись об обслуживании с ID " + id + " не найдена");
    }

    public MaintenanceHistoryNotFoundException(String message) {
        super(message);
    }
}
