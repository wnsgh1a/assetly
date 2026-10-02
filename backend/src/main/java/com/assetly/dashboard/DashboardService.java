package com.assetly.dashboard;

import com.assetly.asset.AssetRepository;
import com.assetly.asset.AssetStatus;
import com.assetly.asset.dto.AssetResponse;
import com.assetly.history.AssetHistoryRepository;
import com.assetly.history.dto.AssetHistoryResponse;
import com.assetly.organization.OrganizationAccessService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final AssetRepository assetRepository;
    private final OrganizationAccessService organizationAccessService;
    private final AssetHistoryRepository assetHistoryRepository;

    public DashboardService(
            AssetRepository assetRepository,
            OrganizationAccessService organizationAccessService,
            AssetHistoryRepository assetHistoryRepository
    ) {
        this.assetRepository = assetRepository;
        this.organizationAccessService = organizationAccessService;
        this.assetHistoryRepository = assetHistoryRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse find(Long userId, Long organizationId) {
        organizationAccessService.requireMember(organizationId, userId);
        return new DashboardResponse(
                assetRepository.countByOrganizationIdAndDeletedAtIsNull(organizationId),
                count(organizationId, AssetStatus.AVAILABLE),
                count(organizationId, AssetStatus.IN_USE),
                count(organizationId, AssetStatus.REPAIR),
                count(organizationId, AssetStatus.LOST),
                assetRepository.findTop5ByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(organizationId)
                        .stream()
                        .map(AssetResponse::from)
                        .toList(),
                assetHistoryRepository.findTop5ByOrganizationIdOrderByCreatedAtDescIdDesc(organizationId)
                        .stream()
                        .map(AssetHistoryResponse::from)
                        .toList()
        );
    }

    private long count(Long organizationId, AssetStatus status) {
        return assetRepository.countByOrganizationIdAndStatusAndDeletedAtIsNull(organizationId, status);
    }
}
