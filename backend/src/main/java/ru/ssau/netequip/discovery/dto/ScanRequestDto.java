package ru.ssau.netequip.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScanRequestDto {

    /**
     * CIDR-подсеть для сканирования.
     * Пример: "192.168.1.0/24", "127.0.0.0/29"
     */
    private String cidr;

    /**
     * UDP-порт SNMP. По умолчанию стандартный 161.
     * Для тестового стенда — 1161.
     */
    private Integer port;

    /**
     * SNMP community.
     * Поддерживается плейсхолдер {ip} — будет заменён на конкретный IP.
     * Примеры: "public", "{ip}/public"
     */
    private String community;
}