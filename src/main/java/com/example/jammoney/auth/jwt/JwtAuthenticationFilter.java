package com.example.jammoney.auth.jwt;

import com.example.jammoney.auth.service.CustomUserDetailsService;
import com.example.jammoney.auth.service.RefreshTokenService;
import com.example.jammoney.exception.ErrorCode;
import com.example.jammoney.exception.ErrorResponseDto;
import com.example.jammoney.exception.InvalidJwtTokenException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final RefreshTokenService refreshTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");
            String requestURI = request.getRequestURI();

            // refresh 요청은 필터 통과
            if ("/auth/refresh".equals(requestURI)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // 1. AccessToken 유효성 검증
                if (!jwtTokenProvider.validateToken(token)) {
                    throw new InvalidJwtTokenException();
                }

                // 2. 블랙리스트 확인 (로그아웃된 토큰 방지)
                if (refreshTokenService.isAccessTokenBlacklisted(token)) {
                    throw new InvalidJwtTokenException();
                }

                // 3. 토큰에서 userId 추출
                Long userId = jwtTokenProvider.getUserIdFromToken(token);

                // 4. UserDetails 로드
                UserDetails userDetails = userDetailsService.loadUserById(userId);

                // 5. 인증 객체 생성 및 SecurityContextHolder 저장
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (InvalidJwtTokenException ex) {
            setErrorResponse(response, ErrorCode.INVALID_TOKEN, request.getRequestURI());
        } catch (Exception ex) {
            ex.printStackTrace();
            setErrorResponse(response, ErrorCode.INVALID_LOGIN, request.getRequestURI());
        }
    }

    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode, String path) throws IOException {
        response.setStatus(errorCode.getStatus());
        response.setContentType("application/json; charset=UTF-8");

        ErrorResponseDto error = new ErrorResponseDto(
                errorCode.getStatus(),
                errorCode.name(),
                errorCode.getMessage(),
                path
        );

        ObjectMapper mapper = new ObjectMapper();
        String responseBody = mapper.writeValueAsString(error);
        response.getWriter().write(responseBody);
    }
}
