package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateUserRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.entity.User;
import com.dut.erp.enums.ActionType;
import com.dut.erp.enums.EntityType;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.UserMapper;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.service.AuditLogService;
import com.dut.erp.service.UserService;
import com.dut.erp.util.SearchUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final AuditLogService auditLogService;

  @Override
  public PagedEntityResponse<UserBaseResponse> searchUsersByOrganizationId(
      UUID organizationId, String query, PaginationRequest paginationRequest) {
    String normalizedQuery = SearchUtils.normalizeOptionalFilter(query);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.DEFAULT_ENTITIES_SORT);
    Page<User> userPage;
    if (normalizedQuery == null) {
      userPage = userRepository.findAllByOrganizationsId(organizationId, pageable);
    } else {
      String escapedQuery = SearchUtils.escapeLikePattern(normalizedQuery);
      userPage =
          userRepository.searchByOrganizationsIdAndQuery(organizationId, escapedQuery, pageable);
    }

    Page<UserBaseResponse> userResponses = userPage.map(userMapper::toUserBaseResponse);
    log.info(
        "Fetched {} users for organization {} (hasQuery={}, total elements: {}, total pages: {})",
        userResponses.getNumberOfElements(),
        organizationId,
        normalizedQuery != null,
        userPage.getTotalElements(),
        userPage.getTotalPages());

    return PagedEntityResponse.from(userResponses);
  }

  @Override
  @Transactional
  public UserBaseResponse updateUser(UUID userId, UpdateUserRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> {
                  log.warn("User with ID {} not found", userId);
                  return new ResourceNotFoundException("User not found with id: " + userId);
                });

    List<String> changes = new ArrayList<>();
    if (!Objects.equals(user.getFirstName(), request.firstName())) {
      changes.add("firstName: '" + user.getFirstName() + "' -> '" + request.firstName() + "'");
    }
    if (!Objects.equals(user.getLastName(), request.lastName())) {
      changes.add("lastName: '" + user.getLastName() + "' -> '" + request.lastName() + "'");
    }

    String message = "Updated user profile: " + request.firstName() + " " + request.lastName();
    String changedField = null;
    String oldValue = null;
    String newValue = null;

    if (!changes.isEmpty()) {
      message += ". Changes: " + String.join(", ", changes);
      String firstChange = changes.get(0);
      int colonIdx = firstChange.indexOf(":");
      int arrowIdx = firstChange.indexOf(" -> ");
      if (colonIdx > 0 && arrowIdx > colonIdx) {
        changedField = firstChange.substring(0, colonIdx).trim();
        oldValue = firstChange.substring(colonIdx + 1, arrowIdx).replace("'", "").trim();
        newValue = firstChange.substring(arrowIdx + 4).replace("'", "").trim();
      }
    } else {
      message += ". No fields changed.";
    }

    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());

    user = userRepository.save(user);
    log.info("User {} updated successfully", userId);

    auditLogService.record(
        null,
        EntityType.USER,
        user.getId(),
        ActionType.UPDATE,
        changedField,
        oldValue,
        newValue,
        message);

    return userMapper.toUserBaseResponse(user);
  }
}
