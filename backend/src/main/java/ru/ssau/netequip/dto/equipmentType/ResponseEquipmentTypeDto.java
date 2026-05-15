package ru.ssau.netequip.dto.equipmentType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseEquipmentTypeDto {

    private Long id;
    private String typeName;

    private String snmpObjectId;

    private String manufacturer;
    private String model;
    private Integer defaultPortCount;
    private String connectionType;
    private String osiLevel;
    private String description;
}
