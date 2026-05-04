package com.usp3.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; // ADD THIS
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.usp3.security.filter.ApiKeyAuthenticationFilter;
import com.usp3.security.filter.JwtAuthenticationFilter;
import com.usp3.security.service.ApiKeyService;
import com.usp3.security.service.JwtService;
import com.usp3.security.repository.UserSessionRepository;

@Configuration
@EnableWebSecurity // CRITICAL: This tells Spring to use THIS config, not the default one
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ApiKeyService apiKeyService,
                                                   JwtService jwtService,
                                                   UserSessionRepository sessionRepository) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/signup").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/accept-invite").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/invitations").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/v1/payments", "/api/v1/payments/").permitAll() // payment ingestion via API key filter
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtService, sessionRepository), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new ApiKeyAuthenticationFilter(apiKeyService), JwtAuthenticationFilter.class);

        return http.build();
    }
}