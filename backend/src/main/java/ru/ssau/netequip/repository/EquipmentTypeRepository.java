package ru.ssau.netequip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ssau.netequip.entity.EquipmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentTypeRepository extends JpaRepository<EquipmentType, Long> {
    Optional<EquipmentType> findByTypeName(String typeName);
    List<EquipmentType> findByManufacturer(String manufacturer);
    Optional<EquipmentType> findByManufacturerAndModel(String manufacturer, String model);
    boolean existsByTypeName(String typeName);
    Optional<EquipmentType> findBySnmpObjectId(String snmpObjectId);
    @Query("""
    SELECT t FROM EquipmentType t
    WHERE LOWER(t.typeName) LIKE LOWER(CONCAT('%', :search, '%'))
       OR LOWER(COALESCE(t.manufacturer, '')) LIKE LOWER(CONCAT('%', :search, '%'))
       OR LOWER(COALESCE(t.model, '')) LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    Page<EquipmentType> search(@Param("search") String search, Pageable pageable);
}
