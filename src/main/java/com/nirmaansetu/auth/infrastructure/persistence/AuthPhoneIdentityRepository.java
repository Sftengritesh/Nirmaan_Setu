package com.nirmaansetu.auth.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthPhoneIdentityRepository extends JpaRepository<AuthPhoneIdentityEntity, UUID> {
    Optional<AuthPhoneIdentityEntity> findByPhoneE164(String phoneE164);
}
