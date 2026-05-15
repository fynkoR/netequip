package ru.ssau.netequip.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.netequip.dto.ipAddress.CreateAndUpdateIpAddress;
import ru.ssau.netequip.dto.ipAddress.ResponseIpAddressDto;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.entity.IpAddress;
import ru.ssau.netequip.exception.equipment.NotFoundEquipmentException;
import ru.ssau.netequip.exception.ipAddress.DuplicateIpAddressException;
import ru.ssau.netequip.exception.ipAddress.NotFoundIpAddressException;
import ru.ssau.netequip.exception.ipAddress.PrimaryIpAddressConflictException;
import ru.ssau.netequip.mapper.IpAddressMapper;
import ru.ssau.netequip.repository.EquipmentRepository;
import ru.ssau.netequip.repository.IpAddressRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class IpAddressService {
    private final IpAddressRepository ipAddressRepository;
    private final IpAddressMapper ipAddressMapper;
    private final EquipmentRepository equipmentRepository;
    public IpAddressService(IpAddressRepository ipAddressRepository, IpAddressMapper ipAddressMapper, EquipmentRepository equipmentRepository) {
        this.ipAddressRepository = ipAddressRepository;
        this.ipAddressMapper = ipAddressMapper;
        this.equipmentRepository = equipmentRepository;
    }
    @Transactional
    public ResponseIpAddressDto create(CreateAndUpdateIpAddress dto){
        log.info("Создание ip-address {} для оборудования {}"
                , dto.getIpAddress(), dto.getEquipmentId());
        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> {
                    log.warn("Оборудование не найдено с id: {}", dto.getEquipmentId());
                    return new NotFoundEquipmentException(dto.getEquipmentId());
                });
        if(ipAddressRepository.existsByIpAddress(dto.getIpAddress())){
            log.warn("Попытка создать дубликат ip-address: {}", dto.getIpAddress());
            throw new DuplicateIpAddressException(dto.getIpAddress());
        }
        if(Boolean.TRUE.equals(dto.getIsPrimary())){
            validatePrimaryIp(equipment, null);
        }
        IpAddress ipAddress = ipAddressMapper.toEntity(dto);
        ipAddress.setEquipment(equipment);
        if(ipAddress.getAssignedDate() == null){
            ipAddress.setAssignedDate(LocalDate.now());
        }
        IpAddress savedIpAddress = ipAddressRepository.save(ipAddress);
        log.info("Ip-address создан с id: {}", savedIpAddress.getId());
        return ipAddressMapper.toResponseDTO(savedIpAddress);
    }
    public ResponseIpAddressDto getById(Long id){
        log.info("Получение ip-address по id: {}",id);
        IpAddress ipAddress = ipAddressRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ip-address не существует с id: {}",id);
                    return new NotFoundIpAddressException(id);
                });
        return ipAddressMapper.toResponseDTO(ipAddress);
    }
    public List<ResponseIpAddressDto> getAll(){
        log.info("Получение всех ip address");
        List<IpAddress> ipAddressList = ipAddressRepository.findAll();
        log.info("Найдено ip-address: {}", ipAddressList.size());
        return ipAddressList.stream()
                .map(ipAddressMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResponseIpAddressDto update(Long id, CreateAndUpdateIpAddress dto){
        log.info("Обновление ip address по id: {}",id);
        IpAddress ipAddress = ipAddressRepository.findById(id).orElseThrow(() -> {
            log.warn("Ip address с id {} не существует",id);
            return new NotFoundIpAddressException(id);
        });
        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> {
                    log.warn("Оборудование не найдено с id: {}", dto.getEquipmentId());
                    return new NotFoundEquipmentException(dto.getEquipmentId());
                });
        if(!ipAddress.getIpAddress().equals(dto.getIpAddress())){
            if(ipAddressRepository.existsByIpAddress(dto.getIpAddress())){
                log.warn("Попытка изменить ip-address: {} на уже существующий", dto.getIpAddress());
                throw new DuplicateIpAddressException(dto.getIpAddress());
            }
        }
        if(Boolean.TRUE.equals(dto.getIsPrimary())){
            if(!equipment.getId().equals(ipAddress.getEquipment().getId()) ||
                !Boolean.TRUE.equals(ipAddress.getIsPrimary())){
                validatePrimaryIp(equipment,id);
            }
        }
        ipAddressMapper.updateEntityFromDTO(dto, ipAddress);
        ipAddress.setEquipment(equipment);

        IpAddress updIpAddress = ipAddressRepository.save(ipAddress);
        log.info("Ip-address обнвлен с id: {}", updIpAddress.getId());
        return ipAddressMapper.toResponseDTO(updIpAddress);
    }

    @Transactional
    public void delete(Long id){
        log.info("Удаление Ip-address с id: {}",id);
        IpAddress ipAddress = ipAddressRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ip-address с id: {} не найден", id);
                    return new NotFoundIpAddressException(id);
                });
        ipAddressRepository.delete(ipAddress);
        log.info("Ip-address удален с id: {}",id);
    }

    private void validatePrimaryIp(Equipment equipment, Long id){
        Optional<IpAddress> primaryIpAddress = ipAddressRepository
                .findByEquipmentAndIsPrimary(equipment,true);
        if(primaryIpAddress.isPresent()){
            if(id == null || !primaryIpAddress.get().getId().equals(id)){
                log.warn("У устройства ID {} уже есть основной ip: {}", equipment.getId(), primaryIpAddress.get().getIpAddress());
                throw new PrimaryIpAddressConflictException(equipment.getId());
            }
        }
    }
}
