package com.example.netequip.dto.equipment;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * DTO для возврата полной информации об оборудовании
 * Используется в GET /api/equipment/{id}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentResponseDTO {

    private Long id;

    // Информация о типе
    private Long typeId;
    private String typeName;
    private String manufacturer;
    private String model;

    // Информация об ответственном
    private Long employeeId;
    private String employeeFullName;

    // Основные данные
    private String name;
    private String serialNumber;
    private String macAddress;
    private String ipAddress;
    private String address;
    private String status;

    // Даты
    private LocalDate dateAdded;
    private LocalDate dateUpdated;

    // Технические параметры (JSON)
    private Map<String, Object> technicalParams;

    // Статистика (опционально)
    private Integer portsCount;        // Количество портов
    private Integer ipAddressesCount;  // Количество IP-адресов
    private Integer maintenanceCount;  // Количество обслуживаний
}
