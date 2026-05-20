package ru.ssau.netequip.dto.audit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseAuditLogDto {
    private Long id;
    private LocalDateTime timestamp;
    private String username;
    private Long employeeId;
    private String employeeFullName;
    private String action;
    private String entityType;
    private Long entityId;
    private String entityName;
    private String description;
}