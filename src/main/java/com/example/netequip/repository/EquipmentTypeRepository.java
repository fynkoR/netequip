package com.example.netequip.repository;

import com.example.netequip.entity.EquipmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentTypeRepository extends JpaRepository<EquipmentType, Long> {

    // Поиск по названию типа
    Optional<EquipmentType> findByTypeName(String typeName);

    // Поиск по производителю
    List<EquipmentType> findByManufacturer(String manufacturer);

    // Поиск по производителю и модели
    Optional<EquipmentType> findByManufacturerAndModel(String manufacturer, String model);

    // Проверка существования типа
    boolean existsByTypeName(String typeName);

    // Получить все типы по производителю (сортировка по модели)
    List<EquipmentType> findByManufacturerOrderByModelAsc(String manufacturer);
}
