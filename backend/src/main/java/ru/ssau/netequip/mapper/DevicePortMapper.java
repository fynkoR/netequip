package ru.ssau.netequip.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.ssau.netequip.dto.devicePort.CreateAndUpdateDevicePortDto;
import ru.ssau.netequip.dto.devicePort.ResponseDevicePortDto;
import ru.ssau.netequip.entity.DevicePort;

@Mapper(componentModel = "spring")
public interface DevicePortMapper {
    @Mapping(source = "equipment.id", target = "equipmentId")
    @Mapping(source = "equipment.name", target = "equipmentName")
    @Mapping(source = "connectedToEquipment.id", target = "connectedToEquipmentId")
    @Mapping(source = "connectedToEquipment.name", target = "connectedToEquipmentName")
    @Mapping(source = "connectedToPort.id", target = "connectedToPortId")
    @Mapping(source = "connectedToPort.portNumber", target = "connectedToPortNumber")
    ResponseDevicePortDto toResponseDTO(DevicePort entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipment", ignore = true)
    @Mapping(target = "connectedToEquipment", ignore = true)
    @Mapping(target = "connectedToPort", ignore = true)
    DevicePort toEntity(CreateAndUpdateDevicePortDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipment", ignore = true)
    @Mapping(target = "connectedToEquipment", ignore = true)
    @Mapping(target = "connectedToPort", ignore = true)
    void updateEntityFromDTO(CreateAndUpdateDevicePortDto dto, @MappingTarget DevicePort entity);
}
