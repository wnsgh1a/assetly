package com.assetly.organization;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {

    List<OrganizationMember> findAllByUserId(Long userId);

    Optional<OrganizationMember> findByOrganizationIdAndUserId(Long organizationId, Long userId);

    Optional<OrganizationMember> findByIdAndOrganizationId(Long id, Long organizationId);

    List<OrganizationMember> findAllByOrganizationIdOrderByCreatedAtAsc(Long organizationId);

    boolean existsByOrganizationIdAndUserId(Long organizationId, Long userId);

    long countByOrganizationIdAndRole(Long organizationId, MemberRole role);
}
