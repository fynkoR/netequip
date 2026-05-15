package ru.ssau.netequip.exception.ipAddress;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PrimaryIpAddressConflictException extends RuntimeException {
    public PrimaryIpAddressConflictException(Long idEquip) {
        super("У устройства ID: " + idEquip + "уже есть основной ip-address." +
                "Сначала снимите флаг с основного ip");
    }
}

