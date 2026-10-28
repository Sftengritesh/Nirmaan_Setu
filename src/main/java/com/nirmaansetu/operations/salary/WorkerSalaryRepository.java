package com.nirmaansetu.operations.salary;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface WorkerSalaryRepository extends JpaRepository<WorkerSalaryEntity, UUID> {
    List<WorkerSalaryEntity> findByWorkerUserIdOrderBySalaryMonthDesc(UUID workerUserId);
    List<WorkerSalaryEntity> findBySalaryMonthOrderByWorkerNameAsc(String salaryMonth);
}
