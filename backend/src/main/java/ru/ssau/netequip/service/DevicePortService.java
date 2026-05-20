package ru.ssau.netequip.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.netequip.dto.devicePort.CreateAndUpdateDevicePortDto;
import ru.ssau.netequip.dto.devicePort.ResponseDevicePortDto;
import ru.ssau.netequip.entity.DevicePort;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.exception.devicePort.DuplicateDevicePortException;
import ru.ssau.netequip.exception.devicePort.InvalidPortConnectionException;
import ru.ssau.netequip.exception.devicePort.NotFoundDevicePortException;
import ru.ssau.netequip.exception.equipment.NotFoundEquipmentException;
import ru.ssau.netequip.mapper.DevicePortMapper;
import ru.ssau.netequip.repository.DevicePortRepository;
import ru.ssau.netequip.repository.EquipmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class DevicePortService {
    private final DevicePortRepository devicePortRepository;
    private final EquipmentRepository equipmentRepository;
    private final DevicePortMapper devicePortMapper;
    private final AuditLogService auditLogService;
    public DevicePortService(DevicePortRepository devicePortRepository, EquipmentRepository equipmentRepository,
                             DevicePortMapper devicePortMapper, AuditLogService auditLogService) {
        this.devicePortRepository = devicePortRepository;
        this.equipmentRepository = equipmentRepository;
        this.devicePortMapper = devicePortMapper;
        this.auditLogService = auditLogService;
    }
    @Transactional
    public ResponseDevicePortDto create(CreateAndUpdateDevicePortDto dto) {
        log.info("Создание нового порта № {} для оборудования ID {}",
                dto.getPortNumber(), dto.getEquipmentId());
        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId()).orElseThrow(
                () -> {
                    log.warn("Оборудование с ID {} не найдено", dto.getEquipmentId());
                    return new NotFoundEquipmentException(dto.getEquipmentId());
                }
        );
        if(devicePortRepository.findByEquipmentAndPortNumber(equipment, dto.getPortNumber()).isPresent()) {
            log.warn("Порт № {} уже существует на устройстве ID {}",
                    dto.getPortNumber(), dto.getEquipmentId());
            throw new DuplicateDevicePortException(dto.getEquipmentId(), dto.getPortNumber());
        }

        DevicePort devicePort = devicePortMapper.toEntity(dto);
        devicePort.setEquipment(equipment);
        setPortConnect(devicePort, dto.getConnectedToEquipmentId(), dto.getConnectedToPortId());

        DevicePort savedDevicePort = devicePortRepository.save(devicePort);
        log.info("Порт создан с ID {}",savedDevicePort.getId());

        auditLogService.record(
                "CREATE",
                "device_port",
                savedDevicePort.getId(),
                equipment.getName() + ":" + savedDevicePort.getPortNumber(),
                "Создан порт " + savedDevicePort.getPortNumber() + " на " + equipment.getName()
        );

        return devicePortMapper.toResponseDTO(savedDevicePort);
    }

    public ResponseDevicePortDto getById(Long id) {
        log.info("Получение порта ID {}", id);
        DevicePort devicePort = devicePortRepository.findById(id).orElseThrow(
                () -> {
                    log.warn("Порт с ID {} не найден", id);
                    return new NotFoundDevicePortException(id);
                }
        );
        return devicePortMapper.toResponseDTO(devicePort);
    }

    public List<ResponseDevicePortDto> getAll(){
        log.info("Получение всех портов");
        List<DevicePort> devicePorts = devicePortRepository.findAll();
        log.info("Получено всего портов: {}", devicePorts.size());
        return devicePorts.stream()
                .map(devicePortMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    public Page<ResponseDevicePortDto> getPage(String search, Pageable pageable) {
        log.info("Получение страницы портов: page={}, size={}, search='{}'",
                pageable.getPageNumber(), pageable.getPageSize(), search);

        Page<DevicePort> page;
        if (search != null && !search.isBlank()) {
            page = devicePortRepository.search(search.trim(), pageable);
        } else {
            page = devicePortRepository.findAll(pageable);
        }

        log.info("Найдено портов на странице: {} из {}",
                page.getNumberOfElements(), page.getTotalElements());
        return page.map(devicePortMapper::toResponseDTO);
    }

    @Transactional
    public ResponseDevicePortDto update(Long id, CreateAndUpdateDevicePortDto dto) {
        log.info("Обновление порта с ID {}", id);

        DevicePort devicePort = devicePortRepository.findById(id).orElseThrow(
                () -> {
                    log.warn("Попытка обновить несуществующий порт с ID {}",id);
                    return new NotFoundDevicePortException(id);
                }
        );

        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId()).orElseThrow(
                () -> {
                    log.warn("Оборудование с ID {} не найден",dto.getEquipmentId());
                    return new NotFoundEquipmentException(dto.getEquipmentId());
                }
        );

        if(!devicePort.getEquipment().getId().equals(dto.getEquipmentId()) ||
                !devicePort.getPortNumber().equals(dto.getPortNumber())){
            if(devicePortRepository.findByEquipmentAndPortNumber(equipment, dto.getPortNumber()).isPresent()){
                log.warn("Порт № {} уже существует на устройстве ID {}",
                        dto.getPortNumber(), dto.getEquipmentId());
                throw new DuplicateDevicePortException(dto.getEquipmentId(), dto.getPortNumber());
            }
        }

        devicePortMapper.updateEntityFromDTO(dto, devicePort);
        devicePort.setEquipment(equipment);

        setPortConnect(devicePort, dto.getConnectedToEquipmentId(), dto.getConnectedToPortId());

        DevicePort updDevicePort = devicePortRepository.save(devicePort);
        log.info("Порт с ID {} обновлен", updDevicePort.getId());

        auditLogService.record(
                "UPDATE",
                "device_port",
                updDevicePort.getId(),
                equipment.getName() + ":" + updDevicePort.getPortNumber(),
                "Обновлен порт " + updDevicePort.getPortNumber() + " на " + equipment.getName()
        );

        return devicePortMapper.toResponseDTO(updDevicePort);
    }

    @Transactional
    public void delete(Long id){
        log.info("Удаление порта с ID {}",id);
        DevicePort devicePort = devicePortRepository.findById(id).orElseThrow(
                () -> {
                    log.warn("Попытка удалить несуществующий порт ID {}", id);
                    return new NotFoundDevicePortException(id);
                }
        );

        List<DevicePort> linkedPorts = devicePortRepository.findByConnectedToPort(devicePort);
        for(DevicePort linked : linkedPorts){
            linked.setConnectedToPort(null);
            linked.setConnectedToEquipment(null);
            devicePortRepository.save(linked);
        }

        devicePortRepository.deleteById(id);

        auditLogService.record(
                "DELETE",
                "device_port",
                devicePort.getId(),
                devicePort.getEquipment().getName() + ":" + devicePort.getPortNumber(),
                "Обновлен порт " + devicePort.getPortNumber() + " на " + devicePort.getEquipment().getName()
        );

        log.info("Порт с ID {} удален", id);
    }

    private void setPortConnect(DevicePort devicePort, Long connectedToEquipmentId, Long connectedToPortId) {
        if (connectedToEquipmentId != null) {
            Equipment connectEquip = equipmentRepository.findById(connectedToEquipmentId).orElseThrow(
                    () -> new NotFoundEquipmentException(connectedToEquipmentId)
            );
            devicePort.setConnectedToEquipment(connectEquip);
        }else{
            devicePort.setConnectedToEquipment(null);
        }
        if (connectedToPortId != null) {
            DevicePort connectPort = devicePortRepository.findById(connectedToPortId).orElseThrow(
                    () -> new NotFoundDevicePortException(connectedToPortId)
            );
            if(connectedToEquipmentId != null && !connectPort.getEquipment().getId().equals(connectedToEquipmentId)) {
                throw new InvalidPortConnectionException(connectedToPortId, connectedToEquipmentId);
            }
            devicePort.setConnectedToPort(connectPort);
        }else{
            devicePort.setConnectedToPort(null);
        }
    }
}
