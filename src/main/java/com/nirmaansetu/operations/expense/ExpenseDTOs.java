package com.nirmaansetu.operations.expense;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

record CreateExpenseRequest(
    UUID projectId,
    String projectTitle,
    UUID siteId,
    @NotBlank String category,
    @NotNull BigDecimal amount,
    String description,
    String paidTo,
    String paymentMode,
    LocalDate date
) {}

record ReviewExpenseRequest(
    @NotBlank String status
) {}

record ExpenseResponse(
    UUID id,
    UUID projectId,
    String projectTitle,
    UUID siteId,
    String category,
    BigDecimal amount,
    String description,
    String paidTo,
    String paymentMode,
    LocalDate date,
    String approvedBy,
    String status,
    Instant createdAt
) {
    public static ExpenseResponse from(ExpenseEntity e) {
        return new ExpenseResponse(
            e.getId(), e.getProjectId(), e.getProjectTitle(), e.getSiteId(),
            e.getCategory(), e.getAmount(), e.getDescription(), e.getPaidTo(),
            e.getPaymentMode(), e.getExpenseDate(), e.getApprovedBy(), e.getStatus(),
            e.getCreatedAt()
        );
    }
}
