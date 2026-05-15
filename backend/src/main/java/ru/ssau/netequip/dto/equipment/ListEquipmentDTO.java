package ru.ssau.netequip.dto.equipment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListEquipmentDTO {

    private Long id;
    private String name;
    private String typeName;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private String ipAddress;
    private String macAddress;
    private String address;
    private String status;
    private LocalDate dateAdded;

    private Long typeId;

    private Long employeeId;
    private String employeeFullName;

    private Integer portsCount;
}
