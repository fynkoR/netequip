package ru.ssau.netequip.exception.devicePort;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundDevicePortException extends RuntimeException {
    public NotFoundDevicePortException(Long id) {
        super("Device port " + id + " not found");
    }
}
