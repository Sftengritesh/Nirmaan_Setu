package com.nirmaansetu.operations.dpr;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DprRepository extends JpaRepository<DprEntity, UUID> {
    List<DprEntity> findByProjectIdOrderByReportDateDesc(UUID projectId);
    List<DprEntity> findAllByOrderByReportDateDesc();
}
