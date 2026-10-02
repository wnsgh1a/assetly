package com.assetly.asset;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    Optional<Asset> findByIdAndOrganizationIdAndDeletedAtIsNull(Long id, Long organizationId);

    Optional<Asset> findByIdAndOrganizationId(Long id, Long organizationId);

    Optional<Asset> findByPublicCodeAndDeletedAtIsNull(String publicCode);

    boolean existsByPublicCode(String publicCode);

    boolean existsByOrganizationIdAndAssetCode(Long organizationId, String assetCode);

    boolean existsByOrganizationIdAndAssetCodeAndIdNot(Long organizationId, String assetCode, Long id);

    boolean existsByOrganizationIdAndCategoryIdAndDeletedAtIsNull(Long organizationId, Long categoryId);

    boolean existsByOrganizationIdAndLocationIdAndDeletedAtIsNull(Long organizationId, Long locationId);

    long countByOrganizationIdAndDeletedAtIsNull(Long organizationId);

    long countByOrganizationIdAndStatusAndDeletedAtIsNull(Long organizationId, AssetStatus status);

    List<Asset> findTop5ByOrganizationIdAndDeletedAtIsNullOrderByCreatedAtDesc(Long organizationId);
}
