package ru.ssau.netequip.dto.devicePort;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ssau.netequip.enums.StatusPort;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAndUpdateDevicePortDto {

    @NotNull(message = "Equipment ID is required")
    private Long equipmentId;

    @NotNull(message = "Port number is required")
    @Min(value = 1, message = "Port number must be greater than 0")
    @Max(value = 256, message = "Port number must be at most 256")
    private Integer portNumber;

    @Size(max = 50, message = "Port type must be at most 50 characters")
    private String portType;

    @NotNull(message = "Port status is required")
    private StatusPort status;

    @Size(max = 20, message = "Speed must be at most 20 characters")
    private String speed;

    private Long connectedToEquipmentId;
    private Long connectedToPortId;

    @Size(max = 200, message = "Description must be at most 200 characters")
    private String description;
}
