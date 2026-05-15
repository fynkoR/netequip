package ru.ssau.netequip.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.netequip.dto.maintenanceHistory.CreateAndUpdateMaintenanceHistoryDTO;
import ru.ssau.netequip.dto.maintenanceHistory.ResponseMaintenanceHistoryDto;
import ru.ssau.netequip.entity.Employee;
import ru.ssau.netequip.entity.Equipment;
import ru.ssau.netequip.entity.MaintenanceHistory;
import ru.ssau.netequip.exception.employee.NotFoundEmployeeException;
import ru.ssau.netequip.exception.equipment.NotFoundEquipmentException;
import ru.ssau.netequip.exception.maintenanceHistory.NotFoundMaintenanceHistoryException;
import ru.ssau.netequip.mapper.MaintenanceHistoryMapper;
import ru.ssau.netequip.repository.EmployeeRepository;
import ru.ssau.netequip.repository.EquipmentRepository;
import ru.ssau.netequip.repository.MaintenanceHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class MaintenanceHistoryService {
    private final MaintenanceHistoryRepository maintenanceHistoryRepository;
    private final MaintenanceHistoryMapper maintenanceHistoryMapper;
    private final EquipmentRepository equipmentRepository;
    private final EmployeeRepository employeeRepository;
    public MaintenanceHistoryService(MaintenanceHistoryRepository maintenanceHistoryRepository, MaintenanceHistoryMapper maintenanceHistoryMapper,
                              EquipmentRepository equipmentRepository, EmployeeRepository employeeRepository){
        this.maintenanceHistoryRepository = maintenanceHistoryRepository;
        this.maintenanceHistoryMapper = maintenanceHistoryMapper;
        this.equipmentRepository = equipmentRepository;
        this.employeeRepository = employeeRepository;
    }
    @Transactional
    public ResponseMaintenanceHistoryDto create(CreateAndUpdateMaintenanceHistoryDTO dto){
        log.info("Создание записи об обслуживании оборудования ID : {}",dto.getEquipmentId());

        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId()).orElseThrow(
                () -> {
                    log.warn("Оборудование с ID {} не найдено", dto.getEquipmentId());
                    return new NotFoundEquipmentException(dto.getEquipmentId());
                }
        );
        MaintenanceHistory maintenanceHistory = maintenanceHistoryMapper.toEntity(dto);
        maintenanceHistory.setEquipment(equipment);
        if(dto.getPerformedById() != null){
            Employee employee = employeeRepository.findById(dto.getPerformedById()).orElseThrow(
                    () -> {
                        log.warn("Сотрудник с ID {} не найден", dto.getPerformedById());
                        return new NotFoundEmployeeException(dto.getPerformedById());
                    }
            );
            maintenanceHistory.setPerformedBy(employee);
        }

        if(maintenanceHistory.getDate() == null){
            maintenanceHistory.setDate(LocalDateTime.now());
        }
        MaintenanceHistory saved = maintenanceHistoryRepository.save(maintenanceHistory);
        log.info("Запись успешна создана с ID: {}", saved.getId());

        return maintenanceHistoryMapper.toResponseDTO(saved);
    }

    public ResponseMaintenanceHistoryDto getById(Long id){
        log.info("Получение записи об обслуж ID : {}",id);
        MaintenanceHistory maintenanceHistory = maintenanceHistoryRepository.findById(id).orElseThrow(
                () -> {
                    log.warn("Запись об обслуживании не найдена c ID: {}", id);
                    return new NotFoundMaintenanceHistoryException(id);
                }
        );
        return maintenanceHistoryMapper.toResponseDTO(maintenanceHistory);
    }

    public List<ResponseMaintenanceHistoryDto> getAll(){
        log.info("Получение всех записей обслуживаний");
        List<MaintenanceHistory> maintenanceHistories = maintenanceHistoryRepository.findAll();
        log.info("Найдено записей об обслуживании: {}", maintenanceHistories.size());

        return maintenanceHistories.stream()
                .map(maintenanceHistoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResponseMaintenanceHistoryDto update(CreateAndUpdateMaintenanceHistoryDTO dto, Long id){
        log.info("Обновление записи об обслуживании ID : {}",id);
        MaintenanceHistory maintenanceHistory = maintenanceHistoryRepository.findById(id).orElseThrow(
                () -> {
                    log.warn("Попытка обновить запись об обслуживании c ID: {}", id);
                    return new NotFoundMaintenanceHistoryException(id);
                }
        );
        Equipment newEquipment = equipmentRepository.findById(dto.getEquipmentId()).orElseThrow(
                () -> {
                    log.warn("Оборудование с ID {} не найдено", dto.getEquipmentId());
                    return new NotFoundEquipmentException(dto.getEquipmentId());
                }
        );
        maintenanceHistoryMapper.updateEntityFromDTO(dto,maintenanceHistory);
        maintenanceHistory.setEquipment(newEquipment);

        if(dto.getPerformedById() != null){
            Employee employee = employeeRepository.findById(dto.getPerformedById()).orElseThrow(
                    () -> {
                        log.warn("Сотрудник с ID {} не найден", dto.getPerformedById());
                        return new NotFoundEmployeeException(dto.getPerformedById());
                    }
            );
        }else{
            maintenanceHistory.setEquipment(null);
        }

        MaintenanceHistory saved = maintenanceHistoryRepository.save(maintenanceHistory);
        log.info("Запись об оборудовании с ID {} обновлено", saved.getId());
        return maintenanceHistoryMapper.toResponseDTO(saved);
    }

    @Transactional
    public void delete(Long id){
        log.info("Удаление записи обслуживания с ID : {}",id);
        MaintenanceHistory maintenanceHistory = maintenanceHistoryRepository.findById(id).orElseThrow(
                () -> {
                    log.warn("Попытка удалить несуществующую запись об обслуживании c ID: {}", id);
                    return new NotFoundMaintenanceHistoryException(id);
                }
        );

        maintenanceHistoryRepository.deleteById(id);
        log.info("Запись об обслуживания с id {} удалена", id);
    }
}
