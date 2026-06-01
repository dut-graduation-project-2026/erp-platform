package com.dut.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "partner_contacts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PartnerContact {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  UUID id;

  @Column(name = "name", nullable = false, length = 255)
  String name;

  @Column(name = "email", length = 255)
  String email;

  @Column(name = "phone", length = 50)
  String phone;

  @Column(name = "job_position", length = 100)
  String jobPosition;

  @Column(name = "notes", columnDefinition = "TEXT")
  String notes;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "partner_id", nullable = false)
  Partner partner;
}
