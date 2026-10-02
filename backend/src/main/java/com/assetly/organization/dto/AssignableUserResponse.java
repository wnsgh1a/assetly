package com.assetly.organization.dto;

import com.assetly.organization.OrganizationMember;

public record AssignableUserResponse(
        Long userId,
        String name,
        String email
) {

    public static AssignableUserResponse from(OrganizationMember member) {
        return new AssignableUserResponse(
                member.getUser().getId(),
                member.getUser().getName(),
                member.getUser().getEmail()
        );
    }
}
