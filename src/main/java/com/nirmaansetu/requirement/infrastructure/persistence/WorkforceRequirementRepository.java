package com.nirmaansetu.requirement.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkforceRequirementRepository extends JpaRepository<WorkforceRequirementEntity, UUID> {

    List<WorkforceRequirementEntity> findByProjectId(UUID projectId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM WorkforceRequirementEntity r WHERE r.id = :id")
    Optional<WorkforceRequirementEntity> findByIdWithLock(@Param("id") UUID id);
}
