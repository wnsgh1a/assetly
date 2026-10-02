package com.assetly.organization;

import com.assetly.common.BusinessException;
import com.assetly.organization.dto.CreateOrganizationRequest;
import com.assetly.organization.dto.OrganizationResponse;
import com.assetly.organization.dto.UpdateOrganizationRequest;
import com.assetly.user.User;
import com.assetly.user.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;
    private final OrganizationAccessService organizationAccessService;

    public OrganizationService(
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository organizationMemberRepository,
            UserRepository userRepository,
            OrganizationAccessService organizationAccessService
    ) {
        this.organizationRepository = organizationRepository;
        this.organizationMemberRepository = organizationMemberRepository;
        this.userRepository = userRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional
    public OrganizationResponse create(Long userId, CreateOrganizationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));

        Organization organization = organizationRepository.save(
                Organization.create(request.name(), request.description())
        );

        OrganizationMember member = organizationMemberRepository.save(
                OrganizationMember.owner(organization, user)
        );

        return OrganizationResponse.from(member);
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> findMine(Long userId) {
        return organizationMemberRepository.findAllByUserId(userId).stream()
                .map(OrganizationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrganizationResponse findOne(Long userId, Long organizationId) {
        return OrganizationResponse.from(organizationAccessService.requireMember(organizationId, userId));
    }

    @Transactional
    public OrganizationResponse update(Long userId, Long organizationId, UpdateOrganizationRequest request) {
        OrganizationMember member = organizationAccessService.requireRole(
                organizationId,
                userId,
                MemberRole.OWNER
        );
        member.getOrganization().update(request.name(), request.description());
        return OrganizationResponse.from(member);
    }
}
