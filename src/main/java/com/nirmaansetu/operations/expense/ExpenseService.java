package com.nirmaansetu.operations.expense;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest req) {
        ExpenseEntity entity = new ExpenseEntity(
            UUID.randomUUID(),
            req.projectId(),
            req.projectTitle() != null ? req.projectTitle() : "General",
            req.siteId(),
            req.category(),
            req.amount(),
            req.description() != null ? req.description() : "Project Expense",
            req.paidTo() != null ? req.paidTo() : "Vendor",
            req.paymentMode() != null ? req.paymentMode() : "UPI",
            req.date()
        );
        return ExpenseResponse.from(expenseRepository.save(entity));
    }

    @Transactional
    public ExpenseResponse reviewExpense(UUID id, String status, String approverName) {
        ExpenseEntity entity = expenseRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Expense record not found: " + id));

        entity.setStatus(status.toUpperCase().trim());
        entity.setApprovedBy(approverName);
        entity.setUpdatedAt(Instant.now());

        return ExpenseResponse.from(expenseRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpenses(UUID projectId) {
        if (projectId != null) {
            return expenseRepository.findByProjectIdOrderByExpenseDateDesc(projectId)
                .stream().map(ExpenseResponse::from).toList();
        }
        return expenseRepository.findAllByOrderByExpenseDateDesc()
            .stream().map(ExpenseResponse::from).toList();
    }
}
