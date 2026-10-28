package com.nirmaansetu.operations.attendance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nirmaansetu.operations.site.SiteEntity;
import com.nirmaansetu.operations.site.SiteRepository;
import com.nirmaansetu.worker.domain.AvailabilityStatus;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileEntity;
import com.nirmaansetu.worker.infrastructure.persistence.WorkerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private SiteRepository siteRepository;
    @Mock private WorkerProfileRepository workerProfileRepository;

    private AttendanceService service;

    private final UUID workerUserId = UUID.randomUUID();
    private final UUID siteId = UUID.randomUUID();
    private final UUID projectId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new AttendanceService(attendanceRepository, siteRepository, workerProfileRepository);
    }

    @Test
    void checkInSucceedsWhenWithinGeofence() {
        SiteEntity site = new SiteEntity(
            siteId, projectId, "Main Tower Site", "Tower Project", "BKC", "Mumbai", "MH",
            19.0760, 72.8777, 200, "Mr. Sharma", "9876543210"
        );
        when(attendanceRepository.findByWorkerUserIdAndWorkDate(workerUserId, LocalDate.now()))
            .thenReturn(Optional.empty());
        when(workerProfileRepository.findByUserId(workerUserId))
            .thenReturn(Optional.of(new WorkerProfileEntity(
                UUID.randomUUID(), workerUserId, "Ramesh Kumar", "Mumbai", AvailabilityStatus.AVAILABLE, true, Instant.now()
            )));
        when(siteRepository.findById(siteId)).thenReturn(Optional.of(site));
        when(attendanceRepository.save(any(AttendanceEntity.class))).thenAnswer(i -> i.getArgument(0));

        CheckInRequest request = new CheckInRequest(
            projectId, "Tower Project", siteId, "Main Tower Site",
            19.0761, 72.8778, "/api/uploads/selfie.jpg", "On time"
        );

        AttendanceResponse response = service.checkIn(workerUserId, request);

        assertThat(response).isNotNull();
        assertThat(response.workerName()).isEqualTo("Ramesh Kumar");
        assertThat(response.isWithinGeofence()).isTrue();
        assertThat(response.approvalStatus()).isEqualTo("PENDING");
    }

    @Test
    void checkInFailsWhenAlreadyCheckedInToday() {
        AttendanceEntity existing = new AttendanceEntity();
        existing.setWorkDate(LocalDate.now());
        when(attendanceRepository.findByWorkerUserIdAndWorkDate(workerUserId, LocalDate.now()))
            .thenReturn(Optional.of(existing));

        CheckInRequest request = new CheckInRequest(
            projectId, "Tower Project", siteId, "Main Tower Site",
            19.0761, 72.8778, "/api/uploads/selfie.jpg", null
        );

        assertThatThrownBy(() -> service.checkIn(workerUserId, request))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Already checked in");
    }

    @Test
    void checkOutSucceedsAndSetsCheckOutTime() {
        AttendanceEntity existing = new AttendanceEntity();
        existing.setId(UUID.randomUUID());
        existing.setWorkerUserId(workerUserId);
        existing.setWorkDate(LocalDate.now());
        existing.setCheckInTime("08:00 AM");

        when(attendanceRepository.findByWorkerUserIdAndWorkDate(workerUserId, LocalDate.now()))
            .thenReturn(Optional.of(existing));
        when(attendanceRepository.save(any(AttendanceEntity.class))).thenAnswer(i -> i.getArgument(0));

        AttendanceResponse response = service.checkOut(workerUserId, new CheckOutRequest("Completed foundation plaster"));

        assertThat(response.checkOutTime()).isNotBlank();
        assertThat(response.hoursWorked()).isNotNull();
    }

    @Test
    void reviewAttendanceUpdatesApprovalStatus() {
        UUID attId = UUID.randomUUID();
        AttendanceEntity existing = new AttendanceEntity();
        existing.setId(attId);
        existing.setApprovalStatus("PENDING");

        when(attendanceRepository.findById(attId)).thenReturn(Optional.of(existing));
        when(attendanceRepository.save(any(AttendanceEntity.class))).thenAnswer(i -> i.getArgument(0));

        AttendanceResponse response = service.reviewAttendance(attId, "APPROVED", "Supervisor", "Good job");

        assertThat(response.approvalStatus()).isEqualTo("APPROVED");
        assertThat(response.approvedBy()).isEqualTo("Supervisor");
    }
}
