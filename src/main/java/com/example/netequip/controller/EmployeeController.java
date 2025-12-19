package com.example.netequip.controller;

import com.example.netequip.dto.employee.CreateEmployeeDTO;
import com.example.netequip.dto.employee.EmployeeResponseDTO;
import com.example.netequip.dto.employee.UpdateEmployeeDTO;
import com.example.netequip.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для управления сотрудниками
 * Базовый путь: /api/employees
 */
@Slf4j
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "API для управления сотрудниками")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * Получение всех сотрудников
     * GET /api/employees
     */
    @GetMapping
    @Operation(summary = "Получить всех сотрудников",
            description = "Возвращает список всех сотрудников")
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        log.debug("REST запрос на получение всех сотрудников");
        List<EmployeeResponseDTO> employees = employeeService.getAll();
        return ResponseEntity.ok(employees);
    }

    /**
     * Получение сотрудника по ID
     * GET /api/employees/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить сотрудника по ID")
    public ResponseEntity<EmployeeResponseDTO> getEmployeeById(
            @Parameter(description = "ID сотрудника")
            @PathVariable Long id) {
        log.debug("REST запрос на получение сотрудника с ID: {}", id);
        EmployeeResponseDTO employee = employeeService.getById(id);
        return ResponseEntity.ok(employee);
    }

    /**
     * Создание нового сотрудника
     * POST /api/employees
     */
    @PostMapping
    @Operation(summary = "Создать нового сотрудника")
    public ResponseEntity<EmployeeResponseDTO> createEmployee(
            @Valid @RequestBody CreateEmployeeDTO dto) {
        log.info("REST запрос на создание сотрудника: {}", dto.getFullName());
        EmployeeResponseDTO created = employeeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновление существующего сотрудника
     * PUT /api/employees/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить сотрудника")
    public ResponseEntity<EmployeeResponseDTO> updateEmployee(
            @Parameter(description = "ID сотрудника")
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployeeDTO dto) {
        log.info("REST запрос на обновление сотрудника с ID: {}", id);
        EmployeeResponseDTO updated = employeeService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удаление сотрудника
     * DELETE /api/employees/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить сотрудника")
    public ResponseEntity<Void> deleteEmployee(
            @Parameter(description = "ID сотрудника")
            @PathVariable Long id) {
        log.info("REST запрос на удаление сотрудника с ID: {}", id);
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Поиск сотрудников по имени (частичное совпадение)
     * GET /api/employees/search?name=...
     */
    @GetMapping("/search")
    @Operation(summary = "Поиск сотрудников по имени",
            description = "Поиск по частичному совпадению имени (без учёта регистра)")
    public ResponseEntity<List<EmployeeResponseDTO>> searchByName(
            @Parameter(description = "Часть имени для поиска")
            @RequestParam String name) {
        log.debug("REST запрос на поиск сотрудников по имени: {}", name);
        List<EmployeeResponseDTO> employees = employeeService.searchByName(name);
        return ResponseEntity.ok(employees);
    }

    /**
     * Получение сотрудника по email
     * GET /api/employees/by-email/{email}
     */
    @GetMapping("/by-email/{email}")
    @Operation(summary = "Получить сотрудника по email")
    public ResponseEntity<EmployeeResponseDTO> getByEmail(
            @Parameter(description = "Email сотрудника")
            @PathVariable String email) {
        log.debug("REST запрос на поиск сотрудника по email: {}", email);
        EmployeeResponseDTO employee = employeeService.getByEmail(email);
        return ResponseEntity.ok(employee);
    }

    /**
     * Получение сотрудников по должности
     * GET /api/employees/position/{position}
     */
    @GetMapping("/position/{position}")
    @Operation(summary = "Получить сотрудников по должности")
    public ResponseEntity<List<EmployeeResponseDTO>> getByPosition(
            @Parameter(description = "Название должности")
            @PathVariable String position) {
        log.debug("REST запрос на получение сотрудников должности: {}", position);
        List<EmployeeResponseDTO> employees = employeeService.getByPosition(position);
        return ResponseEntity.ok(employees);
    }

    /**
     * Получение сотрудников по должности (отсортировано по имени)
     * GET /api/employees/position/{position}/sorted
     */
    @GetMapping("/position/{position}/sorted")
    @Operation(summary = "Получить сотрудников по должности (отсортировано)")
    public ResponseEntity<List<EmployeeResponseDTO>> getByPositionSorted(
            @Parameter(description = "Название должности")
            @PathVariable String position) {
        log.debug("REST запрос на получение отсортированных сотрудников должности: {}", position);
        List<EmployeeResponseDTO> employees = employeeService.getByPositionSorted(position);
        return ResponseEntity.ok(employees);
    }

    /**
     * Проверка существования сотрудника по email
     * GET /api/employees/exists/email?email=...
     */
    @GetMapping("/exists/email")
    @Operation(summary = "Проверить существование сотрудника по email")
    public ResponseEntity<Boolean> existsByEmail(
            @Parameter(description = "Email для проверки")
            @RequestParam String email) {
        log.debug("REST запрос на проверку существования email: {}", email);
        boolean exists = employeeService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    /**
     * Получение количества всех сотрудников
     * GET /api/employees/count
     */
    @GetMapping("/count")
    @Operation(summary = "Получить количество всех сотрудников")
    public ResponseEntity<Long> countAllEmployees() {
        log.debug("REST запрос на подсчёт всех сотрудников");
        long count = employeeService.count();
        return ResponseEntity.ok(count);
    }
}
