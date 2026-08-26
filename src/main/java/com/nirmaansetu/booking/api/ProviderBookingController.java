package com.nirmaansetu.booking.api;

import com.nirmaansetu.auth.application.AuthPrincipal;
import com.nirmaansetu.booking.application.BookingService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAnyRole('WORKER', 'CONTRACTOR')")
public class ProviderBookingController {

    private final BookingService bookingService;

    public ProviderBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/api/bookings/provider")
    public List<BookingResponse> getProviderBookings(@AuthenticationPrincipal AuthPrincipal principal) {
        return bookingService.getProviderBookings(principal.userId());
    }

    @PostMapping("/api/bookings/{bookingId}/accept")
    public BookingResponse acceptBooking(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID bookingId) {
        return bookingService.acceptBooking(principal.userId(), bookingId);
    }

    @PostMapping("/api/bookings/{bookingId}/reject")
    public BookingResponse rejectBooking(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable UUID bookingId) {
        return bookingService.rejectBooking(principal.userId(), bookingId);
    }
}
