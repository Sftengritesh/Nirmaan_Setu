package com.nirmaansetu.booking.api;

import com.nirmaansetu.auth.application.AuthPrincipal;
import com.nirmaansetu.booking.application.BookingService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientBookingController {

    private final BookingService bookingService;

    public ClientBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/api/requirements/{requirementId}/bookings")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<BookingResponse> createBooking(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID requirementId,
            @Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(principal.userId(), requirementId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/requirements/{requirementId}/bookings")
    @PreAuthorize("hasRole('CLIENT')")
    public List<BookingResponse> getRequirementBookings(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID requirementId) {
        return bookingService.getRequirementBookings(principal.userId(), requirementId);
    }

    @GetMapping("/api/bookings/{bookingId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'WORKER', 'CONTRACTOR')")
    public BookingResponse getBookingById(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID bookingId) {
        return bookingService.getBookingById(principal.userId(), bookingId);
    }
}
