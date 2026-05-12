package com.dut.erp.entity;

import com.dut.erp.enums.OrganizationInvitationStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "organization_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class OrganizationInvitation {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @Column(nullable = false)
  String email;

  @Column(nullable = false)
  Instant expiresAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  OrganizationInvitationStatus status = OrganizationInvitationStatus.PENDING;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id")
  Role role;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "responded_by")
  User respondedBy;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "organization_id", nullable = false)
  Organization organization;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "invited_by", nullable = false)
  @CreatedBy
  User invitedBy;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  Instant createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  Instant updatedAt;

  public boolean isExpired() {
    return Instant.now().isAfter(expiresAt);
  }

  public boolean isPending() {
    return status == OrganizationInvitationStatus.PENDING && !isExpired();
  }
}
