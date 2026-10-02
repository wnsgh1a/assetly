package com.assetly.asset;

import com.assetly.asset.dto.AssetPageResponse;
import com.assetly.asset.dto.AssetRequest;
import com.assetly.asset.dto.AssetResponse;
import com.assetly.auth.AuthenticatedUser;
import com.assetly.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/organizations/{organizationId}/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping
    public ApiResponse<AssetResponse> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @Valid @RequestBody AssetRequest request
    ) {
        return ApiResponse.of(assetService.create(user.id(), organizationId, request));
    }

    @GetMapping
    public ApiResponse<AssetPageResponse> findAll(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ApiResponse.of(assetService.findAll(
                user.id(), organizationId, keyword, status, categoryId, locationId, page, size
        ));
    }

    @GetMapping("/{assetId}")
    public ApiResponse<AssetResponse> findOne(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @PathVariable Long assetId
    ) {
        return ApiResponse.of(assetService.findOne(user.id(), organizationId, assetId));
    }

    @PatchMapping("/{assetId}")
    public ApiResponse<AssetResponse> update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @PathVariable Long assetId,
            @Valid @RequestBody AssetRequest request
    ) {
        return ApiResponse.of(assetService.update(user.id(), organizationId, assetId, request));
    }

    @DeleteMapping("/{assetId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @PathVariable Long assetId
    ) {
        assetService.delete(user.id(), organizationId, assetId);
        return ApiResponse.of(null);
    }
}
