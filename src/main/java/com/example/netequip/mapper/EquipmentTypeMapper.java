package com.example.netequip.mapper;

import com.example.netequip.dto.equipmenttype.CreateEquipmentTypeDTO;
import com.example.netequip.dto.equipmenttype.EquipmentTypeResponseDTO;
import com.example.netequip.dto.equipmenttype.UpdateEquipmentTypeDTO;
import com.example.netequip.entity.EquipmentType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper для конвертации между EquipmentType Entity и DTO
 * MapStruct автоматически генерирует реализацию
 */
@Mapper(componentModel = "spring")
public interface EquipmentTypeMapper {

    /**
     * Конвертация Entity → Response DTO
     * Используется для возврата данных клиенту
     */
    EquipmentTypeResponseDTO toResponseDTO(EquipmentType entity);

    /**
     * Конвертация Create DTO → Entity
     * Используется при создании нового типа оборудования
     */
    @Mapping(target = "id", ignore = true) // ID генерируется БД
    EquipmentType toEntity(CreateEquipmentTypeDTO dto);

    /**
     * Конвертация Update DTO → Entity
     * Используется при обновлении существующего типа
     */
    @Mapping(target = "id", ignore = true) // ID не обновляется
    EquipmentType toEntity(UpdateEquipmentTypeDTO dto);

    /**
     * Обновление существующего Entity из Update DTO
     * Используется для частичного обновления полей
     *
     * @param dto - новые данные
     * @param entity - существующий объект для обновления
     */
    @Mapping(target = "id", ignore = true) // ID не меняется
    void updateEntityFromDTO(UpdateEquipmentTypeDTO dto, @MappingTarget EquipmentType entity);
}
