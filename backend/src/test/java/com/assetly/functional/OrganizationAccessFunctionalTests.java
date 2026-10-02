package com.assetly.functional;

import com.assetly.organization.MemberRole;
import com.assetly.organization.Organization;
import com.assetly.organization.OrganizationMember;
import com.assetly.organization.OrganizationMemberRepository;
import com.assetly.organization.OrganizationRepository;
import com.assetly.user.User;
import com.assetly.user.UserRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrganizationAccessFunctionalTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Test
    @DisplayName("FT-ORG-005 조직 멤버는 조직 상세를 조회한다")
    void memberFindsOrganization() throws Exception {
        String token = signupAndLogin("detail-owner@example.com", "상세 소유자");
        long organizationId = createOrganization(token, "상세 조직", "조직 상세 설명");

        mockMvc.perform(get("/api/organizations/{organizationId}", organizationId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(organizationId))
                .andExpect(jsonPath("$.data.name").value("상세 조직"))
                .andExpect(jsonPath("$.data.description").value("조직 상세 설명"))
                .andExpect(jsonPath("$.data.myRole").value("OWNER"));
    }

    @Test
    @DisplayName("FT-ORG-006 다른 조직의 상세 정보는 노출하지 않는다")
    void nonMemberCannotFindOrganization() throws Exception {
        String ownerToken = signupAndLogin("hidden-owner@example.com", "숨김 소유자");
        String outsiderToken = signupAndLogin("outsider@example.com", "외부 사용자");
        long organizationId = createOrganization(ownerToken, "비공개 조직", "외부 비공개");

        mockMvc.perform(get("/api/organizations/{organizationId}", organizationId)
                        .header("Authorization", bearer(outsiderToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("ORGANIZATION_NOT_FOUND"));
    }

    @Test
    @DisplayName("FT-ORG-007 Owner는 조직 정보를 수정한다")
    void ownerUpdatesOrganization() throws Exception {
        String token = signupAndLogin("update-owner@example.com", "수정 소유자");
        long organizationId = createOrganization(token, "수정 전 조직", "수정 전 설명");

        mockMvc.perform(patchJson("/api/organizations/" + organizationId, Map.of(
                        "name", "수정된 조직",
                        "description", "수정된 설명"
                )).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정된 조직"))
                .andExpect(jsonPath("$.data.description").value("수정된 설명"))
                .andExpect(jsonPath("$.data.myRole").value("OWNER"));

        mockMvc.perform(get("/api/organizations/{organizationId}", organizationId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정된 조직"));
    }

    @Test
    @DisplayName("FT-ORG-008 Member의 조직 수정 요청을 차단한다")
    void memberCannotUpdateOrganization() throws Exception {
        String ownerToken = signupAndLogin("role-owner@example.com", "역할 소유자");
        String memberToken = signupAndLogin("role-member@example.com", "일반 멤버");
        long organizationId = createOrganization(ownerToken, "역할 조직", "권한 테스트");
        addMember(organizationId, "role-member@example.com", MemberRole.MEMBER);

        mockMvc.perform(patchJson("/api/organizations/" + organizationId, Map.of(
                        "name", "권한 없는 수정",
                        "description", "변경되면 안 됨"
                )).header("Authorization", bearer(memberToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));
    }

    @Test
    @DisplayName("FT-ORG-009 조직 수정 입력값을 검증한다")
    void validateOrganizationUpdate() throws Exception {
        String token = signupAndLogin("update-validation@example.com", "수정 검증자");
        long organizationId = createOrganization(token, "검증 조직", "검증 설명");

        mockMvc.perform(patchJson("/api/organizations/" + organizationId, Map.of(
                        "name", "",
                        "description", "설명"
                )).header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"));
    }

    private void addMember(long organizationId, String email, MemberRole role) {
        Organization organization = organizationRepository.findById(organizationId).orElseThrow();
        User user = userRepository.findByEmail(email).orElseThrow();
        organizationMemberRepository.save(OrganizationMember.create(organization, user, role));
    }

    private long createOrganization(String token, String name, String description) throws Exception {
        MvcResult result = mockMvc.perform(postJson("/api/organizations", Map.of(
                        "name", name,
                        "description", description
                )).header("Authorization", bearer(token)))
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
