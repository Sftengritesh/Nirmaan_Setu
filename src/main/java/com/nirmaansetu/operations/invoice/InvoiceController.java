package com.nirmaansetu.operations.invoice;

import com.nirmaansetu.auth.application.AuthPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody CreateInvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.createInvoice(request));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT')")
    public InvoiceResponse markPaid(
            @PathVariable UUID id,
            @Valid @RequestBody MarkInvoicePaidRequest request) {
        return invoiceService.markInvoicePaid(id, request.paymentMethod(), request.transactionRef());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CLIENT')")
    public List<InvoiceResponse> getMyInvoices(@AuthenticationPrincipal AuthPrincipal principal) {
        return invoiceService.getClientInvoices(principal.userId());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENT', 'CONTRACTOR')")
    public List<InvoiceResponse> getProjectInvoices(@PathVariable UUID projectId) {
        return invoiceService.getProjectInvoices(projectId);
    }
}
