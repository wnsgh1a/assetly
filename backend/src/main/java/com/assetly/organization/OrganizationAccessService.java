package com.assetly.organization;

import com.assetly.common.BusinessException;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationAccessService {

    private final OrganizationMemberRepository organizationMemberRepository;

    public OrganizationAccessService(OrganizationMemberRepository organizationMemberRepository) {
        this.organizationMemberRepository = organizationMemberRepository;
    }

    @Transactional(readOnly = true)
    public OrganizationMember requireMember(Long organizationId, Long userId) {
        return organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, userId)
                .orElseThrow(() -> new BusinessException(
                        "ORGANIZATION_NOT_FOUND",
                        "조직을 찾을 수 없습니다.",
                        HttpStatus.NOT_FOUND
                ));
    }

    @Transactional(readOnly = true)
    public OrganizationMember requireRole(Long organizationId, Long userId, MemberRole... allowedRoles) {
        OrganizationMember member = requireMember(organizationId, userId);
        boolean allowed = Arrays.stream(allowedRoles).anyMatch(role -> role == member.getRole());
        if (!allowed) {
            throw new BusinessException(
                    "AUTH_FORBIDDEN",
                    "이 작업을 수행할 권한이 없습니다.",
                    HttpStatus.FORBIDDEN
            );
        }
        return member;
    }
}
