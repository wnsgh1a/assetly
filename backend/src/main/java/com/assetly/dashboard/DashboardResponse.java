package com.assetly.dashboard;

import com.assetly.asset.dto.AssetResponse;
import com.assetly.history.dto.AssetHistoryResponse;
import java.util.List;

public record DashboardResponse(
        long totalAssets,
        long availableAssets,
        long inUseAssets,
        long repairAssets,
        long lostAssets,
        List<AssetResponse> recentAssets,
        List<AssetHistoryResponse> recentHistories
) {
}
