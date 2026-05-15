package ru.ssau.netequip.exception.ipAddress;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateIpAddressException extends RuntimeException {
    public DuplicateIpAddressException(String ipAddress) {
        super("Данный ip-address: " + ipAddress + " уже существует");
    }
}
