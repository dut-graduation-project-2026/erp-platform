package com.dut.erp.service;

import com.dut.erp.enums.ActionType;
import com.dut.erp.enums.EntityType;
import java.util.UUID;

public interface AuditLogService {

  /**
   * Record a detailed audit log entry including oldValue and newValue changes.
   */
  void record(
      UUID organizationId,
      EntityType entityType,
      UUID entityId,
      ActionType action,
      String changedField,
      String oldValue,
      String newValue,
      String message);

  /**
   * Record a general audit log entry with a message.
   */
  void record(
      UUID organizationId,
      EntityType entityType,
      UUID entityId,
      ActionType action,
      String message);
}
