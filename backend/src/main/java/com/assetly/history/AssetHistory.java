package com.assetly.history;

import com.assetly.asset.Asset;
import com.assetly.organization.Organization;
import com.assetly.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "asset_histories")
@EntityListeners(AuditingEntityListener.class)
public class AssetHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "action_type", nullable = false, length = 30)
    private AssetHistoryAction actionType;

    @Column(name = "field_name", length = 50)
    private String fieldName;

    @Column(name = "before_value", columnDefinition = "text")
    private String beforeValue;

    @Column(name = "after_value", columnDefinition = "text")
    private String afterValue;

    @Column(columnDefinition = "text")
    private String memo;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AssetHistory() {
    }

    private AssetHistory(
            Asset asset,
            User actor,
            AssetHistoryAction actionType,
            String fieldName,
            String beforeValue,
            String afterValue,
            String memo
    ) {
        this.organization = asset.getOrganization();
        this.asset = asset;
        this.actor = actor;
        this.actionType = actionType;
        this.fieldName = fieldName;
        this.beforeValue = beforeValue;
        this.afterValue = afterValue;
        this.memo = memo;
    }

    public static AssetHistory create(
            Asset asset,
            User actor,
            AssetHistoryAction actionType,
            String fieldName,
            String beforeValue,
            String afterValue,
            String memo
    ) {
        return new AssetHistory(asset, actor, actionType, fieldName, beforeValue, afterValue, memo);
    }

    public Long getId() { return id; }
    public Organization getOrganization() { return organization; }
    public Asset getAsset() { return asset; }
    public User getActor() { return actor; }
    public AssetHistoryAction getActionType() { return actionType; }
    public String getFieldName() { return fieldName; }
    public String getBeforeValue() { return beforeValue; }
    public String getAfterValue() { return afterValue; }
    public String getMemo() { return memo; }
    public Instant getCreatedAt() { return createdAt; }
}
