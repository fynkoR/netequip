package ru.ssau.netequip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ssau.netequip.entity.DevicePort;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.enums.StatusPort;

import java.util.List;
import java.util.Optional;

@Repository
public interface DevicePortRepository extends JpaRepository<DevicePort, Long> {
    List<DevicePort> findByEquipmentOrderByPortNumberAsc(Equipment equipment);

    Optional<DevicePort> findByEquipmentAndPortNumber(Equipment equipment, Integer portNumber);

    List<DevicePort> findByEquipmentAndStatus(Equipment equipment, StatusPort status);

    @Query("SELECT p FROM DevicePort p WHERE p.equipment = :equipment AND p.connectedToEquipment IS NULL")
    List<DevicePort> findAvailablePortsByEquipment(@Param("equipment") Equipment equipment);

    @Query("SELECT p FROM DevicePort p WHERE p.equipment = :equipment AND p.connectedToEquipment IS NOT NULL")
    List<DevicePort> findOccupiedPortsByEquipment(@Param("equipment") Equipment equipment);

    long countByEquipment(Equipment equipment);

    long countByEquipmentAndStatus(Equipment equipment, StatusPort status);

    List<DevicePort> findByConnectedToEquipment(Equipment equipment);

    List<DevicePort> findByConnectedToPort(DevicePort port);
    List<DevicePort> findByEquipmentId(Long equipmentId);
    void deleteByEquipmentId(Long equipmentId);
    List<DevicePort> findByConnectedToEquipmentId(Long equipmentId);



}
