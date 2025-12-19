package com.example.netequip.exception.ipaddress;

/**
 * Исключение выбрасывается при попытке установить несколько основных IP для одного устройства
 */
public class PrimaryIpAddressConflictException extends RuntimeException {

    public PrimaryIpAddressConflictException(Long equipmentId) {
        super("У устройства с ID " + equipmentId + " уже есть основной IP-адрес. " +
                "Сначала снимите флаг с текущего основного IP.");
    }
}
