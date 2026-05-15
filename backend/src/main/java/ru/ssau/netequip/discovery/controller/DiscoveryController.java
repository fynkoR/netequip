package ru.ssau.netequip.discovery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ssau.netequip.discovery.dto.*;
import ru.ssau.netequip.discovery.service.CidrUtils;
import ru.ssau.netequip.discovery.service.DiscoveryImportService;
import ru.ssau.netequip.discovery.service.DiscoveryService;
import ru.ssau.netequip.discovery.service.SnmpQueryService;

import java.util.List;

@RestController
@RequestMapping("/api/discovery")
@RequiredArgsConstructor
public class DiscoveryController {

    private final SnmpQueryService snmpQueryService;
    private final DiscoveryService discoveryService;
    private final DiscoveryImportService discoveryImportService;

    @GetMapping("/ping")
    public String ping() {
        return "Discovery module is active";
    }

    // ===== Главный endpoint =====

    /**
     * Сканирование подсети через SNMP.
     * Пример тела запроса:
     * {
     *   "cidr": "127.0.0.0/29",
     *   "port": 1161,
     *   "community": "{ip}/public"
     * }
     */
    @PostMapping("/scan")
    public ResponseEntity<ScanResultDto> scan(@RequestBody ScanRequestDto request) {
        ScanResultDto result = discoveryService.scan(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import")
    public ResponseEntity<ImportResultDto> importDevices(@RequestBody ImportRequestDto request) {
        ImportResultDto result = discoveryImportService.importDevices(request);
        return ResponseEntity.ok(result);
    }

    // ===== Тестовые/отладочные endpoint-ы (можно будет убрать перед сдачей) =====

    @GetMapping("/test-query")
    public ResponseEntity<SnmpSystemInfo> testQuery(
            @RequestParam String ip,
            @RequestParam(defaultValue = "161") int port,
            @RequestParam(defaultValue = "public") String community
    ) {
        return snmpQueryService.querySystem(ip, port, community)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/test-ports")
    public ResponseEntity<List<SnmpPortInfo>> testPorts(
            @RequestParam String ip,
            @RequestParam(defaultValue = "161") int port,
            @RequestParam(defaultValue = "public") String community
    ) {
        return ResponseEntity.ok(snmpQueryService.queryInterfaces(ip, port, community));
    }

    @GetMapping("/test-ips")
    public ResponseEntity<List<SnmpIpInfo>> testIps(
            @RequestParam String ip,
            @RequestParam(defaultValue = "161") int port,
            @RequestParam(defaultValue = "public") String community
    ) {
        return ResponseEntity.ok(snmpQueryService.queryIpAddresses(ip, port, community));
    }

    @GetMapping("/test-full")
    public ResponseEntity<SnmpDeviceInfo> testFull(
            @RequestParam String ip,
            @RequestParam(defaultValue = "161") int port,
            @RequestParam(defaultValue = "public") String community
    ) {
        return snmpQueryService.queryFullDevice(ip, port, community)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/test-cidr")
    public ResponseEntity<List<String>> testCidr(@RequestParam String cidr) {
        try {
            return ResponseEntity.ok(CidrUtils.expand(cidr));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(List.of("Error: " + e.getMessage()));
        }
    }
}