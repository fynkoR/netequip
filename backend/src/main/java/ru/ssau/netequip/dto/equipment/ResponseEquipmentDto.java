package ru.ssau.netequip.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseEquipmentDto {

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

    // Статистика (опционально)
    private Integer portsCount;        // Количество портов
    private Integer ipAddressesCount;  // Количество IP-адресов
    private Integer maintenanceCount;  // Количество обслуживаний
}
