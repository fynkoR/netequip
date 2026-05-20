package ru.ssau.netequip.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ssau.netequip.dto.audit.ResponseAuditLogDto;
import ru.ssau.netequip.service.AuditLogService;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Slf4j
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<Page<ResponseAuditLogDto>> getAudit(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("Get audit log page={}, size={}, search='{}'",
                pageable.getPageNumber(), pageable.getPageSize(), search);
        return ResponseEntity.ok(auditLogService.getPage(search, pageable));
    }
}