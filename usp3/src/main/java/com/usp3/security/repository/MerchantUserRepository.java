package com.usp3.security.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usp3.security.entity.MerchantUser;

public interface MerchantUserRepository extends JpaRepository<MerchantUser, UUID> {
    Optional<MerchantUser> findByEmailIgnoreCase(String email);

    List<MerchantUser> findByMerchant_Id(UUID merchantId);
}
