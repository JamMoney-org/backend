package com.example.jammoney.auth.jwt;

import com.example.jammoney.auth.service.CustomUserDetailsService;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");
            String requestURI = request.getRequestURI();

            if ("/auth/refresh".equals(requestURI)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (!jwtTokenProvider.validateToken(token)) {
                    throw new InvalidJwtTokenException();
                }

                String userEmail = jwtTokenProvider.getEmailFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (InvalidJwtTokenException ex) {
            setErrorResponse(response, ErrorCode.INVALID_TOKEN);
        } catch (Exception ex) {
            ex.printStackTrace();
            setErrorResponse(response, ErrorCode.INVALID_LOGIN);
        }
    }
    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus());
        response.setContentType("application/json; charset=UTF-8");

        ErrorResponseDto error = new ErrorResponseDto(
                errorCode.getStatus(),
                errorCode.name(),
                errorCode.getMessage(),
                "" // URI는 request.getRequestURI()를 파라미터로 받으면 넣을 수 있음
        );

        ObjectMapper mapper = new ObjectMapper();
        String responseBody = mapper.writeValueAsString(error);
        response.getWriter().write(responseBody);
    }
}

