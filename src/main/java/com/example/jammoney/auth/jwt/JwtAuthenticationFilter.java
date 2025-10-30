package com.example.jammoney.auth.jwt;

import java.io.IOException;
import java.util.List;

import com.example.jammoney.auth.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    private static final List<String> EXCLUDE_PATTERNS = List.of(
            "/auth/**",
            "/api/auth/**"
    );
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Preflight, EXCLUDE_PATTERNS에 속하는 url들은 필터링 x
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true; // Preflight 통과
        String path = request.getRequestURI();
        for (String p : EXCLUDE_PATTERNS) if (pathMatcher.match(p, path)) return true;
        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        // Authorization 헤더에서 Bearer 토큰만 추출
        String token = resolveBearer(request.getHeader(HttpHeaders.AUTHORIZATION));

        try {
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 토큰으로 클레임 파싱
                Claims claims = jwtTokenProvider.parseClaims(token);
                if (claims == null) {
                    unauthorized(response, "Invalid or expired token");
                    return;
                }

                // 로그아웃/차단된 액세스 토큰이면 즉시 거절
                if (refreshTokenService.isAccessTokenBlacklisted(token)) {
                    unauthorized(response, "Token blacklisted");
                    return;
                }

                // 보호 경로에서 refresh 토큰 사용 시 401
                if (jwtTokenProvider.isRefreshToken(claims)) {
                    unauthorized(response, "Refresh token not allowed");
                    return;
                }

                SecurityContextHolder.getContext().setAuthentication(jwtTokenProvider.getAuthentication(claims));
            }
        } catch (Exception e) {
            log.debug("[JWT] authentication set failed: {}", e.getMessage());
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
        res.setHeader("WWW-Authenticate", "Bearer error=\"" + "invalid_token" + "\", error_description=\"" + msg + "\"");
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"code\":\"" + "invalid_token" + "\",\"message\":\"" + msg + "\"}");
    }
}
