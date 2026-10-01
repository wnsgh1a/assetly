package com.assetly.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 1000) String description
) {
}
