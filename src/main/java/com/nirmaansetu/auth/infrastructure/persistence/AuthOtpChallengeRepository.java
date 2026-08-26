package com.nirmaansetu.auth.infrastructure.persistence;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthOtpChallengeRepository extends JpaRepository<AuthOtpChallengeEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AuthOtpChallengeEntity> findFirstByPhoneE164AndConsumedAtIsNullOrderByCreatedAtDesc(String phoneE164);
    long deleteByCreatedAtBefore(Instant before);
}
