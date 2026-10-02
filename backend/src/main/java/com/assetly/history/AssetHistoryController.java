package com.assetly.history;

import com.assetly.auth.AuthenticatedUser;
import com.assetly.common.ApiResponse;
import com.assetly.history.dto.AssetHistoryPageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/organizations/{organizationId}")
public class AssetHistoryController {

    private final AssetHistoryService assetHistoryService;

    public AssetHistoryController(AssetHistoryService assetHistoryService) {
        this.assetHistoryService = assetHistoryService;
    }

    @GetMapping("/assets/{assetId}/histories")
    public ApiResponse<AssetHistoryPageResponse> findByAsset(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @PathVariable Long assetId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ApiResponse.of(assetHistoryService.findByAsset(user.id(), organizationId, assetId, page, size));
    }

    @GetMapping("/histories")
    public ApiResponse<AssetHistoryPageResponse> findAll(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @RequestParam(required = false) Long assetId,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) AssetHistoryAction actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        return ApiResponse.of(assetHistoryService.findAll(
                user.id(), organizationId, assetId, actorId, actionType, from, to, page, size
        ));
    }
}
