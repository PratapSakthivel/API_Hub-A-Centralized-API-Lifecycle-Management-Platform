package com.apihub.backend.features.audit;

import com.apihub.backend.features.audit.domain.AuditLog;
import com.apihub.backend.features.audit.repository.AuditLogRepository;
import com.apihub.backend.features.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @Test
    void recordEvent_MasksSensitivePayload() {
        String sensitiveJson = "{\"username\":\"admin\",\"password\":\"SecretPassword123\",\"token\":\"jwt_token_here\"}";

        auditService.recordEvent(1L, "admin@apihub.local", "USER_LOGIN", "USER", "1", sensitiveJson, "127.0.0.1");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("USER_LOGIN", saved.getAction());
        assertFalse(saved.getDetails().contains("SecretPassword123"));
        assertFalse(saved.getDetails().contains("jwt_token_here"));
    }

    @Test
    void maskSensitiveData_ReplacesPasswords() {
        String input = "{\"password\":\"MyPass\"}";
        String masked = auditService.maskSensitiveData(input);
        assertEquals("{\"password\":\"***REDACTED***\"}", masked);
    }
}
