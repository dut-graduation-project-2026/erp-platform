package com.dut.erp.security;

import com.dut.erp.entity.Role;
import com.dut.erp.entity.User;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
  private final UUID id;
  private final String email;
  private final String password;
  private final Set<Role> roles;
  private final Collection<? extends GrantedAuthority> authorities;

  private static final String PERMISSION_SEPARATOR = ":@:";
  private static final String ROLE_PREFIX = "ROLE_";

  public static CustomUserDetails build(User user) {
    Collection<GrantedAuthority> authorities = new HashSet<>();

    Set<Role> roles = user.getRoles() != null ? user.getRoles() : new HashSet<>();

    for (Role role : roles) {
      if (role == null || role.getName() == null) continue;

      authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role.getName().toUpperCase()));

      if (role.getPermissions() == null) continue;

      role.getPermissions()
          .forEach(
              permission -> {
                if (permission == null || permission.getName() == null) return;
                if (permission.getActions() == null) return;

                permission
                    .getActions()
                    .forEach(
                        action -> {
                          if (action == null || action.getName() == null) return;

                          String auth =
                              permission.getName().toUpperCase()
                                  + PERMISSION_SEPARATOR
                                  + action.getName().toUpperCase();

                          authorities.add(new SimpleGrantedAuthority(auth));
                        });
              });
    }

    return new CustomUserDetails(
        user.getId(), user.getEmail(), user.getPassword(), roles, authorities);
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public String getPassword() {
    return password;
  }
}
