package com.usp3.platform.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usp3.platform.Merchant;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {
    
    Optional<Merchant> findByMerchantCode(String merchantCode);

    Optional<Merchant> findByName(String name);
}
