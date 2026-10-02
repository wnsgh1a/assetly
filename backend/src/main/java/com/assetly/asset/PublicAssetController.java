package com.assetly.asset;

import com.assetly.asset.dto.PublicAssetResponse;
import com.assetly.auth.AuthenticatedUser;
import com.assetly.common.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assets/public")
public class PublicAssetController {

    private final AssetService assetService;

    public PublicAssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping("/{publicCode}")
    public ApiResponse<PublicAssetResponse> find(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String publicCode
    ) {
        return ApiResponse.of(assetService.findByPublicCode(user.id(), publicCode));
    }
}
