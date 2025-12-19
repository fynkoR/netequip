package com.example.netequip.controller;

import com.example.netequip.dto.equipmenttype.CreateEquipmentTypeDTO;
import com.example.netequip.dto.equipmenttype.EquipmentTypeResponseDTO;
import com.example.netequip.dto.equipmenttype.UpdateEquipmentTypeDTO;
import com.example.netequip.service.EquipmentTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST контроллер для управления типами оборудования
 * Базовый путь: /api/equipment-types
 */
@Slf4j
@RestController
@RequestMapping("/api/equipment-types")
@RequiredArgsConstructor
@Tag(name = "Equipment Types", description = "API для управления типами сетевого оборудования")
public class EquipmentTypeController {

    private final EquipmentTypeService equipmentTypeService;

    /**
     * Получение всех типов оборудования
     * GET /api/equipment-types
     */
    @GetMapping
    @Operation(summary = "Получить все типы оборудования",
            description = "Возвращает список всех доступных типов оборудования")
    public ResponseEntity<List<EquipmentTypeResponseDTO>> getAllEquipmentTypes() {
        log.debug("REST запрос на получение всех типов оборудования");
        List<EquipmentTypeResponseDTO> types = equipmentTypeService.getAll();
        return ResponseEntity.ok(types);
    }

    /**
     * Получение типа оборудования по ID
     * GET /api/equipment-types/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Получить тип оборудования по ID")
    public ResponseEntity<EquipmentTypeResponseDTO> getEquipmentTypeById(
            @Parameter(description = "ID типа оборудования")
            @PathVariable Long id) {
        log.debug("REST запрос на получение типа оборудования с ID: {}", id);
        EquipmentTypeResponseDTO type = equipmentTypeService.getById(id);
        return ResponseEntity.ok(type);
    }

    /**
     * Создание нового типа оборудования
     * POST /api/equipment-types
     */
    @PostMapping
    @Operation(summary = "Создать новый тип оборудования")
    public ResponseEntity<EquipmentTypeResponseDTO> createEquipmentType(
            @Valid @RequestBody CreateEquipmentTypeDTO dto) {
        log.info("REST запрос на создание типа оборудования: {}", dto.getTypeName());
        EquipmentTypeResponseDTO created = equipmentTypeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Обновление существующего типа оборудования
     * PUT /api/equipment-types/{id}
     */
    @PutMapping("/{id}")
    @Operation(summary = "Обновить тип оборудования")
    public ResponseEntity<EquipmentTypeResponseDTO> updateEquipmentType(
            @Parameter(description = "ID типа оборудования")
            @PathVariable Long id,
            @Valid @RequestBody UpdateEquipmentTypeDTO dto) {
        log.info("REST запрос на обновление типа оборудования с ID: {}", id);
        EquipmentTypeResponseDTO updated = equipmentTypeService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Удаление типа оборудования
     * DELETE /api/equipment-types/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить тип оборудования")
    public ResponseEntity<Void> deleteEquipmentType(
            @Parameter(description = "ID типа оборудования")
            @PathVariable Long id) {
        log.info("REST запрос на удаление типа оборудования с ID: {}", id);
        equipmentTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получение типов оборудования по производителю
     * GET /api/equipment-types/manufacturer/{manufacturer}
     */
    @GetMapping("/manufacturer/{manufacturer}")
    @Operation(summary = "Получить типы по производителю")
    public ResponseEntity<List<EquipmentTypeResponseDTO>> getByManufacturer(
            @Parameter(description = "Название производителя")
            @PathVariable String manufacturer) {
        log.debug("REST запрос на получение типов производителя: {}", manufacturer);
        List<EquipmentTypeResponseDTO> types = equipmentTypeService.getByManufacturer(manufacturer);
        return ResponseEntity.ok(types);
    }

    /**
     * Получение типов оборудования по производителю (отсортировано по модели)
     * GET /api/equipment-types/manufacturer/{manufacturer}/sorted
     */
    @GetMapping("/manufacturer/{manufacturer}/sorted")
    @Operation(summary = "Получить типы по производителю (отсортировано)")
    public ResponseEntity<List<EquipmentTypeResponseDTO>> getByManufacturerSorted(
            @Parameter(description = "Название производителя")
            @PathVariable String manufacturer) {
        log.debug("REST запрос на получение отсортированных типов производителя: {}", manufacturer);
        List<EquipmentTypeResponseDTO> types = equipmentTypeService.getByManufacturerSorted(manufacturer);
        return ResponseEntity.ok(types);
    }

    /**
     * Поиск типа оборудования по производителю и модели
     * GET /api/equipment-types/search?manufacturer=...&model=...
     */
    @GetMapping("/search")
    @Operation(summary = "Найти тип по производителю и модели")
    public ResponseEntity<EquipmentTypeResponseDTO> getByManufacturerAndModel(
            @Parameter(description = "Название производителя")
            @RequestParam String manufacturer,
            @Parameter(description = "Модель")
            @RequestParam String model) {
        log.debug("REST запрос на поиск типа: производитель={}, модель={}", manufacturer, model);
        EquipmentTypeResponseDTO type = equipmentTypeService.getByManufacturerAndModel(manufacturer, model);
        return ResponseEntity.ok(type);
    }

    /**
     * Поиск типа по названию
     * GET /api/equipment-types/by-name/{typeName}
     */
    @GetMapping("/by-name/{typeName}")
    @Operation(summary = "Найти тип по названию")
    public ResponseEntity<EquipmentTypeResponseDTO> getByTypeName(
            @Parameter(description = "Название типа")
            @PathVariable String typeName) {
        log.debug("REST запрос на поиск типа по названию: {}", typeName);
        EquipmentTypeResponseDTO type = equipmentTypeService.getByTypeName(typeName);
        return ResponseEntity.ok(type);
    }

    /**
     * Проверка существования типа по названию
     * GET /api/equipment-types/exists?typeName=...
     */
    @GetMapping("/exists")
    @Operation(summary = "Проверить существование типа по названию")
    public ResponseEntity<Boolean> existsByTypeName(
            @Parameter(description = "Название типа")
            @RequestParam String typeName) {
        log.debug("REST запрос на проверку существования типа: {}", typeName);
        boolean exists = equipmentTypeService.existsByTypeName(typeName);
        return ResponseEntity.ok(exists);
    }
}
