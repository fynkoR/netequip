package ru.ssau.netequip.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ssau.netequip.dto.ipAddress.CreateAndUpdateIpAddress;
import ru.ssau.netequip.dto.ipAddress.ResponseIpAddressDto;
import ru.ssau.netequip.entity.IpAddress;
import ru.ssau.netequip.service.IpAddressService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ip-addresses")
public class IpAddressController {
    private final IpAddressService ipAddressService;
    public IpAddressController(IpAddressService ipAddressService) {
        this.ipAddressService = ipAddressService;
    }
    @PostMapping
    public ResponseEntity<ResponseIpAddressDto> addIpAddress(
            @Valid @RequestBody CreateAndUpdateIpAddress ipAddress) {
        log.info("Create ip address: {}", ipAddress.getIpAddress());
        ResponseIpAddressDto create = ipAddressService.create(ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(create);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ResponseIpAddressDto> getIpAddressById(
            @PathVariable Long id
    ) {
        log.info("Get ip address by id: {}", id);
        ResponseIpAddressDto ip = ipAddressService.getById(id);
        return ResponseEntity.ok(ip);
    }
    @GetMapping
    public ResponseEntity<List<ResponseIpAddressDto>> getAllIpAddress() {
        log.info("Get all ip address");
        List<ResponseIpAddressDto> ip = ipAddressService.getAll();
        return ResponseEntity.ok(ip);
    }
    @PutMapping("/{id}")
    public ResponseEntity<ResponseIpAddressDto> updateIpAddress(
            @PathVariable Long id,
            @Valid @RequestBody CreateAndUpdateIpAddress ipAddress
    ){
        log.info("Update ip address: {}", ipAddress.getIpAddress());
        ResponseIpAddressDto update = ipAddressService.update(id, ipAddress);
        return ResponseEntity.ok(update);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIpAddress(
            @PathVariable Long id
    ){
        log.info("Delete ip address: {}", id);
        ipAddressService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
