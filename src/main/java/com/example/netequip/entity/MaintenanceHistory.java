package com.example.netequip.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "maintenance_history")
public class MaintenanceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;
    private LocalDateTime date;
    private String type;
    private String description;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by_employee_id")
    private Employee performedBy;
    private BigDecimal cost;
    private LocalDate nextMaintenanceDate;
}
