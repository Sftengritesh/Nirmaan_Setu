package com.nirmaansetu.operations.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, UUID> {
    List<ExpenseEntity> findByProjectIdOrderByExpenseDateDesc(UUID projectId);
    List<ExpenseEntity> findAllByOrderByExpenseDateDesc();
}
