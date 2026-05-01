package com.plr.aduaja.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Temporary/dev security configuration:
     * - Permit all requests so the Thymeleaf UI routes can be tested.
     * - Disable CSRF so POST forms work without CSRF tokens.
     *
     * NOTE: Replace with proper authentication/authorization later.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/warga/**",
                                "/admin/**",
                                "/petugas/**",
                                "/h2-console/**")
                        .permitAll()
                        .anyRequest().permitAll())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                // We handle logout via controller POST endpoints in this demo
                .logout(logout -> logout.disable())
                // Needed for H2 console
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}

