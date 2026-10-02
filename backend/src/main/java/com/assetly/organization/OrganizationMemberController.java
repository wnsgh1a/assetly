package com.assetly.organization;

import com.assetly.auth.AuthenticatedUser;
import com.assetly.common.ApiResponse;
import com.assetly.organization.dto.AddOrganizationMemberRequest;
import com.assetly.organization.dto.OrganizationMemberResponse;
import com.assetly.organization.dto.UpdateMemberRoleRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations/{organizationId}/members")
public class OrganizationMemberController {

    private final OrganizationMemberService organizationMemberService;

    public OrganizationMemberController(OrganizationMemberService organizationMemberService) {
        this.organizationMemberService = organizationMemberService;
    }

    @GetMapping
    public ApiResponse<List<OrganizationMemberResponse>> findAll(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId
    ) {
        return ApiResponse.of(organizationMemberService.findAll(user.id(), organizationId));
    }

    @PostMapping
    public ApiResponse<OrganizationMemberResponse> add(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @Valid @RequestBody AddOrganizationMemberRequest request
    ) {
        return ApiResponse.of(organizationMemberService.add(user.id(), organizationId, request));
    }

    @PatchMapping("/{memberId}/role")
    public ApiResponse<OrganizationMemberResponse> updateRole(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateMemberRoleRequest request
    ) {
        return ApiResponse.of(organizationMemberService.updateRole(user.id(), organizationId, memberId, request));
    }

    @DeleteMapping("/{memberId}")
    public ApiResponse<Void> remove(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @PathVariable Long memberId
    ) {
        organizationMemberService.remove(user.id(), organizationId, memberId);
        return ApiResponse.of(null);
    }
}
