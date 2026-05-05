package com.plr.aduaja.repository;

import com.plr.aduaja.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    List<User> findByRole(User.Role role);

    List<User> findByRoleAndAccountStatus(User.Role role, User.AccountStatus accountStatus);

    List<User> findByAccountStatus(User.AccountStatus accountStatus);
}
