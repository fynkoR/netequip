package com.example.netequip.exception;

import com.example.netequip.exception.*;
import com.example.netequip.exception.deviceport.DevicePortNotFoundException;
import com.example.netequip.exception.deviceport.DuplicateDevicePortException;
import com.example.netequip.exception.deviceport.InvalidPortConnectionException;
import com.example.netequip.exception.deviceport.PortNotConnectedException;
import com.example.netequip.exception.employee.DuplicateEmployeeEmailException;
import com.example.netequip.exception.employee.EmployeeNotFoundException;
import com.example.netequip.exception.equipment.DuplicateEquipmentException;
import com.example.netequip.exception.equipment.EquipmentNotFoundException;
import com.example.netequip.exception.equiptype.DuplicateEquipmentTypeException;
import com.example.netequip.exception.equiptype.EquipmentTypeNotFoundException;
import com.example.netequip.exception.ipaddress.DuplicateIpAddressException;
import com.example.netequip.exception.ipaddress.IpAddressNotFoundException;
import com.example.netequip.exception.ipaddress.PrimaryIpAddressConflictException;
import com.example.netequip.exception.maintenancehistory.MaintenanceHistoryNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений для всех REST контроллеров
 * Преобразует исключения в понятные HTTP ответы
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработка исключений "не найдено"
     * Возвращает 404 NOT FOUND
     */
    @ExceptionHandler({
            EquipmentTypeNotFoundException.class,
            EquipmentNotFoundException.class,
            EmployeeNotFoundException.class,
            DevicePortNotFoundException.class,
            IpAddressNotFoundException.class,
            MaintenanceHistoryNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFoundException(RuntimeException ex) {
        log.warn("Ресурс не найден: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Обработка исключений дубликатов
     * Возвращает 409 CONFLICT
     */
    @ExceptionHandler({
            DuplicateEquipmentTypeException.class,
            DuplicateEquipmentException.class,
            DuplicateEmployeeEmailException.class,
            DuplicateDevicePortException.class,
            DuplicateIpAddressException.class
    })
    public ResponseEntity<ErrorResponse> handleDuplicateException(RuntimeException ex) {
        log.warn("Конфликт дубликатов: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * Обработка исключений валидации
     * Возвращает 400 BAD REQUEST
     */
    @ExceptionHandler({
            InvalidPortConnectionException.class,
            PrimaryIpAddressConflictException.class
    })
    public ResponseEntity<ErrorResponse> handleValidationException(RuntimeException ex) {
        log.warn("Ошибка валидации: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Обработка ошибок валидации DTO (Bean Validation)
     * Возвращает 400 BAD REQUEST с деталями по каждому полю
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.warn("Ошибка валидации полей DTO");

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("message", "Ошибка валидации данных");
        response.put("errors", errors);
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Обработка всех остальных исключений
     * Возвращает 500 INTERNAL SERVER ERROR
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Внутренняя ошибка сервера", ex);

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Внутренняя ошибка сервера",
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Обработка исключения когда порт не подключён
     * Возвращает 404 NOT FOUND
     */
    @ExceptionHandler(PortNotConnectedException.class)
    public ResponseEntity<ErrorResponse> handlePortNotConnectedException(PortNotConnectedException ex) {
        log.warn("Порт не подключён: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }


    /**
     * Класс для структурированного ответа об ошибке
     */
    public record ErrorResponse(
            int status,
            String message,
            LocalDateTime timestamp
    ) {}
}
