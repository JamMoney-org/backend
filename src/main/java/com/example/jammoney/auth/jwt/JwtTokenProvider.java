package com.example.jammoney.auth.jwt;

import com.example.jammoney.auth.service.CustomUserDetailsService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey; // 비밀키


    private final CustomUserDetailsService userDetailsService;

    private static final long ACCESS_TOKEN_VALIDITY = 1000L * 60 * 30; // 액세스 토큰 유효기간: 30분(밀리초)
    private static final long REFRESH_TOKEN_VALIDITY = 1000L * 60 * 60 * 24 * 7; // 리프레시 토큰 유효기간: 7일(밀리초)

    private Key getSigningKey() { // 서명에 사용할 Key 객체를 생성
        byte[] keyBytes;                                               // 문자열 secretKey를 바이트 배열로 변환해 담을 변수
        if (secretKey != null && secretKey.startsWith("base64:")) { // secretKey가 base64: 접두사를 가지면
            keyBytes = Decoders.BASE64.decode(secretKey.substring("base64:".length())); // 접두사 제거 후 Base64 디코딩
        } else {                                                       // 아니면 일반 텍스트로 가정
            keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);     // UTF-8로 바이트 배열 변환
        }
        return Keys.hmacShaKeyFor(keyBytes);                           // 바이트 배열로 HS256 서명용 Key 생성
    }

    public String generateAccessToken(Long userId, String username, String rolesCsv) { // 액세스 토큰 생성
        long now = System.currentTimeMillis();
        Date iat = new Date(now); // 발급 시간(iat)
        Date exp = new Date(now + ACCESS_TOKEN_VALIDITY); // 만료 시간(exp)

        return Jwts.builder()
                .setSubject(username) // 표준 클레임: sub(username)
                .setId(UUID.randomUUID().toString()) // 표준 클레임: jti(토큰 고유 ID)
                .setIssuedAt(iat) // 표준 클레임: iat(발급 시각)
                .setExpiration(exp) // 표준 클레임: exp(만료 시각)
                .addClaims(Map.of(
                        "uid", userId, // 커스텀 클레임 : 사용자 식별자(숫자/문자 허용)
                        "roles", rolesCsv, // 커스텀 클레임 : 권한 정보(CSV 문자열 형태)
                        "token_type", "access" // 커스텀 클레임 : 토큰 유형 구분(액세스)
                ))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // HS256 서명
                .compact(); // 최종 문자열(JWT)로 직렬화
    }

    public String generateRefreshToken(Long userId, String username, String familyId) { // 리프레시 토큰 생성
        long now = System.currentTimeMillis(); // 현재 시간
        Date iat = new Date(now); // 발급 시간
        Date exp = new Date(now + REFRESH_TOKEN_VALIDITY); // 만료 시간

        return Jwts.builder()
                .setSubject(username) // 표준 클레임: sub(username)
                .setId(UUID.randomUUID().toString()) // 표준 클레임: jti(토큰 고유 ID)
                .setIssuedAt(iat) // 표준 클레임: iat(발급 시각)
                .setExpiration(exp) // 표준 클레임: exp(만료 시각)
                .addClaims(Map.of( // 커스텀 클레임
                        "uid", userId, // 커스텀 클레임 : 사용자 식별자(숫자/문자 허용)
                        "token_type", "refresh", // 커스텀 클레임 : 토큰 유형 구분(액세스)
                        "fam", (familyId == null ? UUID.randomUUID().toString() : familyId) // 커스텀 클레임 : family id(회전 그룹)
                ))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)   // HS256 서명
                .compact(); // 최종 문자열(JWT)로 직렬화
    }

    // ── Parse/Validate (1회 파싱 재사용) ──────────────────────────────────────
    public Claims parseClaims(String token) { // JWT를 파싱해 Claims(페이로드) 반환
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // 서명 검증에 사용할 Key 설정
                .setAllowedClockSkewSeconds(60) // 서버 간 시계 오차 허용(최대 60초)
                .build() // 파서 빌드
                .parseClaimsJws(token) // JWS 형태의 토큰 파싱 및 서명 검증
                .getBody(); // 바디(Claims) 추출
    }

    public boolean validate(String token) { // 토큰 유효성(구조/서명/만료) 전체 검증
        try {
            parseClaims(token); // 파싱(=서명/만료/형식)
            return true; // 성공하면 유효
        } catch (JwtException | IllegalArgumentException e) { // 실패하면
            log.debug("[JWT] validate failed: {}", e.getMessage());
            return false; // 무효
        }
    }

    public String getUsername(String token) { // sub(username) 추출
        return parseClaims(token).getSubject(); // Claims에서 subject 반환
    }

    public Long getUserId(String token) { // 커스텀 클레임 uid 추출
        Object uid = parseClaims(token).get("uid"); // uid 가져오기
        if (uid == null) return null; // 없으면 null
        if (uid instanceof Number) return ((Number) uid).longValue();   // 숫자면 long 변환
        return Long.parseLong(uid.toString()); // 문자열 등일 때 파싱해서 long
    }

    public String getFamilyId(String token) { // 커스텀 클레임 fam 추출
        Object fam = parseClaims(token).get("fam"); // fam 가져오기
        return fam == null ? null : fam.toString(); // null 아니면 문자열로 변환
    }

    public String getJti(String token) { // 표준 클레임 jti 추출
        try { return parseClaims(token).getId(); } // getId()는 jti 리턴
        catch (Exception e) { return null; } // 파싱 실패 시 null
    }

    public long getExpiration(String token) { // 만료 시각(exp)을 밀리초로 리턴
        try {
            Date exp = parseClaims(token).getExpiration(); // Claims에서 exp(Date) 추출
            return (exp == null) ? 0L : exp.getTime(); // null이면 0, 아니면 epoch ms로 변환
        } catch (Exception e) {
            return 0L; // 파싱 실패 시 0
        }
    }

    public Authentication getAuthentication(String token) { // 토큰으로 스프링 인증 객체 생성
        Claims claims = parseClaims(token); // Claims 파싱
        UserDetails user = userDetailsService.loadUserByUsername(claims.getSubject()); // DB/서비스에서 사용자 조회
        return new UsernamePasswordAuthenticationToken( // 인증 토큰 생성
                user, // Principal: UserDetails
                null, // Credentials: 보통 null(비밀번호 노출 X)
                user.getAuthorities() // 권한(ROLE_*)
        );
    }

    public boolean isRefreshToken(String token) { // 해당 토큰이 리프레시인지 여부 판정
        try {
            Claims c = parseClaims(token); // Claims 파싱
            return "refresh".equals(c.get("token_type")); // Claims에서 token_type 추출
        } catch (Exception e) {
            return false; // 에러 나면 false
        }
    }

    public long getRemainingSeconds(String token) { // 만료까지 남은 시간(초) 계산
        long expMs = getExpiration(token); // 만료 시각(ms)
        long remainMs = expMs - System.currentTimeMillis(); // 현재와의 차이(ms)
        return (remainMs > 0) ? (remainMs / 1000) : 0L; // 0보다 크면 초 단위로, 아니면 0
    }
}
