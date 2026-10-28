package com.nirmaansetu.operations.site;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface SiteRepository extends JpaRepository<SiteEntity, UUID> {
    List<SiteEntity> findByProjectId(UUID projectId);
}
