package com.usp3.security.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usp3.security.entity.Invitation;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
    Optional<Invitation> findByTokenAndAcceptedAtIsNullAndRevokedAtIsNull(String token);

    List<Invitation> findByMerchantId(UUID merchantId);
}
