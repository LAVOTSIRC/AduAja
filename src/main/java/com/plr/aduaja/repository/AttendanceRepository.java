package com.plr.aduaja.repository;

import com.plr.aduaja.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, String> {

    List<Attendance> findByUserId(String userId);

    Optional<Attendance> findByUserIdAndStatus(String userId, Attendance.Status status);

    List<Attendance> findByUserIdAndDateBetween(String userId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT a FROM Attendance a WHERE a.user.id = :userId ORDER BY a.date DESC")
    List<Attendance> findByUserIdOrderByDateDesc(@Param("userId") String userId);

    long countByUserId(String userId);

    long countByUserIdAndStatus(String userId, Attendance.Status status);
}
