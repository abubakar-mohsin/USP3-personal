package com.usp3.security.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.usp3.security.service.JwtService;
import com.usp3.security.repository.UserSessionRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserSessionRepository sessionRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (path.startsWith("/api/v1/auth/login") ||
            path.startsWith("/api/v1/auth/signup") ||
            path.startsWith("/api/v1/auth/accept-invite") ||
            path.startsWith("/api/v1/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            var claims = jwtService.parse(token);
            String userId = claims.getSubject();
            String role = (String) claims.get("role");
            if (role != null) {
                role = role.trim();
                if (role.startsWith("ROLE_")) {
                    role = role.substring("ROLE_".length());
                }
                role = role.toUpperCase();
            }
            boolean restricted = Boolean.TRUE.equals(claims.get("restricted"));
            String merchantIdRaw = (String) claims.get("merchantId");
            String tenantId = (String) claims.get("tenantId");
            String sessionIdRaw = (String) claims.get("sessionId");

            if (sessionIdRaw == null || sessionIdRaw.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            java.util.UUID sessionId;
            try {
                sessionId = java.util.UUID.fromString(sessionIdRaw);
            } catch (IllegalArgumentException ex) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            var session = sessionRepository.findById(sessionId).orElse(null);
            if (session == null || session.getRevokedAt() != null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            java.util.UUID merchantId = null;
            try {
                if (merchantIdRaw != null && !merchantIdRaw.isBlank()) {
                    merchantId = java.util.UUID.fromString(merchantIdRaw);
                }
            } catch (IllegalArgumentException ignored) {
                merchantId = null;
            }

            // Expose tenant/merchant context to downstream controllers without trusting request input.
            if (merchantId != null) {
                request.setAttribute("authenticatedMerchantId", merchantId);
            }
            if (tenantId != null && !tenantId.isBlank()) {
                request.setAttribute("authenticatedTenantId", tenantId);
            }

            var auth = new UsernamePasswordAuthenticationToken(
                userId,
                null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_" + (role == null || role.isBlank() ? "USER" : role)))
            );
            // Keep context details bundled for quick access in controllers/services.
            java.util.Map<String, Object> details = new java.util.HashMap<>();
            details.put("restricted", restricted);
            if (merchantId != null) {
                details.put("merchantId", merchantId);
            }
            if (tenantId != null && !tenantId.isBlank()) {
                details.put("tenantId", tenantId);
            }
            auth.setDetails(details);
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            // ignore and continue unauthenticated
        }

        filterChain.doFilter(request, response);
    }
}
