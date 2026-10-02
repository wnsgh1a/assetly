package com.assetly.category;

import com.assetly.auth.AuthenticatedUser;
import com.assetly.category.dto.AssetCategoryRequest;
import com.assetly.category.dto.AssetCategoryResponse;
import com.assetly.common.ApiResponse;
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
@RequestMapping("/api/organizations/{organizationId}/categories")
public class AssetCategoryController {

    private final AssetCategoryService assetCategoryService;

    public AssetCategoryController(AssetCategoryService assetCategoryService) {
        this.assetCategoryService = assetCategoryService;
    }

    @GetMapping
    public ApiResponse<List<AssetCategoryResponse>> findAll(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId
    ) {
        return ApiResponse.of(assetCategoryService.findAll(user.id(), organizationId));
    }

    @PostMapping
    public ApiResponse<AssetCategoryResponse> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @Valid @RequestBody AssetCategoryRequest request
    ) {
        return ApiResponse.of(assetCategoryService.create(user.id(), organizationId, request));
    }

    @PatchMapping("/{categoryId}")
    public ApiResponse<AssetCategoryResponse> update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @PathVariable Long categoryId,
            @Valid @RequestBody AssetCategoryRequest request
    ) {
        return ApiResponse.of(assetCategoryService.update(user.id(), organizationId, categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @PathVariable Long categoryId
    ) {
        assetCategoryService.delete(user.id(), organizationId, categoryId);
        return ApiResponse.of(null);
    }
}
