package ru.ssau.netequip.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ssau.netequip.dto.devicePort.CreateAndUpdateDevicePortDto;
import ru.ssau.netequip.dto.devicePort.ResponseDevicePortDto;
import ru.ssau.netequip.service.DevicePortService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ports")
public class DevicePortController {
    private final DevicePortService devicePortService;
    public DevicePortController(DevicePortService devicePortService) {
        this.devicePortService = devicePortService;
    }
    @PostMapping
    public ResponseEntity<ResponseDevicePortDto> addDevicePort(
            @Valid @RequestBody CreateAndUpdateDevicePortDto dto) {
        log.info("Create device port: {}", dto.getPortNumber());
        ResponseDevicePortDto create = devicePortService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(create);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDevicePortDto> getDevicePortById(
            @PathVariable Long id) {
       log.info("Get device port: {}", id);
       ResponseDevicePortDto port = devicePortService.getById(id);
       return ResponseEntity.ok(port);
    }
    @GetMapping
    public ResponseEntity<List<ResponseDevicePortDto>> getAllDevicePorts() {
        log.info("Get all device ports");
        List<ResponseDevicePortDto> ports = devicePortService.getAll();
        return ResponseEntity.ok(ports);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDevicePortDto> updateDevicePort(
            @PathVariable Long id,
            @Valid @RequestBody CreateAndUpdateDevicePortDto dto
    ){
        log.info("Update device port: {}", dto.getPortNumber());
        ResponseDevicePortDto port = devicePortService.update(id, dto);
        return ResponseEntity.ok(port);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevicePort(
            @PathVariable Long id
    ){
        log.info("Delete device port: {}", id);
        devicePortService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
