package com.assetly.asset.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record AssetPageResponse(
        List<AssetResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static AssetPageResponse from(Page<AssetResponse> result) {
        return new AssetPageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
