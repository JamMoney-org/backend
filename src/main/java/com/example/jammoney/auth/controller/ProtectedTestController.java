package com.example.jammoney.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProtectedTestController {

    @GetMapping("/api/protected")
    public String protectedApi() {
        return "인증된 사용자만 접근 가능";
    }
}
