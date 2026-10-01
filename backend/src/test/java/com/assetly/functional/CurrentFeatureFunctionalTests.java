package com.assetly.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CurrentFeatureFunctionalTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("FT-COM-001 헬스체크는 서비스 상태를 반환한다")
    void healthCheck() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("assetly-backend"))
                .andExpect(jsonPath("$.data.checkedAt").isNotEmpty());
    }

    @Test
    @DisplayName("FT-AUTH-001 올바른 정보로 회원가입한다")
    void signup() throws Exception {
        mockMvc.perform(postJson("/api/auth/signup", signupBody("user@example.com", "password1234", "홍길동")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @DisplayName("FT-AUTH-002 중복 이메일 가입을 거절한다")
    void rejectDuplicatedEmail() throws Exception {
        signup("duplicate@example.com", "password1234", "첫 사용자");

        mockMvc.perform(postJson("/api/auth/signup", signupBody("duplicate@example.com", "password5678", "두 번째 사용자")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("USER_EMAIL_DUPLICATED"))
                .andExpect(jsonPath("$.error.message").isNotEmpty());
    }

    @Test
    @DisplayName("FT-AUTH-003 회원가입 입력값을 검증한다")
    void validateSignupRequest() throws Exception {
        mockMvc.perform(postJson("/api/auth/signup", signupBody("invalid-email", "password1234", "사용자")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(postJson("/api/auth/signup", signupBody("short@example.com", "short", "사용자")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(postJson("/api/auth/signup", signupBody("blank@example.com", "password1234", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("FT-AUTH-004 가입한 계정으로 로그인하고 JWT를 발급받는다")
    void login() throws Exception {
        signup("login@example.com", "password1234", "로그인 사용자");

        mockMvc.perform(postJson("/api/auth/login", loginBody("login@example.com", "password1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.email").value("login@example.com"))
                .andExpect(jsonPath("$.data.user.name").value("로그인 사용자"));
    }

    @Test
    @DisplayName("FT-AUTH-005 잘못된 로그인 정보를 동일한 에러로 거절한다")
    void rejectInvalidCredentials() throws Exception {
        signup("credential@example.com", "password1234", "인증 사용자");

        mockMvc.perform(postJson("/api/auth/login", loginBody("credential@example.com", "wrong-password")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_CREDENTIALS"));

        mockMvc.perform(postJson("/api/auth/login", loginBody("unknown@example.com", "wrong-password")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("AUTH_INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("FT-AUTH-006 유효한 JWT로 내 정보를 조회한다")
    void findMe() throws Exception {
        String token = signupAndLogin("me@example.com", "내 정보 사용자");

        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("me@example.com"))
                .andExpect(jsonPath("$.data.name").value("내 정보 사용자"));
    }

    @Test
    @DisplayName("FT-AUTH-007 토큰이 없거나 잘못되면 인증 필요 응답을 반환한다")
    void requireAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_UNAUTHORIZED"));

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_UNAUTHORIZED"));

        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_UNAUTHORIZED"));
    }

    @Test
    @DisplayName("FT-ORG-001 가입 직후 내 조직 목록은 비어 있다")
    void emptyOrganizationList() throws Exception {
        String token = signupAndLogin("empty-org@example.com", "조직 없는 사용자");

        mockMvc.perform(get("/api/organizations").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("FT-ORG-002 조직을 만들면 생성자가 OWNER가 되고 목록에 표시된다")
    void createAndFindOrganization() throws Exception {
        String token = signupAndLogin("owner@example.com", "조직 소유자");

        mockMvc.perform(postJson("/api/organizations", Map.of(
                        "name", "테스트 조직",
                        "description", "기능 테스트 조직"
                )).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("테스트 조직"))
                .andExpect(jsonPath("$.data.description").value("기능 테스트 조직"))
                .andExpect(jsonPath("$.data.myRole").value("OWNER"));

        mockMvc.perform(get("/api/organizations").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("테스트 조직"))
                .andExpect(jsonPath("$.data[0].myRole").value("OWNER"));
    }

    @Test
    @DisplayName("FT-ORG-003 조직 생성 입력값을 검증한다")
    void validateOrganizationRequest() throws Exception {
        String token = signupAndLogin("org-validation@example.com", "검증 사용자");

        mockMvc.perform(postJson("/api/organizations", Map.of("name", "", "description", "설명"))
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));

        mockMvc.perform(postJson("/api/organizations", Map.of("name", "가".repeat(101), "description", "설명"))
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("FT-ORG-004 사용자는 다른 사용자의 조직을 조회할 수 없다")
    void isolateOrganizationsByUser() throws Exception {
        String firstToken = signupAndLogin("first@example.com", "첫 사용자");
        String secondToken = signupAndLogin("second@example.com", "두 번째 사용자");

        mockMvc.perform(postJson("/api/organizations", Map.of("name", "첫 조직", "description", "비공개"))
                        .header("Authorization", bearer(firstToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/organizations").header("Authorization", bearer(secondToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    private void signup(String email, String password, String name) throws Exception {
        mockMvc.perform(postJson("/api/auth/signup", signupBody(email, password, name)))
                .andExpect(status().isOk());
    }

    private String signupAndLogin(String email, String name) throws Exception {
        String password = "password1234";
        signup(email, password, name);
        MvcResult result = mockMvc.perform(postJson("/api/auth/login", loginBody(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.path("data").path("accessToken").asText();
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder postJson(
            String path,
            Object body
    ) throws Exception {
        return post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    private Map<String, String> signupBody(String email, String password, String name) {
        return Map.of("email", email, "password", password, "name", name);
    }

    private Map<String, String> loginBody(String email, String password) {
        return Map.of("email", email, "password", password);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
