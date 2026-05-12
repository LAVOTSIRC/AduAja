package com.plr.aduaja.service;

import com.plr.aduaja.model.User;
import com.plr.aduaja.model.UserProfile;
import com.plr.aduaja.repository.UserRepository;
import com.plr.aduaja.repository.UserProfileRepository;
import com.plr.aduaja.dto.RegisterDTO;
import com.plr.aduaja.dto.ProfileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// ============================================================
// POLYMORPHISM (Run-time Polymorphism): UserServiceImpl
// Mengimplementasikan UserService interface → @Override setiap method
//
// ABSTRACTION: Controller tidak perlu tahu implementasi ini,
// hanya tahu interface UserService
// ============================================================
@Service
@Transactional
public class UserServiceImpl implements UserService {  // ← POLYMORPHISM

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ===========================
    // @Override — Run-time Polymorphism
    // Mengimplementasikan semua method dari UserService interface
    // ===========================

    @Override  // ← POLYMORPHISM: Override dari interface
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    @Override  // ← POLYMORPHISM: Override dari interface (Overload)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override  // ← POLYMORPHISM: Override dari interface (Overload)
    public Optional<User> findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber);
    }

    @Override  // ← POLYMORPHISM: Override dari interface (Overload)
    public List<User> findByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    @Override  // ← POLYMORPHISM: Override dari interface (Overload: 2 parameter)
    public List<User> findByRoleAndStatus(User.Role role, User.AccountStatus status) {
        return userRepository.findByRoleAndAccountStatus(role, status);
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public User createUser(RegisterDTO dto) {
        // ABSTRACTION: Semua logika kompleks disembunyikan dari Controller
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email sudah terdaftar");
        }
        if (dto.getPhoneNumber() != null && userRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new RuntimeException("Nomor HP sudah terdaftar");
        }

        // Buat User baru
        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setPhoneNumber(dto.getPhoneNumber());
        // ENKAPSULASI: password di-hash, tidak pernah disimpan plaintext
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(User.Role.WARGA);
        user.setAccountStatus(User.AccountStatus.PENDING);  // Harus OTP dulu

        User savedUser = userRepository.save(user);

        // Buat UserProfile dengan NIK
        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        if (dto.getNik() != null && !dto.getNik().isBlank()) {
            // Cek NIK sudah dipakai
            if (userProfileRepository.existsByNik(dto.getNik())) {
                throw new RuntimeException("NIK sudah terdaftar");
            }
            profile.setNik(dto.getNik());
        }
        userProfileRepository.save(profile);

        return savedUser;
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public User updateProfile(String userId, ProfileDTO dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        // Update data User
        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            // Cek email sudah dipakai oleh user lain
            Optional<User> existingEmail = userRepository.findByEmail(dto.getEmail());
            if (existingEmail.isPresent() && !existingEmail.get().getUserId().equals(userId)) {
                throw new RuntimeException("Email sudah dipakai oleh akun lain");
            }
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber().isBlank() ? null : dto.getPhoneNumber());
        }

        User savedUser = userRepository.save(user);

        // Update UserProfile
        UserProfile profile = userProfileRepository.findByUserUserId(userId).orElse(null);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUser(user);
        }
        if (dto.getNik() != null && !dto.getNik().isBlank()) {
            profile.setNik(dto.getNik());
        }
        if (dto.getAlamatLengkap() != null) {
            profile.setAlamatLengkap(dto.getAlamatLengkap());
        }
        if (dto.getProfilePhotoUrl() != null && !dto.getProfilePhotoUrl().isBlank()) {
            profile.setProfilePhotoUrl(dto.getProfilePhotoUrl());
        }
        userProfileRepository.save(profile);

        return savedUser;
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public UserProfile getProfileByUserId(String userId) {
        return userRepository.findById(userId)
            .map(User::getUserProfile)
            .orElse(null);
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override  // ← POLYMORPHISM: Override dari interface
    public long countByRole(User.Role role) {
        return userRepository.findByRole(role).size();
    }
}
