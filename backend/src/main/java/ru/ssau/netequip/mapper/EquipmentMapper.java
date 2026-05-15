package ru.ssau.netequip.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.ssau.netequip.dto.equipment.CreateEquipmentDto;
import ru.ssau.netequip.dto.equipment.ListEquipmentDTO;
import ru.ssau.netequip.dto.equipment.ResponseEquipmentDto;
import ru.ssau.netequip.dto.equipment.UpdateEquipmentDto;
import ru.ssau.netequip.entity.Equipment;

@Mapper(componentModel = "spring")
public interface EquipmentMapper {
    @Mapping(source = "type.id", target = "typeId")
    @Mapping(source = "type.typeName", target = "typeName")
    @Mapping(source = "type.manufacturer", target = "manufacturer")
    @Mapping(source = "type.model", target = "model")
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "employee.fullName", target = "employeeFullName")
    @Mapping(target = "portsCount", ignore = true)
    @Mapping(target = "ipAddressesCount", ignore = true)
    @Mapping(target = "maintenanceCount", ignore = true)
    ResponseEquipmentDto toResponseDTO(Equipment entity);

    @Mapping(source = "type.id", target = "typeId")
    @Mapping(source = "type.typeName", target = "typeName")
    @Mapping(source = "type.manufacturer", target = "manufacturer")
    @Mapping(source = "type.model", target = "model")
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(source = "employee.fullName", target = "employeeFullName")
    @Mapping(target = "portsCount", ignore = true)
    ListEquipmentDTO toListDTO(Equipment entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "employee", ignore = true)
    Equipment toEntity(CreateEquipmentDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "dateAdded", ignore = true)
    void updateEntityFromDTO(UpdateEquipmentDto dto, @MappingTarget Equipment entity);
}
