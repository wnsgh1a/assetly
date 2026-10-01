package com.assetly.organization.dto;

import com.assetly.organization.Organization;
import com.assetly.organization.OrganizationMember;

public record OrganizationResponse(Long id, String name, String description, String myRole) {

    public static OrganizationResponse from(OrganizationMember member) {
        Organization organization = member.getOrganization();
        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getDescription(),
                member.getRole().name()
        );
    }
}
