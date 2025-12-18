package com.example.netequip.dto.maintenancehistory;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO для обновления записи об обслуживании
 * Используется в PUT запросах
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMaintenanceHistoryDTO {

    @NotNull(message = "ID оборудования обязательно")
    private Long equipmentId;

    @NotNull(message = "Дата обслуживания обязательна")
    private LocalDateTime date;

    @NotBlank(message = "Тип обслуживания обязателен")
    @Size(max = 50, message = "Тип обслуживания не должен превышать 50 символов")
    @Pattern(
            regexp = "Routine|Repair|Upgrade|Emergency|Preventive",
            message = "Тип должен быть: Routine, Repair, Upgrade, Emergency или Preventive"
    )
    private String type;

    @Size(max = 1000, message = "Описание не должно превышать 1000 символов")
    private String description;

    private Long performedById;

    @DecimalMin(value = "0.0", inclusive = true, message = "Стоимость не может быть отрицательной")
    @Digits(integer = 8, fraction = 2, message = "Стоимость должна иметь максимум 8 цифр и 2 знака после запятой")
    private BigDecimal cost;

    @Future(message = "Дата следующего обслуживания должна быть в будущем")
    private LocalDate nextMaintenanceDate;
}
