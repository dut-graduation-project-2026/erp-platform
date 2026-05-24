package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateUserRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.dto.response.PermissionResponse;
import com.dut.erp.dto.response.UserPermissionsResponse;
import com.dut.erp.entity.User;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.UserMapper;
import com.dut.erp.mapper.PermissionMapper;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.service.UserService;
import com.dut.erp.util.SearchUtils;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

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
  private final PermissionRepository permissionRepository;
  private final PermissionMapper permissionMapper;

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

    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());

    user = userRepository.save(user);
    log.info("User {} updated successfully", userId);
    return userMapper.toUserBaseResponse(user);
  }

  @Override
  public UserPermissionsResponse getMyPermissions(UUID userId, UUID organizationId) {
    List<PermissionResponse> list = permissionRepository.findPermissionsByUserIdAndOrganizationId(userId, organizationId)
        .stream()
        .map(permissionMapper::toPermissionResponse)
        .collect(Collectors.toList());
    return UserPermissionsResponse.builder().permissions(list).build();
  }
}
