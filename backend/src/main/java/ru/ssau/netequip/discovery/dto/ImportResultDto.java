package ru.ssau.netequip.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Результат импорта обнаруженных устройств.
 * Содержит статистику и список проблемных случаев.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportResultDto {

    /**
     * Сколько устройств запрошено к импорту.
     */
    private int requestedCount;

    /**
     * Сколько устройств успешно создано.
     */
    private int createdCount;

    /**
     * Сколько устройств пропущено (уже существуют, не выбран тип, и т.п.).
     */
    private int skippedCount;

    /**
     * Сколько устройств упало с ошибкой.
     */
    private int failedCount;

    /**
     * Список деталей по каждому устройству — что с ним произошло.
     */
    private List<ImportDeviceReport> reports;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImportDeviceReport {
        /**
         * IP-адрес устройства из SNMP-сканирования (идентификатор для UI).
         */
        private String ipAddress;

        /**
         * Имя устройства (sysName).
         */
        private String deviceName;

        /**
         * Итог обработки: CREATED, SKIPPED, FAILED.
         */
        private ImportStatus status;

        /**
         * ID созданной записи Equipment (только для CREATED).
         */
        private Long equipmentId;

        /**
         * Сколько портов создано.
         */
        private int portsCreated;

        /**
         * Сколько IP-адресов создано.
         */
        private int ipsCreated;

        /**
         * Человекочитаемое объяснение (для SKIPPED/FAILED).
         */
        private String message;
    }

    public enum ImportStatus {
        CREATED, SKIPPED, FAILED
    }
}