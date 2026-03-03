package com.sakata.boilerplate.audit;

import com.sakata.boilerplate.audit.models.AuditLog;
import com.sakata.boilerplate.mapper.audit.AuditLogFilter;
import com.sakata.boilerplate.mapper.audit.AuditLogMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Ghi audit log vào PostgreSQL.
 *
 * @Async: ghi log KHÔNG block request chính.
 *         Transactional dùng "auditTransactionManager" (PostgreSQL).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogMapper auditLogMapper;

    // ── Ghi log bất đồng bộ (không block) ────────────────────────

    @Async
    @Transactional(transactionManager = "auditTransactionManager")
    public void logAsync(AuditLog auditLog) {
        try {
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            // Audit log failure KHÔNG được làm crash nghiệp vụ chính
            log.error("Failed to write audit log: {}", e.getMessage(), e);
        }
    }

    // ── Builder helpers ───────────────────────────────────────────

    public void log(String actorName, Long actorId, String action,
            String resource, String resourceId,
            String oldValue, String newValue,
            HttpServletRequest request) {
        AuditLog entry = AuditLog.builder()
                .actorId(actorId)
                .actorName(actorName)
                .action(action)
                .resource(resource)
                .resourceId(resourceId)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(getClientIp(request))
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .requestUri(request != null ? request.getRequestURI() : null)
                .httpMethod(request != null ? request.getMethod() : null)
                .status(AuditLog.STATUS_SUCCESS)
                .build();
        logAsync(entry);
    }

    public void logFailure(String actorName, Long actorId, String action,
            String resource, String errorMsg,
            HttpServletRequest request) {
        AuditLog entry = AuditLog.builder()
                .actorId(actorId)
                .actorName(actorName != null ? actorName : "anonymous")
                .action(action)
                .resource(resource)
                .status(AuditLog.STATUS_FAILURE)
                .errorMsg(errorMsg)
                .ipAddress(getClientIp(request))
                .requestUri(request != null ? request.getRequestURI() : null)
                .httpMethod(request != null ? request.getMethod() : null)
                .build();
        logAsync(entry);
    }

    // ── Query audit logs (từ PostgreSQL) ─────────────────────────

    @Transactional(readOnly = true, transactionManager = "auditTransactionManager")
    public List<AuditLog> query(AuditLogFilter filter) {
        return auditLogMapper.findByFilter(filter);
    }

    // ── Utility ───────────────────────────────────────────────────

    private String getClientIp(HttpServletRequest request) {
        if (request == null)
            return null;
        String xff = request.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank())
                ? xff.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}
