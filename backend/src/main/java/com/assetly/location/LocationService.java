package com.assetly.location;

import com.assetly.asset.AssetRepository;
import com.assetly.common.BusinessException;
import com.assetly.location.dto.LocationRequest;
import com.assetly.location.dto.LocationResponse;
import com.assetly.organization.MemberRole;
import com.assetly.organization.OrganizationAccessService;
import com.assetly.organization.OrganizationMember;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationService {

    private final LocationRepository locationRepository;
    private final AssetRepository assetRepository;
    private final OrganizationAccessService organizationAccessService;

    public LocationService(
            LocationRepository locationRepository,
            AssetRepository assetRepository,
            OrganizationAccessService organizationAccessService
    ) {
        this.locationRepository = locationRepository;
        this.assetRepository = assetRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional(readOnly = true)
    public List<LocationResponse> findAll(Long userId, Long organizationId) {
        organizationAccessService.requireMember(organizationId, userId);
        return locationRepository.findAllByOrganizationIdOrderByNameAsc(organizationId).stream()
                .map(LocationResponse::from)
                .toList();
    }

    @Transactional
    public LocationResponse create(Long userId, Long organizationId, LocationRequest request) {
        OrganizationMember member = requireManager(organizationId, userId);
        String name = request.name().trim();
        validateDuplicateName(organizationId, name, null);
        Location location = Location.create(member.getOrganization(), name, normalizeDescription(request.description()));
        return LocationResponse.from(locationRepository.save(location));
    }

    @Transactional
    public LocationResponse update(Long userId, Long organizationId, Long locationId, LocationRequest request) {
        requireManager(organizationId, userId);
        Location location = findLocation(organizationId, locationId);
        String name = request.name().trim();
        validateDuplicateName(organizationId, name, locationId);
        location.update(name, normalizeDescription(request.description()));
        return LocationResponse.from(location);
    }

    @Transactional
    public void delete(Long userId, Long organizationId, Long locationId) {
        requireManager(organizationId, userId);
        Location location = findLocation(organizationId, locationId);
        if (assetRepository.existsByOrganizationIdAndLocationIdAndDeletedAtIsNull(organizationId, locationId)) {
            throw new BusinessException(
                    "LOCATION_IN_USE",
                    "사용 중인 위치는 삭제할 수 없습니다.",
                    HttpStatus.CONFLICT
            );
        }
        locationRepository.delete(location);
    }

    private OrganizationMember requireManager(Long organizationId, Long userId) {
        return organizationAccessService.requireRole(
                organizationId,
                userId,
                MemberRole.OWNER,
                MemberRole.ADMIN
        );
    }

    private Location findLocation(Long organizationId, Long locationId) {
        return locationRepository.findByIdAndOrganizationId(locationId, organizationId)
                .orElseThrow(() -> new BusinessException(
                        "LOCATION_NOT_FOUND",
                        "위치를 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));
    }

    private void validateDuplicateName(Long organizationId, String name, Long excludedId) {
        boolean duplicated = excludedId == null
                ? locationRepository.existsByOrganizationIdAndName(organizationId, name)
                : locationRepository.existsByOrganizationIdAndNameAndIdNot(organizationId, name, excludedId);
        if (duplicated) {
            throw new BusinessException(
                    "LOCATION_NAME_DUPLICATED",
                    "같은 이름의 위치가 이미 있습니다.",
                    HttpStatus.CONFLICT
            );
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }
}
