package com.nirmaansetu.operations.invoice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest req) {
        String invNum = req.invoiceNumber();
        if (invNum == null || invNum.isBlank()) {
            invNum = "INV-" + (System.currentTimeMillis() % 1000000);
        }

        BigDecimal tax = req.taxAmount() != null 
            ? req.taxAmount() 
            : req.amount().multiply(BigDecimal.valueOf(0.18)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = req.amount().add(tax);

        InvoiceEntity entity = new InvoiceEntity(
            UUID.randomUUID(),
            invNum,
            req.projectId(),
            req.projectTitle() != null ? req.projectTitle() : "Construction Project",
            req.clientUserId(),
            req.clientName() != null ? req.clientName() : "Client",
            req.amount(),
            tax,
            total,
            req.dueDate(),
            req.description() != null ? req.description() : "Milestone Construction Work Payment"
        );

        return InvoiceResponse.from(invoiceRepository.save(entity));
    }

    @Transactional
    public InvoiceResponse markInvoicePaid(UUID id, String paymentMethod, String transactionRef) {
        InvoiceEntity entity = invoiceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + id));

        if ("PAID".equalsIgnoreCase(entity.getStatus())) {
            throw new IllegalStateException("Invoice is already marked as PAID.");
        }

        entity.setStatus("PAID");
        entity.setPaidAt(Instant.now());
        entity.setPaymentMethod(paymentMethod.toUpperCase().trim());
        entity.setTransactionRef(transactionRef);
        entity.setUpdatedAt(Instant.now());

        return InvoiceResponse.from(invoiceRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getClientInvoices(UUID clientUserId) {
        return invoiceRepository.findByClientUserIdOrderByCreatedAtDesc(clientUserId)
            .stream().map(InvoiceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAllByOrderByCreatedAtDesc()
            .stream().map(InvoiceResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getProjectInvoices(UUID projectId) {
        return invoiceRepository.findByProjectIdOrderByCreatedAtDesc(projectId)
            .stream().map(InvoiceResponse::from).toList();
    }
}
