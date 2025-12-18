package com.example.netequip.dto.employee;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для обновления существующего сотрудника
 * Используется в PUT запросах
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeDTO {

    @NotBlank(message = "ФИО обязательно")
    @Size(max = 100, message = "ФИО не должно превышать 100 символов")
    private String fullName;

    @Size(max = 100, message = "Должность не должна превышать 100 символов")
    private String position;

    @Email(message = "Некорректный email")
    @Size(max = 100, message = "Email не должен превышать 100 символов")
    private String email;
}
