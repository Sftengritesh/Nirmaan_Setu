package com.nirmaansetu.operations.invoice;

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
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepository;
    private InvoiceService service;

    private final UUID clientUserId = UUID.randomUUID();
    private final UUID projectId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new InvoiceService(invoiceRepository);
    }

    @Test
    void createsInvoiceWithAutomaticTaxCalculation() {
        when(invoiceRepository.save(any(InvoiceEntity.class))).thenAnswer(i -> i.getArgument(0));

        CreateInvoiceRequest request = new CreateInvoiceRequest(
            "INV-1001", projectId, "Civil Construction", clientUserId, "Acme Infra",
            BigDecimal.valueOf(100000.00), null, LocalDate.now().plusDays(30), "Plinth completion invoice"
        );

        InvoiceResponse response = service.createInvoice(request);

        assertThat(response).isNotNull();
        assertThat(response.amount()).isEqualByComparingTo("100000.00");
        assertThat(response.taxAmount()).isEqualByComparingTo("18000.00"); // 18% GST
        assertThat(response.totalAmount()).isEqualByComparingTo("118000.00");
        assertThat(response.status()).isEqualTo("PENDING");
    }

    @Test
    void marksInvoiceAsPaid() {
        UUID invoiceId = UUID.randomUUID();
        InvoiceEntity entity = new InvoiceEntity();
        entity.setId(invoiceId);
        entity.setStatus("PENDING");

        when(invoiceRepository.findById(invoiceId)).thenReturn(Optional.of(entity));
        when(invoiceRepository.save(any(InvoiceEntity.class))).thenAnswer(i -> i.getArgument(0));

        InvoiceResponse response = service.markInvoicePaid(invoiceId, "RTGS", "RTGS-TXN-45678");

        assertThat(response.status()).isEqualTo("PAID");
        assertThat(response.paymentMethod()).isEqualTo("RTGS");
        assertThat(response.transactionRef()).isEqualTo("RTGS-TXN-45678");
        assertThat(response.paidAt()).isNotNull();
    }
}
