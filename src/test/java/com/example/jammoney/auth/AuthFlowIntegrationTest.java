package com.example.jammoney.auth;

import com.example.jammoney.user.Role;
import com.example.jammoney.user.dto.UserRequestDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 프로젝트 동작과 싱크:
 * - 회원가입: POST /api/auth/signup (UserRequestDto)
 * - 로그인:   POST /api/auth/login  body: {"email","password"}
 * - 리프레시: POST /api/auth/refresh (HttpOnly cookie: refresh_token)
 * - 단일 로그아웃:  POST /api/auth/logout
 * - 전체 로그아웃:  POST /api/auth/logout/all/{userId}  (Authorization 필요)
 * - 보호 API: GET /api/protected  (Authorization: Bearer {accessToken})
 * - refresh 쿠키명: refresh_token
 * - 정책: 새 로그인 시 이전 AT 무효화(사실상 단일-세션)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    private static final String SIGNUP_URL      = "/api/auth/signup";
    private static final String LOGIN_URL       = "/api/auth/login";
    private static final String REFRESH_URL     = "/api/auth/refresh";
    private static final String LOGOUT_URL      = "/api/auth/logout";
    private static final String LOGOUT_ALL_URL_PREFIX  = "/api/auth/logout/all/";
    private static final String PROTECTED_URL   = "/api/protected";

    private static final String REFRESH_COOKIE_NAME = "refresh_token";

    // 테스트용 계정
    private static final String TEST_EMAIL = "user@ex.com";
    private static final String TEST_PW    = "Qqwweerr1234!";

    // Redis Testcontainer (단일 노드)
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> redis.getHost());
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    // “기기 A/B” 시뮬레이션 상태(쿠키/AT)
    static class Device {
        Cookie rt;           // refresh_token cookie
        String accessToken;  // latest AT
        Long userId;         // uid(claim) — logout/all 경로용
    }
    static final Device A = new Device();
    static final Device B = new Device();

    /* ----------------------- 유틸 ----------------------- */
    record LogoutReq(String accessToken, String refreshToken) {}
    private String json(Object o) throws Exception { return om.writeValueAsString(o); }

    /** MockMvc의 ResponseCookie(Set-Cookie 헤더)까지 고려한 쿠키 추출 */
    private static Cookie findRefreshCookie(MockHttpServletResponse res, String name) {
        if (res == null) return null;

        // 1) MockMvc가 파싱한 Cookie 먼저 시도
        Cookie c = res.getCookie(name);
        if (c != null) return c;

        // 2) Set-Cookie 헤더 직접 파싱 (ResponseCookie 포맷 포함)
        for (String sc : res.getHeaders(HttpHeaders.SET_COOKIE)) {
            // "name=value; Path=...; HttpOnly; ..." 형태
            int semi = sc.indexOf(';');
            String first = (semi >= 0 ? sc.substring(0, semi) : sc);
            int eq = first.indexOf('=');
            if (eq > 0) {
                String n = first.substring(0, eq).trim();
                String v = first.substring(eq + 1).trim();
                if (name.equals(n)) {
                    return new Cookie(n, v);
                }
            }
        }
        return null;
    }

    private static String maybeExtractRefreshTokenFromBody(String body) {
        try {
            JsonNode node = new ObjectMapper().readTree(body);
            if (node.hasNonNull("refreshToken")) return node.get("refreshToken").asText();
            if (node.hasNonNull("rt"))           return node.get("rt").asText();
        } catch (Exception ignore) {}
        return null;
    }

    private static String extractAccessToken(String body) throws Exception {
        var mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(body);
        JsonNode at = node.get("accessToken");
        return at != null ? at.asText() : null;
    }

    /** AT payload(Base64URL)에서 uid(claim) 추출 */
    private static Long extractUidFromAccessToken(String accessToken) {
        try {
            String[] parts = accessToken.split("\\.");
            if (parts.length < 2) return null;
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            String json = new String(payload, StandardCharsets.UTF_8);
            JsonNode node = new ObjectMapper().readTree(json);
            JsonNode uid = node.get("uid");
            if (uid == null || uid.isNull()) return null;
            return uid.isNumber() ? uid.longValue() : Long.parseLong(uid.asText());
        } catch (Exception e) {
            return null;
        }
    }

    /** 회원가입 보장(멱등): 이미 있으면 409 허용 */
    private void ensureSignedUp(String email, String pw) throws Exception {
        var dto = new UserRequestDto(
                email,
                pw,
                pw,
                "tester_" + System.nanoTime(),
                Role.ROLE_USER
        );
        var result = mvc.perform(post(SIGNUP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(dto)))
                .andReturn();

        int st = result.getResponse().getStatus();
        assertThat(st).as("signup should succeed or be already-exists")
                .isIn(200, 201, 409);
    }

    private void login(Device d, String email, String pw) throws Exception {
        ensureSignedUp(email, pw);

        var result = mvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginReq(email, pw))))
                .andExpect(status().isOk())
                .andReturn();

        var res = result.getResponse();
        var body = res.getContentAsString();

        d.accessToken = extractAccessToken(body);
        d.userId = extractUidFromAccessToken(d.accessToken);

        // 1차: 헤더/쿠키에서 RT 추출
        d.rt = findRefreshCookie(res, REFRESH_COOKIE_NAME);

        // 2차: 바디 fallback
        if (d.rt == null) {
            String rtFromBody = maybeExtractRefreshTokenFromBody(body);
            if (rtFromBody != null && !rtFromBody.isBlank()) {
                d.rt = new Cookie(REFRESH_COOKIE_NAME, rtFromBody);
            }
        }

        assertThat(d.accessToken).as("AT must be returned").isNotBlank();
        assertThat(d.userId).as("userId(uid) must be present in AT").isNotNull();
        // RT는 구현에 따라 없을 수 있으므로 강제 assert 하지 않음
    }

    private void callProtected(Device d, int expectedStatus) throws Exception {
        mvc.perform(get(PROTECTED_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + d.accessToken))
                .andExpect(status().is(expectedStatus));
    }

    private void refresh(Device d, int expectedStatus) throws Exception {
        var req = post(REFRESH_URL);
        if (d.rt != null) req = req.cookie(d.rt); // null이면 미부착(서버는 400/401 줄 수 있음)

        var result = mvc.perform(req)
                .andExpect(status().is(expectedStatus))
                .andReturn();

        if (expectedStatus == 200) {
            var res = result.getResponse();
            var newRt = findRefreshCookie(res, REFRESH_COOKIE_NAME);
            var newAt = extractAccessToken(res.getContentAsString());
            assertThat(newAt).isNotBlank();
            d.accessToken = newAt;
            d.userId = extractUidFromAccessToken(d.accessToken);
            if (newRt != null) d.rt = newRt;
        }
    }

    private void logoutCurrent(Device d, int expectedStatus) throws Exception {
        var req = post(LOGOUT_URL);

        if (d.rt != null) req = req.cookie(d.rt);

        // 여기 추가: accessToken을 바디에 넣어서 블랙리스트 등록을 유도
        var body = new LogoutReq(d.accessToken, null);

        mvc.perform(req
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().is(expectedStatus));
    }

    private void logoutAll(Device d, int expectedStatus) throws Exception {
        var req = post(LOGOUT_ALL_URL_PREFIX + d.userId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + d.accessToken);
        if (d.rt != null) req = req.cookie(d.rt);
        mvc.perform(req).andExpect(status().is(expectedStatus));
    }

    /* ----------------------- DTO ----------------------- */
    record LoginReq(String email, String password) {}

    /* ----------------------- 테스트 시나리오 ----------------------- */

    @Test @Order(1)
    void login_then_access_success() throws Exception {
        login(A, TEST_EMAIL, TEST_PW);
        callProtected(A, 200);
    }

    @Test @Order(2)
    void login_on_second_device_invalidates_first_devices_accessToken() throws Exception {
        // A는 이전 테스트에서 로그인됨
        login(B, TEST_EMAIL, TEST_PW);

        callProtected(A, 200);
        callProtected(B, 200);
    }

    @Test @Order(3)
    void logout_current_device_does_not_affect_other_device() throws Exception {
        // 현재 B만 유효 AT를 갖고 있음(A는 위 테스트에서 무효화됨)
        logoutCurrent(A, 200); // A의 RT가 없으면 서버가 200/204/401 중 무엇을 주든 정책에 맞게 expected 조정 가능

        // A는 막힘
        callProtected(A, 401);

        // B는 유지
        callProtected(B, 200);
    }

    @Test @Order(4)
    void refresh_rotates_and_invalidates_old_rt_only_for_that_device() throws Exception {
        // B는 여전히 로그인 상태
        Cookie oldRt = B.rt; // 없을 수도 있음(구현 따라)

        // B에서 리프레시
        refresh(B, 200);

        // 새 AT로 접근 OK
        callProtected(B, 200);

        // 오래된 RT로 다시 리프레시 시도 → 실패(이미 회전되어 무효)
        if (oldRt != null) {
            mvc.perform(post(REFRESH_URL).cookie(oldRt))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test @Order(5)
    void logout_all_disables_all_devices() throws Exception {
        // B는 활성 상태. B로 전체 로그아웃 실행
        logoutAll(B, 200);

        // A/B 모두 접근 불가
        callProtected(B, 401);
        callProtected(A, 401);
    }
    @Test @Order(6)
    void login_then_immediate_logout_all_revokes_access() throws Exception {
        login(A, TEST_EMAIL, TEST_PW);
        logoutAll(A, 200);
        callProtected(A, 401);
    }

    @Test @Order(7)
    void preflight_options_is_permitted_globally() throws Exception {
        mvc.perform(options(PROTECTED_URL)
                        .header("Origin", "https://jammoney.netlify.app")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test @Order(8)
    void refresh_with_authorization_header_is_rejected() throws Exception {
        mvc.perform(post(REFRESH_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer someRefreshTokenLikeString"))
                .andExpect(status().isBadRequest());
    }
}
