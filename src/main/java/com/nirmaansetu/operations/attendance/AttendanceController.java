package com.nirmaansetu.operations.attendance;

import com.nirmaansetu.auth.application.AuthPrincipal;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<AttendanceResponse> checkIn(
            @AuthenticationPrincipal AuthPrincipal principal,
            @Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.checkIn(principal.userId(), request));
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<AttendanceResponse> checkOut(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody(required = false) CheckOutRequest request) {
        return ResponseEntity.ok(attendanceService.checkOut(principal.userId(), request));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('WORKER')")
    public List<AttendanceResponse> getMyAttendance(@AuthenticationPrincipal AuthPrincipal principal) {
        return attendanceService.getMyAttendance(principal.userId());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTRACTOR')")
    public List<AttendanceResponse> getAllAttendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceService.getAllAttendance(date);
    }

    @GetMapping("/site/{siteId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTRACTOR')")
    public List<AttendanceResponse> getSiteAttendance(@PathVariable UUID siteId) {
        return attendanceService.getSiteAttendance(siteId);
    }

    @PatchMapping("/{id}/approval")
    @PreAuthorize("hasAnyRole('ADMIN', 'CONTRACTOR')")
    public AttendanceResponse reviewAttendance(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody ReviewAttendanceRequest request) {
        return attendanceService.reviewAttendance(id, request.approvalStatus(), "Admin/Contractor", request.remarks());
    }
}
