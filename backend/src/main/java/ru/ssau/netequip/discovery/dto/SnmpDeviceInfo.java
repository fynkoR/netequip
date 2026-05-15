package ru.ssau.netequip.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Полный результат SNMP-опроса одного устройства.
 * Объединяет системную информацию, список интерфейсов и список IP-адресов.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnmpDeviceInfo {

    /**
     * Базовая системная информация (sysDescr, sysName, sysObjectID и т.д.).
     */
    private SnmpSystemInfo system;

    /**
     * Список сетевых интерфейсов устройства.
     */
    private List<SnmpPortInfo> ports;

    /**
     * Список IP-адресов устройства.
     */
    private List<SnmpIpInfo> ipAddresses;
}