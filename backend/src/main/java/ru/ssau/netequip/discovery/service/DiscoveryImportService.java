package ru.ssau.netequip.discovery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ssau.netequip.discovery.dto.ImportRequestDto;
import ru.ssau.netequip.discovery.dto.ImportResultDto;
import ru.ssau.netequip.discovery.dto.SnmpDeviceInfo;
import ru.ssau.netequip.discovery.dto.SnmpIpInfo;
import ru.ssau.netequip.discovery.dto.SnmpPortInfo;
import ru.ssau.netequip.discovery.dto.SnmpSystemInfo;
import ru.ssau.netequip.dto.devicePort.CreateAndUpdateDevicePortDto;
import ru.ssau.netequip.dto.equipment.CreateEquipmentDto;
import ru.ssau.netequip.dto.equipment.ResponseEquipmentDto;
import ru.ssau.netequip.dto.ipAddress.CreateAndUpdateIpAddress;
import ru.ssau.netequip.enums.StatusEquipment;
import ru.ssau.netequip.enums.StatusPort;
import ru.ssau.netequip.repository.EquipmentRepository;
import ru.ssau.netequip.service.DevicePortService;
import ru.ssau.netequip.service.EquipmentService;
import ru.ssau.netequip.service.IpAddressService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DiscoveryImportService {

    private final EquipmentService equipmentService;
    private final DevicePortService devicePortService;
    private final IpAddressService ipAddressService;
    private final EquipmentRepository equipmentRepository;

    /**
     * Импортирует список обнаруженных устройств в БД.
     * Каждое устройство обрабатывается в отдельной транзакции,
     * чтобы ошибка на одном не блокировала остальные.
     */
    public ImportResultDto importDevices(ImportRequestDto request) {
        List<ImportRequestDto.ImportDeviceItem> items = request.getDevices();
        if (items == null || items.isEmpty()) {
            return ImportResultDto.builder()
                    .requestedCount(0)
                    .createdCount(0)
                    .skippedCount(0)
                    .failedCount(0)
                    .reports(new ArrayList<>())
                    .build();
        }

        log.info("Starting import of {} devices", items.size());

        List<ImportResultDto.ImportDeviceReport> reports = new ArrayList<>();
        int created = 0, skipped = 0, failed = 0;

        for (ImportRequestDto.ImportDeviceItem item : items) {
            ImportResultDto.ImportDeviceReport report = importOne(item);
            reports.add(report);
            switch (report.getStatus()) {
                case CREATED -> created++;
                case SKIPPED -> skipped++;
                case FAILED  -> failed++;
            }
        }

        log.info("Import complete: created={}, skipped={}, failed={}", created, skipped, failed);

        return ImportResultDto.builder()
                .requestedCount(items.size())
                .createdCount(created)
                .skippedCount(skipped)
                .failedCount(failed)
                .reports(reports)
                .build();
    }

    /**
     * Импорт одного устройства в собственной транзакции.
     * Так ошибка одного устройства не откатывает остальные.
     */
    @Transactional
    public ImportResultDto.ImportDeviceReport importOne(ImportRequestDto.ImportDeviceItem item) {
        SnmpDeviceInfo snmp = item.getSnmpData();
        SnmpSystemInfo system = snmp.getSystem();
        String ip = system.getIpAddress();
        String deviceName = system.getName();

        // Базовая валидация
        if (item.getTypeId() == null) {
            return skipped(ip, deviceName, "Не выбран тип оборудования");
        }
        if (deviceName == null || deviceName.isBlank()) {
            return skipped(ip, null, "Имя устройства (sysName) отсутствует");
        }

        // Проверка на дубликат по имени
        if (equipmentRepository.findByName(deviceName).isPresent()) {
            return skipped(ip, deviceName, "Устройство с таким именем уже существует");
        }

        try {
            // 1. Создаём само оборудование
            ResponseEquipmentDto created = createEquipment(item, snmp, system);

            // 2. Создаём порты
            int portsCreated = createPorts(created.getId(), snmp.getPorts());

            // 3. Создаём IP-адреса
            int ipsCreated = createIpAddresses(created.getId(), snmp.getIpAddresses());

            log.info("Imported {}: id={}, ports={}, ips={}",
                    deviceName, created.getId(), portsCreated, ipsCreated);

            return ImportResultDto.ImportDeviceReport.builder()
                    .ipAddress(ip)
                    .deviceName(deviceName)
                    .status(ImportResultDto.ImportStatus.CREATED)
                    .equipmentId(created.getId())
                    .portsCreated(portsCreated)
                    .ipsCreated(ipsCreated)
                    .message("Successfully imported")
                    .build();

        } catch (Exception e) {
            log.warn("Failed to import device {}: {}", deviceName, e.getMessage());
            return ImportResultDto.ImportDeviceReport.builder()
                    .ipAddress(ip)
                    .deviceName(deviceName)
                    .status(ImportResultDto.ImportStatus.FAILED)
                    .message("Ошибка импорта: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Создание Equipment с заполнением из SNMP-данных.
     */
    private ResponseEquipmentDto createEquipment(
            ImportRequestDto.ImportDeviceItem item,
            SnmpDeviceInfo snmp,
            SnmpSystemInfo system) {

        CreateEquipmentDto dto = new CreateEquipmentDto();
        dto.setTypeId(item.getTypeId());
        dto.setName(system.getName());

        // MAC берём с первого "значимого" порта (Ethernet, не виртуальный)
        dto.setMacAddress(pickPrimaryMac(snmp.getPorts()));

        // IP — primary из списка (или первый, если primary не помечен)
        dto.setIpAddress(pickPrimaryIp(snmp.getIpAddresses()));

        dto.setAddress(system.getLocation());

        // Серийный номер из SNMP стандартно не отдаётся — оставляем null
        // Администратор может заполнить позже

        dto.setStatus(StatusEquipment.ACTIVE);
        dto.setDateAdded(LocalDate.now());

        return equipmentService.create(dto);
    }

    /**
     * Создание портов устройства.
     * Возвращает количество успешно созданных портов.
     */
    private int createPorts(Long equipmentId, List<SnmpPortInfo> ports) {
        if (ports == null || ports.isEmpty()) return 0;

        int count = 0;
        for (SnmpPortInfo port : ports) {
            try {
                CreateAndUpdateDevicePortDto dto = new CreateAndUpdateDevicePortDto();
                dto.setEquipmentId(equipmentId);
                dto.setPortNumber(port.getIfIndex());
                dto.setPortType(port.getDescription());
                dto.setStatus(mapPortStatus(port.getAdminStatus(), port.getOperStatus()));
                dto.setSpeed(formatSpeed(port.getSpeed()));
                dto.setDescription(port.getDescription());
                // НЕ заполняем connectedToEquipmentId / connectedToPortId —
                // соединения проставляет администратор вручную

                devicePortService.create(dto);
                count++;
            } catch (Exception e) {
                log.warn("Skipping port ifIndex={} for equipment {}: {}",
                        port.getIfIndex(), equipmentId, e.getMessage());
            }
        }
        return count;
    }

    /**
     * Создание IP-адресов устройства.
     */
    private int createIpAddresses(Long equipmentId, List<SnmpIpInfo> ips) {
        if (ips == null || ips.isEmpty()) return 0;

        int count = 0;
        boolean primaryAssigned = false;

        for (SnmpIpInfo ip : ips) {
            try {
                CreateAndUpdateIpAddress dto = new CreateAndUpdateIpAddress();
                dto.setEquipmentId(equipmentId);
                dto.setIpAddress(ip.getIpAddress());
                dto.setSubnetMask(ip.getSubnetMask());
                // Шлюз через SNMP не получаем в этой версии
                dto.setNetworkType(detectNetworkType(ip.getIpAddress()));
                dto.setIsPrimary(!primaryAssigned);
                dto.setAssignedDate(LocalDate.now());

                ipAddressService.create(dto);
                primaryAssigned = true;
                count++;
            } catch (Exception e) {
                log.warn("Skipping IP {} for equipment {}: {}",
                        ip.getIpAddress(), equipmentId, e.getMessage());
            }
        }
        return count;
    }

    // ===== Утилитарные методы =====

    /**
     * Маппинг SNMP-статусов на ваш enum StatusPort.
     * adminStatus: 1=up, 2=down. operStatus: 1=up, 2=down.
     */
    private StatusPort mapPortStatus(Integer adminStatus, Integer operStatus) {
        if (adminStatus == null) return StatusPort.NOT_CONNECTED;
        if (adminStatus == 2) return StatusPort.DISABLE;
        if (operStatus != null && operStatus == 1) return StatusPort.CONNECTED;
        return StatusPort.NOT_CONNECTED;
    }

    /**
     * Форматирует скорость из бит/с в человекочитаемый вид.
     */
    private String formatSpeed(Long bitsPerSecond) {
        if (bitsPerSecond == null || bitsPerSecond == 0) return null;
        if (bitsPerSecond >= 1_000_000_000L) {
            return (bitsPerSecond / 1_000_000_000L) + " Gbps";
        }
        if (bitsPerSecond >= 1_000_000L) {
            return (bitsPerSecond / 1_000_000L) + " Mbps";
        }
        return (bitsPerSecond / 1_000L) + " Kbps";
    }

    /**
     * Простое определение типа сети: приватная или публичная.
     */
    private String detectNetworkType(String ip) {
        if (ip == null) return null;
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) return "Private";
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            try {
                int second = Integer.parseInt(parts[1]);
                if (second >= 16 && second <= 31) return "Private";
            } catch (NumberFormatException ignored) {}
        }
        if (ip.startsWith("127.")) return "Loopback";
        return "Public";
    }

    /**
     * Берёт MAC первого Ethernet-порта как "основной" для Equipment.
     */
    private String pickPrimaryMac(List<SnmpPortInfo> ports) {
        if (ports == null) return null;
        return ports.stream()
                .filter(p -> p.getIfType() != null && p.getIfType() == 6) // Ethernet
                .map(SnmpPortInfo::getMacAddress)
                .filter(mac -> mac != null && !mac.isBlank())
                .findFirst()
                .orElse(null);
    }

    /**
     * Берёт первый IP как primary.
     */
    private String pickPrimaryIp(List<SnmpIpInfo> ips) {
        if (ips == null || ips.isEmpty()) return null;
        return ips.get(0).getIpAddress();
    }

    private ImportResultDto.ImportDeviceReport skipped(String ip, String name, String reason) {
        return ImportResultDto.ImportDeviceReport.builder()
                .ipAddress(ip)
                .deviceName(name)
                .status(ImportResultDto.ImportStatus.SKIPPED)
                .message(reason)
                .build();
    }
}