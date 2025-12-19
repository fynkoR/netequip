package com.example.netequip.exception.deviceport;

/**
 * Исключение выбрасывается когда порт не подключён к другому порту
 */
public class PortNotConnectedException extends RuntimeException {

    public PortNotConnectedException(Long portId) {
        super("Порт с ID " + portId + " не подключён к другому порту");
    }
}
