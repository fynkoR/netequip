package ru.ssau.netequip.exception.ipAddress;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundIpAddressException extends RuntimeException {
    public NotFoundIpAddressException(Long id) {
        super("Ip address " + id + " не найден");
    }
}
