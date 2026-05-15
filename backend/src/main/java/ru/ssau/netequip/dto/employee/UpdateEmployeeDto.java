package ru.ssau.netequip.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ssau.netequip.enums.Position;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeDto {
    @NotBlank(message = "ФИО обязательно")
    @Size(max = 100, message = "ФИО не должно превышать 100 символов")
    private String fullName;

    @NotNull(message = "Должность обязательна")
    private Position position;

    @Email(message = "Некорректный email")
    @Size(max = 100, message = "Email не должен превышать 100 символов")
    private String email;
}
