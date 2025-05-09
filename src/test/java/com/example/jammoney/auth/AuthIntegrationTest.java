package com.example.jammoney.auth;


import com.example.jammoney.auth.dto.TokenResponseDto;
import com.example.jammoney.auth.entity.RefreshToken;
import com.example.jammoney.auth.repository.RefreshTokenRepository;
import com.example.jammoney.user.Role;
import com.example.jammoney.user.dto.LoginRequestDto;
import com.example.jammoney.user.dto.UserRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    private String password = "Test1234!";

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Test
    @DisplayName("회원가입 → 로그인 → 보호 API 접근")
    void signup_then_login_then_access_protected_api() throws Exception {
        String email = signup();
        String accessToken = login(email, password);
        mockMvc.perform(get("/api/protected").header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("인증된 사용자")));
    }

    @Test
    @DisplayName("로그인 실패: 잘못된 비밀번호")
    void login_fail_wrong_password() throws Exception {
        String email = signup();
        LoginRequestDto loginDto = new LoginRequestDto(email, "WrongPassword123!");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("보호 API 접근 실패: 토큰 없음")
    void access_protected_without_token() throws Exception {
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("RefreshToken으로 AccessToken 재발급")
    void refresh_token_success() throws Exception {
        String email = signup();
        ResultActions loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto(email, password))))
                .andExpect(status().isOk());

        String response = loginResult.andReturn().getResponse().getContentAsString();
        TokenResponseDto tokenDto = objectMapper.readValue(response, TokenResponseDto.class);
        List<RefreshToken> tokens = refreshTokenRepository.findAll();
        System.out.println("🔍 토큰 개수: " + tokens.size());
        tokens.forEach(rt -> {
            System.out.println("Token: " + rt.getToken());
            System.out.println("Expiry: " + rt.getExpiryDate());
            System.out.println("User Email: " + rt.getUser().getEmail());
        });
        mockMvc.perform(post("/auth/refresh")
                        .param("refreshToken", tokenDto.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    @DisplayName("회원가입 실패: 중복 이메일")
    void signup_fail_duplicate_email() throws Exception {
        String email = signup();
        UserRequestDto duplicateDto = new UserRequestDto(email, password, "Tester2", Role.ROLE_USER);
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DuplicateKeyError"));
    }

    @Test
    @DisplayName("RefreshToken 만료 시 AccessToken 재발급 실패")
    void refresh_token_expired() throws Exception {
        String email = signup();
        ResultActions loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto(email, password))))
                .andExpect(status().isOk());

        String response = loginResult.andReturn().getResponse().getContentAsString();
        TokenResponseDto tokenDto = objectMapper.readValue(response, TokenResponseDto.class);

        mockMvc.perform(post("/auth/refresh")
                        .param("refreshToken", "expired-refresh-token-value"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @DisplayName("RefreshToken 만료 임박 시 새로 발급됨 (Sliding Expiration)")
    void refresh_token_near_expiry_sliding_renewal() throws Exception {
        // 1. 회원가입 및 로그인
        String email1 = signup();
        ResultActions loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto(email1, password))))
                .andExpect(status().isOk());

        String response = loginResult.andReturn().getResponse().getContentAsString();
        TokenResponseDto tokenDto = objectMapper.readValue(response, TokenResponseDto.class);

        // 2. 기존 RefreshToken의 만료 시간을 강제로 2일 후로 변경 (3일 이하)
        List<RefreshToken> tokens = refreshTokenRepository.findAll();
        RefreshToken original = tokens.get(0);
        original.setExpiryDate(LocalDateTime.now().plusDays(2));
        refreshTokenRepository.save(original);

        // 3. 리프레시 요청
        ResultActions refreshResult = mockMvc.perform(post("/auth/refresh")
                        .param("refreshToken", tokenDto.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

        String newResponse = refreshResult.andReturn().getResponse().getContentAsString();
        TokenResponseDto newTokenDto = objectMapper.readValue(newResponse, TokenResponseDto.class);

        // 4. 새로 발급된 refreshToken이 기존 것과 다른지 확인
        assert !newTokenDto.getRefreshToken().equals(tokenDto.getRefreshToken());
    }

    @Test
    @DisplayName("회원가입 실패: 잘못된 이메일 형식")
    void signup_fail_invalid_email_format() throws Exception {
        UserRequestDto dto = new UserRequestDto("invalid-email", password, "Tester", Role.ROLE_USER);
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("이메일 형식에 맞지 않습니다.")));
    }

    @Test
    @DisplayName("회원가입 실패: 비밀번호 형식 불일치")
    void signup_fail_invalid_password_format() throws Exception {
        UserRequestDto dto = new UserRequestDto("newuser@example.com", "weakpass", "Tester", Role.ROLE_USER);
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(Matchers.containsString("비밀번호는 8~20자")));
    }

    private String signup() throws Exception {
        String uniqueEmail = "test" + System.nanoTime() + "@example.com";
        String uniqueNickname = "Tester" + System.nanoTime();
        UserRequestDto dto = new UserRequestDto(uniqueEmail, password, uniqueNickname, Role.ROLE_USER);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        return uniqueEmail;
    }

    private String login(String email, String password) throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto(email, password);
        ResultActions result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }
}

