package com.example.netequip.dto.ipaddress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO для возврата информации об IP-адресе
 * Используется в GET запросах
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpAddressResponseDTO {

    private Long id;
    private Long equipmentId;           // ID оборудования (не весь объект!)
    private String equipmentName;       // Название оборудования (для удобства)
    private String ipAddress;
    private String subnetMask;
    private String gateway;
    private String networkType;
    private Boolean isPrimary;
    private LocalDate assignedDate;
}
