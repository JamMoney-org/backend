package com.example.jammoney.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
@RestController
@RequiredArgsConstructor
public class CookieTestController {
    @GetMapping("/test/debug/set-cookie")
    public ResponseEntity<String> setTestCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from("__Host-rt_test", "ok")     // __Host- prefix는 host-only 쿠키 강제
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")                        // Path=/, Domain 미지정
                .maxAge(Duration.ofMinutes(10))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok("cookie set");
    }
}
