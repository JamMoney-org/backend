package com.example.jammoney.auth.controller;

import com.example.jammoney.auth.dto.TokenResponseDto;
import com.example.jammoney.auth.jwt.JwtTokenProvider;
import com.example.jammoney.user.dto.LoginRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@Valid @RequestBody LoginRequestDto requestDto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.getEmail(), requestDto.getPassword())
        );

        String accessToken = jwtTokenProvider.generateAccessToken(requestDto.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(requestDto.getEmail());

        return ResponseEntity.ok(new TokenResponseDto(accessToken, refreshToken));
    }
}
