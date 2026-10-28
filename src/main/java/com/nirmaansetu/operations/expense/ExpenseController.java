package com.nirmaansetu.operations.expense;

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
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTRACTOR')")
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.createExpense(request));
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ExpenseResponse reviewExpense(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody ReviewExpenseRequest request) {
        return expenseService.reviewExpense(id, request.status(), "Admin");
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTRACTOR')")
    public List<ExpenseResponse> getExpenses(@RequestParam(required = false) UUID projectId) {
        return expenseService.getExpenses(projectId);
    }
}
