package ru.ssau.netequip.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private Long employeeId;
    private String employeeFullName;   // ← новое: для отображения в таблице
    private String role;               // ← новое: VIEWER/TECHNIC/ENGINEER/ADMIN
}
