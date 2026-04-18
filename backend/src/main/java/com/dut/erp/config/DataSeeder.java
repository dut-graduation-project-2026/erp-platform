package com.dut.erp.config;

import com.dut.erp.entity.Action;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Permission;
import com.dut.erp.entity.Role;
import com.dut.erp.entity.User;
import com.dut.erp.repository.ActionRepository;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.repository.RoleRepository;
import com.dut.erp.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional
public class DataSeeder implements CommandLineRunner {
  private static final String SUPER_ADMIN_ROLE_NAME = "superadmin";
  private static final String SYSTEM_ADMIN_EMAIL = "system.admin@erp.local";
  private static final String SYSTEM_ADMIN_PASSWORD = "SystemAdmin@123";

  private static final List<String> DEFAULT_ACTION_NAMES = List.of("create", "view", "update", "delete");

  private static final List<PermissionSeed> DEFAULT_PERMISSIONS =
      List.of(
          new PermissionSeed("organizations", "Manage organizations"),
          new PermissionSeed("roles", "Manage roles in the system"),
          new PermissionSeed("users/roles", "Manage role assignment for each account"));

  private final ActionRepository actionRepository;
  private final OrganizationRepository organizationRepository;
  private final PermissionRepository permissionRepository;
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) {
    List<Action> defaultActions = seedDefaultActions();

    Organization organizationOne =
        seedOrganization(
            "Default Organization 1",
            "Default seeded organization for initial setup",
            "1 Default Street",
            "19001001");

    Organization organizationTwo =
        seedOrganization(
        "Default Organization 2",
        "Default seeded organization for initial setup",
        "2 Default Street",
        "19001002");

    List<Permission> organizationOnePermissions = seedPermissions(organizationOne, defaultActions);
    seedPermissions(organizationTwo, defaultActions);

    Role superAdminRole = seedSuperAdminRole(organizationOne, organizationOnePermissions);
    seedSystemAdminUser(organizationOne, superAdminRole);
  }

  private List<Action> seedDefaultActions() {
    return DEFAULT_ACTION_NAMES.stream().map(this::findOrCreateAction).toList();
  }

  private Action findOrCreateAction(String actionName) {
    return actionRepository
        .findByName(actionName)
        .orElseGet(() -> actionRepository.save(Action.builder().name(actionName).build()));
  }

  private Organization seedOrganization(
      String name, String description, String address, String hotline) {
    return organizationRepository
        .findByName(name)
        .orElseGet(
            () ->
                organizationRepository.save(
                    Organization.builder()
                        .name(name)
                        .description(description)
                        .address(address)
                        .hotline(hotline)
                        .build()));
  }

  private List<Permission> seedPermissions(Organization organization, List<Action> defaultActions) {
    return DEFAULT_PERMISSIONS.stream()
        .map(
            permissionSeed ->
                seedPermission(
                    organization,
                    permissionSeed.name(),
                    permissionSeed.description(),
                    defaultActions))
        .toList();
  }

  private Permission seedPermission(
      Organization organization, String permissionName, String description, List<Action> defaultActions) {
    Permission permission =
        permissionRepository
            .findByNameAndOrganizationId(permissionName, organization.getId())
            .orElseGet(
                () ->
                    Permission.builder()
                        .name(permissionName)
                        .organization(organization)
                        .actions(new HashSet<>())
                        .build());

    permission.setDescription(description);
    permission.setActions(new HashSet<>(defaultActions));
    return permissionRepository.save(permission);
  }

  private Role seedSuperAdminRole(Organization organization, List<Permission> permissions) {
    Role role =
        roleRepository
            .findByName(SUPER_ADMIN_ROLE_NAME)
            .orElseGet(
                () ->
                    Role.builder()
                        .name(SUPER_ADMIN_ROLE_NAME)
                        .organization(organization)
                        .permissions(new HashSet<>())
                        .build());

    if (role.getOrganization() == null) {
      role.setOrganization(organization);
    }
    if (role.getPermissions() == null) {
      role.setPermissions(new HashSet<>());
    }
    role.getPermissions().addAll(permissions);
    return roleRepository.save(role);
  }

  private void seedSystemAdminUser(Organization organization, Role superAdminRole) {
    User systemAdmin =
        userRepository
            .findByEmail(SYSTEM_ADMIN_EMAIL)
            .orElseGet(
                () ->
                    User.builder()
                        .firstName("System")
                        .lastName("Admin")
                        .email(SYSTEM_ADMIN_EMAIL)
                        .password(passwordEncoder.encode(SYSTEM_ADMIN_PASSWORD))
                        .roles(new HashSet<>())
                        .organizations(new HashSet<>())
                        .build());

    if (systemAdmin.getRoles() == null) {
      systemAdmin.setRoles(new HashSet<>());
    }
    if (systemAdmin.getOrganizations() == null) {
      systemAdmin.setOrganizations(new HashSet<>());
    }

    systemAdmin.getRoles().add(superAdminRole);
    systemAdmin.getOrganizations().add(organization);
    userRepository.save(systemAdmin);
  }

  private record PermissionSeed(String name, String description) {}
}
