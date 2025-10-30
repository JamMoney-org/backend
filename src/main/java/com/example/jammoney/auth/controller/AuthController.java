package com.example.jammoney.auth.controller;

import com.example.jammoney.auth.dto.TokenRequestDto;
import com.example.jammoney.auth.dto.TokenResponseDto;
import com.example.jammoney.auth.jwt.JwtTokenProvider;
import com.example.jammoney.auth.service.RefreshTokenService;
import com.example.jammoney.exception.UserNotFoundException;
import com.example.jammoney.user.dto.LoginRequestDto;
import com.example.jammoney.user.dto.UserRequestDto;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import com.example.jammoney.user.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final UserDetailsService userDetailsService; // 권한 재구성용

    private static final String REFRESH_COOKIE = "refresh_token";

    /** 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody UserRequestDto request) {
        userService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    /** 로그인: AT는 바디, RT는 HttpOnly 쿠키로 발급 */
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response
    ) {
        // 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);

        // 권한 리스트
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .distinct()
                .toList();

        // 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail(), null);

        // refresh_token 저장 + 쿠키 세팅
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);
        setRefreshCookie(response, refreshToken); //

        // refresh_token은 바디에 굳이 반환하지 않음(쿠키로 운용)
        return ResponseEntity.ok(new TokenResponseDto(accessToken, null));
    }

    /** 재발급: RT는 쿠키에서 읽고, access_token만 바디로 내려줌. refresh_token은 회전 후 쿠키 갱신 */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(
            @CookieValue(value = REFRESH_COOKIE, required = false) String refreshToken,
            @RequestBody(required = false) TokenRequestDto fallbackBody, // 과도기 대응(없어도 됨)
            HttpServletResponse response
    ) {
        // 쿠키 우선, 없으면 바디에서(점진 전환용)
        if ((refreshToken == null || refreshToken.isBlank()) && fallbackBody != null) {
            refreshToken = fallbackBody.getRefreshToken();
        }
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        Claims claims = jwtTokenProvider.parseClaims(refreshToken);
        if (claims == null) return ResponseEntity.status(401).build();

        // refresh_token인지 확인
        if (!jwtTokenProvider.isRefreshToken(claims)) return ResponseEntity.status(400).build();

        // userId 추출
        Long userId = jwtTokenProvider.getUserId(claims);
        if (userId == null) return ResponseEntity.status(401).build();

        // 저장소 일치성/유효성 확인
        refreshTokenService.assertTokenValid(userId, refreshToken);

        // 사용자 & 권한 재구성
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        var details = userDetailsService.loadUserByUsername(user.getEmail());
        List<String> roles = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .distinct()
                .toList();

        // access_token 새로 발급
        String newAccess = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), roles);
        // refresh_token 회전(가족 유지) + 쿠키 갱신
        String newRefresh = refreshTokenService.reissueRefreshToken(userId, refreshToken);
        setRefreshCookie(response, newRefresh);

        return ResponseEntity.ok(new TokenResponseDto(newAccess, null));
    }

    /** 로그아웃(단일 기기): access_token 블랙리스트 + refresh_token 저장소 무효화 + 쿠키 제거 */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(value = REFRESH_COOKIE, required = false) String refreshFromCookie,
            @RequestBody(required = false) TokenRequestDto body, // 과도기 대응
            HttpServletResponse response
    ) {
        String accessToken = (body != null ? body.getAccessToken() : null);
        String refreshToken = refreshFromCookie;
        if ((refreshToken == null || refreshToken.isBlank()) && body != null) {
            refreshToken = body.getRefreshToken();
        }

        // access_token 블랙리스트
        if (accessToken != null && !accessToken.isBlank()) {
            refreshTokenService.blacklistAccessToken(accessToken);
        }

        // refresh_token 무효화
        if (refreshToken != null && !refreshToken.isBlank()) {
            Claims c = jwtTokenProvider.parseClaims(refreshToken);
            if (c != null) {
                Long userId = jwtTokenProvider.getUserId(c);
                if (userId != null) {
                    refreshTokenService.invalidateOne(userId, refreshToken);
                }
            }
        }

        // 쿠키 제거
        clearRefreshCookie(response);
        return ResponseEntity.ok().build();
    }

    /** 전체 로그아웃: 저장소 전체 무효화 + 쿠키 제거 */
    @PostMapping("/logout/all/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<Void> logoutAll(@PathVariable Long userId, HttpServletResponse response) {
        refreshTokenService.invalidateAll(userId);
        clearRefreshCookie(response);
        return ResponseEntity.ok().build();
    }

    // ── Cookie helpers ────────────────────────────────────────────────────────

    /** RT 쿠키 세팅: SameSite=None + Secure + HttpOnly */
    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        // 토큰에서 남은 수명(초) 계산: 1회 파싱 후 Claims 기반 계산
        long maxAgeSec = 0L;
        Claims c = jwtTokenProvider.parseClaims(refreshToken);
        if (c != null) {
            maxAgeSec = Math.max(1, jwtTokenProvider.getRemainingSeconds(c)); // 만료 동기화
        }

        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(true)                 // HTTPS 필수
                .sameSite("None")             // FE/BE 다른 도메인일 때
                .path("/api/auth")            // 재발급/로그아웃 경로로만 전송
                .maxAge(maxAgeSec)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /** RT 쿠키 제거 */
    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/api/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
