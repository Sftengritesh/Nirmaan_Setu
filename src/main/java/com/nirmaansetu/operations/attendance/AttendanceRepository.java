package com.nirmaansetu.operations.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<AttendanceEntity, UUID> {
    Optional<AttendanceEntity> findByWorkerUserIdAndWorkDate(UUID workerUserId, LocalDate workDate);
    List<AttendanceEntity> findByWorkerUserIdOrderByWorkDateDesc(UUID workerUserId);
    List<AttendanceEntity> findByWorkDateOrderByCreatedAtDesc(LocalDate workDate);
    List<AttendanceEntity> findBySiteIdOrderByWorkDateDesc(UUID siteId);
}
