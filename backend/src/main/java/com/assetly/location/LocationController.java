package com.assetly.location;

import com.assetly.auth.AuthenticatedUser;
import com.assetly.common.ApiResponse;
import com.assetly.location.dto.LocationRequest;
import com.assetly.location.dto.LocationResponse;
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
@RequestMapping("/api/organizations/{organizationId}/locations")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping
    public ApiResponse<List<LocationResponse>> findAll(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId
    ) {
        return ApiResponse.of(locationService.findAll(user.id(), organizationId));
    }

    @PostMapping
    public ApiResponse<LocationResponse> create(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @Valid @RequestBody LocationRequest request
    ) {
        return ApiResponse.of(locationService.create(user.id(), organizationId, request));
    }

    @PatchMapping("/{locationId}")
    public ApiResponse<LocationResponse> update(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @PathVariable Long locationId,
            @Valid @RequestBody LocationRequest request
    ) {
        return ApiResponse.of(locationService.update(user.id(), organizationId, locationId, request));
    }

    @DeleteMapping("/{locationId}")
    public ApiResponse<Void> delete(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long organizationId,
            @PathVariable Long locationId
    ) {
        locationService.delete(user.id(), organizationId, locationId);
        return ApiResponse.of(null);
    }
}
