package com.nirmaansetu.operations.salary;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "worker_salary")
public class WorkerSalaryEntity {

    @Id
    private UUID id;

    @Column(name = "worker_user_id", nullable = false)
    private UUID workerUserId;

    @Column(name = "worker_name", nullable = false, length = 120)
    private String workerName;

    @Column(name = "salary_month", nullable = false, length = 7)
    private String salaryMonth; // YYYY-MM

    @Column(name = "days_present", nullable = false)
    private int daysPresent;

    @Column(name = "half_days", nullable = false)
    private int halfDays;

    @Column(name = "overtime_hours", nullable = false, precision = 5, scale = 2)
    private BigDecimal overtimeHours;

    @Column(name = "daily_rate", nullable = false, precision = 12, scale = 2)
    private BigDecimal dailyRate;

    @Column(name = "base_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "overtime_pay", nullable = false, precision = 12, scale = 2)
    private BigDecimal overtimePay;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal bonus;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal deductions;

    @Column(name = "net_payable", nullable = false, precision = 12, scale = 2)
    private BigDecimal netPayable;

    @Column(nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, PAID

    @Column(name = "paid_date")
    private Instant paidDate;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod; // UPI, NEFT, CASH, CHEQUE

    @Column(name = "payment_ref", length = 100)
    private String paymentRef;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public WorkerSalaryEntity() {}

    public WorkerSalaryEntity(UUID id, UUID workerUserId, String workerName, String salaryMonth,
                              int daysPresent, int halfDays, BigDecimal overtimeHours, BigDecimal dailyRate,
                              BigDecimal baseSalary, BigDecimal overtimePay, BigDecimal bonus, BigDecimal deductions,
                              BigDecimal netPayable) {
        this.id = id;
        this.workerUserId = workerUserId;
        this.workerName = workerName;
        this.salaryMonth = salaryMonth;
        this.daysPresent = daysPresent;
        this.halfDays = halfDays;
        this.overtimeHours = overtimeHours;
        this.dailyRate = dailyRate;
        this.baseSalary = baseSalary;
        this.overtimePay = overtimePay;
        this.bonus = bonus;
        this.deductions = deductions;
        this.netPayable = netPayable;
        this.status = "PENDING";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getWorkerUserId() { return workerUserId; }
    public void setWorkerUserId(UUID workerUserId) { this.workerUserId = workerUserId; }
    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }
    public String getSalaryMonth() { return salaryMonth; }
    public void setSalaryMonth(String salaryMonth) { this.salaryMonth = salaryMonth; }
    public int getDaysPresent() { return daysPresent; }
    public void setDaysPresent(int daysPresent) { this.daysPresent = daysPresent; }
    public int getHalfDays() { return halfDays; }
    public void setHalfDays(int halfDays) { this.halfDays = halfDays; }
    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }
    public BigDecimal getBaseSalary() { return baseSalary; }
    public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }
    public BigDecimal getOvertimePay() { return overtimePay; }
    public void setOvertimePay(BigDecimal overtimePay) { this.overtimePay = overtimePay; }
    public BigDecimal getBonus() { return bonus; }
    public void setBonus(BigDecimal bonus) { this.bonus = bonus; }
    public BigDecimal getDeductions() { return deductions; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }
    public BigDecimal getNetPayable() { return netPayable; }
    public void setNetPayable(BigDecimal netPayable) { this.netPayable = netPayable; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getPaidDate() { return paidDate; }
    public void setPaidDate(Instant paidDate) { this.paidDate = paidDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getPaymentRef() { return paymentRef; }
    public void setPaymentRef(String paymentRef) { this.paymentRef = paymentRef; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
