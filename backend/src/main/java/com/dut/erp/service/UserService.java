package com.dut.erp.service;

import com.dut.erp.entity.User;
import java.util.UUID;

public interface UserService {
  User findById(UUID id);

  User findByEmail(String email);
}
