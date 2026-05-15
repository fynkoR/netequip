package ru.ssau.netequip.dto.ipAddress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseIpAddressDto {

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
