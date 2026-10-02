package com.assetly.asset.dto;

import com.assetly.asset.AssetStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AssetRequest(
        @NotBlank @Size(max = 100) String assetCode,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description,
        Long categoryId,
        Long locationId,
        Long assignedUserId,
        @NotNull AssetStatus status,
        LocalDate purchaseDate,
        @DecimalMin("0") @Digits(integer = 13, fraction = 2) BigDecimal purchasePrice
) {
}
