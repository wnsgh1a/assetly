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
class OrganizationMemberFunctionalTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("FT-MEMBER-001 Owner가 가입 사용자를 조직에 추가하고 목록을 조회한다")
    void ownerAddsAndFindsMembers() throws Exception {
        String ownerToken = signupAndLogin("members-owner@example.com", "멤버 소유자");
        signupAndLogin("new-member@example.com", "새 멤버");
        long organizationId = createOrganization(ownerToken, "멤버 조직");

        addMember(ownerToken, organizationId, "new-member@example.com", "MEMBER")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("new-member@example.com"))
                .andExpect(jsonPath("$.data.role").value("MEMBER"));

        mockMvc.perform(get("/api/organizations/{organizationId}/members", organizationId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].role").value("OWNER"))
                .andExpect(jsonPath("$.data[1].email").value("new-member@example.com"));
    }

    @Test
    @DisplayName("FT-MEMBER-002 중복 멤버와 미가입 사용자의 추가를 거절한다")
    void rejectsDuplicateAndUnknownUser() throws Exception {
        String ownerToken = signupAndLogin("add-owner@example.com", "추가 소유자");
        signupAndLogin("duplicate-member@example.com", "중복 멤버");
        long organizationId = createOrganization(ownerToken, "추가 검증 조직");
        addMember(ownerToken, organizationId, "duplicate-member@example.com", "MEMBER")
                .andExpect(status().isOk());

        addMember(ownerToken, organizationId, "duplicate-member@example.com", "MANAGER")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ORGANIZATION_MEMBER_DUPLICATED"));

        addMember(ownerToken, organizationId, "unknown-member@example.com", "MEMBER")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("FT-MEMBER-003 Manager와 Member는 멤버 목록을 조회할 수 없다")
    void regularMemberCannotFindMembers() throws Exception {
        String ownerToken = signupAndLogin("list-owner@example.com", "목록 소유자");
        String memberToken = signupAndLogin("list-member@example.com", "목록 멤버");
        String managerToken = signupAndLogin("list-manager@example.com", "목록 매니저");
        long organizationId = createOrganization(ownerToken, "목록 권한 조직");
        addMember(ownerToken, organizationId, "list-member@example.com", "MEMBER")
                .andExpect(status().isOk());
        addMember(ownerToken, organizationId, "list-manager@example.com", "MANAGER")
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/organizations/{organizationId}/members", organizationId)
                        .header("Authorization", bearer(memberToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));

        mockMvc.perform(get("/api/organizations/{organizationId}/members", organizationId)
                        .header("Authorization", bearer(managerToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));
    }

    @Test
    @DisplayName("FT-MEMBER-004 Owner가 멤버 역할을 변경한다")
    void ownerChangesMemberRole() throws Exception {
        String ownerToken = signupAndLogin("role-change-owner@example.com", "역할 변경 소유자");
        signupAndLogin("role-change-member@example.com", "역할 변경 멤버");
        long organizationId = createOrganization(ownerToken, "역할 변경 조직");
        long memberId = addMemberId(ownerToken, organizationId, "role-change-member@example.com", "MEMBER");

        mockMvc.perform(patchJson(
                        "/api/organizations/" + organizationId + "/members/" + memberId + "/role",
                        Map.of("role", "MANAGER")
                ).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("MANAGER"));
    }

    @Test
    @DisplayName("FT-MEMBER-005 마지막 Owner를 강등하거나 제거할 수 없다")
    void protectsLastOwner() throws Exception {
        String ownerToken = signupAndLogin("last-owner@example.com", "마지막 소유자");
        long organizationId = createOrganization(ownerToken, "Owner 보호 조직");
        long ownerMemberId = firstMemberId(ownerToken, organizationId);

        mockMvc.perform(patchJson(
                        "/api/organizations/" + organizationId + "/members/" + ownerMemberId + "/role",
                        Map.of("role", "ADMIN")
                ).header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ORGANIZATION_LAST_OWNER_REQUIRED"));

        mockMvc.perform(delete("/api/organizations/{organizationId}/members/{memberId}", organizationId, ownerMemberId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ORGANIZATION_LAST_OWNER_REQUIRED"));
    }

    @Test
    @DisplayName("FT-MEMBER-006 Admin은 Owner와 Admin을 지정하거나 변경할 수 없다")
    void limitsAdminManagement() throws Exception {
        String ownerToken = signupAndLogin("admin-owner@example.com", "Admin 소유자");
        String adminToken = signupAndLogin("organization-admin@example.com", "조직 관리자");
        signupAndLogin("admin-target@example.com", "Admin 대상");
        signupAndLogin("admin-role-target@example.com", "Admin 역할 대상");
        long organizationId = createOrganization(ownerToken, "Admin 권한 조직");
        addMember(ownerToken, organizationId, "organization-admin@example.com", "ADMIN")
                .andExpect(status().isOk());
        long ownerMemberId = firstMemberId(ownerToken, organizationId);

        addMember(adminToken, organizationId, "admin-target@example.com", "OWNER")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));

        addMember(adminToken, organizationId, "admin-role-target@example.com", "ADMIN")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));

        addMember(adminToken, organizationId, "admin-target@example.com", "MANAGER")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("MANAGER"));

        mockMvc.perform(patchJson(
                        "/api/organizations/" + organizationId + "/members/" + ownerMemberId + "/role",
                        Map.of("role", "MEMBER")
                ).header("Authorization", bearer(adminToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));
    }

    @Test
    @DisplayName("FT-MEMBER-007 Owner가 일반 멤버를 제거한다")
    void ownerRemovesMember() throws Exception {
        String ownerToken = signupAndLogin("remove-owner@example.com", "제거 소유자");
        signupAndLogin("remove-member@example.com", "제거 멤버");
        long organizationId = createOrganization(ownerToken, "멤버 제거 조직");
        long memberId = addMemberId(ownerToken, organizationId, "remove-member@example.com", "MEMBER");

        mockMvc.perform(delete("/api/organizations/{organizationId}/members/{memberId}", organizationId, memberId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/organizations/{organizationId}/members", organizationId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("FT-MEMBER-008 다른 조직의 멤버 ID를 조작할 수 없다")
    void isolatesMemberResourcesByOrganization() throws Exception {
        String firstOwnerToken = signupAndLogin("first-member-owner@example.com", "첫 조직 소유자");
        String secondOwnerToken = signupAndLogin("second-member-owner@example.com", "둘째 조직 소유자");
        signupAndLogin("isolated-member@example.com", "격리 멤버");
        long firstOrganizationId = createOrganization(firstOwnerToken, "첫 멤버 조직");
        long secondOrganizationId = createOrganization(secondOwnerToken, "둘째 멤버 조직");
        long secondMemberId = addMemberId(
                secondOwnerToken,
                secondOrganizationId,
                "isolated-member@example.com",
                "MEMBER"
        );

        mockMvc.perform(delete(
                        "/api/organizations/{organizationId}/members/{memberId}",
                        firstOrganizationId,
                        secondMemberId
                ).header("Authorization", bearer(firstOwnerToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("ORGANIZATION_MEMBER_NOT_FOUND"));
    }

    private org.springframework.test.web.servlet.ResultActions addMember(
            String token,
            long organizationId,
            String email,
            String role
    ) throws Exception {
        return mockMvc.perform(postJson(
                "/api/organizations/" + organizationId + "/members",
                Map.of("email", email, "role", role)
        ).header("Authorization", bearer(token)));
    }

    private long addMemberId(String token, long organizationId, String email, String role) throws Exception {
        MvcResult result = addMember(token, organizationId, email, role)
                .andExpect(status().isOk())
                .andReturn();
        return responseData(result).path("id").asLong();
    }

    private long firstMemberId(String token, long organizationId) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/organizations/{organizationId}/members", organizationId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        return responseData(result).path(0).path("id").asLong();
    }

    private long createOrganization(String token, String name) throws Exception {
        MvcResult result = mockMvc.perform(postJson(
                        "/api/organizations",
                        Map.of("name", name, "description", "멤버 기능 테스트")
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

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder postJson(
            String path,
            Object body
    ) throws Exception {
        return post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder patchJson(
            String path,
            Object body
    ) throws Exception {
        return patch(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
