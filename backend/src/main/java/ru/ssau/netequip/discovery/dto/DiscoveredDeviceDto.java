package ru.ssau.netequip.discovery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.ssau.netequip.discovery.service.TypeResolverService.MatchConfidence;

/**
 * Обнаруженное устройство с результатом автосопоставления.
 * Содержит сырые SNMP-данные + рекомендуемый тип оборудования (если найден).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscoveredDeviceDto {

    /**
     * Сырые SNMP-данные обнаруженного устройства.
     */
    private SnmpDeviceInfo snmpData;

    /**
     * ID найденного EquipmentType, если автосопоставление сработало.
     * null — пользователь должен выбрать тип вручную.
     */
    private Long suggestedTypeId;

    /**
     * Имя найденного типа (для отображения в UI без дополнительного запроса).
     */
    private String suggestedTypeName;

    /**
     * Уровень достоверности автосопоставления.
     */
    private MatchConfidence matchConfidence;
}