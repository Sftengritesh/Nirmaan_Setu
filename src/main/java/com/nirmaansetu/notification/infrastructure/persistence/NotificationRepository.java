package com.nirmaansetu.notification.infrastructure.persistence;

import com.nirmaansetu.notification.domain.NotificationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    Page<NotificationEntity> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId, Pageable pageable);

    Page<NotificationEntity> findByRecipientUserIdAndStatusOrderByCreatedAtDesc(UUID recipientUserId, NotificationStatus status, Pageable pageable);

    List<NotificationEntity> findByRecipientUserIdAndStatus(UUID recipientUserId, NotificationStatus status);

    long countByRecipientUserIdAndStatus(UUID recipientUserId, NotificationStatus status);

    Optional<NotificationEntity> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);
}
