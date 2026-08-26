package com.nirmaansetu.auth.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionRepository extends JpaRepository<AuthSessionEntity, UUID> {
    Optional<AuthSessionEntity> findByTokenHash(String tokenHash);
}
