package com.example.jammoney.auth.jwt;

import com.example.jammoney.auth.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    // ====== 상수/클레임 키 ======
    private static final String CLAIM_UID   = "uid";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TYPE  = "token_type";
    private static final String CLAIM_FAM  = "fam";
    private static final String CLAIM_VER  = "ver";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    @Value("${jwt.secret}")
    private String secretKey; // 비밀키
    @Value("${jwt.access-token-validity-in-seconds}")
    private long accessValiditySec;
    @Value("${jwt.refresh-token-validity-in-seconds}")
    private long refreshValiditySec;

    private final CustomUserDetailsService userDetailsService;

    private Key getSigningKey() {
        byte[] keyBytes;
        if (secretKey != null && secretKey.startsWith("Base64:")) {
            keyBytes = Decoders.BASE64.decode(secretKey.substring("Base64:".length()));
        } else {
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Long userId, String username, Collection<String> roles, long ver) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessValiditySec * 1000L);
        return Jwts.builder()
                .setSubject(username)
                .claim(CLAIM_UID, userId)
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .claim(CLAIM_VER, ver)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(Long userId, String username, String familyId, long ver) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + refreshValiditySec * 1000L);
        return Jwts.builder()
                .setSubject(username)
                .claim(CLAIM_UID, userId)
                .claim(CLAIM_FAM, familyId)
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .claim(CLAIM_VER, ver)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .setAllowedClockSkewSeconds(60)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("[JWT] parse failed: {}", e.getMessage());
            return null;
        }
    }

    public String getUsername(Claims c) {
        return c.getSubject();
    }

    public Long getUserId(Claims c) {
        Object uid = c.get(CLAIM_UID);
        if (uid == null) return null;
        if (uid instanceof Number) return ((Number) uid).longValue();
        return Long.parseLong(uid.toString());
    }

    public String getFamilyId(Claims c) {
        Object fam = c.get(CLAIM_FAM);
        return fam == null ? null : fam.toString();
    }

    public String getJti(Claims c) {
        return c.getId();
    }

    public long getExpirationMillis(Claims c) {
        Date exp = c.getExpiration();
        return (exp == null) ? 0L : exp.getTime();
    }

    public long getRemainingSeconds(Claims c) {
        long remainMs = getExpirationMillis(c) - System.currentTimeMillis();
        return (remainMs > 0) ? (remainMs / 1000) : 0L;
    }

    public boolean isRefreshToken(Claims c) {
        try {
            return TYPE_REFRESH.equals(c.get(CLAIM_TYPE));
        } catch (Exception e) {
            log.debug("[JWT] not a refresh_token: {}", e.getMessage());
            return false;
        }
    }

    public AbstractAuthenticationToken getAuthentication(Claims c) {
        Long userId = getUserId(c);
        String email = c.getSubject();
        var principal = (userId != null)
                ? userDetailsService.loadUserById(userId)
                : userDetailsService.loadUserByUsername(email);

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
    }

    public long getTokenVersion(Claims c) {
        Object v = c.get(CLAIM_VER);
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return 0L; }
    }
}
