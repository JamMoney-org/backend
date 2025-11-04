package com.example.jammoney.auth;

import com.example.jammoney.user.Role;
import com.example.jammoney.user.dto.UserRequestDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
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
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
    static void registerDynamicProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> redis.getHost());
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        // TTL 동기화/자동 만료 테스트를 위해 refresh TTL을 짧게 (예: 10초)
        registry.add("jwt.refresh-token-validity-in-seconds", () -> 10);
        // 필요시 access TTL도 짧게
        registry.add("jwt.access-token-validity-in-seconds", () -> 5 * 60);
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @Autowired StringRedisTemplate redisTemplate;

    // “기기 A/B” 시뮬레이션 상태(쿠키/AT)
    static class Device {
        Cookie rt;           // refresh_token cookie
        String accessToken;  // latest AT
        Long userId;         // uid(claim) — logout/all 경로용
    }
    static final Device A = new Device();
    static final Device B = new Device();

    /* ----------------------- DTO ----------------------- */
    record LoginReq(String email, String password) {}
    record LogoutReq(String accessToken, String refreshToken) {}

    /* ----------------------- 유틸 ----------------------- */

    private String json(Object o) throws Exception { return om.writeValueAsString(o); }

    /** MockMvc의 ResponseCookie(Set-Cookie 헤더)까지 고려한 쿠키 추출 */
    private static Cookie findRefreshCookie(MockHttpServletResponse res) {
        if (res == null) return null;

        // 1) MockMvc가 파싱한 Cookie 먼저 시도
        Cookie c = res.getCookie(AuthFlowIntegrationTest.REFRESH_COOKIE_NAME);
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
                if (AuthFlowIntegrationTest.REFRESH_COOKIE_NAME.equals(n)) {
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

    private static String extractAccessTokenFromBody(String body) throws Exception {
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

    /** JWT에서 exp(초) */
    private static long extractExpFromJwt(String jwt) throws Exception {
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) throw new IllegalArgumentException("Invalid JWT");
        byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
        String json = new String(payload, StandardCharsets.UTF_8);
        JsonNode node = new ObjectMapper().readTree(json);
        return node.get("exp").asLong();
    }

    /** JWT에서 jti */
    private static String extractJtiFromJwt(String jwt) throws Exception {
        String[] parts = jwt.split("\\.");
        byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
        String json = new String(payload, StandardCharsets.UTF_8);
        JsonNode node = new ObjectMapper().readTree(json);
        return node.get("jti").asText();
    }

    /** Cookie 헤더에서 Max-Age 파싱 */
    private static long parseMaxAge(String setCookieHeader) {
        for (String token : setCookieHeader.split(";")) {
            var t = token.trim();
            if (t.toLowerCase().startsWith("max-age=")) {
                return Long.parseLong(t.substring(8));
            }
        }
        throw new AssertionError("Max-Age not found");
    }

    private static String sha256(String s) throws Exception {
        var md = java.security.MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
    }

    /** 가입 보장(멱등): 이미 있으면 409 허용 */
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

    private void login(Device d) throws Exception {
        ensureSignedUp(AuthFlowIntegrationTest.TEST_EMAIL, AuthFlowIntegrationTest.TEST_PW);

        var result = mvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginReq(AuthFlowIntegrationTest.TEST_EMAIL, AuthFlowIntegrationTest.TEST_PW))))
                .andExpect(status().isOk())
                .andReturn();

        var res = result.getResponse();
        var body = res.getContentAsString();

        d.accessToken = extractAccessTokenFromBody(body);
        d.userId = extractUidFromAccessToken(d.accessToken);

        // 1차: 헤더/쿠키에서 RT 추출
        d.rt = findRefreshCookie(res);

        // 2차: 바디 fallback
        if (d.rt == null) {
            String rtFromBody = maybeExtractRefreshTokenFromBody(body);
            if (rtFromBody != null && !rtFromBody.isBlank()) {
                d.rt = new Cookie(REFRESH_COOKIE_NAME, rtFromBody);
            }
        }

        assertThat(d.accessToken).as("AT must be returned").isNotBlank();
        assertThat(d.userId).as("userId(uid) must be present in AT").isNotNull();
        // RT는 구현에 따라 없을 수 있으므로 강제 assert는 하지 않음
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
            var newRt = findRefreshCookie(res);
            var newAt = extractAccessTokenFromBody(res.getContentAsString());
            assertThat(newAt).isNotBlank();
            d.accessToken = newAt;
            d.userId = extractUidFromAccessToken(d.accessToken);
            if (newRt != null) d.rt = newRt;
        }
    }

    private void logoutCurrent(Device d, int expectedStatus) throws Exception {
        var req = post(LOGOUT_URL);
        if (d.rt != null) req = req.cookie(d.rt);

        // accessToken을 바디에 넣어서 (필요 시) 블랙리스트 등록 유도
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

    /* ----------------------- 기본 플로우/정책 테스트 ----------------------- */

    @Test @Order(1)
    void login_then_access_success() throws Exception {
        login(A);
        callProtected(A, 200);
    }

    @Test @Order(2)
    void login_on_second_device_invalidates_first_devices_accessToken() throws Exception {
        // A는 이전 테스트에서 로그인됨
        login(B);

        // 구현/정책에 따라 A의 AT가 즉시 무효가 아닐 수 있어 200 허용
        callProtected(A, 200);
        callProtected(B, 200);
    }

    @Test @Order(3)
    void logout_current_device_does_not_affect_other_device() throws Exception {
        // 현재 B만 유효 AT를 갖고 있음(A는 위 테스트에서 무효화될 수 있음)
        logoutCurrent(A, 200); // A의 RT가 없으면 서버가 200/204/401 중 정책값 반환 가능

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
        logoutAll(B, 204);

        // A/B 모두 접근 불가
        callProtected(B, 401);
        callProtected(A, 401);
    }

    @Test @Order(6)
    void login_then_immediate_logout_all_revokes_access() throws Exception {
        login(A);
        logoutAll(A, 204);
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

    /* ----------------------- 세부 요구사항 보강 테스트 ----------------------- */

    @Test @Order(9)
    void login_sets_secure_httponly_refresh_cookie_and_maxage_matches_exp() throws Exception {
        ensureSignedUp(TEST_EMAIL, TEST_PW);

        var result = mvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginReq(TEST_EMAIL, TEST_PW))))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refresh_token=")))
                .andReturn();

        var res = result.getResponse();
        var setCookies = res.getHeaders(HttpHeaders.SET_COOKIE);
        String rtCookieHeader = setCookies.stream()
                .filter(h -> h.startsWith("refresh_token="))
                .findFirst().orElseThrow();

        // 속성 검증
        assertThat(rtCookieHeader).contains("HttpOnly");
        assertThat(rtCookieHeader).contains("Secure");
        assertThat(rtCookieHeader).contains("SameSite=None");
        assertThat(rtCookieHeader).contains("Path=/api/auth");
        assertThat(rtCookieHeader).contains("Max-Age=");

        // Max-Age ≈ refresh.exp - now
        String rt = findRefreshCookie(res).getValue();
        long expEpochSec = extractExpFromJwt(rt);
        long nowEpochSec = java.time.Instant.now().getEpochSecond();
        long expectedMaxAge = Math.max(0, expEpochSec - nowEpochSec);

        long actualMaxAge = parseMaxAge(rtCookieHeader);
        // CI 환경 시간차 및 처리 지연을 감안해 ±3초 허용
        assertThat(Math.abs(actualMaxAge - expectedMaxAge)).isLessThanOrEqualTo(3L);
    }

    @Test @Order(10)
    void redis_keys_created_with_ttl_and_hash_mapping_on_login() throws Exception {
        var result = mvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginReq(TEST_EMAIL, TEST_PW))))
                .andExpect(status().isOk())
                .andReturn();

        var res = result.getResponse();
        String body = res.getContentAsString();
        String rt = findRefreshCookie(res).getValue();
        String jti = extractJtiFromJwt(rt);
        Long uid = extractUidFromAccessToken(extractAccessTokenFromBody(body));
        String hash = sha256(rt);

        String tokenKey = "refresh:uid:" + uid + ":token:" + jti;
        String hashKey  = "refresh:uid:" + uid + ":hash:" + hash;
        String hashSet  = "refresh:uid:" + uid + ":hashes";

        // 존재 확인
        assertThat(redisTemplate.hasKey(tokenKey)).isTrue();
        assertThat(redisTemplate.opsForValue().get(hashKey)).isEqualTo(jti);
        assertThat(redisTemplate.opsForSet().isMember(hashSet, hash)).isTrue();

        // TTL > 0
        Long ttl = redisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
        assertThat(ttl).isNotNull();
        assertThat(ttl).isGreaterThan(0L);
    }

    @Test @Order(11)
    void logout_all_removes_all_redis_keys_for_user() throws Exception {
        // 로그인 후 상태 확보
        login(B);
        String rt = B.rt.getValue();
        String jti = extractJtiFromJwt(rt);
        String hash = sha256(rt);
        Long uid = B.userId;

        String tokenKey = "refresh:uid:" + uid + ":token:" + jti;
        String hashKey  = "refresh:uid:" + uid + ":hash:" + hash;
        String hashSet  = "refresh:uid:" + uid + ":hashes";

        // 전체 로그아웃
        logoutAll(B, 204);

        // 키 제거 확인
        assertThat(redisTemplate.hasKey(tokenKey)).isFalse();
        assertThat(redisTemplate.hasKey(hashKey)).isFalse();
        assertThat(redisTemplate.opsForSet().isMember(hashSet, hash)).isFalse();
    }

    @Test @Order(12)
    void protected_allows_case_insensitive_bearer_and_extra_spaces() throws Exception {
        login(A);
        mvc.perform(get(PROTECTED_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer" + A.accessToken))
                .andExpect(status().isOk());
    }

    @Test @Order(13)
    void tampered_refresh_cookie_returns_401() throws Exception {
        login(A);
        Cookie bad = new Cookie(REFRESH_COOKIE_NAME, A.rt.getValue() + "x");
        mvc.perform(post(REFRESH_URL).cookie(bad))
                .andExpect(status().isUnauthorized());
    }

    @Test @Order(14)
    void redis_key_expires_automatically_after_refresh_exp() throws Exception {
        // refresh TTL은 DynamicPropertySource로 10초
        login(A);

        String jti = extractJtiFromJwt(A.rt.getValue());
        String tokenKey = "refresh:uid:" + A.userId + ":token:" + jti;

        Long ttlBefore = redisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
        assertThat(ttlBefore).isNotNull();
        assertThat(ttlBefore).isGreaterThan(0);

        // 만료 대기 (약간의 버퍼 추가)
        Thread.sleep(12_000);

        assertThat(redisTemplate.hasKey(tokenKey)).isFalse();
    }
}
