package com.example.netequip.controller;

import com.example.netequip.dto.maintenancehistory.CreateMaintenanceHistoryDTO;
import com.example.netequip.dto.maintenancehistory.MaintenanceHistoryResponseDTO;
import com.example.netequip.dto.maintenancehistory.UpdateMaintenanceHistoryDTO;
import com.example.netequip.service.MaintenanceHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST контроллер для управления историей обслуживания оборудования
 * Базовый путь: /api/maintenance-history
 */
@Slf4j
@RestController
@RequestMapping("/api/maintenance-history")
@RequiredArgsConstructor
@Tag(name = "Maintenance History", description = "API для управления историей обслуживания оборудования")
public class MaintenanceHistoryController {

    private final MaintenanceHistoryService maintenanceHistoryService;

    /**
     * Получение всех записей об обслуживании
     * GET /api/maintenance-history
     */
    @GetMapping
    @Operation(summary = "Получить все записи об обслуживании",
            description = "Возвращает список всех записей об обслуживании")
    public ResponseEntity<List<MaintenanceHistoryResponseDTO>> getAllMaintenanceHistory() {
        log.debug("REST запрос на получение всех записей об обслуживании");
        List<MaintenanceHistoryResponseDTO> history = maintenanceHistoryService.getAll();
        return ResponseEntity.ok(history);
    }

