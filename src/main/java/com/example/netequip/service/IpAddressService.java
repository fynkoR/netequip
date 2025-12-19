package com.example.netequip.service;

import com.example.netequip.dto.ipaddress.CreateIpAddressDTO;
import com.example.netequip.dto.ipaddress.IpAddressResponseDTO;
import com.example.netequip.dto.ipaddress.UpdateIpAddressDTO;
import com.example.netequip.entity.Equipment;
import com.example.netequip.entity.IpAddress;
import com.example.netequip.exception.equiptype.EquipmentTypeNotFoundException;
import com.example.netequip.exception.ipaddress.*;
import com.example.netequip.mapper.IpAddressMapper;
import com.example.netequip.repository.EquipmentRepository;
import com.example.netequip.repository.IpAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления IP-адресами оборудования
 * Содержит бизнес-логику для CRUD операций и управления сетевой конфигурацией
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IpAddressService {

    private final IpAddressRepository ipAddressRepository;
    private final EquipmentRepository equipmentRepository;
    private final IpAddressMapper ipAddressMapper;

    /**
     * Создание нового IP-адреса
     *
     * @param dto данные для создания
     * @return созданный IP-адрес
     * @throws EquipmentTypeNotFoundException если оборудование не найдено
     * @throws DuplicateIpAddressException если IP-адрес уже используется
     * @throws PrimaryIpAddressConflictException если пытаемся установить второй основной IP
     */
    @Transactional
    public IpAddressResponseDTO create(CreateIpAddressDTO dto) {
        log.info("Создание нового IP-адреса {} для оборудования ID: {}",
                dto.getIpAddress(), dto.getEquipmentId());

        // Поиск оборудования
        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> {
                    log.warn("Оборудование с ID {} не найдено", dto.getEquipmentId());
                    return new EquipmentTypeNotFoundException(dto.getEquipmentId());
                });

        // Проверка уникальности IP-адреса
        if (ipAddressRepository.existsByIpAddress(dto.getIpAddress())) {
            log.warn("Попытка создать дубликат IP-адреса: {}", dto.getIpAddress());
            throw new DuplicateIpAddressException(dto.getIpAddress());
        }

        // Проверка флага isPrimary
        if (Boolean.TRUE.equals(dto.getIsPrimary())) {
            validatePrimaryIp(equipment, null);
        }

        // Конвертация DTO → Entity
        IpAddress entity = ipAddressMapper.toEntity(dto);
        entity.setEquipment(equipment);

        // Установка даты назначения (если не указана)
        if (entity.getAssignedDate() == null) {
            entity.setAssignedDate(LocalDate.now());
        }

        // Сохранение
        IpAddress savedEntity = ipAddressRepository.save(entity);
        log.info("IP-адрес успешно создан с ID: {}", savedEntity.getId());

        return ipAddressMapper.toResponseDTO(savedEntity);
    }

    /**
     * Получение IP-адреса по ID
     *
     * @param id идентификатор IP-адреса
     * @return найденный IP-адрес
     * @throws IpAddressNotFoundException если IP-адрес не найден
     */
    public IpAddressResponseDTO getById(Long id) {
        log.debug("Получение IP-адреса по ID: {}", id);

        IpAddress entity = ipAddressRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("IP-адрес с ID {} не найден", id);
                    return new IpAddressNotFoundException(id);
                });

        return ipAddressMapper.toResponseDTO(entity);
    }

    /**
     * Получение всех IP-адресов
     *
     * @return список всех IP-адресов
     */
    public List<IpAddressResponseDTO> getAll() {
        log.debug("Получение всех IP-адресов");

        List<IpAddress> entities = ipAddressRepository.findAll();
        log.info("Найдено IP-адресов: {}", entities.size());

        return entities.stream()
                .map(ipAddressMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Обновление существующего IP-адреса
     *
     * @param id идентификатор IP-адреса
     * @param dto новые данные
     * @return обновленный IP-адрес
     * @throws IpAddressNotFoundException если IP-адрес не найден
     * @throws DuplicateIpAddressException если новый IP уже используется
     * @throws PrimaryIpAddressConflictException если конфликт основных IP
     */
    @Transactional
    public IpAddressResponseDTO update(Long id, UpdateIpAddressDTO dto) {
        log.info("Обновление IP-адреса с ID: {}", id);

        // Поиск существующего IP-адреса
        IpAddress existingEntity = ipAddressRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка обновить несуществующий IP-адрес с ID: {}", id);
                    return new IpAddressNotFoundException(id);
                });

        // Поиск нового оборудования (если изменилось)
        Equipment newEquipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> {
                    log.warn("Оборудование с ID {} не найдено", dto.getEquipmentId());
                    return new EquipmentTypeNotFoundException(dto.getEquipmentId());
                });

        // Проверка уникальности IP (если изменился)
        if (!existingEntity.getIpAddress().equals(dto.getIpAddress())) {
            if (ipAddressRepository.existsByIpAddress(dto.getIpAddress())) {
                log.warn("Попытка изменить IP на уже существующий: {}", dto.getIpAddress());
                throw new DuplicateIpAddressException(dto.getIpAddress());
            }
        }

        // Проверка флага isPrimary (если устанавливается или изменяется оборудование)
        if (Boolean.TRUE.equals(dto.getIsPrimary())) {
            // Если оборудование изменилось или флаг меняется с false на true
            if (!newEquipment.getId().equals(existingEntity.getEquipment().getId()) ||
                    !Boolean.TRUE.equals(existingEntity.getIsPrimary())) {
                validatePrimaryIp(newEquipment, id);
            }
        }

        // Обновление полей
        ipAddressMapper.updateEntityFromDTO(dto, existingEntity);
        existingEntity.setEquipment(newEquipment);

        // Сохранение
        IpAddress updatedEntity = ipAddressRepository.save(existingEntity);
        log.info("IP-адрес с ID {} успешно обновлен", id);

        return ipAddressMapper.toResponseDTO(updatedEntity);
    }

    /**
     * Удаление IP-адреса
     *
     * @param id идентификатор IP-адреса
     * @throws IpAddressNotFoundException если IP-адрес не найден
     */
    @Transactional
    public void delete(Long id) {
        log.info("Удаление IP-адреса с ID: {}", id);

        // Проверка существования
        if (!ipAddressRepository.existsById(id)) {
            log.warn("Попытка удалить несуществующий IP-адрес с ID: {}", id);
            throw new IpAddressNotFoundException(id);
        }

        ipAddressRepository.deleteById(id);
        log.info("IP-адрес с ID {} успешно удален", id);
    }

    /**
     * Получение всех IP-адресов устройства
     *
     * @param equipmentId ID оборудования
     * @return список IP-адресов устройства
     */
    public List<IpAddressResponseDTO> getByEquipment(Long equipmentId) {
        log.debug("Получение IP-адресов оборудования ID: {}", equipmentId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        List<IpAddress> entities = ipAddressRepository.findByEquipment(equipment);
        log.info("Найдено IP-адресов для оборудования ID {}: {}", equipmentId, entities.size());

        return entities.stream()
                .map(ipAddressMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение основного IP-адреса устройства
     *
     * @param equipmentId ID оборудования
     * @return основной IP-адрес устройства
     * @throws IpAddressNotFoundException если основной IP не найден
     */
    public IpAddressResponseDTO getPrimaryIpByEquipment(Long equipmentId) {
        log.debug("Получение основного IP-адреса оборудования ID: {}", equipmentId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        IpAddress entity = ipAddressRepository.findByEquipmentAndIsPrimary(equipment, true)
                .orElseThrow(() -> {
                    log.warn("Основной IP-адрес для оборудования ID {} не найден", equipmentId);
                    return new IpAddressNotFoundException(
                            "Основной IP-адрес для оборудования не найден"
                    );
                });

        return ipAddressMapper.toResponseDTO(entity);
    }

    /**
     * Поиск IP-адреса по значению
     *
     * @param ipAddress значение IP-адреса
     * @return найденный IP-адрес
     * @throws IpAddressNotFoundException если IP не найден
     */
    public IpAddressResponseDTO getByIpAddress(String ipAddress) {
        log.debug("Поиск IP-адреса: {}", ipAddress);

        IpAddress entity = ipAddressRepository.findByIpAddress(ipAddress)
                .orElseThrow(() -> {
                    log.warn("IP-адрес {} не найден", ipAddress);
                    return new IpAddressNotFoundException(ipAddress);
                });

        return ipAddressMapper.toResponseDTO(entity);
    }

    /**
     * Получение IP-адресов по типу сети
     *
     * @param networkType тип сети
     * @return список IP-адресов данного типа
     */
    public List<IpAddressResponseDTO> getByNetworkType(String networkType) {
        log.debug("Получение IP-адресов типа сети: {}", networkType);

        List<IpAddress> entities = ipAddressRepository.findByNetworkType(networkType);
        log.info("Найдено IP-адресов типа '{}': {}", networkType, entities.size());

        return entities.stream()
                .map(ipAddressMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение IP-адресов оборудования по типу сети
     *
     * @param equipmentId ID оборудования
     * @param networkType тип сети
     * @return список IP-адресов
     */
    public List<IpAddressResponseDTO> getByEquipmentAndNetworkType(Long equipmentId, String networkType) {
        log.debug("Получение IP-адресов оборудования ID {} типа сети: {}", equipmentId, networkType);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        List<IpAddress> entities = ipAddressRepository.findByEquipmentAndNetworkType(equipment, networkType);
        log.info("Найдено IP-адресов типа '{}' для оборудования ID {}: {}",
                networkType, equipmentId, entities.size());

        return entities.stream()
                .map(ipAddressMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Получение всех IP-адресов в подсети
     *
     * @param subnetMask маска подсети
     * @return список IP-адресов в данной подсети
     */
    public List<IpAddressResponseDTO> getBySubnetMask(String subnetMask) {
        log.debug("Получение IP-адресов с маской подсети: {}", subnetMask);

        List<IpAddress> entities = ipAddressRepository.findBySubnetMask(subnetMask);
        log.info("Найдено IP-адресов с маской '{}': {}", subnetMask, entities.size());

        return entities.stream()
                .map(ipAddressMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Установка IP-адреса как основного для устройства
     *
     * @param id ID IP-адреса
     * @return обновленный IP-адрес
     * @throws IpAddressNotFoundException если IP-адрес не найден
     * @throws PrimaryIpAddressConflictException если у устройства уже есть основной IP
     */
    @Transactional
    public IpAddressResponseDTO setPrimaryIp(Long id) {
        log.info("Установка IP-адреса ID {} как основного", id);

        IpAddress ipAddress = ipAddressRepository.findById(id)
                .orElseThrow(() -> new IpAddressNotFoundException(id));

        // Проверка, что у устройства нет другого основного IP
        validatePrimaryIp(ipAddress.getEquipment(), id);

        ipAddress.setIsPrimary(true);
        IpAddress savedEntity = ipAddressRepository.save(ipAddress);
        log.info("IP-адрес ID {} установлен как основной", id);

        return ipAddressMapper.toResponseDTO(savedEntity);
    }

    /**
     * Снятие флага основного IP-адреса
     *
     * @param id ID IP-адреса
     * @return обновленный IP-адрес
     * @throws IpAddressNotFoundException если IP-адрес не найден
     */
    @Transactional
    public IpAddressResponseDTO unsetPrimaryIp(Long id) {
        log.info("Снятие флага основного IP с адреса ID {}", id);

        IpAddress ipAddress = ipAddressRepository.findById(id)
                .orElseThrow(() -> new IpAddressNotFoundException(id));

        ipAddress.setIsPrimary(false);
        IpAddress savedEntity = ipAddressRepository.save(ipAddress);
        log.info("Флаг основного IP снят с адреса ID {}", id);

        return ipAddressMapper.toResponseDTO(savedEntity);
    }

    /**
     * Проверка существования IP-адреса
     *
     * @param ipAddress значение IP-адреса
     * @return true если существует
     */
    public boolean existsByIpAddress(String ipAddress) {
        return ipAddressRepository.existsByIpAddress(ipAddress);
    }

    /**
     * Подсчет IP-адресов устройства
     *
     * @param equipmentId ID оборудования
     * @return количество IP-адресов
     */
    public long countByEquipment(Long equipmentId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new EquipmentTypeNotFoundException(equipmentId));

        return ipAddressRepository.countByEquipment(equipment);
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Проверка возможности установки основного IP для устройства
     *
     * @param equipment оборудование
     * @param excludeIpId ID IP-адреса, который нужно исключить из проверки (при обновлении)
     * @throws PrimaryIpAddressConflictException если у устройства уже есть другой основной IP
     */
    private void validatePrimaryIp(Equipment equipment, Long excludeIpId) {
        Optional<IpAddress> existingPrimaryIp = ipAddressRepository
                .findByEquipmentAndIsPrimary(equipment, true);

        if (existingPrimaryIp.isPresent()) {
            // Если это тот же самый IP (при обновлении), то всё ОК
            if (excludeIpId != null && existingPrimaryIp.get().getId().equals(excludeIpId)) {
                return;
            }

            log.warn("У устройства ID {} уже есть основной IP: {}",
                    equipment.getId(), existingPrimaryIp.get().getIpAddress());
            throw new PrimaryIpAddressConflictException(equipment.getId());
        }
    }
}
