package com.example.netequip.mapper;

import com.example.netequip.dto.employee.CreateEmployeeDTO;
import com.example.netequip.dto.employee.EmployeeResponseDTO;
import com.example.netequip.dto.employee.UpdateEmployeeDTO;
import com.example.netequip.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper для конвертации между Employee Entity и DTO
 */
@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    /**
     * Конвертация Entity → Response DTO
     */
    EmployeeResponseDTO toResponseDTO(Employee entity);

    /**
     * Конвертация Create DTO → Entity
     */
    @Mapping(target = "id", ignore = true)
    Employee toEntity(CreateEmployeeDTO dto);

    /**
     * Конвертация Update DTO → Entity
     */
    @Mapping(target = "id", ignore = true)
    Employee toEntity(UpdateEmployeeDTO dto);

    /**
     * Обновление существующего Entity из Update DTO
     */
    @Mapping(target = "id", ignore = true)
    void updateEntityFromDTO(UpdateEmployeeDTO dto, @MappingTarget Employee entity);
}
