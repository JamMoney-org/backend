package com.example.jammoney.auth.controller;

import com.example.jammoney.auth.dto.TokenResponseDto;
import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.auth.entity.RefreshToken;
import com.example.jammoney.auth.jwt.JwtTokenProvider;
import com.example.jammoney.auth.service.RefreshTokenService;
import com.example.jammoney.user.dto.LoginRequestDto;
import com.example.jammoney.user.dto.UserRequestDto;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.service.UserService;
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
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto requestDto) {
        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestDto.getEmail(), requestDto.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();


            String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return ResponseEntity.ok(new TokenResponseDto(accessToken, refreshToken.getToken()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("로그인 실패: " + e.getMessage());
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        String email = jwtTokenProvider.getEmailFromToken(token);
        refreshTokenService.deleteByEmail(email); // DB에서 삭제
        return ResponseEntity.ok("로그아웃 완료");
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody UserRequestDto requestDto) {
        userService.signup(requestDto);
        return ResponseEntity.ok("회원가입 성공!");
    }
}
