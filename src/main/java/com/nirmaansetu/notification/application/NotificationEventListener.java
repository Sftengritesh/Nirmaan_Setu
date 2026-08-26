package com.nirmaansetu.notification.application;

import com.nirmaansetu.events.BookingCreatedEvent;
import com.nirmaansetu.events.BookingStatusChangedEvent;
import com.nirmaansetu.events.RequirementFulfilledEvent;
import com.nirmaansetu.events.VerificationReviewedEvent;
import com.nirmaansetu.notification.domain.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationEventListener {

    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
