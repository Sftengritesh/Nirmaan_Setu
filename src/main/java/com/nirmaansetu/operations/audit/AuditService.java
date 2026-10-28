package com.nirmaansetu.operations.audit;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logActivity(UUID userId, String userName, String role, String action,
                            String targetType, String targetId, String details,
                            String ipAddress, String userAgent) {
        try {
            AuditLogEntity log = new AuditLogEntity(
                UUID.randomUUID(), userId, userName, role, action, targetType, targetId, details, ipAddress, userAgent
            );
            auditLogRepository.save(log);
        } catch (Exception ignored) {
            // Audit log failures should not crash main transaction
        }
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> getRecentLogs(int limit) {
        int safeLimit = limit > 0 && limit <= 200 ? limit : 50;
        return auditLogRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, safeLimit))
            .stream().map(AuditLogResponse::from).toList();
    }
}
