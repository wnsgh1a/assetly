package com.assetly.organization;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {

    List<OrganizationMember> findAllByUserId(Long userId);

    Optional<OrganizationMember> findByOrganizationIdAndUserId(Long organizationId, Long userId);
}
