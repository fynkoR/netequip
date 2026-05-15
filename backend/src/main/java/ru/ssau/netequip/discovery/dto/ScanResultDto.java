package ru.ssau.netequip.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanResultDto {

    /**
     * CIDR-подсеть, которая была просканирована.
     */
    private String cidr;

    /**
     * Общее количество IP-адресов в подсети, проверенных сканером.
     */
    private int totalAddressesScanned;

    /**
     * Длительность сканирования в миллисекундах.
     */
    private long durationMs;

    /**
     * Найденные устройства.
     */
    private List<SnmpDeviceInfo> discoveredDevices;
}