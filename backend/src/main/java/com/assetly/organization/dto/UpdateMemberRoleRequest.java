package com.assetly.organization.dto;

import com.assetly.organization.MemberRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(@NotNull MemberRole role) {
}
