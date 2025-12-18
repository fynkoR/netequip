package com.example.netequip.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO для возврата краткой информации об оборудовании в списках
 * Используется в GET /api/equipment (список)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentListDTO {

    private Long id;
    private String name;
    private String typeName;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private String ipAddress;
    private String address;
    private String status;
    private LocalDate dateAdded;

    // Краткая статистика
    private Integer portsCount;
}
