package com.example.netequip.repository;

import com.example.netequip.entity.Equipment;
import com.example.netequip.entity.Employee;
import com.example.netequip.entity.MaintenanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaintenanceHistoryRepository extends JpaRepository<MaintenanceHistory, Long> {

    // Получить всю историю обслуживания устройства
    List<MaintenanceHistory> findByEquipmentOrderByDateDesc(Equipment equipment);

    // Получить историю обслуживания по типу
    List<MaintenanceHistory> findByEquipmentAndType(Equipment equipment, String type);

    // Получить обслуживания выполненные сотрудником
    List<MaintenanceHistory> findByPerformedBy(Employee employee);

    // Получить обслуживания за период
    List<MaintenanceHistory> findByDateBetween(LocalDateTime start, LocalDateTime end);

    // Получить последнее обслуживание устройства
    @Query("SELECT m FROM MaintenanceHistory m WHERE m.equipment = :equipment ORDER BY m.date DESC LIMIT 1")
    MaintenanceHistory findLatestByEquipment(@Param("equipment") Equipment equipment);

    // Получить устройства требующие обслуживания (nextMaintenanceDate прошла)
    @Query("SELECT m FROM MaintenanceHistory m WHERE m.nextMaintenanceDate < :currentDate ORDER BY m.nextMaintenanceDate ASC")
    List<MaintenanceHistory> findOverdueMaintenances(@Param("currentDate") LocalDate currentDate);

    // Подсчет обслуживаний устройства
    long countByEquipment(Equipment equipment);

    // Подсчет обслуживаний по типу
    long countByType(String type);

    // Получить обслуживания устройства за последние N дней
    @Query("SELECT m FROM MaintenanceHistory m WHERE m.equipment = :equipment AND m.date >= :since ORDER BY m.date DESC")
    List<MaintenanceHistory> findRecentMaintenances(@Param("equipment") Equipment equipment,
                                                    @Param("since") LocalDateTime since);
}
