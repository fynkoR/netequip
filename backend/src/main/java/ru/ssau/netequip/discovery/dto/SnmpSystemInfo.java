package ru.ssau.netequip.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnmpSystemInfo {

    /**
     * IP-адрес, на котором был обнаружен агент.
     */
    private String ipAddress;

    /**
     * sysDescr — текстовое описание устройства.
     * OID 1.3.6.1.2.1.1.1.0
     * Пример: "Cisco IOS Software, Catalyst SG350-28 Series"
     */
    private String description;

    /**
     * sysObjectID — идентификатор модели устройства у производителя.
     * OID 1.3.6.1.2.1.1.2.0
     * Пример: "1.3.6.1.4.1.9.1.1933"
     * Используется для точного сопоставления с EquipmentType.
     */
    private String objectId;

    /**
     * sysName — имя устройства, заданное администратором.
     * OID 1.3.6.1.2.1.1.5.0
     * Пример: "sw-floor1"
     */
    private String name;

    /**
     * sysLocation — физическое расположение устройства.
     * OID 1.3.6.1.2.1.1.6.0
     * Пример: "Server Room A"
     */
    private String location;

    /**
     * sysUpTime — время работы устройства в сотых секунды.
     * OID 1.3.6.1.2.1.1.3.0
     */
    private Long uptime;
}