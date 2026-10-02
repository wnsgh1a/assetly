package com.assetly.category;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Long> {

    List<AssetCategory> findAllByOrganizationIdOrderByNameAsc(Long organizationId);

    Optional<AssetCategory> findByIdAndOrganizationId(Long id, Long organizationId);

    boolean existsByOrganizationIdAndName(Long organizationId, String name);

    boolean existsByOrganizationIdAndNameAndIdNot(Long organizationId, String name, Long id);
}
