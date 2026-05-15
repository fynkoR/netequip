package ru.ssau.netequip.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.netequip.dto.equipment.CreateEquipmentDto;
import ru.ssau.netequip.dto.equipment.ResponseEquipmentDto;
import ru.ssau.netequip.dto.equipment.UpdateEquipmentDto;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.entity.EquipmentType;
import ru.ssau.netequip.exception.employee.NotFoundEmployeeException;
import ru.ssau.netequip.exception.equipment.DuplicateEquipmentNameException;
import ru.ssau.netequip.exception.equipment.NotFoundEquipmentException;
import ru.ssau.netequip.exception.equipmentType.NotFoundEquipmentTypeException;
import ru.ssau.netequip.mapper.EquipmentMapper;
import ru.ssau.netequip.repository.EmployeeRepository;
import ru.ssau.netequip.repository.EquipmentRepository;
import ru.ssau.netequip.repository.EquipmentTypeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
public class EquipmentService {
    private final EquipmentRepository equipmentRepository;
    private final EquipmentMapper equipmentMapper;
    private final EmployeeRepository employeeRepository;
    private final EquipmentTypeRepository equipmentTypeRepository;
    public EquipmentService(EquipmentRepository equipmentRepository, EquipmentMapper equipmentMapper, EmployeeRepository employeeRepository, EquipmentTypeRepository equipmentTypeRepository) {
        this.equipmentRepository = equipmentRepository;
        this.equipmentMapper = equipmentMapper;
        this.employeeRepository = employeeRepository;
        this.equipmentTypeRepository = equipmentTypeRepository;
    }
    @Transactional
    public ResponseEquipmentDto create(CreateEquipmentDto dto) {
        log.info("Создание оборудования: {}",dto.getName());
        EquipmentType type = equipmentTypeRepository.findById(dto.getTypeId())
                .orElseThrow(() -> {
                    log.warn("Тип оборудования не найден с id: {}",dto.getTypeId());
                    return new NotFoundEquipmentTypeException(dto.getTypeId());
                });

        validateSerialAndMac(dto.getTypeId(), dto.getSerialNumber(), dto.getMacAddress());

        Equipment equipment =equipmentMapper.toEntity(dto);
        equipment.setType(type);

        if(dto.getEmployeeId() != null){
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> {
                        log.warn("Сотрудник не найден с ID: {}",dto.getEmployeeId());
                        return new NotFoundEmployeeException(dto.getEmployeeId());
                    });
            equipment.setEmployee(employee);
        }

        if(dto.getDateAdded() == null){
            equipment.setDateAdded(LocalDate.now());
        }
        Equipment saved = equipmentRepository.save(equipment);
        log.info("Оборудование: {} создано",saved.getName());
        return equipmentMapper.toResponseDTO(saved);
    }

    public ResponseEquipmentDto getById(Long id) {
        log.info("Получение id оборудования: {}",id);
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Оборудование с данным id не найдено: {}", id);
                    return new NotFoundEquipmentException(id);
                });
        return equipmentMapper.toResponseDTO(equipment);
    }

    public List<ResponseEquipmentDto> getAll(){
        log.info("Получение всех оборудований");
        List<Equipment> equipments = equipmentRepository.findAll();
        log.info("Найдено оборудований: {}",equipments.size());
        return equipments.stream()
                .map(equipmentMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public ResponseEquipmentDto update(Long id, UpdateEquipmentDto dto) {
        log.info("Обновление оборудование с id: {}",id);
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка обновить несуществующее оборудование с id: {}",id);
                    return new NotFoundEquipmentException(id);
                });
        EquipmentType type = equipmentTypeRepository.findById(dto.getTypeId())
                .orElseThrow(() -> {
                    log.warn("Тип оборудования не найден с id: {}",dto.getTypeId());
                    return new NotFoundEquipmentTypeException(dto.getTypeId());
                });
        validateSerialAndMac(id, dto.getSerialNumber(), dto.getMacAddress());

        equipmentMapper.updateEntityFromDTO(dto,equipment);
        equipment.setType(type);
        if(dto.getEmployeeId() != null){
            Employee employee = employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> {
                        log.warn("Сотрудник не найден с данным id: {}", dto.getEmployeeId());
                        return new NotFoundEmployeeException(dto.getEmployeeId());
                    });
            equipment.setEmployee(employee);
        }else{
            equipment.setEmployee(null);
        }
        equipment.setDateUpdated(LocalDate.now());

        // проверить на обновление status

        Equipment upd = equipmentRepository.save(equipment);
        log.info("Обородувание обновлено с id: {}",id);
        return equipmentMapper.toResponseDTO(upd);
    }
    @Transactional
    public void delete(Long id){
        log.info("Удаление оборудования с id: {}", id);
        if(!equipmentRepository.existsById(id)){
            log.warn("Оборудование не найдено с id: {}",id);
            throw new NotFoundEquipmentException(id);
        }

        // добавить каскадное удаление , проверка на связанные данных

        equipmentRepository.deleteById(id);
        log.info("Оборудование удалено с id: {}", id);
    }

    private void validateSerialAndMac(Long id, String serial, String mac){
        if(serial != null && !serial.isBlank()){
            equipmentRepository.findBySerialNumber(serial).ifPresent(existing -> {
                if (id == null || !existing.getId().equals(id)){
                    log.warn("Попытка использовать занятой серийный номер {}", serial);
                    throw new DuplicateEquipmentNameException("серийным номер", serial);
                }
            });
        }

        if(mac != null && !mac.isBlank()){
            equipmentRepository.findByMacAddress(mac).ifPresent(existing -> {
                if (id == null || !existing.getId().equals(id)){
                    log.warn("Попытка использовать занятый мак-адрес {}", mac);
                    throw new DuplicateEquipmentNameException("мак адресом", mac);
                }
            });
        }
    }
}
