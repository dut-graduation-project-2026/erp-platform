package com.dut.erp.config;

import com.dut.erp.entity.Organization;
import com.dut.erp.entity.OrganizationInvitation;
import com.dut.erp.entity.Partner;
import com.dut.erp.entity.Role;
import com.dut.erp.entity.User;
import com.dut.erp.enums.ActionType;
import com.dut.erp.enums.EntityType;
import com.dut.erp.service.AuditLogService;
import com.dut.erp.util.SpringContext;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuditLogListener {

  @PostPersist
  public void afterCreate(Object entity) {
    recordLog(entity, ActionType.CREATE, "Created");
  }

  @PostUpdate
  public void afterUpdate(Object entity) {
    recordLog(entity, ActionType.UPDATE, "Updated");
  }

  @PostRemove
  public void afterDelete(Object entity) {
    recordLog(entity, ActionType.DELETE, "Deleted");
  }

  private void recordLog(Object entity, ActionType action, String operation) {
    try {
      EntityType entityType = getEntityType(entity);
      if (entityType == null) {
        return; // Not an auditable entity
      }

      UUID entityId = getEntityId(entity);
      UUID organizationId = getOrganizationId(entity);
      String displayName = getEntityDisplayName(entity);
      String message = operation + " " + entityType.name().toLowerCase().replace("_", " ") + ": " + displayName;

      AuditLogService auditLogService = SpringContext.getBean(AuditLogService.class);
      auditLogService.record(organizationId, entityType, entityId, action, message);
    } catch (Exception e) {
      log.error("Failed to automatically record audit log for entity: {}", entity, e);
    }
  }

  private EntityType getEntityType(Object entity) {
    if (entity instanceof Partner) return EntityType.PARTNER;
    if (entity instanceof Organization) return EntityType.ORGANIZATION;
    if (entity instanceof User) return EntityType.USER;
    if (entity instanceof Role) return EntityType.ROLE;
    if (entity instanceof OrganizationInvitation) return EntityType.INVITATION;
    return null;
  }

  private UUID getEntityId(Object entity) {
    if (entity instanceof Partner) return ((Partner) entity).getId();
    if (entity instanceof Organization) return ((Organization) entity).getId();
    if (entity instanceof User) return ((User) entity).getId();
    if (entity instanceof Role) return ((Role) entity).getId();
    if (entity instanceof OrganizationInvitation) return ((OrganizationInvitation) entity).getId();
    return null;
  }

  private UUID getOrganizationId(Object entity) {
    if (entity instanceof Partner) {
      Organization org = ((Partner) entity).getOrganization();
      return org != null ? org.getId() : null;
    }
    if (entity instanceof Organization) {
      return ((Organization) entity).getId();
    }
    if (entity instanceof Role) {
      Organization org = ((Role) entity).getOrganization();
      return org != null ? org.getId() : null;
    }
    if (entity instanceof OrganizationInvitation) {
      Organization org = ((OrganizationInvitation) entity).getOrganization();
      return org != null ? org.getId() : null;
    }
    return null;
  }

  private String getEntityDisplayName(Object entity) {
    if (entity instanceof Partner) return ((Partner) entity).getName();
    if (entity instanceof Organization) return ((Organization) entity).getName();
    if (entity instanceof User) return ((User) entity).getEmail();
    if (entity instanceof Role) return ((Role) entity).getName();
    if (entity instanceof OrganizationInvitation) return ((OrganizationInvitation) entity).getEmail();
    return entity.getClass().getSimpleName();
  }
}
