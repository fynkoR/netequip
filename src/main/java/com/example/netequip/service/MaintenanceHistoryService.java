package com.example.netequip.service;

import com.example.netequip.dto.maintenancehistory.CreateMaintenanceHistoryDTO;
import com.example.netequip.dto.maintenancehistory.MaintenanceHistoryResponseDTO;
import com.example.netequip.dto.maintenancehistory.UpdateMaintenanceHistoryDTO;
import com.example.netequip.entity.Employee;
import com.example.netequip.entity.Equipment;
import com.example.netequip.entity.MaintenanceHistory;
import com.example.netequip.exception.employee.EmployeeNotFoundException;
import com.example.netequip.exception.equiptype.EquipmentTypeNotFoundException;
import com.example.netequip.exception.maintenancehistory.MaintenanceHistoryNotFoundException;
import com.example.netequip.mapper.MaintenanceHistoryMapper;
import com.example.netequip.repository.EmployeeRepository;
import com.example.netequip.repository.EquipmentRepository;
import com.example.netequip.repository.MaintenanceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления историей обслуживания оборудования
 * Содержит бизнес-логику для CRUD операций и планирования обслуживания
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaintenanceHistoryService {

    private final MaintenanceHistoryRepository maintenanceHistoryRepository;
    private final EquipmentRepository equipmentRepository;
    private final EmployeeRepository employeeRepository;
    private final MaintenanceHistoryMapper maintenanceHistoryMapper;

    /**
     * Создание новой записи об обслуживании
     *
     * @param dto данные для создания
     * @return созданная запись
     * @throws EquipmentTypeNotFoundException если оборудование не найдено
     * @throws EmployeeNotFoundException если сотрудник не найден
     */
    @Transactional
    public MaintenanceHistoryResponseDTO create(CreateMaintenanceHistoryDTO dto) {
        log.info("Создание новой записи об обслуживании для оборудования ID: {}", dto.getEquipmentId());

        // Поиск оборудования
        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> {
                    log.warn("Оборудование с ID {} не найдено", dto.getEquipmentId());
                    return new EquipmentTypeNotFoundException(dto.getEquipmentId());
                });

        // Конвертация DTO → Entity
        MaintenanceHistory entity = maintenanceHistoryMapper.toEntity(dto);
        entity.setEquipment(equipment);

        // Установка сотрудника (если указан)
        if (dto.getPerformedById() != null) {
            Employee employee = employeeRepository.findById(dto.getPerformedById())
                    .orElseThrow(() -> {
                        log.warn("Сотрудник с ID {} не найден", dto.getPerformedById());
                        return new EmployeeNotFoundException(dto.getPerformedById());
                    });
            entity.setPerformedBy(employee);
        }

        // Установка текущей даты, если не указана
        if (entity.getDate() == null) {
            entity.setDate(LocalDateTime.now());
        }

        // Сохранение
        MaintenanceHistory savedEntity = maintenanceHistoryRepository.save(entity);
        log.info("Запись об обслуживании успешно создана с ID: {}", savedEntity.getId());

        return maintenanceHistoryMapper.toResponseDTO(savedEntity);
    }

    /**
     * Получение записи об обслуживании по ID
     *
     * @param id идентификатор записи
     * @return найденная запись
     * @throws MaintenanceHistoryNotFoundException если запись не найдена
     */
    public MaintenanceHistoryResponseDTO getById(Long id) {
        log.debug("Получение записи об обслуживании по ID: {}", id);

        MaintenanceHistory entity = maintenanceHistoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Запись об обслуживании с ID {} не найдена", id);
                    return new MaintenanceHistoryNotFoundException(id);
                });

        return maintenanceHistoryMapper.toResponseDTO(entity);
    }

    /**
     * Получение всех записей об обслуживании
     *
     * @return список всех записей
     */
    public List<MaintenanceHistoryResponseDTO> getAll() {
        log.debug("Получение всех записей об обслуживании");

        List<MaintenanceHistory> entities = maintenanceHistoryRepository.findAll();
        log.info("Найдено записей об обслуживании: {}", entities.size());

        return entities.stream()
                .map(maintenanceHistoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Обновление существующей записи об обслуживании
     *
     * @param id идентификатор записи
     * @param dto новые данные
     * @return обновленная запись
     * @throws MaintenanceHistoryNotFoundException если запись не найдена
     */
    @Transactional
    public MaintenanceHistoryResponseDTO update(Long id, UpdateMaintenanceHistoryDTO dto) {
        log.info("Обновление записи об обслуживании с ID: {}", id);

        // Поиск существующей записи
        MaintenanceHistory existingEntity = maintenanceHistoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка обновить несуществующую запись об обслуживании с ID: {}", id);
                    return new MaintenanceHistoryNotFoundException(id);
                });

        // Поиск нового оборудования (если изменилось)
        Equipment newEquipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> {
                    log.warn("Оборудование с ID {} не найдено", dto.getEquipmentId());
                    return new EquipmentTypeNotFoundException(dto.getEquipmentId());
                });

        // Обновление базовых полей
        maintenanceHistoryMapper.updateEntityFromDTO(dto, existingEntity);
        existingEntity.setEquipment(newEquipment);

        // Обновление сотрудника
        if (dto.getPerformedById() != null) {
            Employee employee = employeeRepository.findById(dto.getPerformedById())
                    .orElseThrow(() -> {
                        log.warn("Сотрудник с ID {} не найден", dto.getPerformedById());
                        return new EmployeeNotFoundException(dto.getPerformedById());
                    });
            existingEntity.setPerformedBy(employee);
        } else {
            existingEntity.setPerformedBy(null);
        }

        // Сохранение
        MaintenanceHistory updatedEntity = maintenanceHistoryRepository.save(existingEntity);
        log.info("Запись об обслуживании с ID {} успешно обновлена", id);

        return maintenanceHistoryMapper.toResponseDTO(updatedEntity);
    }

    /**
     * Удаление записи об обслуживании
     *
     * @param id идентификатор записи
     * @throws MaintenanceHistoryNotFoundException если запись не найдена
     */
    @Transactional
    public void delete(Long id) {
        log.info("Удаление записи об обслуживании с ID: {}", id);

        // Проверка существования
        if (!maintenanceHistoryRepository.existsById(id)) {
            log.warn("Попытка удалить несуществующую запись об обслуживании с ID: {}", id);
            throw new MaintenanceHistoryNotFoundException(id);
        }

        maintenanceHistoryRepository.deleteById(id);
        log.info("Запись об обслуживании с ID {} успешно удалена", id);
    }

    /**
     * Получение всей истории обслуживания устройства (отсортировано по дате, новые первые)
     *
     * @param equipmentId ID оборудования
     * @return список записей об обслуживании
     */
    public List<MaintenanceHistoryResponseDTO> getByEquipment(Long equipmentId) {
        log.debug("Получение истории обслуживания оборудования ID: {}", equipmentId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        List<MaintenanceHistory> entities = maintenanceHistoryRepository
                .findByEquipmentOrderByDateDesc(equipment);
        log.info("Найдено записей об обслуживании для оборудования ID {}: {}",
                equipmentId, entities.size());

        return entities.stream()
                .map(maintenanceHistoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение истории обслуживания устройства по типу
     *
     * @param equipmentId ID оборудования
     * @param type тип обслуживания
     * @return список записей данного типа
     */
    public List<MaintenanceHistoryResponseDTO> getByEquipmentAndType(Long equipmentId, String type) {
        log.debug("Получение обслуживаний типа '{}' для оборудования ID: {}", type, equipmentId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        List<MaintenanceHistory> entities = maintenanceHistoryRepository
                .findByEquipmentAndType(equipment, type);
        log.info("Найдено обслуживаний типа '{}' для оборудования ID {}: {}",
                type, equipmentId, entities.size());

        return entities.stream()
                .map(maintenanceHistoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение всех обслуживаний, выполненных сотрудником
     *
     * @param employeeId ID сотрудника
     * @return список обслуживаний
     */
    public List<MaintenanceHistoryResponseDTO> getByPerformedBy(Long employeeId) {
        log.debug("Получение обслуживаний, выполненных сотрудником ID: {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        List<MaintenanceHistory> entities = maintenanceHistoryRepository.findByPerformedBy(employee);
        log.info("Найдено обслуживаний, выполненных сотрудником ID {}: {}",
                employeeId, entities.size());

        return entities.stream()
                .map(maintenanceHistoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение обслуживаний за период
     *
     * @param start начало периода
     * @param end конец периода
     * @return список обслуживаний в периоде
     */
    public List<MaintenanceHistoryResponseDTO> getByDateRange(LocalDateTime start, LocalDateTime end) {
        log.debug("Получение обслуживаний за период: {} - {}", start, end);

        List<MaintenanceHistory> entities = maintenanceHistoryRepository.findByDateBetween(start, end);
        log.info("Найдено обслуживаний за период: {}", entities.size());

        return entities.stream()
                .map(maintenanceHistoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение последнего обслуживания устройства
     *
     * @param equipmentId ID оборудования
     * @return последнее обслуживание
     * @throws MaintenanceHistoryNotFoundException если обслуживаний нет
     */
    public MaintenanceHistoryResponseDTO getLatestByEquipment(Long equipmentId) {
        log.debug("Получение последнего обслуживания оборудования ID: {}", equipmentId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        MaintenanceHistory entity = maintenanceHistoryRepository.findLatestByEquipment(equipment);

        if (entity == null) {
            log.warn("У оборудования ID {} нет записей об обслуживании", equipmentId);
            throw new MaintenanceHistoryNotFoundException(
                    "У оборудования нет записей об обслуживании"
            );
        }

        return maintenanceHistoryMapper.toResponseDTO(entity);
    }

    /**
     * Получение устройств с просроченным обслуживанием
     *
     * @return список записей с просроченной датой следующего обслуживания
     */
    public List<MaintenanceHistoryResponseDTO> getOverdueMaintenances() {
        log.debug("Получение просроченных обслуживаний");

        LocalDate today = LocalDate.now();
        List<MaintenanceHistory> entities = maintenanceHistoryRepository
                .findOverdueMaintenances(today);
        log.warn("Найдено просроченных обслуживаний: {}", entities.size());

        return entities.stream()
                .map(maintenanceHistoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение обслуживаний устройства за последние N дней
     *
     * @param equipmentId ID оборудования
     * @param days количество дней
     * @return список недавних обслуживаний
     */
    public List<MaintenanceHistoryResponseDTO> getRecentMaintenances(Long equipmentId, int days) {
        log.debug("Получение обслуживаний оборудования ID {} за последние {} дней",
                equipmentId, days);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<MaintenanceHistory> entities = maintenanceHistoryRepository
                .findRecentMaintenances(equipment, since);
        log.info("Найдено обслуживаний за последние {} дней: {}", days, entities.size());

        return entities.stream()
                .map(maintenanceHistoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение обслуживаний по типу (все устройства)
     *
     * @param type тип обслуживания
     * @return список обслуживаний данного типа
     */
    public List<MaintenanceHistoryResponseDTO> getByType(String type) {
        log.debug("Получение всех обслуживаний типа: {}", type);

        List<MaintenanceHistory> entities = maintenanceHistoryRepository.findAll().stream()
                .filter(m -> type.equals(m.getType()))
                .collect(Collectors.toList());
        log.info("Найдено обслуживаний типа '{}': {}", type, entities.size());

        return entities.stream()
                .map(maintenanceHistoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Подсчет обслуживаний устройства
     *
     * @param equipmentId ID оборудования
     * @return количество обслуживаний
     */
    public long countByEquipment(Long equipmentId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        return maintenanceHistoryRepository.countByEquipment(equipment);
    }

    /**
     * Подсчет обслуживаний по типу (все устройства)
     *
     * @param type тип обслуживания
     * @return количество обслуживаний
     */
    public long countByType(String type) {
        return maintenanceHistoryRepository.countByType(type);
    }

    /**
     * Планирование следующего обслуживания для устройства
     *
     * @param equipmentId ID оборудования
     * @param nextDate дата следующего обслуживания
     * @return обновленная последняя запись об обслуживании
     */
    @Transactional
    public MaintenanceHistoryResponseDTO scheduleNextMaintenance(Long equipmentId, LocalDate nextDate) {
        log.info("Планирование следующего обслуживания для оборудования ID {}: {}",
                equipmentId, nextDate);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        MaintenanceHistory latestMaintenance = maintenanceHistoryRepository
                .findLatestByEquipment(equipment);

        if (latestMaintenance == null) {
            throw new MaintenanceHistoryNotFoundException(
                    "У оборудования нет записей об обслуживании"
            );
        }

        latestMaintenance.setNextMaintenanceDate(nextDate);
        MaintenanceHistory savedEntity = maintenanceHistoryRepository.save(latestMaintenance);
        log.info("Дата следующего обслуживания установлена: {}", nextDate);

        return maintenanceHistoryMapper.toResponseDTO(savedEntity);
    }
}
