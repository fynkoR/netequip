package com.example.netequip.service;

import com.example.netequip.dto.deviceport.CreateDevicePortDTO;
import com.example.netequip.dto.deviceport.DevicePortResponseDTO;
import com.example.netequip.dto.deviceport.UpdateDevicePortDTO;
import com.example.netequip.entity.DevicePort;
import com.example.netequip.entity.Equipment;
import com.example.netequip.exception.deviceport.*;
import com.example.netequip.exception.equiptype.EquipmentTypeNotFoundException;
import com.example.netequip.mapper.DevicePortMapper;
import com.example.netequip.repository.DevicePortRepository;
import com.example.netequip.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления портами сетевых устройств
 * Содержит бизнес-логику для CRUD операций и управления подключениями
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DevicePortService {

    private final DevicePortRepository devicePortRepository;
    private final EquipmentRepository equipmentRepository;
    private final DevicePortMapper devicePortMapper;

    /**
     * Создание нового порта устройства
     *
     * @param dto данные для создания
     * @return созданный порт
     * @throws EquipmentTypeNotFoundException если оборудование не найдено
     * @throws DuplicateDevicePortException если порт с таким номером уже существует
     * @throws InvalidPortConnectionException если подключение некорректно
     */
    @Transactional
    public DevicePortResponseDTO create(CreateDevicePortDTO dto) {
        log.info("Создание нового порта №{} для оборудования ID: {}",
                dto.getPortNumber(), dto.getEquipmentId());

        // Поиск оборудования-владельца порта
        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> {
                    log.warn("Оборудование с ID {} не найдено", dto.getEquipmentId());
                    return new EquipmentTypeNotFoundException(dto.getEquipmentId());
                });

        // Проверка уникальности номера порта на устройстве
        if (devicePortRepository.findByEquipmentAndPortNumber(equipment, dto.getPortNumber()).isPresent()) {
            log.warn("Порт №{} уже существует на устройстве ID: {}",
                    dto.getPortNumber(), dto.getEquipmentId());
            throw new DuplicateDevicePortException(dto.getEquipmentId(), dto.getPortNumber());
        }

        // Конвертация DTO → Entity
        DevicePort entity = devicePortMapper.toEntity(dto);
        entity.setEquipment(equipment);

        // Установка подключений (если указаны)
        setPortConnections(entity, dto.getConnectedToEquipmentId(), dto.getConnectedToPortId());

        // Сохранение
        DevicePort savedEntity = devicePortRepository.save(entity);
        log.info("Порт успешно создан с ID: {}", savedEntity.getId());

        return devicePortMapper.toResponseDTO(savedEntity);
    }

    /**
     * Получение порта по ID
     *
     * @param id идентификатор порта
     * @return найденный порт
     * @throws DevicePortNotFoundException если порт не найден
     */
    public DevicePortResponseDTO getById(Long id) {
        log.debug("Получение порта по ID: {}", id);

        DevicePort entity = devicePortRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Порт с ID {} не найден", id);
                    return new DevicePortNotFoundException(id);
                });

        return devicePortMapper.toResponseDTO(entity);
    }

    /**
     * Получение всех портов
     *
     * @return список всех портов
     */
    public List<DevicePortResponseDTO> getAll() {
        log.debug("Получение всех портов");

        List<DevicePort> entities = devicePortRepository.findAll();
        log.info("Найдено портов: {}", entities.size());

        return entities.stream()
                .map(devicePortMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Обновление существующего порта
     *
     * @param id идентификатор порта
     * @param dto новые данные
     * @return обновленный порт
     * @throws DevicePortNotFoundException если порт не найден
     * @throws DuplicateDevicePortException если новый номер порта уже занят
     */
    @Transactional
    public DevicePortResponseDTO update(Long id, UpdateDevicePortDTO dto) {
        log.info("Обновление порта с ID: {}", id);

        // Поиск существующего порта
        DevicePort existingEntity = devicePortRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка обновить несуществующий порт с ID: {}", id);
                    return new DevicePortNotFoundException(id);
                });

        // Поиск нового оборудования (если изменилось)
        Equipment newEquipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> {
                    log.warn("Оборудование с ID {} не найдено", dto.getEquipmentId());
                    return new EquipmentTypeNotFoundException(dto.getEquipmentId());
                });

        // Проверка уникальности номера порта (если изменился или изменилось оборудование)
        if (!existingEntity.getEquipment().getId().equals(dto.getEquipmentId()) ||
                !existingEntity.getPortNumber().equals(dto.getPortNumber())) {

            if (devicePortRepository.findByEquipmentAndPortNumber(newEquipment, dto.getPortNumber()).isPresent()) {
                log.warn("Порт №{} уже существует на устройстве ID: {}",
                        dto.getPortNumber(), dto.getEquipmentId());
                throw new DuplicateDevicePortException(dto.getEquipmentId(), dto.getPortNumber());
            }
        }

        // Обновление базовых полей
        devicePortMapper.updateEntityFromDTO(dto, existingEntity);
        existingEntity.setEquipment(newEquipment);

        // Обновление подключений
        setPortConnections(existingEntity, dto.getConnectedToEquipmentId(), dto.getConnectedToPortId());

        // Сохранение
        DevicePort updatedEntity = devicePortRepository.save(existingEntity);
        log.info("Порт с ID {} успешно обновлен", id);

        return devicePortMapper.toResponseDTO(updatedEntity);
    }

    /**
     * Удаление порта
     *
     * @param id идентификатор порта
     * @throws DevicePortNotFoundException если порт не найден
     */
    @Transactional
    public void delete(Long id) {
        log.info("Удаление порта с ID: {}", id);

        // Проверка существования
        if (!devicePortRepository.existsById(id)) {
            log.warn("Попытка удалить несуществующий порт с ID: {}", id);
            throw new DevicePortNotFoundException(id);
        }

        devicePortRepository.deleteById(id);
        log.info("Порт с ID {} успешно удален", id);
    }

    /**
     * Получение всех портов устройства (с сортировкой по номеру)
     *
     * @param equipmentId ID оборудования
     * @return список портов устройства
     */
    public List<DevicePortResponseDTO> getByEquipment(Long equipmentId) {
        log.debug("Получение портов устройства ID: {}", equipmentId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        List<DevicePort> entities = devicePortRepository.findByEquipmentOrderByPortNumberAsc(equipment);
        log.info("Найдено портов устройства ID {}: {}", equipmentId, entities.size());

        return entities.stream()
                .map(devicePortMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение конкретного порта устройства по номеру
     *
     * @param equipmentId ID оборудования
     * @param portNumber номер порта
     * @return найденный порт
     */
    public DevicePortResponseDTO getByEquipmentAndPortNumber(Long equipmentId, Integer portNumber) {
        log.debug("Получение порта №{} устройства ID: {}", portNumber, equipmentId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        DevicePort entity = devicePortRepository.findByEquipmentAndPortNumber(equipment, portNumber)
                .orElseThrow(() -> {
                    log.warn("Порт №{} устройства ID {} не найден", portNumber, equipmentId);
                    return new DevicePortNotFoundException(equipmentId, portNumber);
                });

        return devicePortMapper.toResponseDTO(entity);
    }

    /**
     * Получение портов устройства по статусу
     *
     * @param equipmentId ID оборудования
     * @param status статус порта
     * @return список портов с данным статусом
     */
    public List<DevicePortResponseDTO> getByEquipmentAndStatus(Long equipmentId, String status) {
        log.debug("Получение портов устройства ID {} со статусом: {}", equipmentId, status);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        List<DevicePort> entities = devicePortRepository.findByEquipmentAndStatus(equipment, status);
        log.info("Найдено портов со статусом '{}' на устройстве ID {}: {}",
                status, equipmentId, entities.size());

        return entities.stream()
                .map(devicePortMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение свободных портов устройства
     *
     * @param equipmentId ID оборудования
     * @return список свободных портов
     */
    public List<DevicePortResponseDTO> getAvailablePorts(Long equipmentId) {
        log.debug("Получение свободных портов устройства ID: {}", equipmentId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        List<DevicePort> entities = devicePortRepository.findAvailablePortsByEquipment(equipment);
        log.info("Найдено свободных портов на устройстве ID {}: {}", equipmentId, entities.size());

        return entities.stream()
                .map(devicePortMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение занятых портов устройства
     *
     * @param equipmentId ID оборудования
     * @return список занятых портов
     */
    public List<DevicePortResponseDTO> getOccupiedPorts(Long equipmentId) {
        log.debug("Получение занятых портов устройства ID: {}", equipmentId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        List<DevicePort> entities = devicePortRepository.findOccupiedPortsByEquipment(equipment);
        log.info("Найдено занятых портов на устройстве ID {}: {}", equipmentId, entities.size());

        return entities.stream()
                .map(devicePortMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение всех подключений к определенному устройству
     *
     * @param equipmentId ID оборудования
     * @return список портов, подключенных к данному устройству
     */
    public List<DevicePortResponseDTO> getConnectionsToEquipment(Long equipmentId) {
        log.debug("Получение всех подключений к устройству ID: {}", equipmentId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        List<DevicePort> entities = devicePortRepository.findByConnectedToEquipment(equipment);
        log.info("Найдено подключений к устройству ID {}: {}", equipmentId, entities.size());

        return entities.stream()
                .map(devicePortMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение портов по статусу (все устройства)
     *
     * @param status статус порта
     * @return список портов с данным статусом
     */
    public List<DevicePortResponseDTO> getByStatus(String status) {
        log.debug("Получение всех портов со статусом: {}", status);

        List<DevicePort> entities = devicePortRepository.findAll().stream()
                .filter(port -> status.equals(port.getStatus()))
                .collect(Collectors.toList());
        log.info("Найдено портов со статусом '{}': {}", status, entities.size());

        return entities.stream()
                .map(devicePortMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение активных портов оборудования
     *
     * @param equipmentId ID оборудования
     * @return список активных портов
     */
    public List<DevicePortResponseDTO> getActivePortsByEquipment(Long equipmentId) {
        log.debug("Получение активных портов оборудования ID: {}", equipmentId);
        return getByEquipmentAndStatus(equipmentId, "Active");
    }

    /**
     * Получение свободных портов оборудования (алиас для getAvailablePorts)
     *
     * @param equipmentId ID оборудования
     * @return список свободных портов
     */
    public List<DevicePortResponseDTO> getAvailablePortsByEquipment(Long equipmentId) {
        log.debug("Получение свободных портов оборудования ID: {}", equipmentId);
        return getAvailablePorts(equipmentId);
    }

    /**
     * Получение портов по типу (все устройства)
     *
     * @param portType тип порта
     * @return список портов данного типа
     */
    public List<DevicePortResponseDTO> getByPortType(String portType) {
        log.debug("Получение всех портов типа: {}", portType);

        List<DevicePort> entities = devicePortRepository.findAll().stream()
                .filter(port -> portType.equals(port.getPortType()))
                .collect(Collectors.toList());
        log.info("Найдено портов типа '{}': {}", portType, entities.size());

        return entities.stream()
                .map(devicePortMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение портов оборудования по типу и статусу
     *
     * @param equipmentId ID оборудования
     * @param portType тип порта
     * @param status статус порта
     * @return список портов
     */
    public List<DevicePortResponseDTO> getByEquipmentAndTypeAndStatus(
            Long equipmentId, String portType, String status) {
        log.debug("Получение портов оборудования ID {} типа '{}' со статусом '{}'",
                equipmentId, portType, status);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        List<DevicePort> entities = devicePortRepository.findByEquipmentOrderByPortNumberAsc(equipment)
                .stream()
                .filter(port -> portType.equals(port.getPortType()) && status.equals(port.getStatus()))
                .collect(Collectors.toList());
        log.info("Найдено портов типа '{}' со статусом '{}' на оборудовании ID {}: {}",
                portType, status, equipmentId, entities.size());

        return entities.stream()
                .map(devicePortMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Изменение статуса порта
     *
     * @param portId ID порта
     * @param newStatus новый статус
     * @return обновленный порт
     * @throws DevicePortNotFoundException если порт не найден
     */
    @Transactional
    public DevicePortResponseDTO changeStatus(Long portId, String newStatus) {
        log.info("Изменение статуса порта ID {} на: {}", portId, newStatus);

        DevicePort port = devicePortRepository.findById(portId)
                .orElseThrow(() -> new DevicePortNotFoundException(portId));

        port.setStatus(newStatus);
        DevicePort savedPort = devicePortRepository.save(port);
        log.info("Статус порта ID {} изменен на '{}'", portId, newStatus);

        return devicePortMapper.toResponseDTO(savedPort);
    }

    /**
     * Получение порта, к которому подключён данный порт
     *
     * @param portId ID порта
     * @return подключённый порт
     * @throws DevicePortNotFoundException если порт не найден
     * @throws PortNotConnectedException если порт не подключён
     */
    public DevicePortResponseDTO getConnectedPort(Long portId) {
        log.debug("Получение подключённого порта для порта ID: {}", portId);

        DevicePort port = devicePortRepository.findById(portId)
                .orElseThrow(() -> new DevicePortNotFoundException(portId));

        if (port.getConnectedToPort() == null) {
            log.warn("Порт ID {} не подключён к другому порту", portId);
            throw new PortNotConnectedException(portId);
        }

        return devicePortMapper.toResponseDTO(port.getConnectedToPort());
    }

    /**
     * Проверка подключён ли порт к другому порту
     *
     * @param portId ID порта
     * @return true если порт подключён
     * @throws DevicePortNotFoundException если порт не найден
     */
    public boolean isPortConnected(Long portId) {
        log.debug("Проверка подключения порта ID: {}", portId);

        DevicePort port = devicePortRepository.findById(portId)
                .orElseThrow(() -> new DevicePortNotFoundException(portId));

        boolean connected = port.getConnectedToPort() != null;
        log.debug("Порт ID {} подключён: {}", portId, connected);

        return connected;
    }

    /**
     * Подсчет активных портов оборудования
     *
     * @param equipmentId ID оборудования
     * @return количество активных портов
     */
    public long countActivePortsByEquipment(Long equipmentId) {
        return countByEquipmentAndStatus(equipmentId, "Active");
    }


    /**
     * Подключение двух портов друг к другу
     *
     * @param portId ID первого порта
     * @param targetPortId ID второго порта
     * @return обновленный первый порт
     */
    @Transactional
    public DevicePortResponseDTO connectPorts(Long portId, Long targetPortId) {
        log.info("Подключение порта ID {} к порту ID {}", portId, targetPortId);

        DevicePort sourcePort = devicePortRepository.findById(portId)
                .orElseThrow(() -> new DevicePortNotFoundException(portId));

        DevicePort targetPort = devicePortRepository.findById(targetPortId)
                .orElseThrow(() -> new DevicePortNotFoundException(targetPortId));

        // Валидация подключения
        validatePortConnection(sourcePort, targetPort);

        // Установка подключения
        sourcePort.setConnectedToEquipment(targetPort.getEquipment());
        sourcePort.setConnectedToPort(targetPort);

        DevicePort savedPort = devicePortRepository.save(sourcePort);
        log.info("Порты успешно подключены");

        return devicePortMapper.toResponseDTO(savedPort);
    }

    /**
     * Отключение порта
     *
     * @param portId ID порта
     * @return обновленный порт
     */
    @Transactional
    public DevicePortResponseDTO disconnectPort(Long portId) {
        log.info("Отключение порта ID: {}", portId);

        DevicePort port = devicePortRepository.findById(portId)
                .orElseThrow(() -> new DevicePortNotFoundException(portId));

        port.setConnectedToEquipment(null);
        port.setConnectedToPort(null);

        DevicePort savedPort = devicePortRepository.save(port);
        log.info("Порт ID {} успешно отключен", portId);

        return devicePortMapper.toResponseDTO(savedPort);
    }

    /**
     * Подсчет портов устройства
     *
     * @param equipmentId ID оборудования
     * @return количество портов
     */
    public long countByEquipment(Long equipmentId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        return devicePortRepository.countByEquipment(equipment);
    }

    /**
     * Подсчет портов устройства с определенным статусом
     *
     * @param equipmentId ID оборудования
     * @param status статус
     * @return количество портов
     */
    public long countByEquipmentAndStatus(Long equipmentId, String status) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        return devicePortRepository.countByEquipmentAndStatus(equipment, status);
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Установка подключений порта к другому оборудованию/порту
     */
    private void setPortConnections(DevicePort port, Long connectedToEquipmentId, Long connectedToPortId) {
        if (connectedToEquipmentId != null) {
            Equipment connectedEquipment = equipmentRepository.findById(connectedToEquipmentId)
                    .orElseThrow(() -> new EquipmentTypeNotFoundException(connectedToEquipmentId));
            port.setConnectedToEquipment(connectedEquipment);
        } else {
            port.setConnectedToEquipment(null);
        }

        if (connectedToPortId != null) {
            DevicePort connectedPort = devicePortRepository.findById(connectedToPortId)
                    .orElseThrow(() -> new DevicePortNotFoundException(connectedToPortId));

            // Валидация: порт должен принадлежать указанному оборудованию
            if (connectedToEquipmentId != null &&
                    !connectedPort.getEquipment().getId().equals(connectedToEquipmentId)) {
                throw new InvalidPortConnectionException(
                        "Порт ID " + connectedToPortId + " не принадлежит оборудованию ID " + connectedToEquipmentId
                );
            }

            port.setConnectedToPort(connectedPort);
        } else {
            port.setConnectedToPort(null);
        }
    }

    /**
     * Валидация подключения между портами
     */
    private void validatePortConnection(DevicePort sourcePort, DevicePort targetPort) {
        // Нельзя подключить порт к самому себе
        if (sourcePort.getId().equals(targetPort.getId())) {
            throw new InvalidPortConnectionException("Нельзя подключить порт к самому себе");
        }

        // Нельзя подключить порты одного устройства друг к другу
        if (sourcePort.getEquipment().getId().equals(targetPort.getEquipment().getId())) {
            throw new InvalidPortConnectionException(
                    "Нельзя подключить порты одного устройства друг к другу"
            );
        }

        // Проверка, что целевой порт свободен
        if (targetPort.getConnectedToPort() != null) {
            log.warn("Целевой порт ID {} уже подключен к порту ID {}",
                    targetPort.getId(), targetPort.getConnectedToPort().getId());
            throw new InvalidPortConnectionException(
                    "Целевой порт уже занят. Сначала отключите его."
            );
        }
    }
}
