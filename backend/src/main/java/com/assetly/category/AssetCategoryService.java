package com.assetly.category;

import com.assetly.category.dto.AssetCategoryRequest;
import com.assetly.category.dto.AssetCategoryResponse;
import com.assetly.common.BusinessException;
import com.assetly.organization.MemberRole;
import com.assetly.organization.OrganizationAccessService;
import com.assetly.organization.OrganizationMember;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssetCategoryService {

    private final AssetCategoryRepository assetCategoryRepository;
    private final OrganizationAccessService organizationAccessService;

    public AssetCategoryService(
            AssetCategoryRepository assetCategoryRepository,
            OrganizationAccessService organizationAccessService
    ) {
        this.assetCategoryRepository = assetCategoryRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional(readOnly = true)
    public List<AssetCategoryResponse> findAll(Long userId, Long organizationId) {
        organizationAccessService.requireMember(organizationId, userId);
        return assetCategoryRepository.findAllByOrganizationIdOrderByNameAsc(organizationId).stream()
                .map(AssetCategoryResponse::from)
                .toList();
    }

    @Transactional
    public AssetCategoryResponse create(Long userId, Long organizationId, AssetCategoryRequest request) {
        OrganizationMember member = requireManager(organizationId, userId);
        String name = request.name().trim();
        validateDuplicateName(organizationId, name, null);
        return AssetCategoryResponse.from(assetCategoryRepository.save(
                AssetCategory.create(member.getOrganization(), name)
        ));
    }

    @Transactional
    public AssetCategoryResponse update(
            Long userId,
            Long organizationId,
            Long categoryId,
            AssetCategoryRequest request
    ) {
        requireManager(organizationId, userId);
        AssetCategory category = findCategory(organizationId, categoryId);
        String name = request.name().trim();
        validateDuplicateName(organizationId, name, categoryId);
        category.rename(name);
        return AssetCategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long userId, Long organizationId, Long categoryId) {
        requireManager(organizationId, userId);
        assetCategoryRepository.delete(findCategory(organizationId, categoryId));
    }

    private OrganizationMember requireManager(Long organizationId, Long userId) {
        return organizationAccessService.requireRole(
                organizationId,
                userId,
                MemberRole.OWNER,
                MemberRole.ADMIN
        );
    }

    private AssetCategory findCategory(Long organizationId, Long categoryId) {
        return assetCategoryRepository.findByIdAndOrganizationId(categoryId, organizationId)
                .orElseThrow(() -> new BusinessException(
                        "CATEGORY_NOT_FOUND",
                        "카테고리를 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));
    }

    private void validateDuplicateName(Long organizationId, String name, Long excludedId) {
        boolean duplicated = excludedId == null
                ? assetCategoryRepository.existsByOrganizationIdAndName(organizationId, name)
                : assetCategoryRepository.existsByOrganizationIdAndNameAndIdNot(organizationId, name, excludedId);
        if (duplicated) {
            throw new BusinessException(
                    "CATEGORY_NAME_DUPLICATED",
                    "같은 이름의 카테고리가 이미 있습니다.",
                    HttpStatus.CONFLICT
            );
        }
    }
}
