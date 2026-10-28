package com.nirmaansetu.operations.salary;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SalaryService {

    private final WorkerSalaryRepository salaryRepository;

    public SalaryService(WorkerSalaryRepository salaryRepository) {
        this.salaryRepository = salaryRepository;
    }

    @Transactional
    public SalaryResponse generateSalary(GenerateSalaryRequest req) {
        BigDecimal dailyRate = BigDecimal.valueOf(req.dailyRate() > 0 ? req.dailyRate() : 750.0);
        BigDecimal daysPresent = BigDecimal.valueOf(req.daysPresent());
        BigDecimal halfDays = BigDecimal.valueOf(req.halfDays());
        BigDecimal baseSalary = (daysPresent.multiply(dailyRate))
            .add(halfDays.multiply(dailyRate.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)));

        BigDecimal hourlyRate = dailyRate.divide(BigDecimal.valueOf(8), 2, RoundingMode.HALF_UP);
        BigDecimal overtimeHours = BigDecimal.valueOf(req.overtimeHours());
        BigDecimal overtimePay = overtimeHours.multiply(hourlyRate).multiply(BigDecimal.valueOf(1.5))
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal bonus = BigDecimal.valueOf(req.bonus());
        BigDecimal deductions = BigDecimal.valueOf(req.deductions());
        BigDecimal netPayable = baseSalary.add(overtimePay).add(bonus).subtract(deductions);
        if (netPayable.compareTo(BigDecimal.ZERO) < 0) {
            netPayable = BigDecimal.ZERO;
        }

        WorkerSalaryEntity entity = new WorkerSalaryEntity(
            UUID.randomUUID(),
            req.workerUserId(),
            req.workerName(),
            req.salaryMonth(),
            req.daysPresent(),
            req.halfDays(),
            overtimeHours,
            dailyRate,
            baseSalary,
            overtimePay,
            bonus,
            deductions,
            netPayable
        );

        return SalaryResponse.from(salaryRepository.save(entity));
    }

    @Transactional
    public SalaryResponse markPaid(UUID salaryId, String paymentMethod, String paymentRef) {
        WorkerSalaryEntity entity = salaryRepository.findById(salaryId)
            .orElseThrow(() -> new IllegalArgumentException("Salary record not found: " + salaryId));

        if ("PAID".equalsIgnoreCase(entity.getStatus())) {
            throw new IllegalStateException("Salary voucher has already been marked as PAID.");
        }

        entity.setStatus("PAID");
        entity.setPaidDate(Instant.now());
        entity.setPaymentMethod(paymentMethod.toUpperCase().trim());
        entity.setPaymentRef(paymentRef);
        entity.setUpdatedAt(Instant.now());

        return SalaryResponse.from(salaryRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<SalaryResponse> getWorkerSalaries(UUID workerUserId) {
        return salaryRepository.findByWorkerUserIdOrderBySalaryMonthDesc(workerUserId)
            .stream().map(SalaryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SalaryResponse> getAllSalaries(String month) {
        if (month != null && !month.isBlank()) {
            return salaryRepository.findBySalaryMonthOrderByWorkerNameAsc(month)
                .stream().map(SalaryResponse::from).toList();
        }
        return salaryRepository.findAll().stream().map(SalaryResponse::from).toList();
    }
}
