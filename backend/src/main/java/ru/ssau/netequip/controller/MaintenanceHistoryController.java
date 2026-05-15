package ru.ssau.netequip.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.ssau.netequip.dto.maintenanceHistory.CreateAndUpdateMaintenanceHistoryDTO;
import ru.ssau.netequip.dto.maintenanceHistory.ResponseMaintenanceHistoryDto;
import ru.ssau.netequip.entity.User;
import ru.ssau.netequip.service.CustomUserDetailsService;
import ru.ssau.netequip.service.MaintenanceHistoryService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/histoires")
@RequiredArgsConstructor
public class MaintenanceHistoryController {
    private final MaintenanceHistoryService maintenanceHistoryService;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping
    public ResponseEntity<ResponseMaintenanceHistoryDto> addMaintenanceHistory
            (@Valid @RequestBody CreateAndUpdateMaintenanceHistoryDTO dto){
        log.info("Add maintenance history by equip: {}", dto.getEquipmentId());

        // Заполняем performedBy на основе сотрудника, привязанного к текущему пользователю
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long currentUserId = (Long) authentication.getPrincipal();
        User currentUser = userDetailsService.findById(currentUserId);

        if (currentUser.getEmployee() != null) {
            dto.setPerformedById(currentUser.getEmployee().getId());
        }

        ResponseMaintenanceHistoryDto create = maintenanceHistoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(create);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseMaintenanceHistoryDto> getMaintenanceHistory
            (@PathVariable Long id){
        log.info("Get maintenance history by id: {}", id);
        ResponseMaintenanceHistoryDto history = maintenanceHistoryService.getById(id);
        return ResponseEntity.ok(history);
    }
    @GetMapping
    public ResponseEntity<List<ResponseMaintenanceHistoryDto>> getListMaintenanceHistory(){
        log.info("Get list maintenance history");
        List<ResponseMaintenanceHistoryDto> history = maintenanceHistoryService.getAll();
        return ResponseEntity.ok(history);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ResponseMaintenanceHistoryDto> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateAndUpdateMaintenanceHistoryDTO dto
    ){
        log.info("Update maintenance history by id: {}", id);
        ResponseMaintenanceHistoryDto update = maintenanceHistoryService.update(dto, id);
        return ResponseEntity.ok(update);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMaintenanceHistory(
            @PathVariable Long id){
        log.info("Delete maintenance history by id: {}", id);
        maintenanceHistoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
