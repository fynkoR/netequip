package ru.ssau.netequip.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.ssau.netequip.dto.audit.ResponseAuditLogDto;
import ru.ssau.netequip.entity.AuditLog;
import ru.ssau.netequip.entity.User;
import ru.ssau.netequip.repository.AuditLogRepository;
import ru.ssau.netequip.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * Записать действие в журнал.
     * Берёт текущего пользователя из SecurityContext автоматически.
     */
    public void record(String action, String entityType, Long entityId,
                       String entityName, String description) {
        try {
            AuditLog entry = new AuditLog();
            entry.setTimestamp(LocalDateTime.now());
            entry.setAction(action);
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
            entry.setEntityName(entityName);
            entry.setDescription(description);

            // достаём текущего пользователя
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {

                Object principal = auth.getPrincipal();
                // В нашем JwtFilter principal — это userId типа Long
                if (principal instanceof Long userId) {
                    userRepository.findById(userId).ifPresent(user -> {
                        entry.setUsername(user.getUsername());
                        if (user.getEmployee() != null) {
                            entry.setEmployeeId(user.getEmployee().getId());
                            entry.setEmployeeFullName(user.getEmployee().getFullName());
                        }
                    });
                } else {
                    // запасной путь — если в principal вдруг строка (например, в тестах)
                    String username = auth.getName();
                    entry.setUsername(username);
                    userRepository.findByUsername(username).ifPresent(user -> {
                        if (user.getEmployee() != null) {
                            entry.setEmployeeId(user.getEmployee().getId());
                            entry.setEmployeeFullName(user.getEmployee().getFullName());
                        }
                    });
                }

                // Если ничего не нашли — username всё ещё null, ставим заглушку
                if (entry.getUsername() == null) {
                    entry.setUsername("unknown-id-" + principal);
                }
            } else {
                entry.setUsername("system");
            }

            auditLogRepository.save(entry);
        } catch (Exception e) {
            // Журнал не должен ломать основную операцию.
            // Если что-то пошло не так — логируем и идём дальше.
            log.error("Не удалось записать в журнал: action={}, entityType={}, entityId={}",
                    action, entityType, entityId, e);
        }
    }

    /**
     * Постранично получить журнал.
     */
    public Page<ResponseAuditLogDto> getPage(String search, Pageable pageable) {
        log.info("Получение страницы журнала: page={}, size={}, search='{}'",
                pageable.getPageNumber(), pageable.getPageSize(), search);

        Page<AuditLog> page;
        if (search != null && !search.isBlank()) {
            page = auditLogRepository.search(search.trim(), pageable);
        } else {
            page = auditLogRepository.findAll(pageable);
        }

        return page.map(this::toDto);
    }

    private ResponseAuditLogDto toDto(AuditLog a) {
        ResponseAuditLogDto dto = new ResponseAuditLogDto();
        dto.setId(a.getId());
        dto.setTimestamp(a.getTimestamp());
        dto.setUsername(a.getUsername());
        dto.setEmployeeId(a.getEmployeeId());
        dto.setEmployeeFullName(a.getEmployeeFullName());
        dto.setAction(a.getAction());
        dto.setEntityType(a.getEntityType());
        dto.setEntityId(a.getEntityId());
        dto.setEntityName(a.getEntityName());
        dto.setDescription(a.getDescription());
        return dto;
    }
}