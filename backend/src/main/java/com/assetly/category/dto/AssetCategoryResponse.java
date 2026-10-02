package com.assetly.category.dto;

import com.assetly.category.AssetCategory;
import java.time.Instant;

public record AssetCategoryResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {

    public static AssetCategoryResponse from(AssetCategory category) {
        return new AssetCategoryResponse(
                category.getId(),
                category.getName(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
