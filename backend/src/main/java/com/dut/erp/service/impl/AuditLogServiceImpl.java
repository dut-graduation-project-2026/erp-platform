package com.dut.erp.service.impl;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.AuditLogResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.entity.AuditLog;
import com.dut.erp.enums.ActionType;
import com.dut.erp.enums.EntityType;
import com.dut.erp.mapper.AuditLogMapper;
import com.dut.erp.repository.AuditLogRepository;
import com.dut.erp.service.AuditLogService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

  private final AuditLogRepository auditLogRepository;
  private final AuditLogMapper auditLogMapper;

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
      String finalMessage = message;
      if (finalMessage == null || finalMessage.trim().isEmpty()) {
        finalMessage = action + " " + entityType.name() + " (" + entityId + ")";
      }

      AuditLog auditLog =
          AuditLog.builder()
              .entityType(entityType.name())
              .entityId(entityId)
              .action(action)
              .changedField(changedField)
              .oldValue(oldValue)
              .newValue(newValue)
              .message(finalMessage)
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

  @Override
  @Transactional(readOnly = true)
  public PagedEntityResponse<AuditLogResponse> getAuditLogsByEntityId(
      UUID organizationId,
      UUID entityId,
      PaginationRequest paginationRequest) {
    Pageable pageable = PageRequest.of(paginationRequest.page() - 1, paginationRequest.limit());
    Page<AuditLog> auditLogsPage = auditLogRepository.findByEntityId(entityId, pageable);
    Page<AuditLogResponse> responsePage = auditLogsPage.map(auditLogMapper::toAuditLogResponse);
    return PagedEntityResponse.from(responsePage);
  }
}
