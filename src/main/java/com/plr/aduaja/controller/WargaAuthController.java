package com.plr.aduaja.controller;

import com.plr.aduaja.model.User;
import com.plr.aduaja.model.OtpVerification;
import com.plr.aduaja.dto.LoginDTO;
import com.plr.aduaja.dto.RegisterDTO;
import com.plr.aduaja.dto.ProfileDTO;
import com.plr.aduaja.service.UserService;
import com.plr.aduaja.service.AuthService;
import com.plr.aduaja.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

// ============================================================
// ABSTRACTION (Abstraksi): Controller hanya tahu Interface Service
// Controller TIDAK mengakses Repository langsung
// Controller TIDAK tahu bagaimana password di-hash, OTP dibuat, dll.
// ============================================================
@Controller
public class WargaAuthController {

    // ABSTRACTION: hanya inject Interface, bukan implementasi
    @Autowired
    private UserService userService;  // ← Abstraction: hanya tahu interface

    @Autowired
    private AuthService authService;  // ← Abstraction: hanya tahu interface

    @Autowired
    private OtpService otpService;    // ← Abstraction: hanya tahu interface

    // ==========================================
    // GET /warga/login — Halaman login warga
    // ==========================================
    @GetMapping("/warga/login")
    public String loginPage(Model model,
                            @RequestParam(value = "register", required = false) String register) {
        model.addAttribute("loginDTO", new LoginDTO());
        model.addAttribute("registerDTO", new RegisterDTO());
        return "warga/login";
    }

    // ==========================================
    // POST /warga/login — Proses login warga
    // ==========================================
    @PostMapping("/warga/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        HttpSession session,
                        HttpServletRequest request,
                        RedirectAttributes redirectAttributes) {

        // ABSTRACTION: Controller tidak tahu detail proses autentikasi
        LoginDTO dto = new LoginDTO();
        dto.setEmail(email);
        dto.setPassword(password);

        String ipAddress = getClientIp(request);
        Optional<User> userOpt = authService.login(dto, ipAddress);

        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                "Email atau password salah, atau akun belum aktif.");
            return "redirect:/warga/login";
        }

        User user = userOpt.get();

        // Validasi role
        if (user.getRole() != User.Role.WARGA) {
            redirectAttributes.addFlashAttribute("error", "Akun ini bukan akun warga.");
            return "redirect:/warga/login";
        }

        // Simpan session
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("userName", user.getFullName());
        session.setAttribute("userRole", "WARGA");

        return "redirect:/warga/dashboard";
    }

    // ==========================================
    // GET /warga/register — Halaman registrasi (redirect ke login dengan mode register)
    // ==========================================
    @GetMapping("/warga/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "warga/register";
    }

    // ==========================================
    // POST /warga/register — Proses registrasi warga baru
    // ==========================================
    @PostMapping("/warga/register")
    public String register(@ModelAttribute RegisterDTO dto,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        // Validasi password match
        if (!dto.isPasswordMatch()) {
            model.addAttribute("error", "Password tidak cocok");
            model.addAttribute("registerDTO", dto);
            return "warga/register";
        }

        // Validasi NIK 16 digit
        if (!dto.isNikValid()) {
            model.addAttribute("error", "NIK harus 16 digit angka");
            model.addAttribute("registerDTO", dto);
            return "warga/register";
        }

        // Validasi password minimal 8 karakter
        if (!dto.isPasswordStrong()) {
            model.addAttribute("error", "Password minimal 8 karakter");
            model.addAttribute("registerDTO", dto);
            return "warga/register";
        }

        try {
            // ABSTRACTION: createUser menyembunyikan detail hashing, save profile, dll.
            User user = userService.createUser(dto);

            // Generate OTP untuk verifikasi registrasi
            // ABSTRACTION: Controller tidak tahu cara OTP dibuat
            otpService.generateOtp(user.getUserId(), OtpVerification.OtpType.REGISTRATION);

            redirectAttributes.addFlashAttribute("success",
                "Registrasi berhasil! Kode OTP telah dibuat. Masukkan kode OTP untuk mengaktifkan akun.");
            return "redirect:/warga/verify-otp?userId=" + user.getUserId() + "&email=" + dto.getEmail();

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerDTO", dto);
            return "warga/register";
        }
    }

    // ==========================================
    // GET /warga/verify-otp — Halaman verifikasi OTP
    // ==========================================
    @GetMapping("/warga/verify-otp")
    public String verifyOtpPage(@RequestParam("userId") String userId,
                                @RequestParam("email") String email,
                                Model model) {
        model.addAttribute("userId", userId);
        model.addAttribute("email", email);
        return "warga/verify-otp";
    }

    // ==========================================
    // POST /warga/verify-otp — Proses verifikasi OTP
    // ==========================================
    @PostMapping("/warga/verify-otp")
    public String verifyOtp(@RequestParam("userId") String userId,
                            @RequestParam("email") String email,
                            @RequestParam("otpCode") String otpCode,
                            RedirectAttributes redirectAttributes) {
        // ABSTRACTION: Controller tidak tahu detail cara OTP diverifikasi
        boolean success = otpService.verifyOtp(userId, otpCode);

        if (success) {
            redirectAttributes.addFlashAttribute("success",
                "Verifikasi berhasil! Silakan login.");
            return "redirect:/warga/login";
        } else {
            redirectAttributes.addFlashAttribute("error",
                "Kode OTP tidak valid atau sudah kadaluarsa.");
            return "redirect:/warga/verify-otp?userId=" + userId + "&email=" + email;
        }
    }

    // ==========================================
    // GET /warga/profile — Lihat profil warga
    // ==========================================
    @GetMapping("/warga/profile")
    public String profilePage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/warga/login";
        }

        // ABSTRACTION: Controller hanya tahu findById dari interface
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isEmpty()) {
            session.invalidate();
            return "redirect:/warga/login";
        }

        User user = userOpt.get();
        model.addAttribute("user", user);

        // Siapkan ProfileDTO dengan data current
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setFullName(user.getFullName());
        profileDTO.setEmail(user.getEmail());
        profileDTO.setPhoneNumber(user.getPhoneNumber());

        // Load data profil dari UserProfile
        if (user.getUserProfile() != null) {
            profileDTO.setNik(user.getUserProfile().getNik());
            profileDTO.setAlamatLengkap(user.getUserProfile().getAlamatLengkap());
        }

        model.addAttribute("profileDTO", profileDTO);
        return "warga/profile";
    }

    // ==========================================
    // POST /warga/profile/edit — Simpan perubahan profil
    // ==========================================
    @PostMapping("/warga/profile/edit")
    public String editProfile(@ModelAttribute ProfileDTO dto,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/warga/login";
        }

        try {
            // ABSTRACTION: updateProfile menyembunyikan detail update Entity
            User user = userService.updateProfile(userId, dto);
            session.setAttribute("userName", user.getFullName());
            redirectAttributes.addFlashAttribute("success", "Profil berhasil diperbarui.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/warga/profile";
    }

    // ==========================================
    // POST /warga/logout — Logout warga
    // ==========================================
    @PostMapping("/warga/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/warga/login";
    }

    // ENKAPSULASI: method private — tidak bisa diakses dari luar
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
