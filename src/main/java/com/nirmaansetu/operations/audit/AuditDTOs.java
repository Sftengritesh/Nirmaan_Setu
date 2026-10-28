package com.nirmaansetu.operations.audit;

import java.time.Instant;
import java.util.UUID;

record AuditLogResponse(
    UUID id,
    UUID userId,
    String userName,
    String role,
    String action,
    String targetType,
    String targetId,
    String details,
    String ipAddress,
    Instant timestamp
) {
    public static AuditLogResponse from(AuditLogEntity a) {
        return new AuditLogResponse(
            a.getId(), a.getUserId(), a.getUserName(), a.getRole(), a.getAction(),
            a.getTargetType(), a.getTargetId(), a.getDetails(), a.getIpAddress(),
            a.getCreatedAt()
        );
    }
}
