package com.assetly.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssetCategoryRequest(
        @NotBlank @Size(max = 100) String name
) {
}
