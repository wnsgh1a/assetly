package com.assetly.organization;

import com.assetly.auth.AuthenticatedUser;
import com.assetly.common.ApiResponse;
import com.assetly.organization.dto.AssignableUserResponse;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations/{organizationId}/assignees")
public class OrganizationAssigneeController {

    private final OrganizationMemberService organizationMemberService;

    public OrganizationAssigneeController(OrganizationMemberService organizationMemberService) {
        this.organizationMemberService = organizationMemberService;
    }

    @GetMapping
    public ApiResponse<List<AssignableUserResponse>> findAll(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId
    ) {
        return ApiResponse.of(organizationMemberService.findAssignableUsers(user.id(), organizationId));
    }
}
