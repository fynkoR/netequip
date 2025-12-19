package com.example.netequip.exception.ipaddress;

/**
 * Исключение выбрасывается при попытке создать уже существующий IP-адрес
 */
public class DuplicateIpAddressException extends RuntimeException {

    public DuplicateIpAddressException(String ipAddress) {
        super("IP-адрес '" + ipAddress + "' уже используется");
    }
}
