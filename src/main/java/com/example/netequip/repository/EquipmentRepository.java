package com.example.netequip.repository;

import com.example.netequip.entity.Equipment;
import com.example.netequip.entity.EquipmentType;
import com.example.netequip.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    // Поиск по серийному номеру
    Optional<Equipment> findBySerialNumber(String serialNumber);

    // Поиск по MAC-адресу
    Optional<Equipment> findByMacAddress(String macAddress);

    // Поиск по IP-адресу
    Optional<Equipment> findByIpAddress(String ipAddress);

    // Получить все устройства определенного типа
    List<Equipment> findByType(EquipmentType type);

    // Получить все устройства определенного сотрудника
    List<Equipment> findByEmployee(Employee employee);

    // Поиск по статусу
    List<Equipment> findByStatus(String status);

    // Поиск по адресу (частичное совпадение)
    List<Equipment> findByAddressContainingIgnoreCase(String address);

    // Поиск по названию (частичное совпадение)
    List<Equipment> findByNameContainingIgnoreCase(String name);

    // Получить оборудование по типу и статусу
    List<Equipment> findByTypeAndStatus(EquipmentType type, String status);

    // Получить оборудование добавленное после определенной даты
    List<Equipment> findByDateAddedAfter(LocalDate date);

    // Получить оборудование требующее обслуживания (не обслуживалось долго)
    @Query("SELECT e FROM Equipment e WHERE e.dateUpdated < :date OR e.dateUpdated IS NULL")
    List<Equipment> findEquipmentNeedingMaintenance(@Param("date") LocalDate date);

    // Подсчет оборудования по типу
    long countByType(EquipmentType type);

    // Подсчет оборудования по статусу
    long countByStatus(String status);

    // Проверка существования серийного номера
    boolean existsBySerialNumber(String serialNumber);

    // Проверка существования MAC-адреса
    boolean existsByMacAddress(String macAddress);

    // Получить все активное оборудование
    List<Equipment> findByStatusOrderByNameAsc(String status);
}
