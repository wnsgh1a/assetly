package com.assetly.asset;

import com.assetly.asset.dto.AssetPageResponse;
import com.assetly.asset.dto.AssetRequest;
import com.assetly.asset.dto.AssetResponse;
import com.assetly.category.AssetCategory;
import com.assetly.category.AssetCategoryRepository;
import com.assetly.common.BusinessException;
import com.assetly.location.Location;
import com.assetly.location.LocationRepository;
import com.assetly.organization.MemberRole;
import com.assetly.organization.OrganizationAccessService;
import com.assetly.organization.OrganizationMember;
import com.assetly.organization.OrganizationMemberRepository;
import com.assetly.user.User;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetCategoryRepository assetCategoryRepository;
    private final LocationRepository locationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationAccessService organizationAccessService;

    public AssetService(
            AssetRepository assetRepository,
            AssetCategoryRepository assetCategoryRepository,
            LocationRepository locationRepository,
            OrganizationMemberRepository organizationMemberRepository,
            OrganizationAccessService organizationAccessService
    ) {
        this.assetRepository = assetRepository;
        this.assetCategoryRepository = assetCategoryRepository;
        this.locationRepository = locationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional
    public AssetResponse create(Long userId, Long organizationId, AssetRequest request) {
        OrganizationMember actor = organizationAccessService.requireRole(
                organizationId, userId, MemberRole.OWNER, MemberRole.ADMIN
        );
        String assetCode = normalizeAssetCode(request.assetCode());
        validateDuplicateCode(organizationId, assetCode, null);
        Asset asset = Asset.create(
                actor.getOrganization(),
                findCategory(organizationId, request.categoryId()),
                findLocation(organizationId, request.locationId()),
                findAssignedUser(organizationId, request.assignedUserId()),
                generatePublicCode(),
                assetCode,
                request.name().trim(),
                normalizeText(request.description()),
                request.status(),
                request.purchaseDate(),
                request.purchasePrice()
        );
        return AssetResponse.from(assetRepository.save(asset));
    }

    @Transactional(readOnly = true)
    public AssetPageResponse findAll(
            Long userId,
            Long organizationId,
            String keyword,
            AssetStatus status,
            Long categoryId,
            Long locationId,
            int page,
            int size
    ) {
        organizationAccessService.requireMember(organizationId, userId);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<AssetResponse> result = assetRepository.findAll(
                        search(organizationId, keyword, status, categoryId, locationId),
                        pageable
                )
                .map(AssetResponse::from);
        return AssetPageResponse.from(result);
    }

    @Transactional(readOnly = true)
    public AssetResponse findOne(Long userId, Long organizationId, Long assetId) {
        organizationAccessService.requireMember(organizationId, userId);
        return AssetResponse.from(findAsset(organizationId, assetId));
    }

    @Transactional
    public AssetResponse update(Long userId, Long organizationId, Long assetId, AssetRequest request) {
        OrganizationMember actor = organizationAccessService.requireRole(
                organizationId, userId, MemberRole.OWNER, MemberRole.ADMIN, MemberRole.MANAGER
        );
        Asset asset = findAsset(organizationId, assetId);
        String assetCode = normalizeAssetCode(request.assetCode());
        String description = normalizeText(request.description());
        AssetCategory category = findCategory(organizationId, request.categoryId());
        Location location = findLocation(organizationId, request.locationId());
        User assignedUser = findAssignedUser(organizationId, request.assignedUserId());

        if (actor.getRole() == MemberRole.MANAGER) {
            validateManagerUpdate(asset, request, assetCode, description, category);
            asset.updateOperations(location, assignedUser, request.status());
        } else {
            validateDuplicateCode(organizationId, assetCode, assetId);
            asset.update(
                    category,
                    location,
                    assignedUser,
                    assetCode,
                    request.name().trim(),
                    description,
                    request.status(),
                    request.purchaseDate(),
                    request.purchasePrice()
            );
        }
        return AssetResponse.from(asset);
    }

    @Transactional
    public void delete(Long userId, Long organizationId, Long assetId) {
        organizationAccessService.requireRole(organizationId, userId, MemberRole.OWNER, MemberRole.ADMIN);
        findAsset(organizationId, assetId).deactivate();
    }

    private Specification<Asset> search(
            Long organizationId,
            String keyword,
            AssetStatus status,
            Long categoryId,
            Long locationId
    ) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("organization").get("id"), organizationId));
            predicates.add(builder.isNull(root.get("deletedAt")));
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("name")), pattern),
                        builder.like(builder.lower(root.get("assetCode")), pattern)
                ));
            }
            if (status != null) predicates.add(builder.equal(root.get("status"), status));
            if (categoryId != null) predicates.add(builder.equal(root.get("category").get("id"), categoryId));
            if (locationId != null) predicates.add(builder.equal(root.get("location").get("id"), locationId));
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Asset findAsset(Long organizationId, Long assetId) {
        return assetRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(assetId, organizationId)
                .orElseThrow(() -> new BusinessException("ASSET_NOT_FOUND", "자산을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private AssetCategory findCategory(Long organizationId, Long categoryId) {
        if (categoryId == null) return null;
        return assetCategoryRepository.findByIdAndOrganizationId(categoryId, organizationId)
                .orElseThrow(() -> new BusinessException("CATEGORY_NOT_FOUND", "카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private Location findLocation(Long organizationId, Long locationId) {
        if (locationId == null) return null;
        return locationRepository.findByIdAndOrganizationId(locationId, organizationId)
                .orElseThrow(() -> new BusinessException("LOCATION_NOT_FOUND", "위치를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }

    private User findAssignedUser(Long organizationId, Long assignedUserId) {
        if (assignedUserId == null) return null;
        return organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, assignedUserId)
                .map(OrganizationMember::getUser)
                .orElseThrow(() -> new BusinessException(
                        "ORGANIZATION_MEMBER_NOT_FOUND",
                        "조직 멤버를 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));
    }

    private void validateDuplicateCode(Long organizationId, String assetCode, Long excludedId) {
        boolean duplicated = excludedId == null
                ? assetRepository.existsByOrganizationIdAndAssetCode(organizationId, assetCode)
                : assetRepository.existsByOrganizationIdAndAssetCodeAndIdNot(organizationId, assetCode, excludedId);
        if (duplicated) {
            throw new BusinessException("ASSET_CODE_DUPLICATED", "같은 자산번호가 이미 있습니다.", HttpStatus.CONFLICT);
        }
    }

    private void validateManagerUpdate(
            Asset asset,
            AssetRequest request,
            String assetCode,
            String description,
            AssetCategory category
    ) {
        Long currentCategoryId = asset.getCategory() == null ? null : asset.getCategory().getId();
        Long requestedCategoryId = category == null ? null : category.getId();
        boolean changesRestrictedField = !asset.getAssetCode().equals(assetCode)
                || !asset.getName().equals(request.name().trim())
                || !Objects.equals(asset.getDescription(), description)
                || !Objects.equals(currentCategoryId, requestedCategoryId)
                || !Objects.equals(asset.getPurchaseDate(), request.purchaseDate())
                || !samePrice(asset.getPurchasePrice(), request.purchasePrice());
        if (changesRestrictedField) {
            throw new BusinessException("AUTH_FORBIDDEN", "Manager는 상태, 위치, 담당자만 변경할 수 있습니다.", HttpStatus.FORBIDDEN);
        }
    }

    private boolean samePrice(BigDecimal first, BigDecimal second) {
        if (first == null || second == null) return first == second;
        return first.compareTo(second) == 0;
    }

    private String generatePublicCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        } while (assetRepository.existsByPublicCode(code));
        return code;
    }

    private String normalizeAssetCode(String assetCode) {
        return assetCode.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
