package ru.ssau.netequip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.entity.EquipmentType;
import ru.ssau.netequip.enums.StatusEquipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    Optional<Equipment> findBySerialNumber(String serialNumber);

    Optional<Equipment> findByMacAddress(String macAddress);

    Optional<Equipment> findByIpAddress(String ipAddress);

    List<Equipment> findByType(EquipmentType type);

    List<Equipment> findByEmployee(Employee employee);

    List<Equipment> findByTypeAndStatus(EquipmentType type, StatusEquipment status);

    List<Equipment> findByDateAddedAfter(LocalDate date);

    int countByType(EquipmentType type);

    int countByStatus(StatusEquipment status);

    Optional<Equipment> findByName(String name);

    @Query("""
    SELECT e FROM Equipment e
    WHERE LOWER(COALESCE(e.name, '')) LIKE LOWER(CONCAT('%', :search, '%'))
       OR LOWER(COALESCE(e.serialNumber, '')) LIKE LOWER(CONCAT('%', :search, '%'))
       OR LOWER(COALESCE(e.macAddress, '')) LIKE LOWER(CONCAT('%', :search, '%'))
       OR LOWER(COALESCE(e.ipAddress, '')) LIKE LOWER(CONCAT('%', :search, '%'))
       OR LOWER(COALESCE(e.address, '')) LIKE LOWER(CONCAT('%', :search, '%'))
       OR LOWER(COALESCE(e.type.typeName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
       OR LOWER(COALESCE(e.employee.fullName, '')) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    Page<Equipment> search(@Param("search") String search, Pageable pageable);
}
