package ru.ssau.netequip.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
        @Index(name = "idx_audit_entity",    columnList = "entity_type, entity_id"),
        @Index(name = "idx_audit_username",  columnList = "username")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    /** Username из User (логин). */
    @Column(nullable = false, length = 100)
    private String username;

    /** ID связанного Employee (если есть). */
    @Column(name = "employee_id")
    private Long employeeId;

    /** ФИО сотрудника на момент действия. */
    @Column(name = "employee_full_name", length = 200)
    private String employeeFullName;

    /** CREATE, UPDATE, DELETE — без enum, чтобы потом добавлять новые без миграций. */
    @Column(nullable = false, length = 50)
    private String action;

    /** "equipment", "employee", "ip_address", "device_port", "maintenance_history", "equipment_type". */
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    /** ID записи в исходной таблице. Без FK — журнал должен пережить удаление. */
    @Column(name = "entity_id")
    private Long entityId;

    /** Имя/название записи для удобства чтения после удаления. */
    @Column(name = "entity_name", length = 300)
    private String entityName;

    /** Человеческое описание: "Статус: ACTIVE → MAINTENANCE", "Создано", "Удалено" и т.п. */
    @Column(length = 2000)
    private String description;
}