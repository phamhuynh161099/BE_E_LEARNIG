package com.sakata.boilerplate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Cache danh sách permissions của user trong Redis.
 *
 * Key pattern: perm:{username} → List<String> permissions
 * TTL: 5 phút (configurable)
 *
 * Khi nào invalidate:
 * - User bị gán role mới
 * - Role bị thay đổi permissions
 * - User bị disable
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.cache.permission-ttl-seconds:300}")
    private long permissionTtlSeconds;

    private static final String PERM_PREFIX = "perm:";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    // ── Permission Cache ──────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public List<String> getPermissions(String username) {
        Object cached = redisTemplate.opsForValue().get(PERM_PREFIX + username);
        if (cached instanceof List<?> list) {
            log.debug("Cache HIT - permissions for {}", username);
            return (List<String>) list;
        }
        return null;
    }

    public void setPermissions(String username, List<String> permissions) {
        String key = PERM_PREFIX + username;
        redisTemplate.opsForValue().set(key, permissions, permissionTtlSeconds, TimeUnit.SECONDS);
        log.debug("Cache SET - permissions for {} (TTL {}s)", username, permissionTtlSeconds);
    }

    public void evictPermissions(String username) {
        redisTemplate.delete(PERM_PREFIX + username);
        log.debug("Cache EVICT - permissions for {}", username);
    }

    /** Xóa cache của tất cả user có role này (khi role bị thay đổi) */
    public void evictByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(PERM_PREFIX + pattern + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("Cache EVICT - {} keys matching pattern {}", keys.size(), pattern);
        }
    }

    // ── JWT Blacklist ─────────────────────────────────────────────

    @Value("${app.cache.token-blacklist-ttl-seconds:86400}")
    private long blacklistTtlSeconds;

    /**
     * Thêm token vào blacklist (khi logout).
     * Key: blacklist:{jti hoặc token hash}
     */
    public void blacklistToken(String tokenId) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + tokenId,
                "1",
                blacklistTtlSeconds,
                TimeUnit.SECONDS);
        log.debug("Token blacklisted: {}", tokenId);
    }

    public boolean isBlacklisted(String tokenId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + tokenId));
    }
}
