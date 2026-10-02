package com.assetly.history.dto;

import com.assetly.history.AssetHistory;
import com.assetly.history.AssetHistoryAction;
import java.time.Instant;

public record AssetHistoryResponse(
        Long id,
        AssetSummary asset,
        ActorSummary actor,
        AssetHistoryAction actionType,
        String fieldName,
        String beforeValue,
        String afterValue,
        String memo,
        Instant createdAt
) {
    public static AssetHistoryResponse from(AssetHistory history) {
        return new AssetHistoryResponse(
                history.getId(),
                new AssetSummary(
                        history.getAsset().getId(),
                        history.getAsset().getAssetCode(),
                        history.getAsset().getName(),
                        history.getAsset().getDeletedAt() == null
                ),
                new ActorSummary(history.getActor().getId(), history.getActor().getName(), history.getActor().getEmail()),
                history.getActionType(),
                history.getFieldName(),
                history.getBeforeValue(),
                history.getAfterValue(),
                history.getMemo(),
                history.getCreatedAt()
        );
    }

    public record AssetSummary(Long id, String assetCode, String name, boolean active) {
    }

    public record ActorSummary(Long id, String name, String email) {
    }
}
