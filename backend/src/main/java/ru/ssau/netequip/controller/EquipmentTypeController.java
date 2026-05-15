package ru.ssau.netequip.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ssau.netequip.dto.equipmentType.CreateAndUpdateEquipmentTypeDto;
import ru.ssau.netequip.dto.equipmentType.ResponseEquipmentTypeDto;
import ru.ssau.netequip.entity.EquipmentType;
import ru.ssau.netequip.service.EquipmentTypeService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/types")
public class EquipmentTypeController {
    private final EquipmentTypeService equipmentTypeService;
    public EquipmentTypeController(EquipmentTypeService equipmentTypeService) {
        this.equipmentTypeService = equipmentTypeService;
    }
    @PostMapping()
    public ResponseEntity<ResponseEquipmentTypeDto> addEquipmentType(
            @Valid @RequestBody CreateAndUpdateEquipmentTypeDto dto) {
        log.info("addEquipmentType: {}", dto.getTypeName());
        ResponseEquipmentTypeDto create = equipmentTypeService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(create);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseEquipmentTypeDto> getEquipmentType(
            @PathVariable Long id
    ){
        log.info("getEquipmentType: {}", id);
        ResponseEquipmentTypeDto type = equipmentTypeService.getById(id);
        return ResponseEntity.ok(type);
    }
    @GetMapping
    public ResponseEntity<List<ResponseEquipmentTypeDto>> getAllEquipmentTypes() {
        log.info("getAllEquipmentTypes");
        List<ResponseEquipmentTypeDto> types = equipmentTypeService.getAll();
        return ResponseEntity.ok(types);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ResponseEquipmentTypeDto> updateEquipmentType(
            @PathVariable Long id,
            @Valid @RequestBody CreateAndUpdateEquipmentTypeDto dto
    ){
        log.info("updateEquipmentType: {}", dto.getTypeName());
        ResponseEquipmentTypeDto upd = equipmentTypeService.update(id,dto);
        return ResponseEntity.ok(upd);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipmentType(
            @PathVariable Long id
    ){
        log.info("deleteEquipmentType: {}", id);
        equipmentTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
