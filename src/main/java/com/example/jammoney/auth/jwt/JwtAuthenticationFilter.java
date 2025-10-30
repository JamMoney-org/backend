package com.example.jammoney.auth.jwt;

import java.io.IOException;
import java.util.List;

import com.example.jammoney.auth.service.RefreshTokenService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
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
    private final RefreshTokenService refreshTokenService; // 블랙리스트 확인 위해 주입

    private static final List<String> EXCLUDE_PATTERNS = List.of(
            "/auth/**",
            "/api/auth/**"
    );
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

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

        String token = resolveBearer(request.getHeader(HttpHeaders.AUTHORIZATION));

        try {
            if (token != null
                    && SecurityContextHolder.getContext().getAuthentication() == null
                    && jwtTokenProvider.validate(token)) {

                // 로그아웃된 액세스 토큰 차단
                if (refreshTokenService.isAccessTokenBlacklisted(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                // 보호 경로에서 refresh 토큰 사용 시 401
                if (jwtTokenProvider.isRefreshToken(token)) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.debug("[JWT] authentication set failed: {}", e.getMessage());
            // EntryPoint/AccessDeniedHandler에게 맡김
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
}
