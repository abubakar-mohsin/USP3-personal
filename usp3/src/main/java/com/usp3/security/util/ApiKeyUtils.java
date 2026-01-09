package com.usp3.security.util;

import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;

public class ApiKeyUtils {
    // SHA-256 is the industry standard for API key hashing
    public static String hashKey(String rawKey) {
        return Hashing.sha256()
                .hashString(rawKey, StandardCharsets.UTF_8)
                .toString();
    }
}