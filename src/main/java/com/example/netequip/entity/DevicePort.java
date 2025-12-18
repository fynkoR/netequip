package com.example.netequip.entity;

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
@Table(name = "device_port")
public class DevicePort {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;
    private Integer portNumber;
    private String portType;
    private String status;
    private String speed;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="connected_to_equipment_id")
    private Equipment connectedToEquipment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connected_to_port_id")
    private DevicePort connectedToPort;
    private String description;
}
