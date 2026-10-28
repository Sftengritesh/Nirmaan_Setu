package com.nirmaansetu.operations.salary;

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
@RequestMapping("/api/salaries")
public class SalaryController {

    private final SalaryService salaryService;

    public SalaryController(SalaryService salaryService) {
        this.salaryService = salaryService;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SalaryResponse> generateSalary(@Valid @RequestBody GenerateSalaryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salaryService.generateSalary(request));
    }

    @PatchMapping("/{id}/pay")
    @PreAuthorize("hasRole('ADMIN')")
    public SalaryResponse markPaid(
            @PathVariable UUID id,
            @Valid @RequestBody MarkSalaryPaidRequest request) {
        return salaryService.markPaid(id, request.paymentMethod(), request.paymentRef());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('WORKER')")
    public List<SalaryResponse> getMySalaries(@AuthenticationPrincipal AuthPrincipal principal) {
        return salaryService.getWorkerSalaries(principal.userId());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SalaryResponse> getAllSalaries(@RequestParam(required = false) String month) {
        return salaryService.getAllSalaries(month);
    }
}
