package ru.ssau.netequip.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Информация об одном сетевом интерфейсе, полученная через SNMP.
 * Источник данных — таблица ifTable (RFC 1213) + ifXTable (RFC 2863).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnmpPortInfo {

    /**
     * ifIndex — уникальный идентификатор интерфейса внутри устройства.
     * OID 1.3.6.1.2.1.2.2.1.1
     * Сквозной ключ, по которому связаны ifTable, ipAddrTable и другие таблицы.
     */
    private Integer ifIndex;

    /**
     * ifDescr — текстовое имя/описание интерфейса.
     * OID 1.3.6.1.2.1.2.2.1.2
     * Пример: "GigabitEthernet0/1", "ether1-wan", "wlan0"
     */
    private String description;

    /**
     * ifType — тип интерфейса (числовой код согласно IANAifType).
     * OID 1.3.6.1.2.1.2.2.1.3
     * 6 = Ethernet, 71 = Wi-Fi (ieee80211), 24 = loopback, и т.д.
     */
    private Integer ifType;

    /**
     * ifSpeed — пропускная способность интерфейса в бит/с.
     * OID 1.3.6.1.2.1.2.2.1.5
     * Пример: 1000000000 = 1 Гбит/с
     */
    private Long speed;

    /**
     * ifPhysAddress — физический (MAC) адрес интерфейса.
     * OID 1.3.6.1.2.1.2.2.1.6
     * Пример: "00:11:22:33:44:01"
     */
    private String macAddress;

    /**
     * ifAdminStatus — административный статус (включён ли админом).
     * OID 1.3.6.1.2.1.2.2.1.7
     * 1 = up, 2 = down, 3 = testing
     */
    private Integer adminStatus;

    /**
     * ifOperStatus — операционный статус (есть ли физический линк).
     * OID 1.3.6.1.2.1.2.2.1.8
     * 1 = up, 2 = down, 3 = testing, и далее по RFC 2863
     */
    private Integer operStatus;
}