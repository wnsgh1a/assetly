package com.assetly.asset;

import com.assetly.category.AssetCategory;
import com.assetly.common.BaseTimeEntity;
import com.assetly.location.Location;
import com.assetly.organization.Organization;
import com.assetly.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "assets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_asset_public_code", columnNames = "public_code"),
                @UniqueConstraint(name = "uk_asset_organization_code", columnNames = {"organization_id", "asset_code"})
        }
)
public class Asset extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private AssetCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;

    @Column(name = "public_code", nullable = false, length = 32)
    private String publicCode;

    @Column(name = "asset_code", nullable = false, length = 100)
    private String assetCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private AssetStatus status;

    private LocalDate purchaseDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal purchasePrice;

    private Instant deletedAt;

    protected Asset() {
    }

    private Asset(
            Organization organization,
            AssetCategory category,
            Location location,
            User assignedUser,
            String publicCode,
            String assetCode,
            String name,
            String description,
            AssetStatus status,
            LocalDate purchaseDate,
            BigDecimal purchasePrice
    ) {
        this.organization = organization;
        this.category = category;
        this.location = location;
        this.assignedUser = assignedUser;
        this.publicCode = publicCode;
        this.assetCode = assetCode;
        this.name = name;
        this.description = description;
        this.status = status;
        this.purchaseDate = purchaseDate;
        this.purchasePrice = purchasePrice;
    }

    public static Asset create(
            Organization organization,
            AssetCategory category,
            Location location,
            User assignedUser,
            String publicCode,
            String assetCode,
            String name,
            String description,
            AssetStatus status,
            LocalDate purchaseDate,
            BigDecimal purchasePrice
    ) {
        return new Asset(organization, category, location, assignedUser, publicCode, assetCode, name,
                description, status, purchaseDate, purchasePrice);
    }

    public void update(
            AssetCategory category,
            Location location,
            User assignedUser,
            String assetCode,
            String name,
            String description,
            AssetStatus status,
            LocalDate purchaseDate,
            BigDecimal purchasePrice
    ) {
        this.category = category;
        this.location = location;
        this.assignedUser = assignedUser;
        this.assetCode = assetCode;
        this.name = name;
        this.description = description;
        this.status = status;
        this.purchaseDate = purchaseDate;
        this.purchasePrice = purchasePrice;
    }

    public void updateOperations(Location location, User assignedUser, AssetStatus status) {
        this.location = location;
        this.assignedUser = assignedUser;
        this.status = status;
    }

    public void deactivate() {
        this.deletedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Organization getOrganization() { return organization; }
    public AssetCategory getCategory() { return category; }
    public Location getLocation() { return location; }
    public User getAssignedUser() { return assignedUser; }
    public String getPublicCode() { return publicCode; }
    public String getAssetCode() { return assetCode; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public AssetStatus getStatus() { return status; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public Instant getDeletedAt() { return deletedAt; }
}
