package ru.ssau.netequip.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.netequip.dto.employee.CreateEmployeeDto;
import ru.ssau.netequip.dto.employee.ResponseEmployeeDto;
import ru.ssau.netequip.dto.employee.UpdateEmployeeDto;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.exception.employee.DuplicateEmployeeEmailException;
import ru.ssau.netequip.exception.employee.NotFoundEmployeeException;
import ru.ssau.netequip.mapper.EmployeeMapper;
import ru.ssau.netequip.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    public EmployeeService(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
    }
    @Transactional
    public ResponseEmployeeDto create(CreateEmployeeDto dto) {
        log.info("Создание сотрудника: {}", dto.getFullName());
        if(dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if(employeeRepository.existsByEmail(dto.getEmail())) {
                log.warn("Попытка создать сотрудника с существующем email: {}", dto.getEmail());
                throw new DuplicateEmployeeEmailException(dto.getEmail());
            }
        }
        Employee employee = employeeMapper.toEntity(dto);

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Сотрудник создан с id: {}", savedEmployee.getId());

        return employeeMapper.toResponseDTO(savedEmployee);
    }
    public ResponseEmployeeDto getById(Long id) {
        log.info("Получение id сотрудника: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Сотрудник не существует с данным id: {}", id);
                    return new NotFoundEmployeeException(id);
                });
        return employeeMapper.toResponseDTO(employee);
    }
    public List<ResponseEmployeeDto> getAll() {
        log.info("Получение списка всех сотрудников");
        List<Employee> employees = employeeRepository.findAll();
        log.info("Найдено сотрудников: {}", employees.size());
        return employees.stream()
                .map(employeeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Page<ResponseEmployeeDto> getPage(String search, Pageable pageable) {
        log.info("Получение страницы сотрудников: page={}, size={}, search='{}'",
                pageable.getPageNumber(), pageable.getPageSize(), search);

        Page<Employee> page;
        if (search != null && !search.isBlank()) {
            page = employeeRepository.search(search.trim(), pageable);
        } else {
            page = employeeRepository.findAll(pageable);
        }

        log.info("Найдено сотрудников на странице: {} из {}",
                page.getNumberOfElements(), page.getTotalElements());
        return page.map(employeeMapper::toResponseDTO);
    }

    @Transactional
    public ResponseEmployeeDto update(Long id, UpdateEmployeeDto dto) {
        log.info("Обновление сотрудника с id: {}", id);
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Сотрудник не найден с данным id: {}",id);
                    return new NotFoundEmployeeException(id);
                });
        if(dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if(!dto.getEmail().equals(existingEmployee.getEmail())) {
                log.warn("Попытка изменить email на уже существующий email: {}", dto.getEmail());
                throw new DuplicateEmployeeEmailException(dto.getEmail());
            }
        }
        employeeMapper.updateEmployeeDto(dto, existingEmployee);
        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        log.info("Сотрудник с id: {} обновлен", updatedEmployee.getId());
        return employeeMapper.toResponseDTO(updatedEmployee);
    }
    @Transactional
    public void delete(Long id) {
        log.info("Удаление сотрудника с id: {}", id);
        if(!employeeRepository.existsById(id)) {
            log.warn("Сотрудник не существует с данным id: {}", id);
            throw new NotFoundEmployeeException(id);
        }

        // еще нужно добавить проверка на оборудование , которое закреплено у данного сотрудника

        employeeRepository.deleteById(id);
        log.info("Сотрудник удален с id: {}", id);
    }
}
