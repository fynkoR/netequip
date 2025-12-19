package com.example.netequip.service;

import com.example.netequip.dto.equipmenttype.CreateEquipmentTypeDTO;
import com.example.netequip.dto.equipmenttype.EquipmentTypeResponseDTO;
import com.example.netequip.dto.equipmenttype.UpdateEquipmentTypeDTO;
import com.example.netequip.entity.EquipmentType;
import com.example.netequip.exception.equiptype.DuplicateEquipmentTypeException;
import com.example.netequip.exception.equiptype.EquipmentTypeNotFoundException;
import com.example.netequip.mapper.EquipmentTypeMapper;
import com.example.netequip.repository.EquipmentTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления типами оборудования
 * Содержит бизнес-логику для CRUD операций и дополнительные методы поиска
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EquipmentTypeService {

    private final EquipmentTypeRepository repository;
    private final EquipmentTypeMapper mapper;

    /**
     * Создание нового типа оборудования
     *
     * @param dto данные для создания
     * @return созданный тип оборудования
     * @throws DuplicateEquipmentTypeException если тип с таким названием уже существует
     */
    @Transactional
    public EquipmentTypeResponseDTO create(CreateEquipmentTypeDTO dto) {
        log.info("Создание нового типа оборудования: {}", dto.getTypeName());

        // Проверка на существование типа с таким названием
        if (repository.existsByTypeName(dto.getTypeName())) {
            log.warn("Попытка создать дубликат типа оборудования: {}", dto.getTypeName());
            throw new DuplicateEquipmentTypeException(dto.getTypeName());
        }

        // Конвертация DTO в Entity
        EquipmentType entity = mapper.toEntity(dto);

        // Сохранение в базу данных
        EquipmentType savedEntity = repository.save(entity);
        log.info("Тип оборудования успешно создан с ID: {}", savedEntity.getId());

        // Возврат Response DTO
        return mapper.toResponseDTO(savedEntity);
    }

    /**
     * Получение типа оборудования по ID
     *
     * @param id идентификатор типа
     * @return найденный тип оборудования
     * @throws EquipmentTypeNotFoundException если тип не найден
     */
    public EquipmentTypeResponseDTO getById(Long id) {
        log.debug("Получение типа оборудования по ID: {}", id);

        EquipmentType entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Тип оборудования с ID {} не найден", id);
                    return new EquipmentTypeNotFoundException(id);
                });

        return mapper.toResponseDTO(entity);
    }

    /**
     * Получение всех типов оборудования
     *
     * @return список всех типов оборудования
     */
    public List<EquipmentTypeResponseDTO> getAll() {
        log.debug("Получение всех типов оборудования");

        List<EquipmentType> entities = repository.findAll();
        log.info("Найдено типов оборудования: {}", entities.size());

        return entities.stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Обновление существующего типа оборудования
     *
     * @param id идентификатор типа для обновления
     * @param dto новые данные
     * @return обновленный тип оборудования
     * @throws EquipmentTypeNotFoundException если тип не найден
     */
    @Transactional
    public EquipmentTypeResponseDTO update(Long id, UpdateEquipmentTypeDTO dto) {
        log.info("Обновление типа оборудования с ID: {}", id);

        // Поиск существующего типа
        EquipmentType existingEntity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка обновить несуществующий тип оборудования с ID: {}", id);
                    return new EquipmentTypeNotFoundException(id);
                });

        // Проверка на дубликат названия (если название изменилось)
        if (!existingEntity.getTypeName().equals(dto.getTypeName()) &&
                repository.existsByTypeName(dto.getTypeName())) {
            log.warn("Попытка изменить название на уже существующее: {}", dto.getTypeName());
            throw new DuplicateEquipmentTypeException(dto.getTypeName());
        }

        // Обновление полей существующего Entity
        mapper.updateEntityFromDTO(dto, existingEntity);

        // Сохранение изменений
        EquipmentType updatedEntity = repository.save(existingEntity);
        log.info("Тип оборудования с ID {} успешно обновлен", id);

        return mapper.toResponseDTO(updatedEntity);
    }

    /**
     * Удаление типа оборудования по ID
     *
     * @param id идентификатор типа для удаления
     * @throws EquipmentTypeNotFoundException если тип не найден
     */
    @Transactional
    public void delete(Long id) {
        log.info("Удаление типа оборудования с ID: {}", id);

        // Проверка существования
        if (!repository.existsById(id)) {
            log.warn("Попытка удалить несуществующий тип оборудования с ID: {}", id);
            throw new EquipmentTypeNotFoundException(id);
        }

        // TODO: Добавить проверку использования типа в оборудовании
        // if (equipmentRepository.existsByTypeId(id)) {
        //     throw new EquipmentTypeInUseException(id);
        // }

        repository.deleteById(id);
        log.info("Тип оборудования с ID {} успешно удален", id);
    }

    /**
     * Поиск типов оборудования по производителю
     *
     * @param manufacturer название производителя
     * @return список типов оборудования данного производителя
     */
    public List<EquipmentTypeResponseDTO> getByManufacturer(String manufacturer) {
        log.debug("Поиск типов оборудования производителя: {}", manufacturer);

        List<EquipmentType> entities = repository.findByManufacturer(manufacturer);
        log.info("Найдено типов оборудования производителя {}: {}", manufacturer, entities.size());

        return entities.stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Поиск типа оборудования по производителю и модели
     *
     * @param manufacturer название производителя
     * @param model модель
     * @return найденный тип оборудования
     * @throws EquipmentTypeNotFoundException если тип не найден
     */
    public EquipmentTypeResponseDTO getByManufacturerAndModel(String manufacturer, String model) {
        log.debug("Поиск типа оборудования: производитель={}, модель={}", manufacturer, model);

        EquipmentType entity = repository.findByManufacturerAndModel(manufacturer, model)
                .orElseThrow(() -> {
                    log.warn("Тип оборудования {}:{} не найден", manufacturer, model);
                    return new EquipmentTypeNotFoundException(manufacturer, model);
                });

        return mapper.toResponseDTO(entity);
    }

    /**
     * Получение типов оборудования по производителю с сортировкой по модели
     *
     * @param manufacturer название производителя
     * @return отсортированный список типов оборудования
     */
    public List<EquipmentTypeResponseDTO> getByManufacturerSorted(String manufacturer) {
        log.debug("Получение отсортированных типов оборудования производителя: {}", manufacturer);

        List<EquipmentType> entities = repository.findByManufacturerOrderByModelAsc(manufacturer);
        log.info("Найдено и отсортировано типов оборудования производителя {}: {}", manufacturer, entities.size());

        return entities.stream()
                .map(mapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Поиск типа оборудования по названию
     *
     * @param typeName название типа
     * @return найденный тип оборудования
     * @throws EquipmentTypeNotFoundException если тип не найден
     */
    public EquipmentTypeResponseDTO getByTypeName(String typeName) {
        log.debug("Поиск типа оборудования по названию: {}", typeName);

        EquipmentType entity = repository.findByTypeName(typeName)
                .orElseThrow(() -> {
                    log.warn("Тип оборудования с названием {} не найден", typeName);
                    return new EquipmentTypeNotFoundException("Тип оборудования '" + typeName + "' не найден");
                });

        return mapper.toResponseDTO(entity);
    }

    /**
     * Проверка существования типа оборудования по ID
     *
     * @param id идентификатор типа
     * @return true если существует, false иначе
     */
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    /**
     * Проверка существования типа оборудования по названию
     *
     * @param typeName название типа
     * @return true если существует, false иначе
     */
    public boolean existsByTypeName(String typeName) {
        return repository.existsByTypeName(typeName);
    }
}
