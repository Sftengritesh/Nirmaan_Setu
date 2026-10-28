package com.nirmaansetu.operations.salary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class SalaryServiceTest {

    @Mock private WorkerSalaryRepository salaryRepository;
    private SalaryService service;

    private final UUID workerUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new SalaryService(salaryRepository);
    }

    @Test
    void generatesMonthlySalaryCorrectly() {
        when(salaryRepository.save(any(WorkerSalaryEntity.class))).thenAnswer(i -> i.getArgument(0));

        GenerateSalaryRequest request = new GenerateSalaryRequest(
            workerUserId, "Suresh Mason", "2026-10", 25, 2, 8.0, 800.0, 500.0, 200.0
        );

        SalaryResponse response = service.generateSalary(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("PENDING");
        // Base = 25 * 800 + 2 * 400 = 20000 + 800 = 20800
        // Hourly = 100, Overtime = 8 * 100 * 1.5 = 1200
        // Net = 20800 + 1200 + 500 - 200 = 22300
        assertThat(response.baseSalary()).isEqualByComparingTo("20800.00");
        assertThat(response.overtimePay()).isEqualByComparingTo("1200.00");
        assertThat(response.netPayable()).isEqualByComparingTo("22300.00");
    }

    @Test
    void marksSalaryAsPaidWithRef() {
        UUID salaryId = UUID.randomUUID();
        WorkerSalaryEntity entity = new WorkerSalaryEntity();
        entity.setId(salaryId);
        entity.setStatus("PENDING");

        when(salaryRepository.findById(salaryId)).thenReturn(Optional.of(entity));
        when(salaryRepository.save(any(WorkerSalaryEntity.class))).thenAnswer(i -> i.getArgument(0));

        SalaryResponse response = service.markPaid(salaryId, "UPI", "UPI-TRANS-98765");

        assertThat(response.status()).isEqualTo("PAID");
        assertThat(response.paymentMethod()).isEqualTo("UPI");
        assertThat(response.paymentRef()).isEqualTo("UPI-TRANS-98765");
        assertThat(response.paidDate()).isNotNull();
    }

    @Test
    void rejectsMarkPaidWhenAlreadyPaid() {
        UUID salaryId = UUID.randomUUID();
        WorkerSalaryEntity entity = new WorkerSalaryEntity();
        entity.setId(salaryId);
        entity.setStatus("PAID");

        when(salaryRepository.findById(salaryId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.markPaid(salaryId, "NEFT", "NEFT-123"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already been marked as PAID");
    }
}
