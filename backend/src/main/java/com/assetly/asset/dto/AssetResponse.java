package com.assetly.asset.dto;

import com.assetly.asset.Asset;
import com.assetly.asset.AssetStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record AssetResponse(
        Long id,
        String publicCode,
        String assetCode,
        String name,
        String description,
        AssetStatus status,
        Reference category,
        Reference location,
        AssignedUser assignedUser,
        LocalDate purchaseDate,
        BigDecimal purchasePrice,
        Instant createdAt,
        Instant updatedAt
) {

    public static AssetResponse from(Asset asset) {
        return new AssetResponse(
                asset.getId(),
                asset.getPublicCode(),
                asset.getAssetCode(),
                asset.getName(),
                asset.getDescription(),
                asset.getStatus(),
                asset.getCategory() == null ? null : new Reference(asset.getCategory().getId(), asset.getCategory().getName()),
                asset.getLocation() == null ? null : new Reference(asset.getLocation().getId(), asset.getLocation().getName()),
                asset.getAssignedUser() == null ? null : new AssignedUser(
                        asset.getAssignedUser().getId(),
                        asset.getAssignedUser().getEmail(),
                        asset.getAssignedUser().getName()
                ),
                asset.getPurchaseDate(),
                asset.getPurchasePrice(),
                asset.getCreatedAt(),
                asset.getUpdatedAt()
        );
    }

    public record Reference(Long id, String name) {
    }

    public record AssignedUser(Long id, String email, String name) {
    }
}
