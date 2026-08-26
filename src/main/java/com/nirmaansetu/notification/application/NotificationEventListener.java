package com.nirmaansetu.notification.application;

import com.nirmaansetu.notification.domain.NotificationType;
import com.nirmaansetu.notification.domain.events.BookingCreatedEvent;
import com.nirmaansetu.notification.domain.events.BookingStatusChangedEvent;
import com.nirmaansetu.notification.domain.events.RequirementFulfilledEvent;
import com.nirmaansetu.notification.domain.events.VerificationReviewedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void handleBookingCreated(BookingCreatedEvent event) {
        notificationService.createNotification(
                event.recipientUserId(),
                NotificationType.BOOKING_RECEIVED,
                "New Booking Request",
                "You have received a new workforce booking request.",
                "BOOKING",
                event.bookingId()
        );
    }

    @EventListener
    public void handleBookingStatusChanged(BookingStatusChangedEvent event) {
        NotificationType type = "ACCEPTED".equalsIgnoreCase(event.status()) ? NotificationType.BOOKING_ACCEPTED : NotificationType.BOOKING_REJECTED;
        String title = "ACCEPTED".equalsIgnoreCase(event.status()) ? "Booking Accepted" : "Booking Rejected";
        String message = "Your booking request status changed to: " + event.status();

        notificationService.createNotification(
                event.recipientUserId(),
                type,
                title,
                message,
                "BOOKING",
                event.bookingId()
        );
    }

    @EventListener
    public void handleVerificationReviewed(VerificationReviewedEvent event) {
        NotificationType type = "VERIFIED".equalsIgnoreCase(event.status()) ? NotificationType.VERIFICATION_APPROVED : NotificationType.VERIFICATION_REJECTED;
        String title = "VERIFIED".equalsIgnoreCase(event.status()) ? "Verification Approved" : "Verification Rejected";
        String message = "VERIFIED".equalsIgnoreCase(event.status())
                ? "Your identity verification request has been approved."
                : "Your identity verification request was rejected. Notes: " + (event.notes() != null ? event.notes() : "N/A");

        notificationService.createNotification(
                event.recipientUserId(),
                type,
                title,
                message,
                "VERIFICATION",
                event.verificationId()
        );
    }

    @EventListener
    public void handleRequirementFulfilled(RequirementFulfilledEvent event) {
        notificationService.createNotification(
                event.recipientUserId(),
                NotificationType.REQUIREMENT_FULFILLED,
                "Workforce Requirement Fulfilled",
                "Your workforce requirement capacity has been fully met by accepted bookings.",
                "REQUIREMENT",
                event.requirementId()
        );
    }
}
