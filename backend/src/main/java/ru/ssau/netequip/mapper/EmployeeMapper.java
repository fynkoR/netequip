package ru.ssau.netequip.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.ssau.netequip.dto.employee.CreateEmployeeDto;
import ru.ssau.netequip.dto.employee.ResponseEmployeeDto;
import ru.ssau.netequip.dto.employee.UpdateEmployeeDto;
import ru.ssau.netequip.entity.Employee;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    ResponseEmployeeDto toResponseDTO(Employee entity);

    @Mapping(target = "id", ignore = true)
    Employee toEntity(CreateEmployeeDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    Employee toEntityFromUpdate(UpdateEmployeeDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    void updateEmployeeDto(UpdateEmployeeDto dto, @MappingTarget Employee entity);
}
