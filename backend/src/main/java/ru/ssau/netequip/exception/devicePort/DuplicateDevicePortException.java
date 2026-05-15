package ru.ssau.netequip.exception.devicePort;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateDevicePortException extends RuntimeException {
    public DuplicateDevicePortException(Long equipID, Integer port) {
        super("Порт № " + port + " уже существует на устройстве ID " + equipID);
    }
}
