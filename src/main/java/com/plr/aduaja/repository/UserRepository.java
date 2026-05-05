package com.plr.aduaja.repository;

import com.plr.aduaja.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByNik(String nik);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByNik(String nik);

    List<User> findByRole(User.Role role);

    List<User> findByRoleAndIsActiveTrue(User.Role role);

    List<User> findByDinasIdAndRole(String dinasId, User.Role role);

    boolean existsByPhoneNumber(String phoneNumber);
}
