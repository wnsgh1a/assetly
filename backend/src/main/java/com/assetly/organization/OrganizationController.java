package com.assetly.organization;

import com.assetly.auth.AuthenticatedUser;
import com.assetly.common.ApiResponse;
import com.assetly.organization.dto.CreateOrganizationRequest;
import com.assetly.organization.dto.OrganizationResponse;
import com.assetly.organization.dto.UpdateOrganizationRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    public ApiResponse<OrganizationResponse> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody CreateOrganizationRequest request
    ) {
        return ApiResponse.of(organizationService.create(user.id(), request));
    }

    @GetMapping
    public ApiResponse<List<OrganizationResponse>> findMine(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.of(organizationService.findMine(user.id()));
    }

    @GetMapping("/{organizationId}")
    public ApiResponse<OrganizationResponse> findOne(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId
    ) {
        return ApiResponse.of(organizationService.findOne(user.id(), organizationId));
    }

    @PatchMapping("/{organizationId}")
    public ApiResponse<OrganizationResponse> update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @Valid @RequestBody UpdateOrganizationRequest request
    ) {
        return ApiResponse.of(organizationService.update(user.id(), organizationId, request));
    }
}
