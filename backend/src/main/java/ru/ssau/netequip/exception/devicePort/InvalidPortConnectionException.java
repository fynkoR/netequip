package ru.ssau.netequip.exception.devicePort;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidPortConnectionException extends RuntimeException {
    public InvalidPortConnectionException(Long port, Long equip) {
        super("Port " + port + " is not connected to equip " + equip);
    }
}
