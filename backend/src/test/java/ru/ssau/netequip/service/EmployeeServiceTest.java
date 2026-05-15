package ru.ssau.netequip.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.ssau.netequip.dto.employee.CreateEmployeeDto;
import ru.ssau.netequip.dto.employee.ResponseEmployeeDto;
import ru.ssau.netequip.dto.employee.UpdateEmployeeDto;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.enums.Position;
import ru.ssau.netequip.mapper.EmployeeMapper;
import ru.ssau.netequip.repository.EmployeeRepository;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void testGetAll() {
        Employee emp1 = new Employee();
        emp1.setId(1L);
        emp1.setFullName("Иванов Сергей");
        emp1.setPosition(Position.ENGINEER);
        emp1.setEmail("ivanov@ssau.ru");

        Employee emp2 = new Employee();
        emp2.setId(2L);
        emp2.setFullName("Петрова Мария");
        emp2.setPosition(Position.ADMIN);
        emp2.setEmail("petrova@ssau.ru");

        ResponseEmployeeDto dto1 = new ResponseEmployeeDto();
        dto1.setId(1L);
        dto1.setFullName("Иванов Сергей");

        ResponseEmployeeDto dto2 = new ResponseEmployeeDto();
        dto2.setId(2L);
        dto2.setFullName("Петрова Мария");

        when(employeeRepository.findAll()).thenReturn(List.of(emp1, emp2));
        when(employeeMapper.toResponseDTO(emp1)).thenReturn(dto1);
        when(employeeMapper.toResponseDTO(emp2)).thenReturn(dto2);

        List<ResponseEmployeeDto> result = employeeService.getAll();

        assertEquals(2, result.size());
        assertEquals("Иванов Сергей", result.get(0).getFullName());
        assertEquals("Петрова Мария", result.get(1).getFullName());
        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void testGetById() {
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setFullName("Иванов Сергей");
        emp.setPosition(Position.ENGINEER);

        ResponseEmployeeDto dto = new ResponseEmployeeDto();
        dto.setId(1L);
        dto.setFullName("Иванов Сергей");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeMapper.toResponseDTO(emp)).thenReturn(dto);

        ResponseEmployeeDto result = employeeService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Иванов Сергей", result.getFullName());
    }

    @Test
    void testGetByIdNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> employeeService.getById(99L));
    }

    @Test
    void testCreate() {
        CreateEmployeeDto createDto = new CreateEmployeeDto();
        createDto.setFullName("Новиков Алексей");
        createDto.setEmail("novikov@ssau.ru");
        createDto.setPosition(Position.TECHNIC);

        Employee entity = new Employee();
        entity.setFullName("Новиков Алексей");
        entity.setEmail("novikov@ssau.ru");
        entity.setPosition(Position.TECHNIC);

        Employee saved = new Employee();
        saved.setId(10L);
        saved.setFullName("Новиков Алексей");
        saved.setEmail("novikov@ssau.ru");
        saved.setPosition(Position.TECHNIC);

        ResponseEmployeeDto responseDto = new ResponseEmployeeDto();
        responseDto.setId(10L);
        responseDto.setFullName("Новиков Алексей");

        when(employeeRepository.existsByEmail("novikov@ssau.ru")).thenReturn(false);
        when(employeeMapper.toEntity(createDto)).thenReturn(entity);
        when(employeeRepository.save(entity)).thenReturn(saved);
        when(employeeMapper.toResponseDTO(saved)).thenReturn(responseDto);

        ResponseEmployeeDto result = employeeService.create(createDto);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("Новиков Алексей", result.getFullName());
        verify(employeeRepository, times(1)).save(entity);
    }

    @Test
    void testCreateDuplicateEmail() {
        CreateEmployeeDto createDto = new CreateEmployeeDto();
        createDto.setFullName("Дубликат");
        createDto.setEmail("existing@ssau.ru");

        when(employeeRepository.existsByEmail("existing@ssau.ru")).thenReturn(true);

        assertThrows(Exception.class, () -> employeeService.create(createDto));
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void testUpdate() {
        Employee existing = new Employee();
        existing.setId(1L);
        existing.setFullName("Иванов Сергей");
        existing.setEmail("ivanov@ssau.ru");
        existing.setPosition(Position.ENGINEER);

        UpdateEmployeeDto updateDto = new UpdateEmployeeDto();
        updateDto.setFullName("Иванов Сергей Иванович");
        updateDto.setEmail("ivanov@ssau.ru");
        updateDto.setPosition(Position.ADMIN);

        Employee updated = new Employee();
        updated.setId(1L);
        updated.setFullName("Иванов Сергей Иванович");
        updated.setEmail("ivanov@ssau.ru");
        updated.setPosition(Position.ADMIN);

        ResponseEmployeeDto responseDto = new ResponseEmployeeDto();
        responseDto.setId(1L);
        responseDto.setFullName("Иванов Сергей Иванович");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        doNothing().when(employeeMapper).updateEmployeeDto(updateDto, existing);
        when(employeeRepository.save(existing)).thenReturn(updated);
        when(employeeMapper.toResponseDTO(updated)).thenReturn(responseDto);

        ResponseEmployeeDto result = employeeService.update(1L, updateDto);

        assertNotNull(result);
        assertEquals("Иванов Сергей Иванович", result.getFullName());
        verify(employeeRepository, times(1)).save(existing);
    }

    @Test
    void testUpdateNotFound() {
        UpdateEmployeeDto updateDto = new UpdateEmployeeDto();
        updateDto.setFullName("Несуществующий");

        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> employeeService.update(99L, updateDto));
    }

    @Test
    void testDelete() {
        when(employeeRepository.existsById(1L)).thenReturn(true);
        doNothing().when(employeeRepository).deleteById(1L);

        assertDoesNotThrow(() -> employeeService.delete(1L));
        verify(employeeRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteNotFound() {
        when(employeeRepository.existsById(99L)).thenReturn(false);

        assertThrows(Exception.class, () -> employeeService.delete(99L));
        verify(employeeRepository, never()).deleteById(99L);
    }
}