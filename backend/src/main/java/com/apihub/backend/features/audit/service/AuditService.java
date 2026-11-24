package com.apihub.backend.features.audit.service;

import com.apihub.backend.features.audit.domain.AuditLog;
import com.apihub.backend.features.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(?i)\"(password|secret|token|apiKey|authorization)\"\\s*:\\s*\"[^\"]+\""
    );

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordEvent(Long actorId, String actorEmail, String action, String entityType, String entityId, String details, String ipAddress) {
        String maskedDetails = maskSensitiveData(details);

        AuditLog logEntry = AuditLog.builder()
                .actorId(actorId)
                .actorEmail(actorEmail != null ? actorEmail : "SYSTEM")
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(maskedDetails)
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(logEntry);
        log.info("Audit logged: action={}, entityType={}, entityId={}, actor={}", action, entityType, entityId, actorEmail);
    }

    public Page<AuditLog> getLogs(String entityType, String actorEmail, Pageable pageable) {
        return auditLogRepository.searchLogs(entityType, actorEmail, pageable);
    }

    public String maskSensitiveData(String input) {
        if (input == null || input.isBlank()) {
            return input;
        }
        return SENSITIVE_PATTERN.matcher(input).replaceAll("\"$1\":\"***REDACTED***\"");
    }
}
