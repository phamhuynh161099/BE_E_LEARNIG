package com.sakata.boilerplate.controller;

import com.sakata.boilerplate.audit.AuditService;
import com.sakata.boilerplate.audit.models.AuditLog;
import com.sakata.boilerplate.mapper.audit.AuditLogFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    /**
     * GET /api/audit/logs
     *   ?actorId=1
     *   &action=LOGIN
     *   &resource=user
     *   &status=FAILURE
     *   &fromDate=2024-01-01T00:00:00Z
     *   &toDate=2024-12-31T23:59:59Z
     *   &page=0&size=20
     *
     * Query PostgreSQL. Chỉ ADMIN mới xem được.
     */
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLog> getLogs(
        @RequestParam(required = false) Long actorId,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) String resource,
        @RequestParam(required = false) String resourceId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        AuditLogFilter filter = AuditLogFilter.builder()
            .actorId(actorId).action(action).resource(resource)
            .resourceId(resourceId).status(status)
            .fromDate(fromDate).toDate(toDate)
            .page(page).size(Math.min(size, 100))
            .build();

        return auditService.query(filter);
    }
}
