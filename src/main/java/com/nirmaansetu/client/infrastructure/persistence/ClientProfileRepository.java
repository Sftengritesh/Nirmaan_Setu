package com.nirmaansetu.client.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientProfileRepository extends JpaRepository<ClientProfileEntity, UUID> {
    Optional<ClientProfileEntity> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}
