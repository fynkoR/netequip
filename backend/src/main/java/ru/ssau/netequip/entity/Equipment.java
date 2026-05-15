package ru.ssau.netequip.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ssau.netequip.enums.StatusEquipment;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "equipment")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private EquipmentType type;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    private String name;
    private String serialNumber;
    private String macAddress;
    private String ipAddress;
    private String address;
    @Enumerated(EnumType.STRING)
    private StatusEquipment status;
    private LocalDate dateAdded;
    private LocalDate dateUpdated;
}
