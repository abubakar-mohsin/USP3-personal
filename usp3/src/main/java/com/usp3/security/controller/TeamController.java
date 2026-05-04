package com.usp3.security.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.usp3.security.dto.TeamOverviewResponse;
import com.usp3.security.dto.TeamUserResponse;
import com.usp3.security.dto.UpdateUserRoleRequest;
import com.usp3.security.service.TeamService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamOverviewResponse> overview(HttpServletRequest request) {
        UUID merchantId = (UUID) request.getAttribute("authenticatedMerchantId");
        if (merchantId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(teamService.overview(merchantId));
    }

    @PostMapping("/users/{userId}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> revokeUser(@PathVariable UUID userId, HttpServletRequest request) {
        UUID merchantId = (UUID) request.getAttribute("authenticatedMerchantId");
        if (merchantId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        teamService.revokeUser(userId, merchantId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeamUserResponse> updateRole(
        @PathVariable UUID userId,
        @RequestBody UpdateUserRoleRequest requestBody,
        HttpServletRequest request
    ) {
        UUID merchantId = (UUID) request.getAttribute("authenticatedMerchantId");
        if (merchantId == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(teamService.updateRole(userId, requestBody.getRole(), merchantId));
    }
}
