package com.example.netequip.dto.equipmenttype;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для создания нового типа оборудования
 * Используется в POST запросах
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEquipmentTypeDTO {

    @NotBlank(message = "Название типа обязательно")
    @Size(max = 50, message = "Название типа не должно превышать 50 символов")
    private String typeName;

    @Size(max = 100, message = "Название производителя не должно превышать 100 символов")
    private String manufacturer;

    @Size(max = 100, message = "Название модели не должно превышать 100 символов")
    private String model;

    @Min(value = 0, message = "Количество портов не может быть отрицательным")
    @Max(value = 256, message = "Количество портов не должно превышать 256")
    private Integer defaultPortCount;

    @Size(max = 50, message = "Тип подключения не должен превышать 50 символов")
    private String connectionType;

    @Size(max = 20, message = "Уровень OSI не должен превышать 20 символов")
    private String osiLevel;

    private String description;
}