    /**
     * Получение записи об обслуживании по ID
     * GET /api/maintenance-history/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить запись об обслуживании по ID")
    public ResponseEntity<MaintenanceHistoryResponseDTO> getMaintenanceHistoryById(
            @Parameter(description = "ID записи об обслуживании")
            @PathVariable Long id) {
        log.debug("REST запрос на получение записи об обслуживании с ID: {}", id);
        MaintenanceHistoryResponseDTO history = maintenanceHistoryService.getById(id);
        return ResponseEntity.ok(history);
    }

    /**
     * Создание новой записи об обслуживании
     * POST /api/maintenance-history
     */
    @PostMapping
    @Operation(summary = "Создать новую запись об обслуживании",
            description = "Создаёт новую запись о проведённом обслуживании оборудования")
    public ResponseEntity<MaintenanceHistoryResponseDTO> createMaintenanceHistory(
            @Valid @RequestBody CreateMaintenanceHistoryDTO dto) {
        log.info("REST запрос на создание записи об обслуживании для оборудования ID: {}",
                dto.getEquipmentId());
        MaintenanceHistoryResponseDTO created = maintenanceHistoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновление существующей записи об обслуживании
     * PUT /api/maintenance-history/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить запись об обслуживании")
    public ResponseEntity<MaintenanceHistoryResponseDTO> updateMaintenanceHistory(
            @Parameter(description = "ID записи об обслуживании")
            @PathVariable Long id,
            @Valid @RequestBody UpdateMaintenanceHistoryDTO dto) {
        log.info("REST запрос на обновление записи об обслуживании с ID: {}", id);
        MaintenanceHistoryResponseDTO updated = maintenanceHistoryService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удаление записи об обслуживании
     * DELETE /api/maintenance-history/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить запись об обслуживании")
    public ResponseEntity<Void> deleteMaintenanceHistory(
            @Parameter(description = "ID записи об обслуживании")
            @PathVariable Long id) {
        log.info("REST запрос на удаление записи об обслуживании с ID: {}", id);
        maintenanceHistoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получение истории обслуживания оборудования
     * GET /api/maintenance-history/equipment/{equipmentId}
     */
    @GetMapping("/equipment/{equipmentId}")
    @Operation(summary = "Получить историю обслуживания оборудования",
            description = "Возвращает все записи об обслуживании конкретного оборудования (отсортировано по дате, новые первые)")
    public ResponseEntity<List<MaintenanceHistoryResponseDTO>> getByEquipment(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId) {
        log.debug("REST запрос на получение истории обслуживания оборудования ID: {}", equipmentId);
        List<MaintenanceHistoryResponseDTO> history = maintenanceHistoryService.getByEquipment(equipmentId);
        return ResponseEntity.ok(history);
    }

    /**
     * Получение последнего обслуживания оборудования
     * GET /api/maintenance-history/equipment/{equipmentId}/latest
     */
    @GetMapping("/equipment/{equipmentId}/latest")
    @Operation(summary = "Получить последнее обслуживание оборудования",
            description = "Возвращает самую свежую запись об обслуживании")
    public ResponseEntity<MaintenanceHistoryResponseDTO> getLatestByEquipment(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId) {
        log.debug("REST запрос на получение последнего обслуживания оборудования ID: {}", equipmentId);
        MaintenanceHistoryResponseDTO history = maintenanceHistoryService.getLatestByEquipment(equipmentId);
        return ResponseEntity.ok(history);
    }

    /**
     * Получение обслуживаний оборудования по типу
     * GET /api/maintenance-history/equipment/{equipmentId}/type/{type}
     */
    @GetMapping("/equipment/{equipmentId}/type/{type}")
    @Operation(summary = "Получить обслуживания оборудования по типу",
            description = "Возвращает записи определённого типа (Routine, Repair, Upgrade, Emergency, Preventive)")
    public ResponseEntity<List<MaintenanceHistoryResponseDTO>> getByEquipmentAndType(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId,
            @Parameter(description = "Тип обслуживания")
            @PathVariable String type) {
        log.debug("REST запрос на получение обслуживаний типа '{}' для оборудования ID: {}",
                type, equipmentId);
        List<MaintenanceHistoryResponseDTO> history = maintenanceHistoryService
                .getByEquipmentAndType(equipmentId, type);
        return ResponseEntity.ok(history);
    }

    /**
     * Получение обслуживаний выполненных сотрудником
     * GET /api/maintenance-history/employee/{employeeId}
     */
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Получить обслуживания выполненные сотрудником",
            description = "Возвращает все обслуживания, выполненные конкретным сотрудником")
    public ResponseEntity<List<MaintenanceHistoryResponseDTO>> getByPerformedBy(
            @Parameter(description = "ID сотрудника")
            @PathVariable Long employeeId) {
        log.debug("REST запрос на получение обслуживаний сотрудника ID: {}", employeeId);
        List<MaintenanceHistoryResponseDTO> history = maintenanceHistoryService
                .getByPerformedBy(employeeId);
        return ResponseEntity.ok(history);
    }

    /**
     * Получение обслуживаний за период
     * GET /api/maintenance-history/date-range?start=...&end=...
     */
    @GetMapping("/date-range")
    @Operation(summary = "Получить обслуживания за период",
            description = "Возвращает записи об обслуживании в указанном диапазоне дат")
    public ResponseEntity<List<MaintenanceHistoryResponseDTO>> getByDateRange(
            @Parameter(description = "Начало периода (ISO DateTime)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "Конец периода (ISO DateTime)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        log.debug("REST запрос на получение обслуживаний за период: {} - {}", start, end);
        List<MaintenanceHistoryResponseDTO> history = maintenanceHistoryService
                .getByDateRange(start, end);
        return ResponseEntity.ok(history);
    }

    /**
     * Получение недавних обслуживаний оборудования
     * GET /api/maintenance-history/equipment/{equipmentId}/recent?days=...
     */
    @GetMapping("/equipment/{equipmentId}/recent")
    @Operation(summary = "Получить недавние обслуживания оборудования",
            description = "Возвращает обслуживания за последние N дней")
    public ResponseEntity<List<MaintenanceHistoryResponseDTO>> getRecentMaintenances(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId,
            @Parameter(description = "Количество дней (по умолчанию 30)")
            @RequestParam(defaultValue = "30") int days) {
        log.debug("REST запрос на получение обслуживаний оборудования ID {} за последние {} дней",
                equipmentId, days);
        List<MaintenanceHistoryResponseDTO> history = maintenanceHistoryService
                .getRecentMaintenances(equipmentId, days);
        return ResponseEntity.ok(history);
    }

    /**
     * Получение обслуживаний по типу (все устройства)
     * GET /api/maintenance-history/type/{type}
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "Получить обслуживания по типу",
            description = "Возвращает все обслуживания определённого типа для всех устройств")
    public ResponseEntity<List<MaintenanceHistoryResponseDTO>> getByType(
            @Parameter(description = "Тип обслуживания")
            @PathVariable String type) {
        log.debug("REST запрос на получение всех обслуживаний типа: {}", type);
        List<MaintenanceHistoryResponseDTO> history = maintenanceHistoryService.getByType(type);
        return ResponseEntity.ok(history);
    }

    /**
     * Получение просроченных обслуживаний
     * GET /api/maintenance-history/overdue
     */
    @GetMapping("/overdue")
    @Operation(summary = "Получить просроченные обслуживания",
            description = "Возвращает записи, где дата следующего обслуживания прошла")
    public ResponseEntity<List<MaintenanceHistoryResponseDTO>> getOverdueMaintenances() {
        log.debug("REST запрос на получение просроченных обслуживаний");
        List<MaintenanceHistoryResponseDTO> history = maintenanceHistoryService
                .getOverdueMaintenances();
        return ResponseEntity.ok(history);
    }

    /**
     * Планирование следующего обслуживания
     * PATCH /api/maintenance-history/equipment/{equipmentId}/schedule-next?date=...
     */
    @PatchMapping("/equipment/{equipmentId}/schedule-next")
    @Operation(summary = "Запланировать следующее обслуживание",
            description = "Устанавливает дату следующего обслуживания для оборудования")
    public ResponseEntity<MaintenanceHistoryResponseDTO> scheduleNextMaintenance(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId,
            @Parameter(description = "Дата следующего обслуживания (ISO Date)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("REST запрос на планирование обслуживания оборудования ID {} на: {}",
                equipmentId, date);
        MaintenanceHistoryResponseDTO updated = maintenanceHistoryService
                .scheduleNextMaintenance(equipmentId, date);
        return ResponseEntity.ok(updated);
    }

    /**
     * Подсчёт обслуживаний оборудования
     * GET /api/maintenance-history/equipment/{equipmentId}/count
     */
    @GetMapping("/equipment/{equipmentId}/count")
    @Operation(summary = "Подсчитать обслуживания оборудования")
    public ResponseEntity<Long> countByEquipment(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId) {
        log.debug("REST запрос на подсчёт обслуживаний оборудования ID: {}", equipmentId);
        long count = maintenanceHistoryService.countByEquipment(equipmentId);
        return ResponseEntity.ok(count);
    }

    /**
     * Подсчёт обслуживаний по типу (все устройства)
     * GET /api/maintenance-history/type/{type}/count
     */
    @GetMapping("/type/{type}/count")
    @Operation(summary = "Подсчитать обслуживания по типу")
    public ResponseEntity<Long> countByType(
            @Parameter(description = "Тип обслуживания")
            @PathVariable String type) {
        log.debug("REST запрос на подсчёт обслуживаний типа: {}", type);
        long count = maintenanceHistoryService.countByType(type);
        return ResponseEntity.ok(count);
    }
}
