package com.assetly.history;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AssetHistoryRepository extends JpaRepository<AssetHistory, Long>, JpaSpecificationExecutor<AssetHistory> {

    Page<AssetHistory> findByOrganizationIdAndAssetId(Long organizationId, Long assetId, Pageable pageable);

    List<AssetHistory> findTop5ByOrganizationIdOrderByCreatedAtDescIdDesc(Long organizationId);
}
