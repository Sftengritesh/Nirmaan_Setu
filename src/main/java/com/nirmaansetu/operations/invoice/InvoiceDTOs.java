package com.nirmaansetu.operations.invoice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

record CreateInvoiceRequest(
    String invoiceNumber,
    UUID projectId,
    String projectTitle,
    UUID clientUserId,
    String clientName,
    @NotNull BigDecimal amount,
    BigDecimal taxAmount,
    @NotNull LocalDate dueDate,
    String description
) {}

record MarkInvoicePaidRequest(
    @NotBlank String paymentMethod,
    @NotBlank String transactionRef
) {}

record InvoiceResponse(
    UUID id,
    String invoiceNumber,
    UUID projectId,
    String projectTitle,
    UUID clientUserId,
    String clientName,
    String paymentType,
    BigDecimal amount,
    BigDecimal taxAmount,
    BigDecimal totalAmount,
    String status,
    LocalDate dueDate,
    Instant paidAt,
    String paymentMethod,
    String transactionRef,
    String description,
    Instant createdAt
) {
    public static InvoiceResponse from(InvoiceEntity i) {
        return new InvoiceResponse(
            i.getId(), i.getInvoiceNumber(), i.getProjectId(), i.getProjectTitle(),
            i.getClientUserId(), i.getClientName(), i.getPaymentType(), i.getAmount(),
            i.getTaxAmount(), i.getTotalAmount(), i.getStatus(), i.getDueDate(),
            i.getPaidAt(), i.getPaymentMethod(), i.getTransactionRef(), i.getDescription(),
            i.getCreatedAt()
        );
    }
}
