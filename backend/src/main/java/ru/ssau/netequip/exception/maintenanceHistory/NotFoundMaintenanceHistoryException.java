package ru.ssau.netequip.exception.maintenanceHistory;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundMaintenanceHistoryException extends RuntimeException {
    public NotFoundMaintenanceHistoryException(Long id) {
        super("Запись обслуживания с ID: " + id + " не найдена");
    }
}
