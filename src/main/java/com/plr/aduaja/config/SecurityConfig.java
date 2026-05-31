package com.plr.aduaja.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// ============================================================
// CATATAN PENTING — KEAMANAN APLIKASI
// ============================================================
// Saat ini aplikasi MASIH menggunakan otentikasi manual berbasis
// HttpSession di Controller layer (AdminAuthController,
// WargaAuthController). Spring Security filterChain belum
// diintegrasikan dengan session auth.
//
// TODO: Implementasi keamanan jangka panjang:
// 1. Buat class implement UserDetailsService dari User entity
// 2. Ganti session manual dengan Spring Security session auth
//    atau JWT token-based auth
// 3. Gunakan hasRole("ADMIN_PUSAT"), hasRole("PETUGAS"), dll
// 4. Integrasikan OAuth2 client yang sudah ada di dependency
// ============================================================
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ===== PUBLIC ASSETS =====
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/static/**", "/favicon.ico").permitAll()

                        // ===== PUBLIC PAGES =====
                        .requestMatchers("/", "/index", "/layouts/**").permitAll()

                        // ===== H2 CONSOLE (DEV ONLY) =====
                        .requestMatchers("/h2-console/**").permitAll()

                        // ===== AUTH ENDPOINTS (public) =====
                        .requestMatchers("/admin/login", "/petugas/login", "/warga/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/admin/login", "/petugas/login", "/warga/login").permitAll()
                        .requestMatchers("/warga/register", "/warga/verify-otp").permitAll()
                        .requestMatchers(HttpMethod.POST, "/warga/register", "/warga/verify-otp").permitAll()

                        // ===== REST API (public — akan dibatasi dengan token nanti) =====
                        .requestMatchers("/api/**").permitAll()

                        // ===== SEMUA RUTE LAINNYA (permitAll SEMENTARA) =====
                        // TODO: Ganti dengan role-based access setelah migrasi ke
                        //       Spring Security authentication:
                        // .requestMatchers("/admin/**").hasAnyRole("ADMIN_PUSAT", "ADMIN_DINAS")
                        // .requestMatchers("/petugas/**").hasRole("PETUGAS")
                        // .requestMatchers("/warga/**").hasRole("WARGA")
                        .anyRequest().permitAll()
                )
                // Matikan form login default Spring Security —
                // kita pakai custom login di AdminAuthController dkk.
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                )
                .anonymous(anon -> anon.disable());

        return http.build();
    }
}
