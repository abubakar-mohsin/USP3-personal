package com.usp3.security.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usp3.security.dto.AcceptInviteRequest;
import com.usp3.security.dto.AcceptInviteResponse;
import com.usp3.security.dto.CreateInvitationRequest;
import com.usp3.security.dto.InvitationResponse;
import com.usp3.security.dto.LoginRequest;
import com.usp3.security.dto.LoginResponse;
import com.usp3.security.dto.RefreshRequest;
import com.usp3.security.dto.SignupRequest;
import com.usp3.security.dto.SignupResponse;
import com.usp3.security.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        String userAgent = http.getHeader("User-Agent");
        return ResponseEntity.ok(authService.login(request, ip, userAgent));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshRequest request, HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        String userAgent = http.getHeader("User-Agent");
        return ResponseEntity.ok(authService.refresh(request, userAgent, ip));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/invitations")
    public ResponseEntity<InvitationResponse> createInvitation(@RequestBody CreateInvitationRequest request, Authentication authentication) {
        String inviterId = authentication != null ? String.valueOf(authentication.getPrincipal()) : "unknown";
        String authorities = authentication != null ? authentication.getAuthorities().toString() : "none";
        log.info("Invite request received: inviterId={} authorities={} email={} role={}", inviterId, authorities, request.getEmail(), request.getRole());
        try {
            var invitation = authService.invite(request, inviterId);
            log.info("Invite created: inviterId={} token={} email={} role={}", inviterId, invitation.getToken(), invitation.getEmail(), invitation.getRole());
            return ResponseEntity.ok(new InvitationResponse(invitation.getToken(), invitation.getEmail(), invitation.getRole(), invitation.getExpiresAt()));
        } catch (org.springframework.web.server.ResponseStatusException ex) {
            log.warn("Invite failed: inviterId={} status={} reason={}", inviterId, ex.getStatusCode(), ex.getReason());
            throw ex;
        }
    }

    @PostMapping("/accept-invite")
    public ResponseEntity<AcceptInviteResponse> acceptInvitation(@RequestBody AcceptInviteRequest request) {
        return ResponseEntity.ok(authService.acceptInvite(request));
    }
}
