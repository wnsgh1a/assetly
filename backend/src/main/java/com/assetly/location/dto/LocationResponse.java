package com.assetly.location.dto;

import com.assetly.location.Location;
import java.time.Instant;

public record LocationResponse(
        Long id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {

    public static LocationResponse from(Location location) {
        return new LocationResponse(
                location.getId(),
                location.getName(),
                location.getDescription(),
                location.getCreatedAt(),
                location.getUpdatedAt()
        );
    }
}
