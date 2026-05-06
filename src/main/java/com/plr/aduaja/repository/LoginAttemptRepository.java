package com.plr.aduaja.repository;

import com.plr.aduaja.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, String> {

    List<LoginAttempt> findByUserUserId(String userId);

    long countByUserUserIdAndIsSuccessFalseAndAttemptedAtAfter(String userId, LocalDateTime since);

    long countByIpAddressAndIsSuccessFalseAndAttemptedAtAfter(String ipAddress, LocalDateTime since);

    List<LoginAttempt> findByAttemptEmailAndIsSuccessFalseAndAttemptedAtAfter(String email, LocalDateTime since);
}
