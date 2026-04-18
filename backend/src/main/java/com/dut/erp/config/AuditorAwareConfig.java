package com.dut.erp.config;

import com.dut.erp.entity.User;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.security.CustomUserDetails;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuditorAwareConfig implements AuditorAware<User> {
  private final UserRepository userRepository;

  @Override
  public Optional<User> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }

    if (authentication.getPrincipal() instanceof CustomUserDetails userDetails
        && userDetails.getId() != null) {
      return userRepository.findById(userDetails.getId());
    }

    return Optional.empty();
  }
}
