package com.dut.erp.service;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.AuditLogResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
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

  /**
   * Retrieve paginated audit logs for a specific entity ID within an organization.
   */
  PagedEntityResponse<AuditLogResponse> getAuditLogsByEntityId(
      UUID organizationId,
      UUID entityId,
      PaginationRequest paginationRequest);
}
