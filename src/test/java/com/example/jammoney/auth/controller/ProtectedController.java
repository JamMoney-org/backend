package com.example.jammoney.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/protected")
public class ProtectedController {

    /**
     * 인증 성공 시 접근 가능한 보호 API
     * - Authorization: Bearer {accessToken}
     * - JwtAuthenticationFilter에서 설정한 Authentication이 주입됨
     */
    @GetMapping
    public Map<String, Object> getProtectedResource(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return Map.of("status", "unauthorized");
        }
        return Map.of(
                "status", "ok",
                "username", auth.getName(),
                "authorities", auth.getAuthorities()
        );
    }
}
