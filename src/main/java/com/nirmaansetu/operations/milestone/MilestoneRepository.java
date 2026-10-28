package com.nirmaansetu.operations.milestone;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MilestoneRepository extends JpaRepository<MilestoneEntity, UUID> {
    List<MilestoneEntity> findByProjectIdOrderByOrderIndexAsc(UUID projectId);
}
