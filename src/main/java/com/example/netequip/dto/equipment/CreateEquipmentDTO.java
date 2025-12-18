package com.example.netequip.dto.equipment;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO для создания нового оборудования
 * Используется в POST запросах
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEquipmentDTO {

    @NotNull(message = "ID типа оборудования обязателен")
    private Long typeId;

    private Long employeeId;  // Опционально: ответственный сотрудник

    @NotBlank(message = "Название оборудования обязательно")
    @Size(max = 100, message = "Название не должно превышать 100 символов")
    private String name;

    @Size(max = 100, message = "Серийный номер не должен превышать 100 символов")
    private String serialNumber;

    @Size(max = 50, message = "MAC-адрес не должен превышать 50 символов")
    @Pattern(
            regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$",
            message = "Некорректный формат MAC-адреса (например: 00:1A:2B:3C:4D:5E)"
    )
    private String macAddress;

    @Size(max = 45, message = "IP-адрес не должен превышать 45 символов")
    @Pattern(
            regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$",
            message = "Некорректный формат IP-адреса"
    )
    private String ipAddress;

    @Size(max = 250, message = "Адрес не должен превышать 250 символов")
    private String address;

    @Size(max = 20, message = "Статус не должен превышать 20 символов")
    @Pattern(
            regexp = "Active|Inactive|Maintenance|Retired",
            message = "Статус должен быть: Active, Inactive, Maintenance или Retired"
    )
    private String status = "Active";  // По умолчанию Active

    private LocalDate dateAdded;  // Если не указано, установится текущая дата

    // Технические параметры в формате JSON
    private JsonNode technicalParams;
}
