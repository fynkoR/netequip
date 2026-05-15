package ru.ssau.netequip.dto.maintenanceHistory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAndUpdateMaintenanceHistoryDTO {

    @NotNull(message = "Equipment ID is required")
    private Long equipmentId;

    @NotNull(message = "Maintenance date is required")
    private LocalDateTime date;

    @NotBlank(message = "Maintenance type is required")
    @Size(max = 50, message = "Maintenance type must be at most 50 characters")
    @Pattern(
            regexp = "Routine|Repair|Upgrade|Emergency|Preventive",
            message = "Maintenance type must be one of: Routine, Repair, Upgrade, Emergency, Preventive"
    )
    private String type;

    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    private Long performedById;

    @DecimalMin(value = "0.0", inclusive = true, message = "Cost must not be negative")
    @Digits(integer = 8, fraction = 2, message = "Cost must have up to 8 integer digits and 2 fractional digits")
    private BigDecimal cost;

    @Future(message = "Next maintenance date must be in the future")
    private LocalDate nextMaintenanceDate;
}
