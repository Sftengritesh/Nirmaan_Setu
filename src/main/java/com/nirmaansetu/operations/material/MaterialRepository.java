package com.nirmaansetu.operations.material;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MaterialRepository extends JpaRepository<MaterialEntity, UUID> {
    List<MaterialEntity> findByProjectIdOrderByItemNameAsc(UUID projectId);
    List<MaterialEntity> findAllByOrderByItemNameAsc();
}
