package ru.ssau.netequip.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseEmployeeDto {
    private Long id;
    private String fullName;
    private String position;
    private String email;
}
