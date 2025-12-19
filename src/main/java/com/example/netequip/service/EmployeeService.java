package com.example.netequip.service;

import com.example.netequip.dto.employee.CreateEmployeeDTO;
import com.example.netequip.dto.employee.EmployeeResponseDTO;
import com.example.netequip.dto.employee.UpdateEmployeeDTO;
import com.example.netequip.entity.Employee;
import com.example.netequip.exception.employee.DuplicateEmployeeEmailException;
import com.example.netequip.exception.employee.EmployeeNotFoundException;
import com.example.netequip.mapper.EmployeeMapper;
import com.example.netequip.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления сотрудниками
 * Содержит бизнес-логику для CRUD операций и поиска
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    /**
     * Создание нового сотрудника
     *
     * @param dto данные для создания
     * @return созданный сотрудник
     * @throws DuplicateEmployeeEmailException если email уже используется
     */
    @Transactional
    public EmployeeResponseDTO create(CreateEmployeeDTO dto) {
        log.info("Создание нового сотрудника: {}", dto.getFullName());

        // Проверка уникальности email (если указан)
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (employeeRepository.existsByEmail(dto.getEmail())) {
                log.warn("Попытка создать сотрудника с существующим email: {}", dto.getEmail());
                throw new DuplicateEmployeeEmailException(dto.getEmail());
            }
        }

        // Конвертация DTO → Entity
        Employee entity = employeeMapper.toEntity(dto);

        // Сохранение в БД
        Employee savedEntity = employeeRepository.save(entity);
        log.info("Сотрудник успешно создан с ID: {}", savedEntity.getId());

        // Возврат Response DTO
        return employeeMapper.toResponseDTO(savedEntity);
    }

    /**
     * Получение сотрудника по ID
     *
     * @param id идентификатор сотрудника
     * @return найденный сотрудник
     * @throws EmployeeNotFoundException если сотрудник не найден
     */
    public EmployeeResponseDTO getById(Long id) {
        log.debug("Получение сотрудника по ID: {}", id);

        Employee entity = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Сотрудник с ID {} не найден", id);
                    return new EmployeeNotFoundException(id);
                });

        return employeeMapper.toResponseDTO(entity);
    }

    /**
     * Получение всех сотрудников
     *
     * @return список всех сотрудников
     */
    public List<EmployeeResponseDTO> getAll() {
        log.debug("Получение всех сотрудников");

        List<Employee> entities = employeeRepository.findAll();
        log.info("Найдено сотрудников: {}", entities.size());

        return entities.stream()
                .map(employeeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Обновление существующего сотрудника
     *
     * @param id идентификатор сотрудника для обновления
     * @param dto новые данные
     * @return обновленный сотрудник
     * @throws EmployeeNotFoundException если сотрудник не найден
     * @throws DuplicateEmployeeEmailException если новый email уже используется
     */
    @Transactional
    public EmployeeResponseDTO update(Long id, UpdateEmployeeDTO dto) {
        log.info("Обновление сотрудника с ID: {}", id);

        // Поиск существующего сотрудника
        Employee existingEntity = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка обновить несуществующего сотрудника с ID: {}", id);
                    return new EmployeeNotFoundException(id);
                });

        // Проверка уникальности email (если email изменился и не пустой)
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            if (!dto.getEmail().equals(existingEntity.getEmail()) &&
                    employeeRepository.existsByEmail(dto.getEmail())) {
                log.warn("Попытка изменить email на уже существующий: {}", dto.getEmail());
                throw new DuplicateEmployeeEmailException(dto.getEmail());
            }
        }

        // Обновление полей через Mapper
        employeeMapper.updateEntityFromDTO(dto, existingEntity);

        // Сохранение изменений
        Employee updatedEntity = employeeRepository.save(existingEntity);
        log.info("Сотрудник с ID {} успешно обновлен", id);

        return employeeMapper.toResponseDTO(updatedEntity);
    }

    /**
     * Удаление сотрудника по ID
     *
     * @param id идентификатор сотрудника для удаления
     * @throws EmployeeNotFoundException если сотрудник не найден
     */
    @Transactional
    public void delete(Long id) {
        log.info("Удаление сотрудника с ID: {}", id);

        // Проверка существования
        if (!employeeRepository.existsById(id)) {
            log.warn("Попытка удалить несуществующего сотрудника с ID: {}", id);
            throw new EmployeeNotFoundException(id);
        }

        // TODO: Добавить проверку - нет ли оборудования, привязанного к сотруднику
        // if (equipmentRepository.existsByEmployeeId(id)) {
        //     throw new EmployeeInUseException(id);
        // }

        employeeRepository.deleteById(id);
        log.info("Сотрудник с ID {} успешно удален", id);
    }

    /**
     * Поиск сотрудников по части имени (без учёта регистра)
     *
     * @param name часть имени для поиска
     * @return список найденных сотрудников
     */
    public List<EmployeeResponseDTO> searchByName(String name) {
        log.debug("Поиск сотрудников по имени: {}", name);

        List<Employee> entities = employeeRepository.findByFullNameContainingIgnoreCase(name);
        log.info("Найдено сотрудников по запросу '{}': {}", name, entities.size());

        return entities.stream()
                .map(employeeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Поиск сотрудника по email
     *
     * @param email email сотрудника
     * @return найденный сотрудник
     * @throws EmployeeNotFoundException если сотрудник не найден
     */
    public EmployeeResponseDTO getByEmail(String email) {
        log.debug("Поиск сотрудника по email: {}", email);

        Employee entity = employeeRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Сотрудник с email {} не найден", email);
                    return new EmployeeNotFoundException(email);
                });

        return employeeMapper.toResponseDTO(entity);
    }

    /**
     * Получение всех сотрудников определённой должности
     *
     * @param position название должности
     * @return список сотрудников данной должности
     */
    public List<EmployeeResponseDTO> getByPosition(String position) {
        log.debug("Поиск сотрудников по должности: {}", position);

        List<Employee> entities = employeeRepository.findByPosition(position);
        log.info("Найдено сотрудников на должности '{}': {}", position, entities.size());

        return entities.stream()
                .map(employeeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение сотрудников определённой должности с сортировкой по имени
     *
     * @param position название должности
     * @return отсортированный список сотрудников
     */
    public List<EmployeeResponseDTO> getByPositionSorted(String position) {
        log.debug("Получение отсортированных сотрудников по должности: {}", position);

        List<Employee> entities = employeeRepository.findByPositionOrderByFullNameAsc(position);
        log.info("Найдено и отсортировано сотрудников на должности '{}': {}", position, entities.size());

        return entities.stream()
                .map(employeeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Проверка существования сотрудника по ID
     *
     * @param id идентификатор сотрудника
     * @return true если существует
     */
    public boolean existsById(Long id) {
        return employeeRepository.existsById(id);
    }

    /**
     * Проверка существования сотрудника по email
     *
     * @param email email для проверки
     * @return true если существует
     */
    public boolean existsByEmail(String email) {
        return employeeRepository.existsByEmail(email);
    }

    /**
     * Получить количество всех сотрудников
     *
     * @return количество сотрудников
     */
    public long count() {
        return employeeRepository.count();
    }
}
