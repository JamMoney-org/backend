package com.example.jammoney.user;

import com.example.jammoney.user.dto.LoginRequestDto;
import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.user.dto.UserRequestDto;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private final String email = "test" + System.nanoTime() + "@example.com";
    private final String password = "Test1234!";
    private final String nickname = "tester";

    private void signup() throws Exception {
        UserRequestDto dto = new UserRequestDto(email, password, password, nickname, Role.ROLE_USER);
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    private String loginAndGetAccessToken() throws Exception {
        LoginRequestDto dto = new LoginRequestDto(email, password);
        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }

    @Test
    @DisplayName("회원 정보 조회 성공")
    void getCurrentUser_success() throws Exception {
        signup();
        String token = loginAndGetAccessToken();

        mockMvc.perform(get("/api/user/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.nickname").value(nickname));
    }

    @Test
    @DisplayName("회원 닉네임 변경 성공")
    void updateNickname_success() throws Exception {
        signup();
        String token = loginAndGetAccessToken();

        mockMvc.perform(patch("/api/user/nickname")
                        .header("Authorization", "Bearer " + token)
                        .param("newNickname", "newTester"))
                .andExpect(status().isOk())
                .andExpect(content().string("닉네임 변경 완료"));

        User user = userRepository.findByEmail(email).orElseThrow();
        assertThat(user.getNickname()).isEqualTo("newTester");
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deactivateUser_success() throws Exception {
        signup();
        String token = loginAndGetAccessToken();

        mockMvc.perform(delete("/api/user")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("회원 탈퇴 완료"));

        User user = userRepository.findByEmail(email).orElseThrow();
        assertThat(user.isActive()).isFalse();
    }
}
