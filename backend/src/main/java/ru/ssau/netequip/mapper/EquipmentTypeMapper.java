package ru.ssau.netequip.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.ssau.netequip.dto.equipmentType.CreateAndUpdateEquipmentTypeDto;
import ru.ssau.netequip.dto.equipmentType.ResponseEquipmentTypeDto;
import ru.ssau.netequip.entity.EquipmentType;

@Mapper(componentModel = "spring")
public interface EquipmentTypeMapper {
    ResponseEquipmentTypeDto toResponseDTO(EquipmentType entity);

    @Mapping(target = "id", ignore = true) // ID генерируется БД
    EquipmentType toEntity(CreateAndUpdateEquipmentTypeDto dto);

    @Mapping(target = "id", ignore = true) // ID не меняется
    void updateEntityFromDTO(CreateAndUpdateEquipmentTypeDto dto, @MappingTarget EquipmentType entity);
}
