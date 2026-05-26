package com.dut.erp;

import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Permission;
import com.dut.erp.entity.Role;
import com.dut.erp.entity.User;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.repository.RoleRepository;
import com.dut.erp.repository.UserRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class SampleDataGenerator implements CommandLineRunner {
  private static final String ADMIN_ROLE_NAME = "ADMIN";
  private static final String NO_PERMISSION_ROLE_NAME = "NO_PERMISSION";

  private static final String ORG_A_NAME = "Organization A";
  private static final String ORG_B_NAME = "Organization B";
  private static final String ORG_A_TAX_CODE = "SEED-TAX-0001";
  private static final String ORG_B_TAX_CODE = "SEED-TAX-0002";

  private static final String ADMIN_EMAIL = "admin@dut.com";
  private static final String ADMIN_PASSWORD = "Admin@123";
  private static final String LIMITED_EMAIL = "testuser@dut.com";
  private static final String LIMITED_PASSWORD = "testuser@123";

  private final OrganizationRepository organizationRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PermissionRepository permissionRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(String... args) {
    Map<String, Organization> organizationsByName = loadOrganizationsByName();
    Organization organizationA =
        getOrCreateOrganization(
            organizationsByName,
            ORG_A_NAME,
            "Seed organization A",
            "Address A",
            "0900000001",
            ORG_A_TAX_CODE);
    Organization organizationB =
        getOrCreateOrganization(
            organizationsByName,
            ORG_B_NAME,
            "Seed organization B",
            "Address B",
            "0900000002",
            ORG_B_TAX_CODE);

    Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());
    Role adminRoleInOrgA = getOrCreateRole(ADMIN_ROLE_NAME, organizationA, allPermissions);
    Role adminRoleInOrgB = getOrCreateRole(ADMIN_ROLE_NAME, organizationB, allPermissions);
    Role noPermissionRoleInOrgA =
        getOrCreateRole(NO_PERMISSION_ROLE_NAME, organizationA, new HashSet<>());
    Role noPermissionRoleInOrgB =
        getOrCreateRole(NO_PERMISSION_ROLE_NAME, organizationB, new HashSet<>());

    User adminUser = getOrCreateUser(ADMIN_EMAIL, ADMIN_PASSWORD, "System", "Admin");
    User limitedUser = getOrCreateUser(LIMITED_EMAIL, LIMITED_PASSWORD, "Test", "User");

    Set<Organization> allOrganizations = Set.of(organizationA, organizationB);

    adminUser.getOrganizations().addAll(allOrganizations);
    adminUser.getRoles().addAll(Set.of(adminRoleInOrgA, adminRoleInOrgB));

    limitedUser.getOrganizations().addAll(allOrganizations);
    limitedUser.getRoles().addAll(Set.of(noPermissionRoleInOrgA, noPermissionRoleInOrgB));

    userRepository.saveAll(List.of(adminUser, limitedUser));
  }

  private Map<String, Organization> loadOrganizationsByName() {
    Map<String, Organization> organizationsByName = new HashMap<>();
    for (Organization organization : organizationRepository.findAll()) {
      organizationsByName.putIfAbsent(organization.getName(), organization);
    }
    return organizationsByName;
  }

  private Organization getOrCreateOrganization(
      Map<String, Organization> organizationsByName,
      String name,
      String description,
      String address,
      String hotline,
      String taxCode) {
    Organization existing = organizationsByName.get(name);
    if (existing != null) {
      return existing;
    }

    Organization created =
        organizationRepository.save(
            Organization.builder()
                .name(name)
                .description(description)
                .address(address)
                .hotline(hotline)
                .taxCode(taxCode)
                .build());
    organizationsByName.put(name, created);
    return created;
  }

  private Role getOrCreateRole(String roleName, Organization organization, Set<Permission> permissions) {
    return roleRepository
        .findByNameAndOrganizationId(roleName, organization.getId())
        .map(
            existingRole -> {
              if (!existingRole.getPermissions().equals(permissions)) {
                existingRole.setPermissions(new HashSet<>(permissions));
                return roleRepository.save(existingRole);
              }
              return existingRole;
            })
        .orElseGet(
            () ->
                roleRepository.save(
                    Role.builder()
                        .name(roleName)
                        .organization(organization)
                        .permissions(new HashSet<>(permissions))
                        .build()));
  }

  private User getOrCreateUser(String email, String rawPassword, String firstName, String lastName) {
    return userRepository
        .findByEmail(email)
        .orElseGet(
            () ->
                userRepository.save(
                    User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(rawPassword))
                        .firstName(firstName)
                        .lastName(lastName)
                        .build()));
  }
}
