package ru.ssau.netequip.dto.equipment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ssau.netequip.enums.StatusEquipment;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEquipmentDto {
    @NotNull(message = "Equipment type ID is required")
    private Long typeId;

    private Long employeeId;

    @NotBlank(message = "Equipment name is required")
    @Size(max = 100, message = "Equipment name must be at most 100 characters")
    private String name;

    @Size(max = 100, message = "Serial number must be at most 100 characters")
    private String serialNumber;

    @Size(max = 50, message = "MAC address must be at most 50 characters")
    @Pattern(
            regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$",
            message = "Invalid MAC address format"
    )
    private String macAddress;

    @Size(max = 45, message = "IP address must be at most 45 characters")
    @Pattern(
            regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$",
            message = "Invalid IP address format"
    )
    private String ipAddress;

    @Size(max = 250, message = "Address must be at most 250 characters")
    private String address;

    @NotNull(message = "Equipment status is required")
    private StatusEquipment status;

    private LocalDate dateUpdated;
}
