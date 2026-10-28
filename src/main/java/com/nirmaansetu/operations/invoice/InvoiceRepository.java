package com.nirmaansetu.operations.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, UUID> {
    Optional<InvoiceEntity> findByInvoiceNumber(String invoiceNumber);
    List<InvoiceEntity> findByClientUserIdOrderByCreatedAtDesc(UUID clientUserId);
    List<InvoiceEntity> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
    List<InvoiceEntity> findAllByOrderByCreatedAtDesc();
}
