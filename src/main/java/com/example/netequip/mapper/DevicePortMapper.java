package com.example.netequip.mapper;

import com.example.netequip.dto.deviceport.CreateDevicePortDTO;
import com.example.netequip.dto.deviceport.DevicePortResponseDTO;
import com.example.netequip.dto.deviceport.UpdateDevicePortDTO;
import com.example.netequip.entity.DevicePort;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper для конвертации между DevicePort Entity и DTO
 */
@Mapper(componentModel = "spring")
public interface DevicePortMapper {

    /**
     * Конвертация Entity → Response DTO
     * Извлекаем информацию из связанных объектов
     */
    @Mapping(source = "equipment.id", target = "equipmentId")
    @Mapping(source = "equipment.name", target = "equipmentName")
    @Mapping(source = "connectedToEquipment.id", target = "connectedToEquipmentId")
    @Mapping(source = "connectedToEquipment.name", target = "connectedToEquipmentName")
    @Mapping(source = "connectedToPort.id", target = "connectedToPortId")
    @Mapping(source = "connectedToPort.portNumber", target = "connectedToPortNumber")
    DevicePortResponseDTO toResponseDTO(DevicePort entity);

    /**
     * Конвертация Create DTO → Entity
     * Связи equipment, connectedToEquipment, connectedToPort
     * устанавливаются в Service
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipment", ignore = true)
    @Mapping(target = "connectedToEquipment", ignore = true)
    @Mapping(target = "connectedToPort", ignore = true)
    DevicePort toEntity(CreateDevicePortDTO dto);

    /**
     * Конвертация Update DTO → Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipment", ignore = true)
    @Mapping(target = "connectedToEquipment", ignore = true)
    @Mapping(target = "connectedToPort", ignore = true)
    DevicePort toEntity(UpdateDevicePortDTO dto);

    /**
     * Обновление существующего Entity из Update DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipment", ignore = true)
    @Mapping(target = "connectedToEquipment", ignore = true)
    @Mapping(target = "connectedToPort", ignore = true)
    void updateEntityFromDTO(UpdateDevicePortDTO dto, @MappingTarget DevicePort entity);
}
