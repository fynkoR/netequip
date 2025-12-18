package com.example.netequip.dto.deviceport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для возврата информации о порте устройства
 * Используется в GET запросах
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DevicePortResponseDTO {

    private Long id;

    // Информация об оборудовании-владельце порта
    private Long equipmentId;
    private String equipmentName;

    // Информация о порте
    private Integer portNumber;
    private String portType;
    private String status;
    private String speed;

    // Информация о подключении (опционально)
    private Long connectedToEquipmentId;      // К какому устройству подключен
    private String connectedToEquipmentName;  // Название устройства
    private Long connectedToPortId;           // К какому порту подключен
    private Integer connectedToPortNumber;    // Номер порта

    private String description;
}
