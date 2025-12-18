package com.example.netequip.repository;

import com.example.netequip.entity.DevicePort;
import com.example.netequip.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DevicePortRepository extends JpaRepository<DevicePort, Long> {

    // Получить все порты устройства
    List<DevicePort> findByEquipment(Equipment equipment);

    // Получить порты устройства отсортированные по номеру
    List<DevicePort> findByEquipmentOrderByPortNumberAsc(Equipment equipment);

    // Найти конкретный порт устройства
    Optional<DevicePort> findByEquipmentAndPortNumber(Equipment equipment, Integer portNumber);

    // Получить порты по статусу
    List<DevicePort> findByEquipmentAndStatus(Equipment equipment, String status);

    // Получить свободные порты устройства
    @Query("SELECT p FROM DevicePort p WHERE p.equipment = :equipment AND p.connectedToEquipment IS NULL")
    List<DevicePort> findAvailablePortsByEquipment(@Param("equipment") Equipment equipment);

    // Получить занятые порты
    @Query("SELECT p FROM DevicePort p WHERE p.equipment = :equipment AND p.connectedToEquipment IS NOT NULL")
    List<DevicePort> findOccupiedPortsByEquipment(@Param("equipment") Equipment equipment);

    // Подсчет портов устройства
    long countByEquipment(Equipment equipment);

    // Подсчет активных портов
    long countByEquipmentAndStatus(Equipment equipment, String status);

    // Найти все подключения к определенному устройству
    List<DevicePort> findByConnectedToEquipment(Equipment equipment);
}
