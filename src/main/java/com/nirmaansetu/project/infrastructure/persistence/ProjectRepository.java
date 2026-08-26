package com.nirmaansetu.project.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
    List<ProjectEntity> findByClientProfileId(UUID clientProfileId);
}
