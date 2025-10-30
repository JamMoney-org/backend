package com.example.jammoney.auth.jwt;

import com.example.jammoney.auth.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    // 인증 불필요(익명 허용) 엔드포인트만 명시
    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout"
    };
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /** Preflight, 공개 엔드포인트는 필터 패스 */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true;
        String path = request.getRequestURI();
        for (String p : PUBLIC_ENDPOINTS) {
            if (pathMatcher.match(p, path)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        final String token = resolveBearer(request.getHeader(HttpHeaders.AUTHORIZATION));

        try {
            // 인증 객체가 아직 없고, 토큰이 있으면 검증 시작
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 블랙리스트 선차단
                if (refreshTokenService.isAccessTokenBlacklisted(token)) {
                    unauthorized(response, "Token blacklisted");
                    return;
                }

                // 파싱 및 검증
                final Claims claims;
                try {
                    claims = jwtTokenProvider.parseClaims(token);
                    if (claims == null) {
                        unauthorized(response, "Invalid or expired token");
                        return;
                    }
                } catch (JwtException je) {
                    // 만료/서명 오류 등
                    unauthorized(response, "Invalid or expired token");
                    return;
                }

                // Authorization 헤더로 들어온 Refresh 토큰은 차단
                if (jwtTokenProvider.isRefreshToken(claims)) {
                    unauthorized(response, "Refresh token not allowed");
                    return;
                }

                // 최종 인증 객체 세팅 (UserDetails 로드 포함)
                SecurityContextHolder.getContext().setAuthentication(jwtTokenProvider.getAuthentication(claims));
            }
        } catch (Exception e) {
            // 예상치 못한 예외는 401로 정리(민감정보 로깅 금지)
            log.debug("[JWT] authentication failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        chain.doFilter(request, response);
    }

    private String resolveBearer(String header) {
        if (header == null || header.isBlank()) return null;
        // "Bearer " 대소문자 무시
        if (!header.regionMatches(true, 0, "Bearer ", 0, 7)) return null;
        String token = header.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    private void unauthorized(HttpServletResponse res, String msg) throws IOException {
        res.setHeader("WWW-Authenticate",
                "Bearer error=\"invalid_token\", error_description=\"" + msg + "\"");
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"code\":\"invalid_token\",\"message\":\"" + msg + "\"}");
    }
}
