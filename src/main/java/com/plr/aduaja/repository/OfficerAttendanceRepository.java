package com.plr.aduaja.repository;

import com.plr.aduaja.model.OfficerAttendance;
import com.plr.aduaja.model.OfficerAttendance.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OfficerAttendanceRepository extends JpaRepository<OfficerAttendance, String> {

    List<OfficerAttendance> findByOfficerUserId(String officerId);

    Optional<OfficerAttendance> findTopByOfficerUserIdAndShiftStatusNotOrderByCheckInAtDesc(
            String officerId, ShiftStatus shiftStatus);

    List<OfficerAttendance> findByOfficerUserIdAndCheckInAtBetween(
            String officerId, LocalDateTime start, LocalDateTime end);

    List<OfficerAttendance> findByShiftStatus(ShiftStatus shiftStatus);
}
