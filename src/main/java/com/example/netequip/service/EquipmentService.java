package com.example.netequip.service;

import com.example.netequip.dto.equipment.CreateEquipmentDTO;
import com.example.netequip.dto.equipment.EquipmentListDTO;
import com.example.netequip.dto.equipment.EquipmentResponseDTO;
import com.example.netequip.dto.equipment.UpdateEquipmentDTO;
import com.example.netequip.entity.Employee;
import com.example.netequip.entity.Equipment;
import com.example.netequip.entity.EquipmentType;
import com.example.netequip.exception.*;
import com.example.netequip.exception.employee.EmployeeNotFoundException;
import com.example.netequip.exception.equipment.DuplicateEquipmentException;
import com.example.netequip.exception.equipment.EquipmentNotFoundException;
import com.example.netequip.exception.equiptype.EquipmentTypeNotFoundException;
import com.example.netequip.mapper.EquipmentMapper;
import com.example.netequip.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления сетевым оборудованием
 * Центральный сервис системы с богатой бизнес-логикой
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final EmployeeRepository employeeRepository;
    private final DevicePortRepository devicePortRepository;
    private final IpAddressRepository ipAddressRepository;
    private final MaintenanceHistoryRepository maintenanceHistoryRepository;
    private final EquipmentMapper equipmentMapper;

    /**
     * Создание нового оборудования
     *
     * @param dto данные для создания
     * @return созданное оборудование
     * @throws EquipmentTypeNotFoundException если тип не найден
     * @throws EmployeeNotFoundException если сотрудник не найден
     * @throws DuplicateEquipmentException если уникальные поля заняты
     */
    @Transactional
    public EquipmentResponseDTO create(CreateEquipmentDTO dto) {
        log.info("Создание нового оборудования: {}", dto.getName());

        // Поиск типа оборудования
        EquipmentType type = equipmentTypeRepository.findById(dto.getTypeId())
                .orElseThrow(() -> {
                    log.warn("Тип оборудования с ID {} не найден", dto.getTypeId());
                    return new EquipmentTypeNotFoundException(dto.getTypeId());
                });

        // Проверка уникальности полей
        validateUniqueFields(null, dto.getSerialNumber(), dto.getMacAddress());

        // Конвертация DTO → Entity
        Equipment entity = equipmentMapper.toEntity(dto);
        entity.setType(type);

        // Установка сотрудника (если указан)
        if (dto.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> {
                        log.warn("Сотрудник с ID {} не найден", dto.getEmployeeId());
                        return new EmployeeNotFoundException(dto.getEmployeeId());
                    });
            entity.setEmployee(employee);
        }

        // Установка даты добавления (если не указана)
        if (entity.getDateAdded() == null) {
            entity.setDateAdded(LocalDate.now());
        }

        // Установка статуса по умолчанию
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("Active");
        }

        // Сохранение
        Equipment savedEntity = equipmentRepository.save(entity);
        log.info("Оборудование успешно создано с ID: {}", savedEntity.getId());

        return toResponseDTOWithStats(savedEntity);
    }

    /**
     * Получение оборудования по ID
     *
     * @param id идентификатор оборудования
     * @return найденное оборудование с статистикой
     * @throws EquipmentNotFoundException если оборудование не найдено
     */
    public EquipmentResponseDTO getById(Long id) {
        log.debug("Получение оборудования по ID: {}", id);

        Equipment entity = equipmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Оборудование с ID {} не найдено", id);
                    return new EquipmentNotFoundException(id);
                });

        return toResponseDTOWithStats(entity);
    }

    /**
     * Получение всего оборудования (краткий список)
     *
     * @return список оборудования
     */
    public List<EquipmentListDTO> getAll() {
        log.debug("Получение всего оборудования");

        List<Equipment> entities = equipmentRepository.findAll();
        log.info("Найдено оборудования: {}", entities.size());

        return entities.stream()
                .map(this::toListDTOWithStats)
                .collect(Collectors.toList());
    }

    /**
     * Обновление существующего оборудования
     *
     * @param id идентификатор оборудования
     * @param dto новые данные
     * @return обновленное оборудование
     * @throws EquipmentNotFoundException если оборудование не найдено
     * @throws DuplicateEquipmentException если уникальные поля заняты
     */
    @Transactional
    public EquipmentResponseDTO update(Long id, UpdateEquipmentDTO dto) {
        log.info("Обновление оборудования с ID: {}", id);

        // Поиск существующего оборудования
        Equipment existingEntity = equipmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка обновить несуществующее оборудование с ID: {}", id);
                    return new EquipmentNotFoundException(id);
                });

        // Поиск нового типа
        EquipmentType newType = equipmentTypeRepository.findById(dto.getTypeId())
                .orElseThrow(() -> {
                    log.warn("Тип оборудования с ID {} не найден", dto.getTypeId());
                    return new EquipmentTypeNotFoundException(dto.getTypeId());
                });

        // Проверка уникальности полей (исключая текущее оборудование)
        validateUniqueFields(id, dto.getSerialNumber(), dto.getMacAddress());

        // Обновление полей
        equipmentMapper.updateEntityFromDTO(dto, existingEntity);
        existingEntity.setType(newType);

        // Обновление сотрудника
        if (dto.getEmployeeId() != null) {
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> {
                        log.warn("Сотрудник с ID {} не найден", dto.getEmployeeId());
                        return new EmployeeNotFoundException(dto.getEmployeeId());
                    });
            existingEntity.setEmployee(employee);
        } else {
            existingEntity.setEmployee(null);
        }

        // Установка даты обновления (если не указана)
        if (dto.getDateUpdated() == null) {
            existingEntity.setDateUpdated(LocalDate.now());
        }

        // Сохранение
        Equipment updatedEntity = equipmentRepository.save(existingEntity);
        log.info("Оборудование с ID {} успешно обновлено", id);

        return toResponseDTOWithStats(updatedEntity);
    }

    /**
     * Удаление оборудования
     *
     * @param id идентификатор оборудования
     * @throws EquipmentNotFoundException если оборудование не найдено
     */
    @Transactional
    public void delete(Long id) {
        log.info("Удаление оборудования с ID: {}", id);

        // Проверка существования
        if (!equipmentRepository.existsById(id)) {
            log.warn("Попытка удалить несуществующее оборудование с ID: {}", id);
            throw new EquipmentNotFoundException(id);
        }

        // TODO: Добавить каскадное удаление или проверку связанных данных
        // - DevicePort
        // - IpAddress
        // - MaintenanceHistory

        equipmentRepository.deleteById(id);
        log.info("Оборудование с ID {} успешно удалено", id);
    }

    /**
     * Поиск оборудования по серийному номеру
     *
     * @param serialNumber серийный номер
     * @return найденное оборудование
     * @throws EquipmentNotFoundException если не найдено
     */
    public EquipmentResponseDTO getBySerialNumber(String serialNumber) {
        log.debug("Поиск оборудования по серийному номеру: {}", serialNumber);

        Equipment entity = equipmentRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> {
                    log.warn("Оборудование с серийным номером {} не найдено", serialNumber);
                    return new EquipmentNotFoundException(
                            "Оборудование с серийным номером '" + serialNumber + "' не найдено"
                    );
                });

        return toResponseDTOWithStats(entity);
    }

    /**
     * Поиск оборудования по MAC-адресу
     *
     * @param macAddress MAC-адрес
     * @return найденное оборудование
     */
    public EquipmentResponseDTO getByMacAddress(String macAddress) {
        log.debug("Поиск оборудования по MAC-адресу: {}", macAddress);

        Equipment entity = equipmentRepository.findByMacAddress(macAddress)
                .orElseThrow(() -> {
                    log.warn("Оборудование с MAC-адресом {} не найдено", macAddress);
                    return new EquipmentNotFoundException(
                            "Оборудование с MAC-адресом '" + macAddress + "' не найдено"
                    );
                });

        return toResponseDTOWithStats(entity);
    }

    /**
     * Поиск оборудования по IP-адресу
     *
     * @param ipAddress IP-адрес
     * @return найденное оборудование
     */
    public EquipmentResponseDTO getByIpAddress(String ipAddress) {
        log.debug("Поиск оборудования по IP-адресу: {}", ipAddress);

        Equipment entity = equipmentRepository.findByIpAddress(ipAddress)
                .orElseThrow(() -> {
                    log.warn("Оборудование с IP-адресом {} не найдено", ipAddress);
                    return new EquipmentNotFoundException(
                            "Оборудование с IP-адресом '" + ipAddress + "' не найдено"
                    );
                });

        return toResponseDTOWithStats(entity);
    }

    /**
     * Получение оборудования по типу
     *
     * @param typeId ID типа оборудования
     * @return список оборудования данного типа
     */
    public List<EquipmentListDTO> getByType(Long typeId) {
        log.debug("Получение оборудования типа ID: {}", typeId);

        EquipmentType type = equipmentTypeRepository.findById(typeId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(typeId));

        List<Equipment> entities = equipmentRepository.findByType(type);
        log.info("Найдено оборудования типа '{}': {}", type.getTypeName(), entities.size());

        return entities.stream()
                .map(this::toListDTOWithStats)
                .collect(Collectors.toList());
    }

    /**
     * Получение оборудования сотрудника
     *
     * @param employeeId ID сотрудника
     * @return список оборудования
     */
    public List<EquipmentListDTO> getByEmployee(Long employeeId) {
        log.debug("Получение оборудования сотрудника ID: {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        List<Equipment> entities = equipmentRepository.findByEmployee(employee);
        log.info("Найдено оборудования сотрудника '{}': {}",
                employee.getFullName(), entities.size());

        return entities.stream()
                .map(this::toListDTOWithStats)
                .collect(Collectors.toList());
    }

    /**
     * Получение оборудования по статусу
     *
     * @param status статус
     * @return список оборудования с данным статусом
     */
    public List<EquipmentListDTO> getByStatus(String status) {
        log.debug("Получение оборудования со статусом: {}", status);

        List<Equipment> entities = equipmentRepository.findByStatusOrderByNameAsc(status);
        log.info("Найдено оборудования со статусом '{}': {}", status, entities.size());

        return entities.stream()
                .map(this::toListDTOWithStats)
                .collect(Collectors.toList());
    }

    /**
     * Поиск оборудования по названию (частичное совпадение)
     *
     * @param name часть названия
     * @return список найденного оборудования
     */
    public List<EquipmentListDTO> searchByName(String name) {
        log.debug("Поиск оборудования по названию: {}", name);

        List<Equipment> entities = equipmentRepository.findByNameContainingIgnoreCase(name);
        log.info("Найдено оборудования по запросу '{}': {}", name, entities.size());

        return entities.stream()
                .map(this::toListDTOWithStats)
                .collect(Collectors.toList());
    }

    /**
     * Поиск оборудования по адресу (частичное совпадение)
     *
     * @param address часть адреса
     * @return список найденного оборудования
     */
    public List<EquipmentListDTO> searchByAddress(String address) {
        log.debug("Поиск оборудования по адресу: {}", address);

        List<Equipment> entities = equipmentRepository.findByAddressContainingIgnoreCase(address);
        log.info("Найдено оборудования по адресу '{}': {}", address, entities.size());

        return entities.stream()
                .map(this::toListDTOWithStats)
                .collect(Collectors.toList());
    }

    /**
     * Получение оборудования по типу и статусу
     *
     * @param typeId ID типа
     * @param status статус
     * @return список оборудования
     */
    public List<EquipmentListDTO> getByTypeAndStatus(Long typeId, String status) {
        log.debug("Получение оборудования типа ID {} со статусом: {}", typeId, status);

        EquipmentType type = equipmentTypeRepository.findById(typeId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(typeId));

        List<Equipment> entities = equipmentRepository.findByTypeAndStatus(type, status);
        log.info("Найдено оборудования типа '{}' со статусом '{}': {}",
                type.getTypeName(), status, entities.size());

        return entities.stream()
                .map(this::toListDTOWithStats)
                .collect(Collectors.toList());
    }

    /**
     * Получение оборудования добавленного после определенной даты
     *
     * @param date дата
     * @return список оборудования
     */
    public List<EquipmentListDTO> getAddedAfter(LocalDate date) {
        log.debug("Получение оборудования добавленного после: {}", date);

        List<Equipment> entities = equipmentRepository.findByDateAddedAfter(date);
        log.info("Найдено оборудования добавленного после {}: {}", date, entities.size());

        return entities.stream()
                .map(this::toListDTOWithStats)
                .collect(Collectors.toList());
    }

    /**
     * Получение оборудования требующего обслуживания
     *
     * @param monthsWithoutUpdate количество месяцев без обновления
     * @return список оборудования
     */
    public List<EquipmentListDTO> getEquipmentNeedingMaintenance(int monthsWithoutUpdate) {
        log.debug("Получение оборудования без обновлений более {} месяцев", monthsWithoutUpdate);

        LocalDate thresholdDate = LocalDate.now().minusMonths(monthsWithoutUpdate);
        List<Equipment> entities = equipmentRepository.findEquipmentNeedingMaintenance(thresholdDate);
        log.warn("Найдено оборудования требующего обслуживания: {}", entities.size());

        return entities.stream()
                .map(this::toListDTOWithStats)
                .collect(Collectors.toList());
    }

    /**
     * Подсчет оборудования по типу
     *
     * @param typeId ID типа
     * @return количество оборудования
     */
    public long countByType(Long typeId) {
        EquipmentType type = equipmentTypeRepository.findById(typeId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(typeId));

        return equipmentRepository.countByType(type);
    }

    /**
     * Подсчет оборудования по статусу
     *
     * @param status статус
     * @return количество оборудования
     */
    public long countByStatus(String status) {
        return equipmentRepository.countByStatus(status);
    }

    /**
     * Изменение статуса оборудования
     *
     * @param id ID оборудования
     * @param newStatus новый статус
     * @return обновленное оборудование
     */
    @Transactional
    public EquipmentResponseDTO changeStatus(Long id, String newStatus) {
        log.info("Изменение статуса оборудования ID {} на: {}", id, newStatus);

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new EquipmentNotFoundException(id));

        equipment.setStatus(newStatus);
        equipment.setDateUpdated(LocalDate.now());

        Equipment savedEntity = equipmentRepository.save(equipment);
        log.info("Статус оборудования ID {} изменен на '{}'", id, newStatus);

        return toResponseDTOWithStats(savedEntity);
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Валидация уникальности полей
     */
    private void validateUniqueFields(Long excludeId, String serialNumber, String macAddress) {
        // Проверка серийного номера
        if (serialNumber != null && !serialNumber.isBlank()) {
            equipmentRepository.findBySerialNumber(serialNumber).ifPresent(existing -> {
                if (excludeId == null || !existing.getId().equals(excludeId)) {
                    log.warn("Попытка использовать занятый серийный номер: {}", serialNumber);
                    throw new DuplicateEquipmentException("серийным номером", serialNumber);
                }
            });
        }

        // Проверка MAC-адреса
        if (macAddress != null && !macAddress.isBlank()) {
            equipmentRepository.findByMacAddress(macAddress).ifPresent(existing -> {
                if (excludeId == null || !existing.getId().equals(excludeId)) {
                    log.warn("Попытка использовать занятый MAC-адрес: {}", macAddress);
                    throw new DuplicateEquipmentException("MAC-адресом", macAddress);
                }
            });
        }
    }

    /**
     * Конвертация Entity → Response DTO со статистикой
     */
    private EquipmentResponseDTO toResponseDTOWithStats(Equipment entity) {
        EquipmentResponseDTO dto = equipmentMapper.toResponseDTO(entity);

        // Добавление статистики
        dto.setPortsCount((int) devicePortRepository.countByEquipment(entity));
        dto.setIpAddressesCount((int) ipAddressRepository.countByEquipment(entity));
        dto.setMaintenanceCount((int) maintenanceHistoryRepository.countByEquipment(entity));

        return dto;
    }

    /**
     * Конвертация Entity → List DTO со статистикой
     */
    private EquipmentListDTO toListDTOWithStats(Equipment entity) {
        EquipmentListDTO dto = equipmentMapper.toListDTO(entity);

        // Добавление счетчика портов
        dto.setPortsCount((int) devicePortRepository.countByEquipment(entity));

        return dto;
    }
}
