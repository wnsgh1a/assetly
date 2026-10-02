package com.assetly.organization;

import com.assetly.common.BusinessException;
import com.assetly.organization.dto.AddOrganizationMemberRequest;
import com.assetly.organization.dto.OrganizationMemberResponse;
import com.assetly.organization.dto.UpdateMemberRoleRequest;
import com.assetly.user.User;
import com.assetly.user.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationMemberService {

    private final OrganizationAccessService organizationAccessService;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;

    public OrganizationMemberService(
            OrganizationAccessService organizationAccessService,
            OrganizationMemberRepository organizationMemberRepository,
            UserRepository userRepository
    ) {
        this.organizationAccessService = organizationAccessService;
        this.organizationMemberRepository = organizationMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<OrganizationMemberResponse> findAll(Long userId, Long organizationId) {
        organizationAccessService.requireRole(organizationId, userId, MemberRole.OWNER, MemberRole.ADMIN);
        return organizationMemberRepository.findAllByOrganizationIdOrderByCreatedAtAsc(organizationId).stream()
                .map(OrganizationMemberResponse::from)
                .toList();
    }

    @Transactional
    public OrganizationMemberResponse add(
            Long userId,
            Long organizationId,
            AddOrganizationMemberRequest request
    ) {
        OrganizationMember actor = organizationAccessService.requireRole(
                organizationId,
                userId,
                MemberRole.OWNER,
                MemberRole.ADMIN
        );
        validateAssignableRole(actor.getRole(), request.role());

        User targetUser = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(
                        "USER_NOT_FOUND",
                        "가입된 사용자를 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));

        if (organizationMemberRepository.existsByOrganizationIdAndUserId(organizationId, targetUser.getId())) {
            throw new BusinessException(
                    "ORGANIZATION_MEMBER_DUPLICATED",
                    "이미 조직에 속한 사용자입니다.",
                    HttpStatus.CONFLICT
            );
        }

        OrganizationMember member = OrganizationMember.create(
                actor.getOrganization(),
                targetUser,
                request.role()
        );
        return OrganizationMemberResponse.from(organizationMemberRepository.save(member));
    }

    @Transactional
    public OrganizationMemberResponse updateRole(
            Long userId,
            Long organizationId,
            Long memberId,
            UpdateMemberRoleRequest request
    ) {
        OrganizationMember actor = organizationAccessService.requireRole(
                organizationId,
                userId,
                MemberRole.OWNER,
                MemberRole.ADMIN
        );
        OrganizationMember target = findMember(organizationId, memberId);
        validateManageableMember(actor.getRole(), target);
        validateAssignableRole(actor.getRole(), request.role());
        preventLastOwnerChange(organizationId, target, request.role());

        target.changeRole(request.role());
        return OrganizationMemberResponse.from(target);
    }

    @Transactional
    public void remove(Long userId, Long organizationId, Long memberId) {
        OrganizationMember actor = organizationAccessService.requireRole(
                organizationId,
                userId,
                MemberRole.OWNER,
                MemberRole.ADMIN
        );
        OrganizationMember target = findMember(organizationId, memberId);
        validateManageableMember(actor.getRole(), target);
        preventLastOwnerRemoval(organizationId, target);
        organizationMemberRepository.delete(target);
    }

    private OrganizationMember findMember(Long organizationId, Long memberId) {
        return organizationMemberRepository.findByIdAndOrganizationId(memberId, organizationId)
                .orElseThrow(() -> new BusinessException(
                        "ORGANIZATION_MEMBER_NOT_FOUND",
                        "조직 멤버를 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));
    }

    private void validateAssignableRole(MemberRole actorRole, MemberRole requestedRole) {
        if (actorRole == MemberRole.ADMIN
                && (requestedRole == MemberRole.OWNER || requestedRole == MemberRole.ADMIN)) {
            throw forbidden();
        }
    }

    private void validateManageableMember(MemberRole actorRole, OrganizationMember target) {
        if (actorRole == MemberRole.ADMIN
                && (target.getRole() == MemberRole.OWNER || target.getRole() == MemberRole.ADMIN)) {
            throw forbidden();
        }
    }

    private void preventLastOwnerChange(
            Long organizationId,
            OrganizationMember target,
            MemberRole requestedRole
    ) {
        if (target.getRole() == MemberRole.OWNER
                && requestedRole != MemberRole.OWNER
                && isLastOwner(organizationId)) {
            throw lastOwnerRequired();
        }
    }

    private void preventLastOwnerRemoval(Long organizationId, OrganizationMember target) {
        if (target.getRole() == MemberRole.OWNER && isLastOwner(organizationId)) {
            throw lastOwnerRequired();
        }
    }

    private boolean isLastOwner(Long organizationId) {
        return organizationMemberRepository.countByOrganizationIdAndRole(organizationId, MemberRole.OWNER) <= 1;
    }

    private BusinessException forbidden() {
        return new BusinessException(
                "AUTH_FORBIDDEN",
                "이 작업을 수행할 권한이 없습니다.",
                HttpStatus.FORBIDDEN
        );
    }

    private BusinessException lastOwnerRequired() {
        return new BusinessException(
                "ORGANIZATION_LAST_OWNER_REQUIRED",
                "조직에는 최소 한 명의 Owner가 필요합니다.",
                HttpStatus.CONFLICT
        );
    }
}
