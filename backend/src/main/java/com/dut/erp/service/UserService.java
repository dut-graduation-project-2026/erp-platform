package com.dut.erp.service;

import java.util.UUID;

public interface UserService {
  boolean existsById(UUID userId);
}
