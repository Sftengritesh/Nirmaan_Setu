package com.nirmaansetu.worker.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerProfileRepository extends JpaRepository<WorkerProfileEntity, UUID> {
    Optional<WorkerProfileEntity> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);
}
