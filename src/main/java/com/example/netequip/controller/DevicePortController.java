package com.example.netequip.controller;

import com.example.netequip.dto.deviceport.CreateDevicePortDTO;
import com.example.netequip.dto.deviceport.DevicePortResponseDTO;
import com.example.netequip.dto.deviceport.UpdateDevicePortDTO;
import com.example.netequip.service.DevicePortService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для управления портами сетевого оборудования
 * Базовый путь: /api/device-ports
 */

@RestController
@RequestMapping("/api/device-ports")
@RequiredArgsConstructor
@Tag(name = "Device Ports", description = "API для управления портами сетевого оборудования")
public class DevicePortController {

    private static final Logger log = LoggerFactory.getLogger(DevicePortController.class);

    private final DevicePortService devicePortService;

    /**
     * Получение всех портов
     * GET /api/device-ports
     */
    @GetMapping
    @Operation(summary = "Получить все порты",
            description = "Возвращает список всех портов в системе")
    public ResponseEntity<List<DevicePortResponseDTO>> getAllDevicePorts() {
        log.debug("REST запрос на получение всех портов");
        List<DevicePortResponseDTO> ports = devicePortService.getAll();
        return ResponseEntity.ok(ports);
    }

    /**
     * Получение порта по ID
     * GET /api/device-ports/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить порт по ID")
    public ResponseEntity<DevicePortResponseDTO> getDevicePortById(
            @Parameter(description = "ID порта")
            @PathVariable Long id) {
        log.debug("REST запрос на получение порта с ID: {}", id);
        DevicePortResponseDTO port = devicePortService.getById(id);
        return ResponseEntity.ok(port);
    }

    /**
     * Создание нового порта
     * POST /api/device-ports
     */
    @PostMapping
    @Operation(summary = "Создать новый порт",
            description = "Создаёт новый порт для оборудования")
    public ResponseEntity<DevicePortResponseDTO> createDevicePort(
            @Valid @RequestBody CreateDevicePortDTO dto) {
        log.info("REST запрос на создание порта {} для оборудования ID: {}",
                dto.getPortNumber(), dto.getEquipmentId());
        DevicePortResponseDTO created = devicePortService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновление существующего порта
     * PUT /api/device-ports/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить порт")
    public ResponseEntity<DevicePortResponseDTO> updateDevicePort(
            @Parameter(description = "ID порта")
            @PathVariable Long id,
            @Valid @RequestBody UpdateDevicePortDTO dto) {
        log.info("REST запрос на обновление порта с ID: {}", id);
        DevicePortResponseDTO updated = devicePortService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удаление порта
     * DELETE /api/device-ports/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить порт")
    public ResponseEntity<Void> deleteDevicePort(
            @Parameter(description = "ID порта")
            @PathVariable Long id) {
        log.info("REST запрос на удаление порта с ID: {}", id);
        devicePortService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получение всех портов оборудования
     * GET /api/device-ports/equipment/{equipmentId}
     */
    @GetMapping("/equipment/{equipmentId}")
    @Operation(summary = "Получить все порты оборудования",
            description = "Возвращает все порты конкретного оборудования (отсортировано по номеру порта)")
    public ResponseEntity<List<DevicePortResponseDTO>> getByEquipment(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId) {
        log.debug("REST запрос на получение портов оборудования ID: {}", equipmentId);
        List<DevicePortResponseDTO> ports = devicePortService.getByEquipment(equipmentId);
        return ResponseEntity.ok(ports);
    }

    /**
     * Получение порта по номеру на оборудовании
     * GET /api/device-ports/equipment/{equipmentId}/port/{portNumber}
     */
    @GetMapping("/equipment/{equipmentId}/port/{portNumber}")
    @Operation(summary = "Получить порт по номеру",
            description = "Находит конкретный порт оборудования по его номеру")
    public ResponseEntity<DevicePortResponseDTO> getByEquipmentAndPortNumber(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId,
            @Parameter(description = "Номер порта")
            @PathVariable Integer portNumber) {
        log.debug("REST запрос на получение порта {} оборудования ID: {}", portNumber, equipmentId);
        DevicePortResponseDTO port = devicePortService
                .getByEquipmentAndPortNumber(equipmentId, portNumber);
        return ResponseEntity.ok(port);
    }

    /**
     * Получение портов по статусу (все устройства)
     * GET /api/device-ports/status/{status}
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Получить порты по статусу",
            description = "Возвращает все порты с указанным статусом (Active, Inactive, Reserved)")
    public ResponseEntity<List<DevicePortResponseDTO>> getByStatus(
            @Parameter(description = "Статус порта")
            @PathVariable String status) {
        log.debug("REST запрос на получение портов со статусом: {}", status);
        List<DevicePortResponseDTO> ports = devicePortService.getByStatus(status);
        return ResponseEntity.ok(ports);
    }

    /**
     * Получение активных портов оборудования
     * GET /api/device-ports/equipment/{equipmentId}/active
     */
    @GetMapping("/equipment/{equipmentId}/active")
    @Operation(summary = "Получить активные порты оборудования",
            description = "Возвращает только активные (используемые) порты")
    public ResponseEntity<List<DevicePortResponseDTO>> getActivePortsByEquipment(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId) {
        log.debug("REST запрос на получение активных портов оборудования ID: {}", equipmentId);
        List<DevicePortResponseDTO> ports = devicePortService.getActivePortsByEquipment(equipmentId);
        return ResponseEntity.ok(ports);
    }

    /**
     * Получение свободных портов оборудования
     * GET /api/device-ports/equipment/{equipmentId}/available
     */
    @GetMapping("/equipment/{equipmentId}/available")
    @Operation(summary = "Получить свободные порты оборудования",
            description = "Возвращает порты без подключений (connectedPortId = null)")
    public ResponseEntity<List<DevicePortResponseDTO>> getAvailablePortsByEquipment(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId) {
        log.debug("REST запрос на получение свободных портов оборудования ID: {}", equipmentId);
        List<DevicePortResponseDTO> ports = devicePortService
                .getAvailablePortsByEquipment(equipmentId);
        return ResponseEntity.ok(ports);
    }

    /**
     * Получение портов по типу
     * GET /api/device-ports/type/{portType}
     */
    @GetMapping("/type/{portType}")
    @Operation(summary = "Получить порты по типу",
            description = "Возвращает все порты определённого типа (Ethernet, Fiber, SFP и т.д.)")
    public ResponseEntity<List<DevicePortResponseDTO>> getByPortType(
            @Parameter(description = "Тип порта")
            @PathVariable String portType) {
        log.debug("REST запрос на получение портов типа: {}", portType);
        List<DevicePortResponseDTO> ports = devicePortService.getByPortType(portType);
        return ResponseEntity.ok(ports);
    }

    /**
     * Получение портов оборудования по типу и статусу
     * GET /api/device-ports/equipment/{equipmentId}/type/{portType}/status/{status}
     */
    @GetMapping("/equipment/{equipmentId}/type/{portType}/status/{status}")
    @Operation(summary = "Получить порты по типу и статусу",
            description = "Комбинированный фильтр по оборудованию, типу и статусу порта")
    public ResponseEntity<List<DevicePortResponseDTO>> getByEquipmentAndTypeAndStatus(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId,
            @Parameter(description = "Тип порта")
            @PathVariable String portType,
            @Parameter(description = "Статус порта")
            @PathVariable String status) {
        log.debug("REST запрос на получение портов оборудования ID {} типа '{}' со статусом '{}'",
                equipmentId, portType, status);
        List<DevicePortResponseDTO> ports = devicePortService
                .getByEquipmentAndTypeAndStatus(equipmentId, portType, status);
        return ResponseEntity.ok(ports);
    }

    /**
     * Подключение двух портов друг к другу
     * PATCH /api/device-ports/{sourcePortId}/connect/{targetPortId}
     */
    @PatchMapping("/{sourcePortId}/connect/{targetPortId}")
    @Operation(summary = "Подключить два порта",
            description = "Создаёт двустороннюю связь между двумя портами")
    public ResponseEntity<DevicePortResponseDTO> connectPorts(
            @Parameter(description = "ID исходного порта")
            @PathVariable Long sourcePortId,
            @Parameter(description = "ID целевого порта")
            @PathVariable Long targetPortId) {
        log.info("REST запрос на подключение портов: {} <-> {}", sourcePortId, targetPortId);
        DevicePortResponseDTO updated = devicePortService.connectPorts(sourcePortId, targetPortId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Отключение порта
     * PATCH /api/device-ports/{portId}/disconnect
     */
    @PatchMapping("/{portId}/disconnect")
    @Operation(summary = "Отключить порт",
            description = "Разрывает связь порта с другим портом")
    public ResponseEntity<DevicePortResponseDTO> disconnectPort(
            @Parameter(description = "ID порта")
            @PathVariable Long portId) {
        log.info("REST запрос на отключение порта ID: {}", portId);
        DevicePortResponseDTO updated = devicePortService.disconnectPort(portId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Изменение статуса порта
     * PATCH /api/device-ports/{portId}/status?status=...
     */
    @PatchMapping("/{portId}/status")
    @Operation(summary = "Изменить статус порта",
            description = "Устанавливает новый статус для порта")
    public ResponseEntity<DevicePortResponseDTO> changeStatus(
            @Parameter(description = "ID порта")
            @PathVariable Long portId,
            @Parameter(description = "Новый статус (Active, Inactive, Reserved)")
            @RequestParam String status) {
        log.info("REST запрос на изменение статуса порта ID {} на: {}", portId, status);
        DevicePortResponseDTO updated = devicePortService.changeStatus(portId, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * Получение подключённого порта
     * GET /api/device-ports/{portId}/connected
     */
    @GetMapping("/{portId}/connected")
    @Operation(summary = "Получить подключённый порт",
            description = "Возвращает порт, к которому подключён данный порт")
    public ResponseEntity<DevicePortResponseDTO> getConnectedPort(
            @Parameter(description = "ID порта")
            @PathVariable Long portId) {
        log.debug("REST запрос на получение подключённого порта для ID: {}", portId);
        DevicePortResponseDTO connectedPort = devicePortService.getConnectedPort(portId);
        return ResponseEntity.ok(connectedPort);
    }

    /**
     * Проверка подключения порта
     * GET /api/device-ports/{portId}/is-connected
     */
    @GetMapping("/{portId}/is-connected")
    @Operation(summary = "Проверить подключён ли порт",
            description = "Возвращает true если порт подключён к другому порту")
    public ResponseEntity<Boolean> isPortConnected(
            @Parameter(description = "ID порта")
            @PathVariable Long portId) {
        log.debug("REST запрос на проверку подключения порта ID: {}", portId);
        boolean connected = devicePortService.isPortConnected(portId);
        return ResponseEntity.ok(connected);
    }

    /**
     * Подсчёт портов оборудования
     * GET /api/device-ports/equipment/{equipmentId}/count
     */
    @GetMapping("/equipment/{equipmentId}/count")
    @Operation(summary = "Подсчитать порты оборудования")
    public ResponseEntity<Long> countByEquipment(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId) {
        log.debug("REST запрос на подсчёт портов оборудования ID: {}", equipmentId);
        long count = devicePortService.countByEquipment(equipmentId);
        return ResponseEntity.ok(count);
    }

    /**
     * Подсчёт активных портов оборудования
     * GET /api/device-ports/equipment/{equipmentId}/count-active
     */
    @GetMapping("/equipment/{equipmentId}/count-active")
    @Operation(summary = "Подсчитать активные порты оборудования")
    public ResponseEntity<Long> countActivePortsByEquipment(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId) {
        log.debug("REST запрос на подсчёт активных портов оборудования ID: {}", equipmentId);
        long count = devicePortService.countActivePortsByEquipment(equipmentId);
        return ResponseEntity.ok(count);
    }
}
