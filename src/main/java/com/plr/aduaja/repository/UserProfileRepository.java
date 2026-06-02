package com.plr.aduaja.repository;

import com.plr.aduaja.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    Optional<UserProfile> findByNik(String nik);

    boolean existsByNik(String nik);

    Optional<UserProfile> findByUserUserId(String userId);
}
