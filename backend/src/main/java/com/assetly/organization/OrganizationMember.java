package com.assetly.organization;

import com.assetly.common.BaseTimeEntity;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(
        name = "organization_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_organization_member", columnNames = {"organization_id", "user_id"})
        }
)
public class OrganizationMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    protected OrganizationMember() {
    }

    private OrganizationMember(Organization organization, User user, MemberRole role) {
        this.organization = organization;
        this.user = user;
        this.role = role;
    }

    public static OrganizationMember owner(Organization organization, User user) {
        return new OrganizationMember(organization, user, MemberRole.OWNER);
    }

    public static OrganizationMember create(Organization organization, User user, MemberRole role) {
        return new OrganizationMember(organization, user, role);
    }

    public Long getId() {
        return id;
    }

    public Organization getOrganization() {
        return organization;
    }

    public User getUser() {
        return user;
    }

    public MemberRole getRole() {
        return role;
    }
}
