package com.example.netequip.mapper;

import com.example.netequip.dto.maintenancehistory.CreateMaintenanceHistoryDTO;
import com.example.netequip.dto.maintenancehistory.MaintenanceHistoryResponseDTO;
import com.example.netequip.dto.maintenancehistory.UpdateMaintenanceHistoryDTO;
import com.example.netequip.entity.MaintenanceHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper для конвертации между MaintenanceHistory Entity и DTO
 */
@Mapper(componentModel = "spring")
public interface MaintenanceHistoryMapper {

    /**
     * Конвертация Entity → Response DTO
     * Извлекаем информацию из Equipment и Employee
     */
    @Mapping(source = "equipment.id", target = "equipmentId")
    @Mapping(source = "equipment.name", target = "equipmentName")
    @Mapping(source = "performedBy.id", target = "performedById")
    @Mapping(source = "performedBy.fullName", target = "performedByName")
    MaintenanceHistoryResponseDTO toResponseDTO(MaintenanceHistory entity);

    /**
     * Конвертация Create DTO → Entity
     * Связи equipment и performedBy устанавливаются в Service
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipment", ignore = true)
    @Mapping(target = "performedBy", ignore = true)
    MaintenanceHistory toEntity(CreateMaintenanceHistoryDTO dto);

    /**
     * Конвертация Update DTO → Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipment", ignore = true)
    @Mapping(target = "performedBy", ignore = true)
    MaintenanceHistory toEntity(UpdateMaintenanceHistoryDTO dto);

    /**
     * Обновление существующего Entity из Update DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipment", ignore = true)
    @Mapping(target = "performedBy", ignore = true)
    void updateEntityFromDTO(UpdateMaintenanceHistoryDTO dto, @MappingTarget MaintenanceHistory entity);
}
