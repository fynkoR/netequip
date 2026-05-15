package ru.ssau.netequip.dto.maintenanceHistory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseMaintenanceHistoryDto {

    private Long id;

    // Информация об оборудовании
    private Long equipmentId;
    private String equipmentName;

    // Информация об обслуживании
    private LocalDateTime date;
    private String type;              // Routine, Repair, Upgrade
    private String description;

    // Информация о сотруднике
    private Long performedById;
    private String performedByName;

    // Финансовая информация
    private BigDecimal cost;

    // Планирование
    private LocalDate nextMaintenanceDate;
}
