package ru.ssau.netequip.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.ssau.netequip.dto.ipAddress.CreateAndUpdateIpAddress;
import ru.ssau.netequip.dto.ipAddress.ResponseIpAddressDto;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.entity.IpAddress;

@Mapper(componentModel = "spring")
public interface IpAddressMapper {
    @Mapping(source = "equipment.id", target = "equipmentId")
    @Mapping(source = "equipment.name", target = "equipmentName")
    ResponseIpAddressDto toResponseDTO(IpAddress entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipment", ignore = true) // Устанавливаем в Service
    IpAddress toEntity(CreateAndUpdateIpAddress dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipment", ignore = true) // Не меняем связь
    void updateEntityFromDTO(CreateAndUpdateIpAddress dto, @MappingTarget IpAddress entity);

//    default Equipment mapEquipmentId(Long equipmentId) {
//        if (equipmentId == null) {
//            return null;
//        }
//        Equipment equipment = new Equipment();
//        equipment.setId(equipmentId);
//        return equipment;
//    }
}
