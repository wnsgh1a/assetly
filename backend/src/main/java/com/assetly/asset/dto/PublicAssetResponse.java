package com.assetly.asset.dto;

import com.assetly.asset.Asset;
import com.assetly.organization.OrganizationMember;
import com.assetly.organization.MemberRole;

public record PublicAssetResponse(
        Long organizationId,
        String organizationName,
        MemberRole myRole,
        AssetResponse asset
) {
    public static PublicAssetResponse from(Asset asset, OrganizationMember member) {
        return new PublicAssetResponse(
                asset.getOrganization().getId(),
                asset.getOrganization().getName(),
                member.getRole(),
                AssetResponse.from(asset)
        );
    }
}
