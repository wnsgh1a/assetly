package com.assetly.history.dto;

import java.util.List;
import org.springframework.data.domain.Page;

public record AssetHistoryPageResponse(
        List<AssetHistoryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static AssetHistoryPageResponse from(Page<AssetHistoryResponse> result) {
        return new AssetHistoryPageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }
}
