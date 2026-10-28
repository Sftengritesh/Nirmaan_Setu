package com.nirmaansetu.operations.salary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

record GenerateSalaryRequest(
    @NotNull UUID workerUserId,
    @NotBlank String workerName,
    @NotBlank String salaryMonth,
    int daysPresent,
    int halfDays,
    double overtimeHours,
    double dailyRate,
    double bonus,
    double deductions
) {}

record MarkSalaryPaidRequest(
    @NotBlank String paymentMethod,
    @NotBlank String paymentRef
) {}

record SalaryResponse(
    UUID id,
    UUID workerUserId,
    String workerName,
    String salaryMonth,
    int daysPresent,
    int halfDays,
    BigDecimal overtimeHours,
    BigDecimal dailyRate,
    BigDecimal baseSalary,
    BigDecimal overtimePay,
    BigDecimal bonus,
    BigDecimal deductions,
    BigDecimal netPayable,
    String status,
    Instant paidDate,
    String paymentMethod,
    String paymentRef,
    Instant createdAt
) {
    public static SalaryResponse from(WorkerSalaryEntity s) {
        return new SalaryResponse(
            s.getId(), s.getWorkerUserId(), s.getWorkerName(), s.getSalaryMonth(),
            s.getDaysPresent(), s.getHalfDays(), s.getOvertimeHours(), s.getDailyRate(),
            s.getBaseSalary(), s.getOvertimePay(), s.getBonus(), s.getDeductions(),
            s.getNetPayable(), s.getStatus(), s.getPaidDate(), s.getPaymentMethod(),
            s.getPaymentRef(), s.getCreatedAt()
        );
    }
}
