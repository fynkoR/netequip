package ru.ssau.netequip.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.netequip.dto.equipmentType.CreateAndUpdateEquipmentTypeDto;
import ru.ssau.netequip.dto.equipmentType.ResponseEquipmentTypeDto;
import ru.ssau.netequip.entity.EquipmentType;
import ru.ssau.netequip.exception.equipmentType.DuplicateEquipmentTypeNameException;
import ru.ssau.netequip.exception.equipmentType.NotFoundEquipmentTypeException;
import ru.ssau.netequip.mapper.EquipmentTypeMapper;
import ru.ssau.netequip.repository.EquipmentTypeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional(readOnly = true)
public class EquipmentTypeService {
    private final EquipmentTypeRepository equipmentTypeRepository;
    private final EquipmentTypeMapper equipmentTypeMapper;
    public EquipmentTypeService(EquipmentTypeRepository equipmentTypeRepository, EquipmentTypeMapper equipmentTypeMapper) {
        this.equipmentTypeRepository = equipmentTypeRepository;
        this.equipmentTypeMapper = equipmentTypeMapper;
    }
    @Transactional
    public ResponseEquipmentTypeDto create(CreateAndUpdateEquipmentTypeDto dto){
        log.info("Создание типа оборудования: {}", dto.getTypeName());
        if(equipmentTypeRepository.existsByTypeName(dto.getTypeName())){
            log.warn("Попытка создать тип оборудования уже с существующем именем: {}", dto.getTypeName());
            throw new DuplicateEquipmentTypeNameException(dto.getTypeName());
        }
        EquipmentType equipmentType = equipmentTypeMapper.toEntity(dto);

        EquipmentType savedEquipmentType = equipmentTypeRepository.save(equipmentType);
        log.info("Создан тип оборудования: {}", dto.getTypeName());
        return equipmentTypeMapper.toResponseDTO(savedEquipmentType);
    }
    public ResponseEquipmentTypeDto getById(Long id){
        log.info("Получение id типа оборудования: {}", id);
        EquipmentType equipmentType = equipmentTypeRepository.findById(id)
                .orElseThrow(() ->{
                    log.warn("Тип оборудования не существует с id {}", id);
                    return new NotFoundEquipmentTypeException(id);
                });
        return equipmentTypeMapper.toResponseDTO(equipmentType);
    }
    public List<ResponseEquipmentTypeDto> getAll(){
        log.info("Получение списка всех типов оборудования");
        List<EquipmentType> equipmentTypes = equipmentTypeRepository.findAll();
        log.info("Найдено типов оборудования: {}", equipmentTypes.size());
        return equipmentTypes.stream()
                .map(equipmentTypeMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    @Transactional
    public ResponseEquipmentTypeDto update(Long id, CreateAndUpdateEquipmentTypeDto dto){
        log.info("Обновление типа сотрудника с id: {}", id);
        EquipmentType equipmentType = equipmentTypeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Тип оборудования не существует с id {}", id);
                    return new NotFoundEquipmentTypeException(id);
                });
        if(!equipmentType.getTypeName().equals(dto.getTypeName()) &&
        equipmentTypeRepository.existsByTypeName(dto.getTypeName())){
            log.warn("Попытка изменить название на уже существуещее: {}", dto.getTypeName());
            throw new DuplicateEquipmentTypeNameException(dto.getTypeName());
        }
        equipmentTypeMapper.updateEntityFromDTO(dto, equipmentType);
        EquipmentType updatedEquipmentType = equipmentTypeRepository.save(equipmentType);
        log.info("Тип оборудования с id: {} обновлен", updatedEquipmentType.getId());
        return equipmentTypeMapper.toResponseDTO(updatedEquipmentType);
    }
    @Transactional
    public void delete(Long id){
        log.info("Удаление типа оборудования с id: {}", id);
        if(!equipmentTypeRepository.existsById(id)){
            log.warn("Тип оборудования не существует с id: {}",id);
            throw new NotFoundEquipmentTypeException(id);
        }

        // проверка на то , что данный тип используется в оборудовании

        equipmentTypeRepository.deleteById(id);
        log.info("Тип оборудования успешно удален с id: {}", id);
    }
}
