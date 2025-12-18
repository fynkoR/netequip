package com.example.netequip.mapper;

import com.example.netequip.dto.equipment.CreateEquipmentDTO;
import com.example.netequip.dto.equipment.EquipmentListDTO;
import com.example.netequip.dto.equipment.EquipmentResponseDTO;
import com.example.netequip.dto.equipment.UpdateEquipmentDTO;
import com.example.netequip.entity.Equipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper для конвертации между Equipment Entity и DTO
 */
@Mapper(componentModel = "spring")
public interface EquipmentMapper {

    /**
     * Конвертация Entity → Response DTO (полная информация)
     * Извлекаем данные из связанных объектов
     */
    @Mapping(source = "type.id", target = "typeId")
    @Mapping(source = "type.typeName", target = "typeName")
    @Mapping(source = "type.manufacturer", target = "manufacturer")
    @Mapping(source = "type.model", target = "model")
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "employee.fullName", target = "employeeFullName")
    @Mapping(target = "portsCount", ignore = true)         // Устанавливаем в Service
    @Mapping(target = "ipAddressesCount", ignore = true)   // Устанавливаем в Service
    @Mapping(target = "maintenanceCount", ignore = true)   // Устанавливаем в Service
    EquipmentResponseDTO toResponseDTO(Equipment entity);

    /**
     * Конвертация Entity → List DTO (краткая информация)
     */
    @Mapping(source = "type.typeName", target = "typeName")
    @Mapping(source = "type.manufacturer", target = "manufacturer")
    @Mapping(source = "type.model", target = "model")
    @Mapping(target = "portsCount", ignore = true)  // Устанавливаем в Service
    EquipmentListDTO toListDTO(Equipment entity);

    /**
     * Конвертация Create DTO → Entity
     * Связи type и employee устанавливаются в Service
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "employee", ignore = true)
    Equipment toEntity(CreateEquipmentDTO dto);

    /**
     * Конвертация Update DTO → Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "employee", ignore = true)
    Equipment toEntity(UpdateEquipmentDTO dto);

    /**
     * Обновление существующего Entity из Update DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "dateAdded", ignore = true)  // Дата добавления не меняется
    void updateEntityFromDTO(UpdateEquipmentDTO dto, @MappingTarget Equipment entity);
}
