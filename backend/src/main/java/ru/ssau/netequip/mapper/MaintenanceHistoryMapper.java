package ru.ssau.netequip.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.ssau.netequip.dto.maintenanceHistory.CreateAndUpdateMaintenanceHistoryDTO;
import ru.ssau.netequip.dto.maintenanceHistory.ResponseMaintenanceHistoryDto;
import ru.ssau.netequip.entity.MaintenanceHistory;

@Mapper(componentModel = "spring")
public interface MaintenanceHistoryMapper {
    @Mapping(source = "equipment.id", target = "equipmentId")
    @Mapping(source = "equipment.name", target = "equipmentName")
    @Mapping(source = "performedBy.id", target = "performedById")
    @Mapping(source = "performedBy.fullName", target = "performedByName")
    ResponseMaintenanceHistoryDto toResponseDTO(MaintenanceHistory entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipment", ignore = true)
    @Mapping(target = "performedBy", ignore = true)
    MaintenanceHistory toEntity(CreateAndUpdateMaintenanceHistoryDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipment", ignore = true)
    @Mapping(target = "performedBy", ignore = true)
    void updateEntityFromDTO(CreateAndUpdateMaintenanceHistoryDTO dto, @MappingTarget MaintenanceHistory entity);
}
