package com.example.netequip.exception.ipaddress;

/**
 * Исключение выбрасывается когда IP-адрес не найден в базе данных
 */
public class IpAddressNotFoundException extends RuntimeException {

    public IpAddressNotFoundException(Long id) {
        super("IP-адрес с ID " + id + " не найден");
    }

    public IpAddressNotFoundException(String ipAddress) {
        super("IP-адрес '" + ipAddress + "' не найден");
    }
}
