package ru.ssau.netequip.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Информация об одном IP-адресе устройства, полученная через SNMP.
 * Источник данных — таблица ipAddrTable (RFC 1213).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnmpIpInfo {

    /**
     * Сам IP-адрес.
     * Является ключом строки в ipAddrTable.
     * Пример: "192.168.1.10"
     */
    private String ipAddress;

    /**
     * ifIndex интерфейса, на котором висит этот IP.
     * OID 1.3.6.1.2.1.4.20.1.2
     * Связь с ifTable — позволяет найти, какой именно порт владеет адресом.
     */
    private Integer ifIndex;

    /**
     * Маска подсети.
     * OID 1.3.6.1.2.1.4.20.1.3
     * Пример: "255.255.255.0"
     */
    private String subnetMask;
}