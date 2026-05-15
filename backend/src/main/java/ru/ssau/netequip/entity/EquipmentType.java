package ru.ssau.netequip.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "equipment_type")
public class EquipmentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String typeName;

    @Column(name = "snmp_object_id", unique = true)
    private String snmpObjectId;

    private String manufacturer;
    private String model;
    private Integer defaultPortCount;
    private String connectionType;
    private String osiLevel;
    private String description;
}
