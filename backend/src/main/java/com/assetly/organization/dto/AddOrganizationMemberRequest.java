package com.assetly.organization.dto;

import com.assetly.organization.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddOrganizationMemberRequest(
        @Email @NotBlank String email,
        @NotNull MemberRole role
) {
}
