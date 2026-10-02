package com.assetly.location;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findAllByOrganizationIdOrderByNameAsc(Long organizationId);

    Optional<Location> findByIdAndOrganizationId(Long id, Long organizationId);

    boolean existsByOrganizationIdAndName(Long organizationId, String name);

    boolean existsByOrganizationIdAndNameAndIdNot(Long organizationId, String name, Long id);
}
