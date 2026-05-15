package ru.ssau.netequip.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ssau.netequip.enums.StatusPort;

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

    @Enumerated(EnumType.STRING)
    private StatusPort status;

    private String speed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connected_to_equipment_id")
    private Equipment connectedToEquipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connected_to_port_id")
    private DevicePort connectedToPort;

    private String description;
}
