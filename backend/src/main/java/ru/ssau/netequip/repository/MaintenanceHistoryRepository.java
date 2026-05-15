package ru.ssau.netequip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.entity.MaintenanceHistory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MaintenanceHistoryRepository extends JpaRepository<MaintenanceHistory, Long> {
    List<MaintenanceHistory> findByEquipmentOrderByDateDesc(Equipment equipment);
    List<MaintenanceHistory> findByEquipmentAndType(Equipment equipment, String type);
    List<MaintenanceHistory> findByPerformedBy(Employee employee);
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

    void deleteByEquipmentId(Long equipmentId);
}
