package com.example.netequip.mapper;

import com.example.netequip.dto.ipaddress.CreateIpAddressDTO;
import com.example.netequip.dto.ipaddress.IpAddressResponseDTO;
import com.example.netequip.dto.ipaddress.UpdateIpAddressDTO;
import com.example.netequip.entity.Equipment;
import com.example.netequip.entity.IpAddress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper для конвертации между IpAddress Entity и DTO
 */
@Mapper(componentModel = "spring")
public interface IpAddressMapper {

    /**
     * Конвертация Entity → Response DTO
     * Извлекаем ID и имя из связанного Equipment
     */
    @Mapping(source = "equipment.id", target = "equipmentId")
    @Mapping(source = "equipment.name", target = "equipmentName")
    IpAddressResponseDTO toResponseDTO(IpAddress entity);

    /**
     * Конвертация Create DTO → Entity
     * equipmentId будет установлен вручную в Service
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipment", ignore = true) // Устанавливаем в Service
    IpAddress toEntity(CreateIpAddressDTO dto);

    /**
     * Конвертация Update DTO → Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipment", ignore = true) // Устанавливаем в Service
    IpAddress toEntity(UpdateIpAddressDTO dto);

    /**
     * Обновление существующего Entity из Update DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipment", ignore = true) // Не меняем связь
    void updateEntityFromDTO(UpdateIpAddressDTO dto, @MappingTarget IpAddress entity);

    /**
     * Вспомогательный метод: Long equipmentId → Equipment entity
     * Используется в Service для установки связи
     */
    default Equipment mapEquipmentId(Long equipmentId) {
        if (equipmentId == null) {
            return null;
        }
        Equipment equipment = new Equipment();
        equipment.setId(equipmentId);
        return equipment;
    }
}
