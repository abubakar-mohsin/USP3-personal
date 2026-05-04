package com.usp3.security.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.usp3.security.entity.DeviceBlacklist;

public interface DeviceBlacklistRepository extends JpaRepository<DeviceBlacklist, UUID> {
    boolean existsByMerchantIdAndDeviceFingerprint(UUID merchantId, String deviceFingerprint);
    Optional<DeviceBlacklist> findByMerchantIdAndDeviceFingerprint(UUID merchantId, String deviceFingerprint);
    long deleteByMerchantIdAndDeviceFingerprint(UUID merchantId, String deviceFingerprint);
}
