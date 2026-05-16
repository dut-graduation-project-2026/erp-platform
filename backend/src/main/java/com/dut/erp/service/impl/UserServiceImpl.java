package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateUserRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.entity.User;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.UserMapper;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.service.UserService;
import java.util.Locale;
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

  @Override
  public PagedEntityResponse<UserBaseResponse> getUsersByOrganizationId(
      UUID organizationId,
      String query,
      String moduleCode,
      PaginationRequest paginationRequest) {
    String normalizedQuery = normalizeOptionalFilter(query);
    String normalizedModuleCode = normalizeOptionalFilter(moduleCode);

    if (normalizedModuleCode != null) {
      normalizedModuleCode = normalizedModuleCode.toLowerCase(Locale.ROOT);
    }

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.DEFAULT_ENTITIES_SORT);
    Page<User> userPage;
    if (normalizedQuery == null) {
      if (normalizedModuleCode == null) {
        userPage = userRepository.findAllByOrganizationsId(organizationId, pageable);
      } else {
        userPage =
            userRepository.searchByOrganizationsIdAndModuleCode(
                organizationId, normalizedModuleCode, pageable);
      }
    } else if (normalizedModuleCode == null) {
      userPage =
          userRepository.searchByOrganizationsIdAndQuery(organizationId, normalizedQuery, pageable);
    } else {
      userPage =
          userRepository.searchByOrganizationsIdAndQueryAndModuleCode(
              organizationId, normalizedQuery, normalizedModuleCode, pageable);
    }

    Page<UserBaseResponse> userResponses = userPage.map(userMapper::toUserBaseResponse);
    log.info(
        "Fetched {} users for organization {} with query='{}', moduleCode='{}' (total elements: {}, total pages: {})",
        userResponses.getNumberOfElements(),
        organizationId,
        normalizedQuery,
        normalizedModuleCode,
        userPage.getTotalElements(),
        userPage.getTotalPages());

    return PagedEntityResponse.from(userResponses);
  }

  private String normalizeOptionalFilter(String value) {
    if (value == null) {
      return null;
    }

    String trimmedValue = value.trim();
    return trimmedValue.isEmpty() ? null : trimmedValue;
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

    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());

    user = userRepository.save(user);
    log.info("User {} updated successfully", userId);
    return userMapper.toUserBaseResponse(user);
  }
}
