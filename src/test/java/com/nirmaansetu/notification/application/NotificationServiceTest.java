package com.nirmaansetu.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nirmaansetu.notification.api.NotificationResponse;
import com.nirmaansetu.notification.domain.NotificationException;
import com.nirmaansetu.notification.domain.NotificationStatus;
import com.nirmaansetu.notification.domain.NotificationType;
import com.nirmaansetu.events.BookingCreatedEvent;
import com.nirmaansetu.events.VerificationReviewedEvent;
import com.nirmaansetu.notification.infrastructure.persistence.NotificationEntity;
import com.nirmaansetu.notification.infrastructure.persistence.NotificationRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-27T00:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneId.of("UTC"));

    @Mock private NotificationRepository notificationRepository;

    private NotificationService notificationService;
    private NotificationEventListener eventListener;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, clock);
        eventListener = new NotificationEventListener(notificationService);
    }

    @Test
    void createNotificationSuccess() {
        UUID userId = UUID.randomUUID();
        UUID refId = UUID.randomUUID();

        when(notificationRepository.save(any(NotificationEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response = notificationService.createNotification(
                userId, NotificationType.BOOKING_RECEIVED, "Test Title", "Test Message", "BOOKING", refId
        );

        assertThat(response.recipientUserId()).isEqualTo(userId);
        assertThat(response.type()).isEqualTo("BOOKING_RECEIVED");
        assertThat(response.status()).isEqualTo("UNREAD");
        assertThat(response.readAt()).isNull();
    }

    @Test
    void createNotificationFailsWhenRecipientIdNull() {
        assertThatThrownBy(() -> notificationService.createNotification(
                null, NotificationType.BOOKING_RECEIVED, "Title", "Message", "BOOKING", UUID.randomUUID()
        )).isInstanceOf(NotificationException.class)
          .hasMessageContaining("Recipient user ID cannot be null");
    }

    @Test
    void getUserNotificationsWithPaginationCap() {
        UUID userId = UUID.randomUUID();
        NotificationEntity entity = new NotificationEntity(UUID.randomUUID(), userId, NotificationType.BOOKING_RECEIVED, "Title", "Msg", "BOOKING", UUID.randomUUID(), NOW);

        when(notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(any(UUID.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 50), 1));

        Pageable overSized = PageRequest.of(0, 100);
        Page<NotificationResponse> page = notificationService.getUserNotifications(userId, null, overSized);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getPageable().getPageSize()).isEqualTo(50);
    }

    @Test
    void getUnreadCountSuccess() {
        UUID userId = UUID.randomUUID();
        when(notificationRepository.countByRecipientUserIdAndStatus(userId, NotificationStatus.UNREAD)).thenReturn(3L);

        long count = notificationService.getUnreadCount(userId);

        assertThat(count).isEqualTo(3L);
    }

    @Test
    void markAsReadSuccess() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        NotificationEntity entity = new NotificationEntity(notificationId, userId, NotificationType.BOOKING_RECEIVED, "Title", "Msg", "BOOKING", UUID.randomUUID(), NOW);

        when(notificationRepository.findByIdAndRecipientUserId(notificationId, userId)).thenReturn(Optional.of(entity));
        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse response = notificationService.markAsRead(userId, notificationId);

        assertThat(response.status()).isEqualTo("READ");
        assertThat(response.readAt()).isEqualTo(NOW);
    }

    @Test
    void markAsReadThrowsExceptionWhenNotFound() {
        UUID userId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();

        when(notificationRepository.findByIdAndRecipientUserId(notificationId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(userId, notificationId))
                .isInstanceOf(NotificationException.class)
                .hasMessageContaining("Notification not found for user");
    }

    @Test
    void markAllAsReadSuccess() {
        UUID userId = UUID.randomUUID();
        NotificationEntity entity1 = new NotificationEntity(UUID.randomUUID(), userId, NotificationType.BOOKING_RECEIVED, "Title1", "Msg1", "BOOKING", UUID.randomUUID(), NOW);
        NotificationEntity entity2 = new NotificationEntity(UUID.randomUUID(), userId, NotificationType.BOOKING_ACCEPTED, "Title2", "Msg2", "BOOKING", UUID.randomUUID(), NOW);

        when(notificationRepository.findByRecipientUserIdAndStatus(userId, NotificationStatus.UNREAD))
                .thenReturn(List.of(entity1, entity2));

        Map<String, Integer> result = notificationService.markAllAsRead(userId);

        assertThat(result.get("markedReadCount")).isEqualTo(2);
        assertThat(entity1.getStatus()).isEqualTo(NotificationStatus.READ);
        assertThat(entity2.getStatus()).isEqualTo(NotificationStatus.READ);
    }

    @Test
    void handleBookingCreatedEventListener() {
        UUID userId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID reqId = UUID.randomUUID();

        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        eventListener.handleBookingCreated(new BookingCreatedEvent(bookingId, reqId, userId));

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());

        NotificationEntity saved = captor.getValue();
        assertThat(saved.getRecipientUserId()).isEqualTo(userId);
        assertThat(saved.getType()).isEqualTo(NotificationType.BOOKING_RECEIVED);
        assertThat(saved.getReferenceEntityId()).isEqualTo(bookingId);
    }

    @Test
    void handleVerificationReviewedEventListener() {
        UUID userId = UUID.randomUUID();
        UUID verId = UUID.randomUUID();

        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        eventListener.handleVerificationReviewed(new VerificationReviewedEvent(verId, "VERIFIED", userId, null));

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());

        NotificationEntity saved = captor.getValue();
        assertThat(saved.getRecipientUserId()).isEqualTo(userId);
        assertThat(saved.getType()).isEqualTo(NotificationType.VERIFICATION_APPROVED);
        assertThat(saved.getReferenceEntityId()).isEqualTo(verId);
    }
}
