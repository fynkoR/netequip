package com.example.netequip.dto.equipmenttype;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для возврата информации о типе оборудования
 * Используется в GET запросах
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentTypeResponseDTO {

    private Long id;
    private String typeName;
    private String manufacturer;
    private String model;
    private Integer defaultPortCount;
    private String connectionType;
    private String osiLevel;
    private String description;
}
