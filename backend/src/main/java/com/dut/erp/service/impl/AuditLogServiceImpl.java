package com.dut.erp.service.impl;

import com.dut.erp.entity.AuditLog;
import com.dut.erp.enums.ActionType;
import com.dut.erp.enums.EntityType;
import com.dut.erp.repository.AuditLogRepository;
import com.dut.erp.service.AuditLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

  private final AuditLogRepository auditLogRepository;

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void record(
      UUID organizationId,
      EntityType entityType,
      UUID entityId,
      ActionType action,
      String changedField,
      String oldValue,
      String newValue,
      String message) {

    try {
      AuditLog auditLog =
          AuditLog.builder()
              .entityType(entityType.name())
              .entityId(entityId)
              .action(action)
              .changedField(changedField)
              .oldValue(oldValue)
              .newValue(newValue)
              .message(message)
              .organizationId(organizationId)
              .build();

      auditLogRepository.save(auditLog);
      log.debug(
          "Recorded audit log for organization: {}, entity: {} ({}), action: {}",
          organizationId,
          entityId,
          entityType,
          action);
    } catch (Exception e) {
      log.error(
          "Failed to record audit log for organization: {}, entity: {} ({}), action: {}",
          organizationId,
          entityId,
          entityType,
          action,
          e);
    }
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void record(
      UUID organizationId,
      EntityType entityType,
      UUID entityId,
      ActionType action,
      String message) {
    record(organizationId, entityType, entityId, action, null, null, null, message);
  }
}
