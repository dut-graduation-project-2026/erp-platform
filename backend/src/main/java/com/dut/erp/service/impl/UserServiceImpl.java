package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.entity.User;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.UserMapper;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.service.UserService;
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
      UUID organizationId, PaginationRequest paginationRequest) {
    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.DEFAULT_ENTITIES_SORT);
    log.info(
        "Fetching users for organization {} with pagination: page={}, limit={}",
        organizationId,
        paginationRequest.page(),
        paginationRequest.limit());
    Page<User> userPage = userRepository.findAllByOrganizations_Id(organizationId, pageable);
    Page<UserBaseResponse> userResponses = userPage.map(userMapper::toUserBaseResponse);
    log.info(
        "Fetched {} users for organization {} (total elements: {}, total pages: {})",
        userResponses.getNumberOfElements(),
        organizationId,
        userPage.getTotalElements(),
        userPage.getTotalPages());

    return PagedEntityResponse.from(userResponses);
  }

  @Override
  public PagedEntityResponse<UserBaseResponse> searchUsersByEmailContaining(
      String email, PaginationRequest paginationRequest) {
    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.DEFAULT_ENTITIES_SORT);
    log.info(
        "Searching users by email containing '{}' with pagination: page={}, limit={}",
        email,
        paginationRequest.page(),
        paginationRequest.limit());

    Page<User> userPage;
    if (email == null || email.trim().isEmpty()) {
      log.info("Email parameter is empty, fetching all users with pagination");
      userPage = userRepository.findAll(pageable);
    } else {
      log.info("Email parameter is not empty, searching users with email containing '{}'", email);
      userPage = userRepository.findAllByEmailContainingIgnoreCase(email, pageable);
    }
    Page<UserBaseResponse> userResponses = userPage.map(userMapper::toUserBaseResponse);
    log.info(
        "Found {} users with email containing '{}' (total elements: {}, total pages: {})",
        userResponses.getNumberOfElements(),
        email,
        userPage.getTotalElements(),
        userPage.getTotalPages());
    return PagedEntityResponse.from(userResponses);
  }

  @Override
  public User findUserById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> {
              log.warn("User with ID {} not found", userId);
              return new ResourceNotFoundException("User not found");
            });
  }
}
