package com.example.netequip.controller;

import com.example.netequip.dto.equipment.CreateEquipmentDTO;
import com.example.netequip.dto.equipment.EquipmentListDTO;
import com.example.netequip.dto.equipment.EquipmentResponseDTO;
import com.example.netequip.dto.equipment.UpdateEquipmentDTO;
import com.example.netequip.service.EquipmentService;
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
import java.util.List;

/**
 * REST контроллер для управления сетевым оборудованием
 * Базовый путь: /api/equipment
 * Центральный контроллер системы
 */
@Slf4j
@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
@Tag(name = "Equipment", description = "API для управления сетевым оборудованием")
public class EquipmentController {

    private final EquipmentService equipmentService;

    /**
     * Получение всего оборудования (краткая информация)
     * GET /api/equipment
     */
    @GetMapping
    @Operation(summary = "Получить всё оборудование",
            description = "Возвращает список всего оборудования с краткой информацией")
    public ResponseEntity<List<EquipmentListDTO>> getAllEquipment() {
        log.debug("REST запрос на получение всего оборудования");
        List<EquipmentListDTO> equipment = equipmentService.getAll();
        return ResponseEntity.ok(equipment);
    }

    /**
     * Получение оборудования по ID (полная информация)
     * GET /api/equipment/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить оборудование по ID",
            description = "Возвращает полную информацию об оборудовании включая статистику")
    public ResponseEntity<EquipmentResponseDTO> getEquipmentById(
            @Parameter(description = "ID оборудования")
            @PathVariable Long id) {
        log.debug("REST запрос на получение оборудования с ID: {}", id);
        EquipmentResponseDTO equipment = equipmentService.getById(id);
        return ResponseEntity.ok(equipment);
    }

    /**
     * Создание нового оборудования
     * POST /api/equipment
     */
    @PostMapping
    @Operation(summary = "Создать новое оборудование",
            description = "Создаёт новое сетевое оборудование в системе")
    public ResponseEntity<EquipmentResponseDTO> createEquipment(
            @Valid @RequestBody CreateEquipmentDTO dto) {
        log.info("REST запрос на создание оборудования: {}", dto.getName());
        EquipmentResponseDTO created = equipmentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновление существующего оборудования
     * PUT /api/equipment/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить оборудование")
    public ResponseEntity<EquipmentResponseDTO> updateEquipment(
            @Parameter(description = "ID оборудования")
            @PathVariable Long id,
            @Valid @RequestBody UpdateEquipmentDTO dto) {
        log.info("REST запрос на обновление оборудования с ID: {}", id);
        EquipmentResponseDTO updated = equipmentService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удаление оборудования
     * DELETE /api/equipment/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить оборудование",
            description = "Удаляет оборудование из системы")
    public ResponseEntity<Void> deleteEquipment(
            @Parameter(description = "ID оборудования")
            @PathVariable Long id) {
        log.info("REST запрос на удаление оборудования с ID: {}", id);
        equipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Поиск оборудования по серийному номеру
     * GET /api/equipment/serial/{serialNumber}
     */
    @GetMapping("/serial/{serialNumber}")
    @Operation(summary = "Найти оборудование по серийному номеру")
    public ResponseEntity<EquipmentResponseDTO> getBySerialNumber(
            @Parameter(description = "Серийный номер")
            @PathVariable String serialNumber) {
        log.debug("REST запрос на поиск оборудования по серийному номеру: {}", serialNumber);
        EquipmentResponseDTO equipment = equipmentService.getBySerialNumber(serialNumber);
        return ResponseEntity.ok(equipment);
    }

    /**
     * Поиск оборудования по MAC-адресу
     * GET /api/equipment/mac/{macAddress}
     */
    @GetMapping("/mac/{macAddress}")
    @Operation(summary = "Найти оборудование по MAC-адресу")
    public ResponseEntity<EquipmentResponseDTO> getByMacAddress(
            @Parameter(description = "MAC-адрес")
            @PathVariable String macAddress) {
        log.debug("REST запрос на поиск оборудования по MAC-адресу: {}", macAddress);
        EquipmentResponseDTO equipment = equipmentService.getByMacAddress(macAddress);
        return ResponseEntity.ok(equipment);
    }

    /**
     * Поиск оборудования по IP-адресу
     * GET /api/equipment/ip/{ipAddress}
     */
    @GetMapping("/ip/{ipAddress}")
    @Operation(summary = "Найти оборудование по IP-адресу")
    public ResponseEntity<EquipmentResponseDTO> getByIpAddress(
            @Parameter(description = "IP-адрес")
            @PathVariable String ipAddress) {
        log.debug("REST запрос на поиск оборудования по IP-адресу: {}", ipAddress);
        EquipmentResponseDTO equipment = equipmentService.getByIpAddress(ipAddress);
        return ResponseEntity.ok(equipment);
    }

    /**
     * Получение оборудования по типу
     * GET /api/equipment/type/{typeId}
     */
    @GetMapping("/type/{typeId}")
    @Operation(summary = "Получить оборудование по типу",
            description = "Возвращает всё оборудование определённого типа")
    public ResponseEntity<List<EquipmentListDTO>> getByType(
            @Parameter(description = "ID типа оборудования")
            @PathVariable Long typeId) {
        log.debug("REST запрос на получение оборудования типа ID: {}", typeId);
        List<EquipmentListDTO> equipment = equipmentService.getByType(typeId);
        return ResponseEntity.ok(equipment);
    }

    /**
     * Получение оборудования сотрудника
     * GET /api/equipment/employee/{employeeId}
     */
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Получить оборудование сотрудника",
            description = "Возвращает всё оборудование, закреплённое за сотрудником")
    public ResponseEntity<List<EquipmentListDTO>> getByEmployee(
            @Parameter(description = "ID сотрудника")
            @PathVariable Long employeeId) {
        log.debug("REST запрос на получение оборудования сотрудника ID: {}", employeeId);
        List<EquipmentListDTO> equipment = equipmentService.getByEmployee(employeeId);
        return ResponseEntity.ok(equipment);
    }

    /**
     * Получение оборудования по статусу
     * GET /api/equipment/status/{status}
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Получить оборудование по статусу",
            description = "Возвращает оборудование с указанным статусом (Active, Inactive, Maintenance, Retired)")
    public ResponseEntity<List<EquipmentListDTO>> getByStatus(
            @Parameter(description = "Статус оборудования")
            @PathVariable String status) {
        log.debug("REST запрос на получение оборудования со статусом: {}", status);
        List<EquipmentListDTO> equipment = equipmentService.getByStatus(status);
        return ResponseEntity.ok(equipment);
    }

    /**
     * Поиск оборудования по названию
     * GET /api/equipment/search/name?name=...
     */
    @GetMapping("/search/name")
    @Operation(summary = "Поиск оборудования по названию",
            description = "Поиск по частичному совпадению названия (без учёта регистра)")
    public ResponseEntity<List<EquipmentListDTO>> searchByName(
            @Parameter(description = "Часть названия для поиска")
            @RequestParam String name) {
        log.debug("REST запрос на поиск оборудования по названию: {}", name);
        List<EquipmentListDTO> equipment = equipmentService.searchByName(name);
        return ResponseEntity.ok(equipment);
    }

    /**
     * Поиск оборудования по адресу
     * GET /api/equipment/search/address?address=...
     */
    @GetMapping("/search/address")
    @Operation(summary = "Поиск оборудования по адресу",
            description = "Поиск по частичному совпадению адреса (без учёта регистра)")
    public ResponseEntity<List<EquipmentListDTO>> searchByAddress(
            @Parameter(description = "Часть адреса для поиска")
            @RequestParam String address) {
        log.debug("REST запрос на поиск оборудования по адресу: {}", address);
        List<EquipmentListDTO> equipment = equipmentService.searchByAddress(address);
        return ResponseEntity.ok(equipment);
    }

    /**
     * Получение оборудования по типу и статусу
     * GET /api/equipment/type/{typeId}/status/{status}
     */
    @GetMapping("/type/{typeId}/status/{status}")
    @Operation(summary = "Получить оборудование по типу и статусу",
            description = "Комбинированный фильтр по типу и статусу")
    public ResponseEntity<List<EquipmentListDTO>> getByTypeAndStatus(
            @Parameter(description = "ID типа оборудования")
            @PathVariable Long typeId,
            @Parameter(description = "Статус")
            @PathVariable String status) {
        log.debug("REST запрос на получение оборудования типа ID {} со статусом: {}", typeId, status);
        List<EquipmentListDTO> equipment = equipmentService.getByTypeAndStatus(typeId, status);
        return ResponseEntity.ok(equipment);
    }

    /**
     * Получение оборудования добавленного после определённой даты
     * GET /api/equipment/added-after?date=...
     */
    @GetMapping("/added-after")
    @Operation(summary = "Получить оборудование добавленное после даты",
            description = "Возвращает оборудование добавленное после указанной даты")
    public ResponseEntity<List<EquipmentListDTO>> getAddedAfter(
            @Parameter(description = "Дата (ISO Date)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.debug("REST запрос на получение оборудования добавленного после: {}", date);
        List<EquipmentListDTO> equipment = equipmentService.getAddedAfter(date);
        return ResponseEntity.ok(equipment);
    }

    /**
     * Получение оборудования требующего обслуживания
     * GET /api/equipment/needs-maintenance?months=...
     */
    @GetMapping("/needs-maintenance")
    @Operation(summary = "Получить оборудование требующее обслуживания",
            description = "Возвращает оборудование, не обновлявшееся указанное количество месяцев")
    public ResponseEntity<List<EquipmentListDTO>> getEquipmentNeedingMaintenance(
            @Parameter(description = "Количество месяцев без обновления (по умолчанию 6)")
            @RequestParam(defaultValue = "6") int months) {
        log.debug("REST запрос на получение оборудования без обслуживания более {} месяцев", months);
        List<EquipmentListDTO> equipment = equipmentService.getEquipmentNeedingMaintenance(months);
        return ResponseEntity.ok(equipment);
    }

    /**
     * Изменение статуса оборудования
     * PATCH /api/equipment/{id}/status?status=...
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Изменить статус оборудования",
            description = "Устанавливает новый статус для оборудования")
    public ResponseEntity<EquipmentResponseDTO> changeStatus(
            @Parameter(description = "ID оборудования")
            @PathVariable Long id,
            @Parameter(description = "Новый статус (Active, Inactive, Maintenance, Retired)")
            @RequestParam String status) {
        log.info("REST запрос на изменение статуса оборудования ID {} на: {}", id, status);
        EquipmentResponseDTO updated = equipmentService.changeStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * Подсчёт оборудования по типу
     * GET /api/equipment/type/{typeId}/count
     */
    @GetMapping("/type/{typeId}/count")
    @Operation(summary = "Подсчитать оборудование по типу")
    public ResponseEntity<Long> countByType(
            @Parameter(description = "ID типа оборудования")
            @PathVariable Long typeId) {
        log.debug("REST запрос на подсчёт оборудования типа ID: {}", typeId);
        long count = equipmentService.countByType(typeId);
        return ResponseEntity.ok(count);
    }

    /**
     * Подсчёт оборудования по статусу
     * GET /api/equipment/status/{status}/count
     */
    @GetMapping("/status/{status}/count")
    @Operation(summary = "Подсчитать оборудование по статусу")
    public ResponseEntity<Long> countByStatus(
            @Parameter(description = "Статус оборудования")
            @PathVariable String status) {
        log.debug("REST запрос на подсчёт оборудования со статусом: {}", status);
        long count = equipmentService.countByStatus(status);
        return ResponseEntity.ok(count);
    }
}
