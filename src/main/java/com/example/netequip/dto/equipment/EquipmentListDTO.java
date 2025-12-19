package com.example.netequip.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
    private String macAddress;        // ✅ ДОБАВИТЬ
    private String address;
    private String status;
    private LocalDate dateAdded;

    // Информация о типе оборудования
    private Long typeId;              // ✅ ДОБАВИТЬ

    // Информация о сотруднике
    private Long employeeId;          // ✅ ДОБАВИТЬ
    private String employeeFullName;  // ✅ ДОБАВИТЬ

    // Краткая статистика
    private Integer portsCount;
}
