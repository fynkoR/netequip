package ru.ssau.netequip.dto.ipAddress;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAndUpdateIpAddress {

    @NotNull(message = "ID оборудования обязательно")
    private Long equipmentId;  // Только ID, не весь объект

    @NotBlank(message = "IP-адрес обязателен")
    @Size(max = 45, message = "IP-адрес не должен превышать 45 символов")
    @Pattern(
            regexp = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$",
            message = "Некорректный формат IP-адреса (IPv4 или IPv6)"
    )
    private String ipAddress;

    @Size(max = 45, message = "Маска подсети не должна превышать 45 символов")
    private String subnetMask;

    @Size(max = 45, message = "Шлюз не должен превышать 45 символов")
    private String gateway;

    @Size(max = 20, message = "Тип сети не должен превышать 20 символов")
    private String networkType;

    private Boolean isPrimary;

    private LocalDate assignedDate;
}
