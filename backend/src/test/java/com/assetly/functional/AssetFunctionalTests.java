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
class AssetFunctionalTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("FT-ASSET-001 필수 정보와 참조 정보로 자산을 등록하고 publicCode를 발급한다")
    void createsAssetWithPublicCode() throws Exception {
        Workspace workspace = workspace("asset-create");
        Account assignee = signupAndLogin("asset-assignee@example.com", "자산 담당자");
        addMember(workspace.owner().token(), workspace.organizationId(), assignee.email(), "MEMBER");

        createAsset(workspace.owner().token(), workspace.organizationId(), assetBody(
                " it-2026-001 ", "개발 노트북", workspace.categoryId(), workspace.locationId(), assignee.userId(), "IN_USE"
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assetCode").value("IT-2026-001"))
                .andExpect(jsonPath("$.data.publicCode").isNotEmpty())
                .andExpect(jsonPath("$.data.category.name").value("노트북"))
                .andExpect(jsonPath("$.data.location.name").value("개발실"))
                .andExpect(jsonPath("$.data.assignedUser.email").value(assignee.email()));
    }

    @Test
    @DisplayName("FT-ASSET-002 자산번호는 조직 안에서 중복될 수 없다")
    void rejectsDuplicateAssetCodeWithinOrganization() throws Exception {
        Workspace first = workspace("asset-duplicate-first");
        Workspace second = workspace("asset-duplicate-second");
        createAsset(first.owner().token(), first.organizationId(), assetBody(
                "ASSET-001", "첫 자산", first.categoryId(), first.locationId(), null, "AVAILABLE"
        )).andExpect(status().isOk());

        createAsset(first.owner().token(), first.organizationId(), assetBody(
                "asset-001", "중복 자산", first.categoryId(), first.locationId(), null, "AVAILABLE"
        ))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ASSET_CODE_DUPLICATED"));

        createAsset(second.owner().token(), second.organizationId(), assetBody(
                "ASSET-001", "다른 조직 자산", second.categoryId(), second.locationId(), null, "AVAILABLE"
        )).andExpect(status().isOk());
    }

    @Test
    @DisplayName("FT-ASSET-003 자산 목록을 검색하고 상태·카테고리·위치로 필터링한다")
    void filtersAndPagesAssets() throws Exception {
        Workspace workspace = workspace("asset-list");
        createAsset(workspace.owner().token(), workspace.organizationId(), assetBody(
                "NOTE-001", "개발 노트북", workspace.categoryId(), workspace.locationId(), null, "IN_USE"
        )).andExpect(status().isOk());
        createAsset(workspace.owner().token(), workspace.organizationId(), assetBody(
                "CAM-001", "회의실 카메라", null, null, null, "AVAILABLE"
        )).andExpect(status().isOk());

        mockMvc.perform(get("/api/organizations/{organizationId}/assets", workspace.organizationId())
                        .param("keyword", "노트북")
                        .param("status", "IN_USE")
                        .param("categoryId", String.valueOf(workspace.categoryId()))
                        .param("locationId", String.valueOf(workspace.locationId()))
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", bearer(workspace.owner().token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].assetCode").value("NOTE-001"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.size").value(1));
    }

    @Test
    @DisplayName("FT-ASSET-004 조직 멤버가 자산 상세를 조회한다")
    void memberFindsAssetDetail() throws Exception {
        Workspace workspace = workspace("asset-detail");
        Account member = signupAndLogin("asset-detail-member@example.com", "상세 조회 멤버");
        addMember(workspace.owner().token(), workspace.organizationId(), member.email(), "MEMBER");
        long assetId = createAssetId(workspace, "DETAIL-001", "상세 자산");

        mockMvc.perform(get("/api/organizations/{organizationId}/assets/{assetId}", workspace.organizationId(), assetId)
                        .header("Authorization", bearer(member.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(assetId))
                .andExpect(jsonPath("$.data.description").value("기능 테스트 자산"));

        mockMvc.perform(get("/api/organizations/{organizationId}/assignees", workspace.organizationId())
                        .header("Authorization", bearer(member.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("FT-ASSET-005 Admin이 자산 기본 정보를 수정한다")
    void adminUpdatesAsset() throws Exception {
        Workspace workspace = workspace("asset-admin-update");
        Account admin = signupAndLogin("asset-update-admin@example.com", "자산 관리자");
        addMember(workspace.owner().token(), workspace.organizationId(), admin.email(), "ADMIN");
        long assetId = createAssetId(workspace, "UPDATE-001", "수정 전 자산");

        updateAsset(admin.token(), workspace.organizationId(), assetId, new AssetBody(
                "UPDATE-002", "수정 후 자산", "수정된 설명", workspace.categoryId(), workspace.locationId(),
                admin.userId(), "REPAIR", "2026-01-02", 2500000
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assetCode").value("UPDATE-002"))
                .andExpect(jsonPath("$.data.name").value("수정 후 자산"))
                .andExpect(jsonPath("$.data.status").value("REPAIR"))
                .andExpect(jsonPath("$.data.assignedUser.id").value(admin.userId()));
    }

    @Test
    @DisplayName("FT-ASSET-006 Manager는 상태·위치·담당자만 변경한다")
    void limitsManagerAssetUpdate() throws Exception {
        Workspace workspace = workspace("asset-manager-update");
        Account manager = signupAndLogin("asset-update-manager@example.com", "자산 매니저");
        addMember(workspace.owner().token(), workspace.organizationId(), manager.email(), "MANAGER");
        long secondLocationId = createLocation(workspace.owner().token(), workspace.organizationId(), "회의실");
        long assetId = createAssetId(workspace, "MANAGER-001", "Manager 자산");

        updateAsset(manager.token(), workspace.organizationId(), assetId, new AssetBody(
                "MANAGER-001", "Manager 자산", "기능 테스트 자산", workspace.categoryId(), secondLocationId,
                manager.userId(), "IN_USE", "2025-01-01", 1000000
        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.location.id").value(secondLocationId))
                .andExpect(jsonPath("$.data.status").value("IN_USE"));

        updateAsset(manager.token(), workspace.organizationId(), assetId, new AssetBody(
                "MANAGER-001", "변경하면 안 되는 이름", "기능 테스트 자산", workspace.categoryId(), secondLocationId,
                manager.userId(), "IN_USE", "2025-01-01", 1000000
        ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));
    }

    @Test
    @DisplayName("FT-ASSET-007 Member의 자산 수정과 삭제를 차단한다")
    void blocksMemberMutation() throws Exception {
        Workspace workspace = workspace("asset-member-block");
        Account member = signupAndLogin("asset-block-member@example.com", "자산 일반 멤버");
        addMember(workspace.owner().token(), workspace.organizationId(), member.email(), "MEMBER");
        long assetId = createAssetId(workspace, "BLOCK-001", "권한 자산");

        updateAsset(member.token(), workspace.organizationId(), assetId, assetBody(
                "BLOCK-001", "권한 자산", workspace.categoryId(), workspace.locationId(), null, "LOST"
        ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));

        mockMvc.perform(delete("/api/organizations/{organizationId}/assets/{assetId}", workspace.organizationId(), assetId)
                        .header("Authorization", bearer(member.token())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("AUTH_FORBIDDEN"));
    }

    @Test
    @DisplayName("FT-ASSET-008 자산 비활성화와 사용 중 기준 정보 삭제를 처리한다")
    void softDeletesAssetAndProtectsReferences() throws Exception {
        Workspace workspace = workspace("asset-delete");
        long assetId = createAssetId(workspace, "DELETE-001", "삭제 자산");

        mockMvc.perform(delete("/api/organizations/{organizationId}/locations/{locationId}",
                        workspace.organizationId(), workspace.locationId())
                        .header("Authorization", bearer(workspace.owner().token())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("LOCATION_IN_USE"));
        mockMvc.perform(delete("/api/organizations/{organizationId}/categories/{categoryId}",
                        workspace.organizationId(), workspace.categoryId())
                        .header("Authorization", bearer(workspace.owner().token())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CATEGORY_IN_USE"));

        mockMvc.perform(delete("/api/organizations/{organizationId}/assets/{assetId}", workspace.organizationId(), assetId)
                        .header("Authorization", bearer(workspace.owner().token())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/organizations/{organizationId}/assets/{assetId}", workspace.organizationId(), assetId)
                        .header("Authorization", bearer(workspace.owner().token())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("ASSET_NOT_FOUND"));
        mockMvc.perform(get("/api/organizations/{organizationId}/assets", workspace.organizationId())
                        .header("Authorization", bearer(workspace.owner().token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(0));

        mockMvc.perform(delete("/api/organizations/{organizationId}/locations/{locationId}",
                        workspace.organizationId(), workspace.locationId())
                        .header("Authorization", bearer(workspace.owner().token())))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/organizations/{organizationId}/categories/{categoryId}",
                        workspace.organizationId(), workspace.categoryId())
                        .header("Authorization", bearer(workspace.owner().token())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("FT-ASSET-009 다른 조직의 자산과 참조 ID를 사용할 수 없다")
    void isolatesAssetResourcesByOrganization() throws Exception {
        Workspace first = workspace("asset-isolate-first");
        Workspace second = workspace("asset-isolate-second");
        Account outsider = signupAndLogin("asset-outsider@example.com", "외부 담당자");
        long secondAssetId = createAssetId(second, "FOREIGN-001", "다른 조직 자산");

        mockMvc.perform(get("/api/organizations/{organizationId}/assets/{assetId}", first.organizationId(), secondAssetId)
                        .header("Authorization", bearer(first.owner().token())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("ASSET_NOT_FOUND"));

        createAsset(first.owner().token(), first.organizationId(), assetBody(
                "FOREIGN-REF", "외부 참조 자산", second.categoryId(), first.locationId(), null, "AVAILABLE"
        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("CATEGORY_NOT_FOUND"));

        createAsset(first.owner().token(), first.organizationId(), assetBody(
                "FOREIGN-USER", "외부 담당자 자산", first.categoryId(), first.locationId(), outsider.userId(), "AVAILABLE"
        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("ORGANIZATION_MEMBER_NOT_FOUND"));
    }

    @Test
    @DisplayName("FT-DASH-001 대시보드가 실제 자산 상태 합계와 최근 자산을 반환한다")
    void dashboardUsesCurrentAssetData() throws Exception {
        Workspace workspace = workspace("asset-dashboard");
        createAsset(workspace.owner().token(), workspace.organizationId(), assetBody(
                "DASH-001", "사용 중 노트북", workspace.categoryId(), workspace.locationId(), null, "IN_USE"
        )).andExpect(status().isOk());
        createAsset(workspace.owner().token(), workspace.organizationId(), assetBody(
                "DASH-002", "수리 노트북", workspace.categoryId(), workspace.locationId(), null, "REPAIR"
        )).andExpect(status().isOk());

        mockMvc.perform(get("/api/organizations/{organizationId}/dashboard", workspace.organizationId())
                        .header("Authorization", bearer(workspace.owner().token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAssets").value(2))
                .andExpect(jsonPath("$.data.inUseAssets").value(1))
                .andExpect(jsonPath("$.data.repairAssets").value(1))
                .andExpect(jsonPath("$.data.recentAssets.length()").value(2));
    }

    @Test
    @DisplayName("FT-DASH-003 신규 조직 대시보드는 0과 빈 목록을 반환한다")
    void newOrganizationDashboardIsEmpty() throws Exception {
        Workspace workspace = workspace("asset-empty-dashboard");

        mockMvc.perform(get("/api/organizations/{organizationId}/dashboard", workspace.organizationId())
                        .header("Authorization", bearer(workspace.owner().token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAssets").value(0))
                .andExpect(jsonPath("$.data.availableAssets").value(0))
                .andExpect(jsonPath("$.data.inUseAssets").value(0))
                .andExpect(jsonPath("$.data.repairAssets").value(0))
                .andExpect(jsonPath("$.data.lostAssets").value(0))
                .andExpect(jsonPath("$.data.recentAssets.length()").value(0));
    }

    private Workspace workspace(String prefix) throws Exception {
        Account owner = signupAndLogin(prefix + "-owner@example.com", prefix + " 소유자");
        long organizationId = createOrganization(owner.token(), prefix + " 조직");
        long categoryId = createCategory(owner.token(), organizationId, "노트북");
        long locationId = createLocation(owner.token(), organizationId, "개발실");
        return new Workspace(owner, organizationId, categoryId, locationId);
    }

    private AssetBody assetBody(
            String code,
            String name,
            Long categoryId,
            Long locationId,
            Long assignedUserId,
            String status
    ) {
        return new AssetBody(code, name, "기능 테스트 자산", categoryId, locationId, assignedUserId,
                status, "2025-01-01", 1000000);
    }

    private ResultActions createAsset(String token, long organizationId, AssetBody body) throws Exception {
        return mockMvc.perform(postJson("/api/organizations/" + organizationId + "/assets", body)
                .header("Authorization", bearer(token)));
    }

    private long createAssetId(Workspace workspace, String code, String name) throws Exception {
        MvcResult result = createAsset(workspace.owner().token(), workspace.organizationId(), assetBody(
                code, name, workspace.categoryId(), workspace.locationId(), null, "AVAILABLE"
        )).andExpect(status().isOk()).andReturn();
        return responseData(result).path("id").asLong();
    }

    private ResultActions updateAsset(String token, long organizationId, long assetId, AssetBody body) throws Exception {
        return mockMvc.perform(patchJson("/api/organizations/" + organizationId + "/assets/" + assetId, body)
                .header("Authorization", bearer(token)));
    }

    private long createCategory(String token, long organizationId, String name) throws Exception {
        MvcResult result = mockMvc.perform(postJson(
                        "/api/organizations/" + organizationId + "/categories", Map.of("name", name)
                ).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        return responseData(result).path("id").asLong();
    }

    private long createLocation(String token, long organizationId, String name) throws Exception {
        MvcResult result = mockMvc.perform(postJson(
                        "/api/organizations/" + organizationId + "/locations",
                        Map.of("name", name, "description", "기능 테스트 위치")
                ).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        return responseData(result).path("id").asLong();
    }

    private void addMember(String token, long organizationId, String email, String role) throws Exception {
        mockMvc.perform(postJson(
                        "/api/organizations/" + organizationId + "/members", Map.of("email", email, "role", role)
                ).header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    private long createOrganization(String token, String name) throws Exception {
        MvcResult result = mockMvc.perform(postJson(
                        "/api/organizations", Map.of("name", name, "description", "자산 기능 테스트")
                ).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        return responseData(result).path("id").asLong();
    }

    private Account signupAndLogin(String email, String name) throws Exception {
        String password = "password1234";
        MvcResult signup = mockMvc.perform(postJson("/api/auth/signup", Map.of(
                        "email", email, "password", password, "name", name
                )))
                .andExpect(status().isOk())
                .andReturn();
        long userId = responseData(signup).path("id").asLong();
        MvcResult login = mockMvc.perform(postJson("/api/auth/login", Map.of(
                        "email", email, "password", password
                )))
                .andExpect(status().isOk())
                .andReturn();
        return new Account(responseData(login).path("accessToken").asText(), userId, email);
    }

    private JsonNode responseData(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private MockHttpServletRequestBuilder postJson(String path, Object body) throws Exception {
        return post(path).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
    }

    private MockHttpServletRequestBuilder patchJson(String path, Object body) throws Exception {
        return patch(path).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record Account(String token, long userId, String email) {
    }

    private record Workspace(Account owner, long organizationId, long categoryId, long locationId) {
    }

    private record AssetBody(
            String assetCode,
            String name,
            String description,
            Long categoryId,
            Long locationId,
            Long assignedUserId,
            String status,
            String purchaseDate,
            Integer purchasePrice
    ) {
    }
}
