package com.assetly.organization.dto;

import com.assetly.organization.OrganizationMember;
import java.time.Instant;

public record OrganizationMemberResponse(
        Long id,
        Long userId,
        String email,
        String name,
        String role,
        Instant joinedAt
) {

    public static OrganizationMemberResponse from(OrganizationMember member) {
        return new OrganizationMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getUser().getName(),
                member.getRole().name(),
                member.getCreatedAt()
        );
    }
}
