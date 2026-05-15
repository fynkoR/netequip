package ru.ssau.netequip.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ssau.netequip.dto.equipment.CreateEquipmentDto;
import ru.ssau.netequip.dto.equipment.ResponseEquipmentDto;
import ru.ssau.netequip.dto.equipment.UpdateEquipmentDto;
import ru.ssau.netequip.service.EquipmentService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/equipments")
public class EquipmentController {
    private final EquipmentService equipmentService;
    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }
    @PostMapping
    public ResponseEntity<ResponseEquipmentDto> addEquipment(
            @Valid @RequestBody CreateEquipmentDto dto) {
        log.info("Add equipment {}", dto.getName());
        ResponseEquipmentDto create = equipmentService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(create);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseEquipmentDto> getEquipmentById(
            @RequestParam Long id
    ){
        log.info("Get equipment {}", id);
        ResponseEquipmentDto equipment = equipmentService.getById(id);
        return ResponseEntity.ok(equipment);
    }
    @GetMapping
    public ResponseEntity<List<ResponseEquipmentDto>> getAllEquipment() {
        log.info("Get all equipment");
        List<ResponseEquipmentDto> equipments = equipmentService.getAll();
        return ResponseEntity.ok(equipments);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ResponseEquipmentDto> updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEquipmentDto dto
    ){
        log.info("Update equipment {}", dto.getName());
        ResponseEquipmentDto update = equipmentService.update(id, dto);
        return ResponseEntity.ok(update);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(
            @PathVariable Long id
    ){
        log.info("Delete equipment {}", id);
        equipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
