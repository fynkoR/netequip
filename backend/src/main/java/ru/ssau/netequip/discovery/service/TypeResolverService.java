package ru.ssau.netequip.discovery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.ssau.netequip.discovery.dto.SnmpSystemInfo;
import ru.ssau.netequip.entity.EquipmentType;
import ru.ssau.netequip.repository.EquipmentTypeRepository;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TypeResolverService {

    private final EquipmentTypeRepository equipmentTypeRepository;

    /**
     * Результат сопоставления типа оборудования.
     * Содержит сам тип (или null) и уровень достоверности.
     */
    public record ResolverResult(EquipmentType type, MatchConfidence confidence) {
        public static ResolverResult exact(EquipmentType type) {
            return new ResolverResult(type, MatchConfidence.EXACT);
        }
        public static ResolverResult heuristic(EquipmentType type) {
            return new ResolverResult(type, MatchConfidence.HEURISTIC);
        }
        public static ResolverResult unresolved() {
            return new ResolverResult(null, MatchConfidence.NONE);
        }
    }

    public enum MatchConfidence {
        /** Точное сопоставление по sysObjectID. */
        EXACT,
        /** Эвристическое сопоставление по строке sysDescr. */
        HEURISTIC,
        /** Не удалось сопоставить — пользователь должен выбрать вручную. */
        NONE
    }

    /**
     * Пытается определить тип оборудования для обнаруженного устройства.
     */
    public ResolverResult resolve(SnmpSystemInfo systemInfo) {
        if (systemInfo == null) return ResolverResult.unresolved();

        // 1. Точное сопоставление по sysObjectID
        if (systemInfo.getObjectId() != null) {
            Optional<EquipmentType> byOid = equipmentTypeRepository
                    .findBySnmpObjectId(systemInfo.getObjectId());
            if (byOid.isPresent()) {
                log.debug("Exact match by sysObjectID '{}' -> type '{}'",
                        systemInfo.getObjectId(), byOid.get().getTypeName());
                return ResolverResult.exact(byOid.get());
            }
        }

        // 2. Эвристика: разбираем sysDescr, ищем подходящий тип по производителю+модели
        if (systemInfo.getDescription() != null && !systemInfo.getDescription().isBlank()) {
            EquipmentType heuristicMatch = findByDescription(systemInfo.getDescription());
            if (heuristicMatch != null) {
                log.debug("Heuristic match by sysDescr '{}' -> type '{}'",
                        systemInfo.getDescription(), heuristicMatch.getTypeName());
                return ResolverResult.heuristic(heuristicMatch);
            }
        }

        // 3. Не нашли
        log.debug("No type match for device {} (oid={}, descr={})",
                systemInfo.getIpAddress(), systemInfo.getObjectId(), systemInfo.getDescription());
        return ResolverResult.unresolved();
    }

    /**
     * Эвристический поиск типа по строке sysDescr.
     * Алгоритм: для каждого типа из справочника проверяем,
     * содержит ли строка sysDescr и manufacturer, и model (без учёта регистра).
     */
    private EquipmentType findByDescription(String sysDescr) {
        String descrLower = sysDescr.toLowerCase();
        List<EquipmentType> allTypes = equipmentTypeRepository.findAll();

        for (EquipmentType type : allTypes) {
            String manufacturer = type.getManufacturer();
            String model = type.getModel();

            if (manufacturer == null || model == null) continue;

            boolean manufacturerMatches = descrLower.contains(manufacturer.toLowerCase());
            boolean modelMatches = descrLower.contains(model.toLowerCase());

            if (manufacturerMatches && modelMatches) {
                return type;
            }
        }
        return null;
    }
}