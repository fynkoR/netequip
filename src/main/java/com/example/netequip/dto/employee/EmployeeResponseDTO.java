package com.example.netequip.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для возврата информации о сотруднике
 * Используется в GET запросах
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDTO {

    private Long id;
    private String fullName;
    private String position;
    private String email;
}
