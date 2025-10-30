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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

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
        // 1) 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2) 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);

        // 3) 권한 CSV
        String rolesCsv = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 4) 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), rolesCsv);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail(), null);

        // 5) RT 저장(해시) + 쿠키 세팅
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);
        setRefreshCookie(response, refreshToken);

        // RT는 바디에 굳이 반환하지 않음(쿠키로 운용)
        return ResponseEntity.ok(new TokenResponseDto(accessToken, null));
    }

    /** 재발급: RT는 쿠키에서 읽고, AT만 바디로 내려줌. RT는 회전 후 쿠키 갱신 */
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

        // 1) 서명/만료
        if (!jwtTokenProvider.validate(refreshToken)) return ResponseEntity.status(401).build();
        // 2) RT인지 확인
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) return ResponseEntity.status(400).build();
        // 3) userId 추출
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        if (userId == null) return ResponseEntity.status(401).build();
        // 4) 저장소 일치성/유효성
        refreshTokenService.assertTokenValid(userId, refreshToken);

        // 5) 사용자 & 권한 재구성
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        var details = userDetailsService.loadUserByUsername(user.getEmail());
        String rolesCsv = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 6) AT 새로 발급
        String newAccess = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), rolesCsv);
        // 7) RT 회전(가족 유지) + 쿠키 갱신
        String newRefresh = refreshTokenService.reissueRefreshToken(userId, refreshToken);
        setRefreshCookie(response, newRefresh);

        return ResponseEntity.ok(new TokenResponseDto(newAccess, null));
    }

    /** 로그아웃(단일 기기): AT 블랙리스트 + RT 저장소 무효화 + 쿠키 제거 */
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

        // AT 블랙리스트 (선택)
        if (accessToken != null && !accessToken.isBlank()) {
            refreshTokenService.blacklistAccessToken(accessToken);
        }

        // RT 무효화(저장소)
        if (refreshToken != null && !refreshToken.isBlank() && jwtTokenProvider.validate(refreshToken)) {
            Long userId = jwtTokenProvider.getUserId(refreshToken);
            if (userId != null) {
                refreshTokenService.invalidateOne(userId, refreshToken);
            }
        }

        // 쿠키 제거
        clearRefreshCookie(response);
        return ResponseEntity.ok().build();
    }

    /** 전체 로그아웃: 저장소 전체 무효화 + 쿠키 제거 */
    @PostMapping("/logout/all/{userId}")
    public ResponseEntity<Void> logoutAll(@PathVariable Long userId, HttpServletResponse response) {
        // 실서비스에서는 @PreAuthorize 등으로 인가 체크 필수
        refreshTokenService.invalidateAll(userId);
        clearRefreshCookie(response);
        return ResponseEntity.ok().build();
    }

    // ── Cookie helpers ────────────────────────────────────────────────────────

    /** RT 쿠키 세팅: SameSite=None + Secure + HttpOnly */
    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        long maxAgeSec = Math.max(1, jwtTokenProvider.getRemainingSeconds(refreshToken)); // 만료 동기화
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
