package com.example.netequip.controller;

import com.example.netequip.dto.ipaddress.CreateIpAddressDTO;
import com.example.netequip.dto.ipaddress.IpAddressResponseDTO;
import com.example.netequip.dto.ipaddress.UpdateIpAddressDTO;
import com.example.netequip.service.IpAddressService;
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
 * REST контроллер для управления IP-адресами оборудования
 * Базовый путь: /api/ip-addresses
 */
@Slf4j
@RestController
@RequestMapping("/api/ip-addresses")
@RequiredArgsConstructor
@Tag(name = "IP Addresses", description = "API для управления IP-адресами сетевого оборудования")
public class IpAddressController {

    private final IpAddressService ipAddressService;

    /**
     * Получение всех IP-адресов
     * GET /api/ip-addresses
     */
    @GetMapping
    @Operation(summary = "Получить все IP-адреса",
            description = "Возвращает список всех IP-адресов в системе")
    public ResponseEntity<List<IpAddressResponseDTO>> getAllIpAddresses() {
        log.debug("REST запрос на получение всех IP-адресов");
        List<IpAddressResponseDTO> ipAddresses = ipAddressService.getAll();
        return ResponseEntity.ok(ipAddresses);
    }

    /**
     * Получение IP-адреса по ID
     * GET /api/ip-addresses/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить IP-адрес по ID")
    public ResponseEntity<IpAddressResponseDTO> getIpAddressById(
            @Parameter(description = "ID IP-адреса")
            @PathVariable Long id) {
        log.debug("REST запрос на получение IP-адреса с ID: {}", id);
        IpAddressResponseDTO ipAddress = ipAddressService.getById(id);
        return ResponseEntity.ok(ipAddress);
    }

    /**
     * Создание нового IP-адреса
     * POST /api/ip-addresses
     */
    @PostMapping
    @Operation(summary = "Создать новый IP-адрес",
            description = "Создаёт новый IP-адрес для оборудования")
    public ResponseEntity<IpAddressResponseDTO> createIpAddress(
            @Valid @RequestBody CreateIpAddressDTO dto) {
        log.info("REST запрос на создание IP-адреса: {}", dto.getIpAddress());
        IpAddressResponseDTO created = ipAddressService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновление существующего IP-адреса
     * PUT /api/ip-addresses/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить IP-адрес")
    public ResponseEntity<IpAddressResponseDTO> updateIpAddress(
            @Parameter(description = "ID IP-адреса")
            @PathVariable Long id,
            @Valid @RequestBody UpdateIpAddressDTO dto) {
        log.info("REST запрос на обновление IP-адреса с ID: {}", id);
        IpAddressResponseDTO updated = ipAddressService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удаление IP-адреса
     * DELETE /api/ip-addresses/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить IP-адрес")
    public ResponseEntity<Void> deleteIpAddress(
            @Parameter(description = "ID IP-адреса")
            @PathVariable Long id) {
        log.info("REST запрос на удаление IP-адреса с ID: {}", id);
        ipAddressService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получение всех IP-адресов оборудования
     * GET /api/ip-addresses/equipment/{equipmentId}
     */
    @GetMapping("/equipment/{equipmentId}")
    @Operation(summary = "Получить все IP-адреса оборудования",
            description = "Возвращает все IP-адреса, привязанные к конкретному оборудованию")
    public ResponseEntity<List<IpAddressResponseDTO>> getByEquipment(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId) {
        log.debug("REST запрос на получение IP-адресов оборудования ID: {}", equipmentId);
        List<IpAddressResponseDTO> ipAddresses = ipAddressService.getByEquipment(equipmentId);
        return ResponseEntity.ok(ipAddresses);
    }

    /**
     * Получение основного IP-адреса оборудования
     * GET /api/ip-addresses/equipment/{equipmentId}/primary
     */
    @GetMapping("/equipment/{equipmentId}/primary")
    @Operation(summary = "Получить основной IP-адрес оборудования",
            description = "Возвращает IP-адрес с флагом isPrimary=true")
    public ResponseEntity<IpAddressResponseDTO> getPrimaryIpByEquipment(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId) {
        log.debug("REST запрос на получение основного IP оборудования ID: {}", equipmentId);
        IpAddressResponseDTO ipAddress = ipAddressService.getPrimaryIpByEquipment(equipmentId);
        return ResponseEntity.ok(ipAddress);
    }

    /**
     * Поиск IP-адреса по значению
     * GET /api/ip-addresses/search?ip=...
     */
    @GetMapping("/search")
    @Operation(summary = "Найти IP-адрес по значению",
            description = "Поиск IP-адреса по точному значению (например, 192.168.1.1)")
    public ResponseEntity<IpAddressResponseDTO> getByIpAddress(
            @Parameter(description = "Значение IP-адреса")
            @RequestParam String ip) {
        log.debug("REST запрос на поиск IP-адреса: {}", ip);
        IpAddressResponseDTO ipAddress = ipAddressService.getByIpAddress(ip);
        return ResponseEntity.ok(ipAddress);
    }

    /**
     * Получение IP-адресов по типу сети
     * GET /api/ip-addresses/network-type/{networkType}
     */
    @GetMapping("/network-type/{networkType}")
    @Operation(summary = "Получить IP-адреса по типу сети",
            description = "Возвращает все IP-адреса определённого типа сети")
    public ResponseEntity<List<IpAddressResponseDTO>> getByNetworkType(
            @Parameter(description = "Тип сети (например, LAN, WAN)")
            @PathVariable String networkType) {
        log.debug("REST запрос на получение IP-адресов типа сети: {}", networkType);
        List<IpAddressResponseDTO> ipAddresses = ipAddressService.getByNetworkType(networkType);
        return ResponseEntity.ok(ipAddresses);
    }

    /**
     * Получение IP-адресов оборудования по типу сети
     * GET /api/ip-addresses/equipment/{equipmentId}/network-type/{networkType}
     */
    @GetMapping("/equipment/{equipmentId}/network-type/{networkType}")
    @Operation(summary = "Получить IP-адреса оборудования по типу сети")
    public ResponseEntity<List<IpAddressResponseDTO>> getByEquipmentAndNetworkType(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId,
            @Parameter(description = "Тип сети")
            @PathVariable String networkType) {
        log.debug("REST запрос на получение IP-адресов оборудования ID {} типа сети: {}",
                equipmentId, networkType);
        List<IpAddressResponseDTO> ipAddresses = ipAddressService
                .getByEquipmentAndNetworkType(equipmentId, networkType);
        return ResponseEntity.ok(ipAddresses);
    }

    /**
     * Получение IP-адресов по маске подсети
     * GET /api/ip-addresses/subnet-mask/{subnetMask}
     */
    @GetMapping("/subnet-mask/{subnetMask}")
    @Operation(summary = "Получить IP-адреса по маске подсети",
            description = "Возвращает все IP-адреса с указанной маской подсети")
    public ResponseEntity<List<IpAddressResponseDTO>> getBySubnetMask(
            @Parameter(description = "Маска подсети (например, 255.255.255.0)")
            @PathVariable String subnetMask) {
        log.debug("REST запрос на получение IP-адресов с маской подсети: {}", subnetMask);
        List<IpAddressResponseDTO> ipAddresses = ipAddressService.getBySubnetMask(subnetMask);
        return ResponseEntity.ok(ipAddresses);
    }

    /**
     * Установка IP-адреса как основного для оборудования
     * PATCH /api/ip-addresses/{id}/set-primary
     */
    @PatchMapping("/{id}/set-primary")
    @Operation(summary = "Установить IP-адрес как основной",
            description = "Устанавливает флаг isPrimary=true для указанного IP-адреса")
    public ResponseEntity<IpAddressResponseDTO> setPrimaryIp(
            @Parameter(description = "ID IP-адреса")
            @PathVariable Long id) {
        log.info("REST запрос на установку IP-адреса ID {} как основного", id);
        IpAddressResponseDTO updated = ipAddressService.setPrimaryIp(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Снятие флага основного IP-адреса
     * PATCH /api/ip-addresses/{id}/unset-primary
     */
    @PatchMapping("/{id}/unset-primary")
    @Operation(summary = "Снять флаг основного IP-адреса",
            description = "Устанавливает флаг isPrimary=false")
    public ResponseEntity<IpAddressResponseDTO> unsetPrimaryIp(
            @Parameter(description = "ID IP-адреса")
            @PathVariable Long id) {
        log.info("REST запрос на снятие флага основного IP с адреса ID {}", id);
        IpAddressResponseDTO updated = ipAddressService.unsetPrimaryIp(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Проверка существования IP-адреса
     * GET /api/ip-addresses/exists?ip=...
     */
    @GetMapping("/exists")
    @Operation(summary = "Проверить существование IP-адреса")
    public ResponseEntity<Boolean> existsByIpAddress(
            @Parameter(description = "Значение IP-адреса для проверки")
            @RequestParam String ip) {
        log.debug("REST запрос на проверку существования IP-адреса: {}", ip);
        boolean exists = ipAddressService.existsByIpAddress(ip);
        return ResponseEntity.ok(exists);
    }

    /**
     * Подсчёт IP-адресов оборудования
     * GET /api/ip-addresses/equipment/{equipmentId}/count
     */
    @GetMapping("/equipment/{equipmentId}/count")
    @Operation(summary = "Подсчитать IP-адреса оборудования")
    public ResponseEntity<Long> countByEquipment(
            @Parameter(description = "ID оборудования")
            @PathVariable Long equipmentId) {
        log.debug("REST запрос на подсчёт IP-адресов оборудования ID: {}", equipmentId);
        long count = ipAddressService.countByEquipment(equipmentId);
        return ResponseEntity.ok(count);
    }
}
