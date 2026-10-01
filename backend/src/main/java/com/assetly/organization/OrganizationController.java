package com.assetly.organization;

import com.assetly.auth.AuthenticatedUser;
import com.assetly.common.ApiResponse;
import com.assetly.organization.dto.CreateOrganizationRequest;
import com.assetly.organization.dto.OrganizationResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
}
