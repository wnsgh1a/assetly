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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReferenceDataFunctionalTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("FT-LOC-001 Owner가 위치를 생성한다")
    void ownerCreatesLocation() throws Exception {
        Workspace workspace = workspace("loc-create");

        createLocation(workspace.ownerToken(), workspace.organizationId(), "  본관 3층  ", "  개발팀 좌석  ")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("본관 3층"))
                .andExpect(jsonPath("$.data.description").value("개발팀 좌석"));
    }

    @Test
    @DisplayName("FT-LOC-002 조직 멤버가 위치 목록을 이름순으로 조회한다")
    void memberFindsLocations() throws Exception {
        Workspace workspace = workspace("loc-list");
        String memberToken = signupAndLogin("loc-list-member@example.com", "위치 조회 멤버");
        addMember(workspace.ownerToken(), workspace.organizationId(), "loc-list-member@example.com", "MEMBER");
        createLocation(workspace.ownerToken(), workspace.organizationId(), "창고", null);
        createLocation(workspace.ownerToken(), workspace.organizationId(), "개발실", null);

        mockMvc.perform(get("/api/organizations/{organizationId}/locations", workspace.organizationId())
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("개발실"))
                .andExpect(jsonPath("$.data[1].name").value("창고"));
    }

    @Test
    @DisplayName("FT-LOC-003 Admin이 위치를 수정한다")
    void adminUpdatesLocation() throws Exception {
        Workspace workspace = workspace("loc-update");
        String adminToken = signupAndLogin("loc-update-admin@example.com", "위치 관리자");
        addMember(workspace.ownerToken(), workspace.organizationId(), "loc-update-admin@example.com", "ADMIN");
        long locationId = createLocationId(workspace.ownerToken(), workspace.organizationId(), "기존 위치");

        mockMvc.perform(patchJson(
                        "/api/organizations/" + workspace.organizationId() + "/locations/" + locationId,
                        Map.of("name", "변경 위치", "description", "별관 2층")
                ).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("변경 위치"))
                .andExpect(jsonPath("$.data.description").value("별관 2층"));
    }

    @Test
    @DisplayName("FT-LOC-004 Owner가 위치를 삭제한다")
    void ownerDeletesLocation() throws Exception {
        Workspace workspace = workspace("loc-delete");
        long locationId = createLocationId(workspace.ownerToken(), workspace.organizationId(), "삭제 위치");

        mockMvc.perform(delete("/api/organizations/{organizationId}/locations/{locationId}",
                        workspace.organizationId(), locationId)
                        .header("Authorization", bearer(workspace.ownerToken())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/organizations/{organizationId}/locations", workspace.organizationId())
                        .header("Authorization", bearer(workspace.ownerToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("FT-LOC-005 위치 이름 중복과 입력 오류를 차단한다")
    void validatesLocation() throws Exception {
        Workspace workspace = workspace("loc-validation");
        createLocation(workspace.ownerToken(), workspace.organizationId(), "회의실", null)
                .andExpect(status().isOk());

        createLocation(workspace.ownerToken(), workspace.organizationId(), "회의실", null)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("LOCATION_NAME_DUPLICATED"));

        createLocation(workspace.ownerToken(), workspace.organizationId(), " ", null)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("FT-LOC-006 위치 권한과 조직 간 리소스를 격리한다")
    void protectsLocationAccess() throws Exception {
        Workspace first = workspace("loc-first");
        Workspace second = workspace("loc-second");
        String memberToken = signupAndLogin("loc-protect-member@example.com", "위치 일반 멤버");
        addMember(first.ownerToken(), first.organizationId(), "loc-protect-member@example.com", "MEMBER");
        long foreignLocationId = createLocationId(second.ownerToken(), second.organizationId(), "다른 조직 위치");

        createLocation(memberToken, first.organizationId(), "권한 없는 생성", null)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));

        mockMvc.perform(get("/api/organizations/{organizationId}/locations", second.organizationId())
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("ORGANIZATION_NOT_FOUND"));

        mockMvc.perform(patchJson(
                        "/api/organizations/" + first.organizationId() + "/locations/" + foreignLocationId,
                        Map.of("name", "조작 시도")
                ).header("Authorization", bearer(first.ownerToken())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("LOCATION_NOT_FOUND"));
    }

    @Test
    @DisplayName("FT-CAT-001 Owner가 카테고리를 생성한다")
    void ownerCreatesCategory() throws Exception {
        Workspace workspace = workspace("cat-create");

        createCategory(workspace.ownerToken(), workspace.organizationId(), "  노트북  ")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("노트북"));
    }

    @Test
    @DisplayName("FT-CAT-002 조직 멤버가 카테고리 목록을 이름순으로 조회한다")
    void memberFindsCategories() throws Exception {
        Workspace workspace = workspace("cat-list");
        String memberToken = signupAndLogin("cat-list-member@example.com", "카테고리 조회 멤버");
        addMember(workspace.ownerToken(), workspace.organizationId(), "cat-list-member@example.com", "MEMBER");
        createCategory(workspace.ownerToken(), workspace.organizationId(), "모니터");
        createCategory(workspace.ownerToken(), workspace.organizationId(), "노트북");

        mockMvc.perform(get("/api/organizations/{organizationId}/categories", workspace.organizationId())
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].name").value("노트북"))
                .andExpect(jsonPath("$.data[1].name").value("모니터"));
    }

    @Test
    @DisplayName("FT-CAT-003 Admin이 카테고리를 수정한다")
    void adminUpdatesCategory() throws Exception {
        Workspace workspace = workspace("cat-update");
        String adminToken = signupAndLogin("cat-update-admin@example.com", "카테고리 관리자");
        addMember(workspace.ownerToken(), workspace.organizationId(), "cat-update-admin@example.com", "ADMIN");
        long categoryId = createCategoryId(workspace.ownerToken(), workspace.organizationId(), "기존 분류");

        mockMvc.perform(patchJson(
                        "/api/organizations/" + workspace.organizationId() + "/categories/" + categoryId,
                        Map.of("name", "변경 분류")
                ).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("변경 분류"));
    }

    @Test
    @DisplayName("FT-CAT-004 Owner가 카테고리를 삭제한다")
    void ownerDeletesCategory() throws Exception {
        Workspace workspace = workspace("cat-delete");
        long categoryId = createCategoryId(workspace.ownerToken(), workspace.organizationId(), "삭제 분류");

        mockMvc.perform(delete("/api/organizations/{organizationId}/categories/{categoryId}",
                        workspace.organizationId(), categoryId)
                        .header("Authorization", bearer(workspace.ownerToken())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/organizations/{organizationId}/categories", workspace.organizationId())
                        .header("Authorization", bearer(workspace.ownerToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("FT-CAT-005 카테고리 이름 중복과 입력 오류를 차단한다")
    void validatesCategory() throws Exception {
        Workspace workspace = workspace("cat-validation");
        createCategory(workspace.ownerToken(), workspace.organizationId(), "카메라")
                .andExpect(status().isOk());

        createCategory(workspace.ownerToken(), workspace.organizationId(), "카메라")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CATEGORY_NAME_DUPLICATED"));

        createCategory(workspace.ownerToken(), workspace.organizationId(), " ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("FT-CAT-006 카테고리 권한과 조직 간 리소스를 격리한다")
    void protectsCategoryAccess() throws Exception {
        Workspace first = workspace("cat-first");
        Workspace second = workspace("cat-second");
        String managerToken = signupAndLogin("cat-protect-manager@example.com", "카테고리 매니저");
        addMember(first.ownerToken(), first.organizationId(), "cat-protect-manager@example.com", "MANAGER");
        long foreignCategoryId = createCategoryId(second.ownerToken(), second.organizationId(), "다른 조직 분류");

        createCategory(managerToken, first.organizationId(), "권한 없는 생성")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));

        mockMvc.perform(get("/api/organizations/{organizationId}/categories", second.organizationId())
                        .header("Authorization", bearer(managerToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("ORGANIZATION_NOT_FOUND"));

        mockMvc.perform(patchJson(
                        "/api/organizations/" + first.organizationId() + "/categories/" + foreignCategoryId,
                        Map.of("name", "조작 시도")
                ).header("Authorization", bearer(first.ownerToken())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("CATEGORY_NOT_FOUND"));
    }

    private Workspace workspace(String prefix) throws Exception {
        String token = signupAndLogin(prefix + "-owner@example.com", prefix + " 소유자");
        return new Workspace(token, createOrganization(token, prefix + " 조직"));
    }

    private ResultActions createLocation(
            String token,
            long organizationId,
            String name,
            String description
    ) throws Exception {
        return mockMvc.perform(postJson(
                "/api/organizations/" + organizationId + "/locations",
                new LocationBody(name, description)
        ).header("Authorization", bearer(token)));
    }

    private long createLocationId(String token, long organizationId, String name) throws Exception {
        MvcResult result = createLocation(token, organizationId, name, null)
                .andExpect(status().isOk())
                .andReturn();
        return responseData(result).path("id").asLong();
    }

    private ResultActions createCategory(String token, long organizationId, String name) throws Exception {
        return mockMvc.perform(postJson(
                "/api/organizations/" + organizationId + "/categories",
                Map.of("name", name)
        ).header("Authorization", bearer(token)));
    }

    private long createCategoryId(String token, long organizationId, String name) throws Exception {
        MvcResult result = createCategory(token, organizationId, name)
                .andExpect(status().isOk())
                .andReturn();
        return responseData(result).path("id").asLong();
    }

    private void addMember(String token, long organizationId, String email, String role) throws Exception {
        mockMvc.perform(postJson(
                        "/api/organizations/" + organizationId + "/members",
                        Map.of("email", email, "role", role)
                ).header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    private long createOrganization(String token, String name) throws Exception {
        MvcResult result = mockMvc.perform(postJson(
                        "/api/organizations",
                        Map.of("name", name, "description", "기준 정보 기능 테스트")
                ).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        return responseData(result).path("id").asLong();
    }

    private String signupAndLogin(String email, String name) throws Exception {
        String password = "password1234";
        mockMvc.perform(postJson("/api/auth/signup", Map.of(
                        "email", email,
                        "password", password,
                        "name", name
                )))
                .andExpect(status().isOk());
        MvcResult result = mockMvc.perform(postJson("/api/auth/login", Map.of(
                        "email", email,
                        "password", password
                )))
                .andExpect(status().isOk())
                .andReturn();
        return responseData(result).path("accessToken").asText();
    }

    private JsonNode responseData(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private MockHttpServletRequestBuilder postJson(String path, Object body) throws Exception {
        return post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    private MockHttpServletRequestBuilder patchJson(String path, Object body) throws Exception {
        return patch(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record Workspace(String ownerToken, long organizationId) {
    }

    private record LocationBody(String name, String description) {
    }
}
