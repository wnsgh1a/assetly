package com.assetly.history;

import com.assetly.asset.Asset;
import com.assetly.asset.AssetRepository;
import com.assetly.common.BusinessException;
import com.assetly.history.dto.AssetHistoryPageResponse;
import com.assetly.history.dto.AssetHistoryResponse;
import com.assetly.organization.OrganizationAccessService;
import com.assetly.user.User;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssetHistoryService {

    private final AssetHistoryRepository assetHistoryRepository;
    private final AssetRepository assetRepository;
    private final OrganizationAccessService organizationAccessService;

    public AssetHistoryService(
            AssetHistoryRepository assetHistoryRepository,
            AssetRepository assetRepository,
            OrganizationAccessService organizationAccessService
    ) {
        this.assetHistoryRepository = assetHistoryRepository;
        this.assetRepository = assetRepository;
        this.organizationAccessService = organizationAccessService;
    }

    public void recordCreated(Asset asset, User actor) {
        save(asset, actor, AssetHistoryAction.CREATED, null, null, null, "자산 등록");
    }

    public void recordDeleted(Asset asset, User actor) {
        save(asset, actor, AssetHistoryAction.DELETED, null, null, null, "자산 비활성화");
    }

    public void recordChange(Asset asset, User actor, String fieldName, Object beforeValue, Object afterValue) {
        String before = displayValue(beforeValue);
        String after = displayValue(afterValue);
        if (Objects.equals(before, after)) return;
        save(asset, actor, actionFor(fieldName), fieldName, before, after, null);
    }

    @Transactional(readOnly = true)
    public AssetHistoryPageResponse findByAsset(
            Long userId,
            Long organizationId,
            Long assetId,
            int page,
            int size
    ) {
        organizationAccessService.requireMember(organizationId, userId);
        assetRepository.findByIdAndOrganizationId(assetId, organizationId)
                .orElseThrow(() -> new BusinessException("ASSET_NOT_FOUND", "자산을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return AssetHistoryPageResponse.from(
                assetHistoryRepository.findByOrganizationIdAndAssetId(organizationId, assetId, pageable)
                        .map(AssetHistoryResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public AssetHistoryPageResponse findAll(
            Long userId,
            Long organizationId,
            Long assetId,
            Long actorId,
            AssetHistoryAction actionType,
            Instant from,
            Instant to,
            int page,
            int size
    ) {
        organizationAccessService.requireMember(organizationId, userId);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        Page<AssetHistoryResponse> result = assetHistoryRepository
                .findAll(search(organizationId, assetId, actorId, actionType, from, to), pageable)
                .map(AssetHistoryResponse::from);
        return AssetHistoryPageResponse.from(result);
    }

    private Specification<AssetHistory> search(
            Long organizationId,
            Long assetId,
            Long actorId,
            AssetHistoryAction actionType,
            Instant from,
            Instant to
    ) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("organization").get("id"), organizationId));
            if (assetId != null) predicates.add(builder.equal(root.get("asset").get("id"), assetId));
            if (actorId != null) predicates.add(builder.equal(root.get("actor").get("id"), actorId));
            if (actionType != null) predicates.add(builder.equal(root.get("actionType"), actionType));
            if (from != null) predicates.add(builder.greaterThanOrEqualTo(root.get("createdAt"), from));
            if (to != null) predicates.add(builder.lessThanOrEqualTo(root.get("createdAt"), to));
            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private void save(
            Asset asset,
            User actor,
            AssetHistoryAction action,
            String fieldName,
            String beforeValue,
            String afterValue,
            String memo
    ) {
        assetHistoryRepository.save(AssetHistory.create(
                asset, actor, action, fieldName, beforeValue, afterValue, memo
        ));
    }

    private AssetHistoryAction actionFor(String fieldName) {
        return switch (fieldName) {
            case "status" -> AssetHistoryAction.STATUS_CHANGED;
            case "location" -> AssetHistoryAction.LOCATION_CHANGED;
            case "assignedUser" -> AssetHistoryAction.ASSIGNEE_CHANGED;
            default -> AssetHistoryAction.UPDATED;
        };
    }

    private String displayValue(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal decimal) return decimal.stripTrailingZeros().toPlainString();
        String text = value.toString();
        return text.isBlank() ? null : text;
    }
}
