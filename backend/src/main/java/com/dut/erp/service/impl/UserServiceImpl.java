package com.dut.erp.service.impl;

import com.dut.erp.entity.User;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;

  @Override
  public User findByEmail(String email) {
    return userRepository
        .findByEmail(email)
        .orElseThrow(
            () -> {
              log.warn("User with email {} not found", email);
              return new ResourceNotFoundException("User not found with email: " + email);
            });
  }

  @Override
  public User findById(UUID userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> {
              log.warn("User with id {} not found", userId);
              return new ResourceNotFoundException("User not found with id: " + userId);
            });
  }
}
