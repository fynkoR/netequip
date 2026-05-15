package ru.ssau.netequip.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Запрос на импорт обнаруженных устройств в БД.
 * Содержит результаты предыдущего скана с подтверждением пользователя.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportRequestDto {

    /**
     * Список устройств для импорта.
     * Каждое устройство содержит SNMP-данные и выбранный пользователем typeId.
     */
    private List<ImportDeviceItem> devices;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportDeviceItem {
        /**
         * SNMP-данные, полученные при сканировании.
         */
        private SnmpDeviceInfo snmpData;

        /**
         * ID типа оборудования, выбранный пользователем.
         * Обязателен — без типа Equipment не может быть создано.
         */
        private Long typeId;
    }
}