package com.example.netequip.dto.deviceport;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDevicePortDTO {

    @NotNull(message = "ID оборудования обязательно")
    private Long equipmentId;

    @NotNull(message = "Номер порта обязателен")
    @Min(value = 1, message = "Номер порта должен быть больше 0")
    @Max(value = 256, message = "Номер порта не должен превышать 256")
    private Integer portNumber;

    @Size(max = 50, message = "Тип порта не должен превышать 50 символов")
    private String portType;  // ✅ Теперь с валидацией!

    @Size(max = 20, message = "Статус не должен превышать 20 символов")
    private String status;  // ✅ Теперь с валидацией!

    @Size(max = 20, message = "Скорость не должна превышать 20 символов")
    private String speed;  // ✅ Теперь с валидацию!

    private Long connectedToEquipmentId;
    private Long connectedToPortId;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;
}
