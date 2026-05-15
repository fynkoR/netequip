package ru.ssau.netequip.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ssau.netequip.entity.EquipmentType;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentTypeRepository extends JpaRepository<EquipmentType, Long> {
    Optional<EquipmentType> findByTypeName(String typeName);
    List<EquipmentType> findByManufacturer(String manufacturer);
    Optional<EquipmentType> findByManufacturerAndModel(String manufacturer, String model);
    boolean existsByTypeName(String typeName);
}
