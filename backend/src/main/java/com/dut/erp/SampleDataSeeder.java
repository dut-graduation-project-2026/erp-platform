package com.dut.erp;

import com.dut.erp.entity.ErpModule;
import com.dut.erp.entity.InventoryBalance;
import com.dut.erp.entity.InventoryDocument;
import com.dut.erp.entity.InventoryDocumentLine;
import com.dut.erp.entity.Invoice;
import com.dut.erp.entity.Lead;
import com.dut.erp.entity.Order;
import com.dut.erp.entity.OrderItem;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Partner;
import com.dut.erp.entity.PartnerContact;
import com.dut.erp.entity.Permission;
import com.dut.erp.entity.Product;
import com.dut.erp.entity.ProductCategory;
import com.dut.erp.entity.ReplenishmentRequest;
import com.dut.erp.entity.Role;
import com.dut.erp.entity.SaleTeam;
import com.dut.erp.entity.StockValuation;
import com.dut.erp.entity.Tax;
import com.dut.erp.entity.User;
import com.dut.erp.entity.Warehouse;
import com.dut.erp.repository.ErpModuleRepository;
import com.dut.erp.repository.InventoryBalanceRepository;
import com.dut.erp.repository.InventoryDocumentLineRepository;
import com.dut.erp.repository.InventoryDocumentRepository;
import com.dut.erp.repository.InvoiceRepository;
import com.dut.erp.repository.LeadRepository;
import com.dut.erp.repository.OrderRepository;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.PartnerContactRepository;
import com.dut.erp.repository.PartnerRepository;
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.repository.ProductCategoryRepository;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.ReplenishmentRequestRepository;
import com.dut.erp.repository.RoleRepository;
import com.dut.erp.repository.SaleTeamRepository;
import com.dut.erp.repository.StockValuationRepository;
import com.dut.erp.repository.TaxRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.repository.WarehouseRepository;
import com.dut.erp.service.AuthenticationService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SampleDataSeeder implements CommandLineRunner {

  private static final String ADMIN_ROLE_NAME = "ADMIN";
  private static final String ADMIN_EMAIL = "admin@erp.local";
  private static final String ADMIN_PASSWORD = "Admin@123";

  private static final String ORG_NAME = "SmartERP Da Nang Co., Ltd.";
  private static final String ORG_TAX_CODE = "0401234567";

  private final OrganizationRepository organizationRepository;
  private final ErpModuleRepository erpModuleRepository;
  private final PermissionRepository permissionRepository;
  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final PartnerRepository partnerRepository;
  private final ProductRepository productRepository;
  private final OrderRepository orderRepository;
  private final WarehouseRepository warehouseRepository;
  private final AuthenticationService authenticationService;

  private final LeadRepository leadRepository;
  private final TaxRepository taxRepository;
  private final InvoiceRepository invoiceRepository;
  private final SaleTeamRepository saleTeamRepository;
  private final InventoryBalanceRepository inventoryBalanceRepository;
  private final InventoryDocumentRepository inventoryDocumentRepository;
  private final InventoryDocumentLineRepository inventoryDocumentLineRepository;
  private final StockValuationRepository stockValuationRepository;
  private final ReplenishmentRequestRepository replenishmentRequestRepository;
  private final PartnerContactRepository partnerContactRepository;
  private final ProductCategoryRepository productCategoryRepository;
  private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

  @Override
  @Transactional
  public void run(String... args) {
    seedModulesAndPermissions();

    // Trực tiếp kiểm tra xem số lượng đơn hàng có < 100 hay không, nếu ít tức là chưa seed đủ 180 ngày lịch sử
    boolean needsSeeding = orderRepository.count() < 100;
    if (needsSeeding) {
      replenishmentRequestRepository.deleteAll();
      stockValuationRepository.deleteAll();
      inventoryDocumentLineRepository.deleteAll();
      inventoryDocumentRepository.deleteAll();
      inventoryBalanceRepository.deleteAll();
      invoiceRepository.deleteAll();
      orderRepository.deleteAll();
      leadRepository.deleteAll();
      partnerContactRepository.deleteAll();
      partnerRepository.deleteAll();
      productRepository.deleteAll();
      productCategoryRepository.deleteAll();
    }

    Map<String, Organization> organizationsByName = loadOrganizationsByName();
    Map<String, Organization> organizationsByTaxCode = loadOrganizationsByTaxCode();
    Organization organization = getOrCreateOrganization(
        organizationsByName,
        organizationsByTaxCode,
        ORG_NAME,
        "Default seeded organization for initial setup",
        "36 Bach Dang, Hai Chau I Ward, Hai Chau District, Da Nang, Vietnam",
        "19001001",
        ORG_TAX_CODE);

    Set<Permission> allPermissions = new HashSet<>(permissionRepository.findAll());
    Role adminRole = getOrCreateRole(ADMIN_ROLE_NAME, organization, allPermissions);

    User adminUser = getOrCreateUser(ADMIN_EMAIL, ADMIN_PASSWORD, "System", "Admin");
    adminUser.getOrganizations().add(organization);
    adminUser.getRoles().add(adminRole);
    userRepository.save(adminUser);

    // Seed original Partner mienhin123@gmail.com
    Partner originalPartner = partnerRepository.findAllByOrganizationId(organization.getId()).stream()
        .filter(p -> "mienhin123@gmail.com".equals(p.getEmail()))
        .findFirst()
        .orElseGet(
            () -> partnerRepository.save(
                Partner.builder()
                    .name("Otomolondo Partner")
                    .email("mienhin123@gmail.com")
                    .phone("0123456789")
                    .address("Da Nang, Vietnam")
                    .partnerType(com.dut.erp.enums.PartnerType.INDIVIDUAL)
                    .organization(organization)
                    .build()));

    // Seed default Product Category
    ProductCategory defaultCategory = productCategoryRepository.findAll().stream()
        .filter(
            pc -> "General".equals(pc.getName())
                && pc.getOrganization().getId().equals(organization.getId()))
        .findFirst()
        .orElseGet(
            () -> productCategoryRepository.save(
                ProductCategory.builder()
                    .name("General")
                    .description("Default product category")
                    .organization(organization)
                    .build()));

    // Seed original Product
    Product originalProduct = productRepository.findAll().stream()
        .filter(p -> "Seeded Product".equals(p.getName()))
        .findFirst()
        .orElseGet(
            () -> productRepository.save(
                Product.builder()
                    .name("Seeded Product")
                    .sku("PROD-SEED-0001")
                    .purchasePrice(BigDecimal.valueOf(150.00))
                    .salesPrice(BigDecimal.valueOf(150.00))
                    .description("High quality seeded product for testing")
                    .organization(organization)
                    .category(defaultCategory)
                    .build()));

    // Seed original Quotation (Order in DRAFT status)
    boolean quotationExists = orderRepository.existsByOrganizationIdAndOrderNumber(organization.getId(),
        "QUO-SEED-0001");
    if (!quotationExists) {
      Order order = Order.builder()
          .organization(organization)
          .partner(originalPartner)
          .orderNumber("QUO-SEED-0001")
          .status(com.dut.erp.enums.OrderStatus.DRAFT)
          .expirationDate(Instant.now().plus(java.time.Duration.ofDays(7)))
          .totalAmount(BigDecimal.valueOf(300.00))
          .build();

      OrderItem item = OrderItem.builder()
          .organization(organization)
          .order(order)
          .product(originalProduct)
          .quantity(BigDecimal.valueOf(2))
          .unitPrice(BigDecimal.valueOf(150.00))
          .subtotal(BigDecimal.valueOf(300.00))
          .build();

      order.setItems(List.of(item));
      orderRepository.save(order);
    }

    // Seed original Warehouse
    boolean warehouseExists = warehouseRepository.existsByOrganizationIdAndCode(organization.getId(), "WH-MAIN");
    if (!warehouseExists) {
      warehouseRepository.save(
          Warehouse.builder()
              .organization(organization)
              .name("Main Warehouse")
              .code("WH-MAIN")
              .address("123 Industrial Parkway, Suite A")
              .isActive(Boolean.TRUE)
              .build());
    }

    // Load permissions for roles seeding
    Map<String, Permission> permissionsByCode = new HashMap<>();
    for (Permission permission : permissionRepository.findAll()) {
      permissionsByCode.put(permission.getCode(), permission);
    }

    Role salesRole = getOrCreateSalesRole(organization, permissionsByCode);
    Role keeperRole = getOrCreateKeeperRole(organization, permissionsByCode);

    User salesMgr = getOrCreateUser("sales.mgr@erp.local", "Sales@123", "Sales", "Manager");
    salesMgr.getOrganizations().add(organization);
    salesMgr.getRoles().add(salesRole);
    userRepository.save(salesMgr);

    User salesAgent1 = getOrCreateUser("sales.agent1@erp.local", "Sales@123", "Sales", "Agent 1");
    salesAgent1.getOrganizations().add(organization);
    salesAgent1.getRoles().add(salesRole);
    userRepository.save(salesAgent1);

    User salesAgent2 = getOrCreateUser("sales.agent2@erp.local", "Sales@123", "Sales", "Agent 2");
    salesAgent2.getOrganizations().add(organization);
    salesAgent2.getRoles().add(salesRole);
    userRepository.save(salesAgent2);

    User keeperMgr = getOrCreateUser("keeper.mgr@erp.local", "Keeper@123", "Keeper", "Manager");
    keeperMgr.getOrganizations().add(organization);
    keeperMgr.getRoles().add(keeperRole);
    userRepository.save(keeperMgr);

    User keeperStaff1 = getOrCreateUser("keeper.staff1@erp.local", "Keeper@123", "Keeper", "Staff 1");
    keeperStaff1.getOrganizations().add(organization);
    keeperStaff1.getRoles().add(keeperRole);
    userRepository.save(keeperStaff1);

    User keeperStaff2 = getOrCreateUser("keeper.staff2@erp.local", "Keeper@123", "Keeper", "Staff 2");
    keeperStaff2.getOrganizations().add(organization);
    keeperStaff2.getRoles().add(keeperRole);
    userRepository.save(keeperStaff2);

    // Bulk business dataset seeding (if empty or needs re-seeding)
    if (needsSeeding || productRepository.count() <= 1) {
      seedHighVolumeData(
          organization,
          salesRole,
          keeperRole,
          salesMgr,
          List.of(salesAgent1, salesAgent2),
          keeperMgr,
          List.of(keeperStaff1, keeperStaff2));
    }
  }

  private Role getOrCreateSalesRole(Organization org, Map<String, Permission> permissions) {
    Set<Permission> salesPerms = new HashSet<>();
    for (String permCode : List.of(
        "leads:read",
        "leads:select",
        "leads:create",
        "leads:write",
        "leads:delete",
        "partners:read",
        "partners:select",
        "partners:create",
        "partners:write",
        "partners:delete",
        "orders:read",
        "orders:select",
        "orders:create",
        "orders:write",
        "orders:delete",
        "sale_teams:read",
        "sale_teams:select",
        "sale_teams:create",
        "sale_teams:write",
        "sale_teams:delete",
        "products:read",
        "products:select",
        "products:create",
        "products:write",
        "products:delete",
        "product_categories:read",
        "product_categories:select",
        "taxes:read",
        "taxes:select",
        "invoices:read",
        "invoices:select",
        "invoices:create",
        "invoices:write",
        "invoices:delete",
        "users:read")) {
      Permission p = permissions.get(permCode);
      if (p != null)
        salesPerms.add(p);
    }
    return getOrCreateRole("SALES", org, salesPerms);
  }

  private Role getOrCreateKeeperRole(Organization org, Map<String, Permission> permissions) {
    Set<Permission> whPerms = new HashSet<>();
    for (String permCode : List.of(
        "warehouses:read",
        "warehouses:select",
        "warehouses:create",
        "warehouses:write",
        "warehouses:delete",
        "inventory-documents:read",
        "inventory-documents:select",
        "inventory-documents:create",
        "inventory-documents:write",
        "inventory-documents:delete",
        "inventory-transactions:read",
        "inventory-transactions:select",
        "inventory-transactions:create",
        "inventory-transactions:write",
        "inventory-transactions:delete",
        "replenishment-requests:read",
        "replenishment-requests:select",
        "replenishment-requests:create",
        "replenishment-requests:write",
        "replenishment-requests:delete",
        "stock-valuations:read",
        "stock-valuations:select",
        "stock-valuations:create",
        "stock-valuations:write",
        "stock-valuations:delete",
        "products:read",
        "products:select",
        "products:create",
        "products:write",
        "products:delete",
        "orders:read",
        "orders:select",
        "users:read")) {
      Permission p = permissions.get(permCode);
      if (p != null)
        whPerms.add(p);
    }
    return getOrCreateRole("WAREHOUSE_KEEPER", org, whPerms);
  }

  private void seedHighVolumeData(
      Organization org,
      Role salesRole,
      Role keeperRole,
      User salesMgr,
      List<User> salesAgents,
      User keeperMgr,
      List<User> keeperStaff) {

    // 1. Taxes
    List<Tax> taxes = seedTaxes(org);
    Tax vat10 = taxes.stream().filter(t -> "VAT 10%".equals(t.getName())).findFirst().orElse(taxes.get(0));
    Tax vat5 = taxes.stream().filter(t -> "VAT 5%".equals(t.getName())).findFirst().orElse(taxes.get(0));

    // 2. Products
    List<Product> products = seedProducts(org);

    // 3. Partners & Contacts
    List<Partner> partners = seedPartners(org);

    // 4. Sales Teams
    List<SaleTeam> saleTeams = seedSaleTeams(org, salesMgr, salesAgents);

    // 5. Leads
    List<Lead> leads = seedLeads(org, partners, salesAgents, saleTeams);

    // 6. Warehouses
    List<Warehouse> warehouses = seedWarehouses(org, keeperMgr, keeperStaff);
    Warehouse centralWh = warehouses.stream()
        .filter(w -> "WH-DAD-CENTRAL".equals(w.getCode()))
        .findFirst()
        .orElse(warehouses.get(0));

    // 7. Inbound Inventory (Stock Receipts)
    seedInboundInventory(org, warehouses, products);

    // 8. Orders, Issues, Stock Valuations, Invoices
    seedOrdersAndInvoices(org, partners, products, leads, salesAgents, centralWh, vat10, vat5);

    // 8.5. Seed 180 days of historical sales for AI Forecasting & Inventory calculations
    seedHistoricalSales(org, partners, products, vat10, vat5, centralWh);

    // 9. Replenishment Requests
    seedReplenishmentRequests(warehouses);
  }

  private List<Tax> seedTaxes(Organization org) {
    List<Tax> taxes = new ArrayList<>();
    String[][] taxData = {
        { "VAT 10%", "10.00", "Value Added Tax 10%" },
        { "VAT 8%", "8.00", "Value Added Tax 8%" },
        { "VAT 5%", "5.00", "Value Added Tax 5%" },
        { "VAT 0%", "0.00", "Value Added Tax 0%" }
    };
    for (String[] data : taxData) {
      String name = data[0];
      BigDecimal amount = new BigDecimal(data[1]);
      String desc = data[2];

      Tax tax = taxRepository.findAll().stream()
          .filter(
              t -> t.getOrganization().getId().equals(org.getId()) && name.equals(t.getName()))
          .findFirst()
          .orElseGet(
              () -> taxRepository.save(
                  Tax.builder()
                      .organization(org)
                      .name(name)
                      .computation(com.dut.erp.enums.TaxComputation.PERCENTAGE)
                      .amount(amount)
                      .description(desc)
                      .isArchived(false)
                      .build()));
      taxes.add(tax);
    }
    return taxes;
  }

  private List<Product> seedProducts(Organization org) {
    List<Product> products = new ArrayList<>();
    Object[][] productData = {
        {
            "XPS Developer Laptop",
            "2200.00",
            "Powerful workstation for developers",
            com.dut.erp.enums.CogsMethod.FIFO,
            "XPS-DEV-LAPTOP"
        },
        {
            "MacBook Pro M3 Max", "3500.00", "Premium Apple notebook", com.dut.erp.enums.CogsMethod.FIFO, "MBP-M3-MAX"
        },
        {
            "ThinkPad X1 Carbon",
            "1800.00",
            "Business ultra-portable laptop",
            com.dut.erp.enums.CogsMethod.FIFO,
            "TP-X1-CARBON"
        },
        {
            "4K Curved Monitor 34\"",
            "650.00",
            "Ultrawide high resolution monitor",
            com.dut.erp.enums.CogsMethod.AVERAGE,
            "MON-4K-34"
        },
        {
            "Ergonomic Office Chair Premium",
            "450.00",
            "Ergonomic chair with mesh backing",
            com.dut.erp.enums.CogsMethod.AVERAGE,
            "CHR-ERG-PREM"
        },
        {
            "Mechanical Keyboard RGB",
            "180.00",
            "Premium mechanical gaming keyboard",
            com.dut.erp.enums.CogsMethod.LIFO,
            "KBD-MECH-RGB"
        },
        {
            "Precision Wireless Mouse",
            "90.00",
            "Ergonomic productivity mouse",
            com.dut.erp.enums.CogsMethod.AVERAGE,
            "MSE-PRC-WL"
        },
        {
            "USB-C Dual Docking Station",
            "200.00",
            "Docking station with dual monitor output",
            com.dut.erp.enums.CogsMethod.FIFO,
            "DK-USBC-DUAL"
        },
        {
            "Active Noise Cancelling Headset",
            "250.00",
            "Wireless headphones with ANC",
            com.dut.erp.enums.CogsMethod.FIFO,
            "HD-ANC-WL"
        },
        {
            "HD Web Camera 1080p",
            "120.00",
            "High definition camera for meetings",
            com.dut.erp.enums.CogsMethod.AVERAGE,
            "CAM-HD-1080P"
        },
        {
            "Smart LED Desk Lamp",
            "80.00",
            "Dimmable desk lamp with wireless charger",
            com.dut.erp.enums.CogsMethod.LIFO,
            "LMP-LED-SMART"
        },
        {
            "External SSD 2TB Rugged",
            "160.00",
            "High speed water-resistant drive",
            com.dut.erp.enums.CogsMethod.FIFO,
            "SSD-2TB-RUG"
        },
        {
            "Smart Stand Desk Frame",
            "400.00",
            "Dual-motor motorized standing desk frame",
            com.dut.erp.enums.CogsMethod.AVERAGE,
            "DSK-FRM-SMART"
        },
        {
            "Bamboo Standing Desk Top",
            "150.00",
            "Sustainable solid bamboo desk top",
            com.dut.erp.enums.CogsMethod.AVERAGE,
            "DSK-TOP-BAMBOO"
        },
        {
            "Multi-device Bluetooth Trackpad",
            "110.00",
            "Wireless trackpad with gesture support",
            com.dut.erp.enums.CogsMethod.LIFO,
            "TRK-BT-MULTI"
        }
    };

    Map<String, ProductCategory> categoriesByName = new HashMap<>();
    for (String catName : List.of("Electronics", "Office Supplies", "Hardware", "Apparel", "General")) {
      ProductCategory cat = productCategoryRepository.findAll().stream()
          .filter(
              pc -> catName.equals(pc.getName())
                  && pc.getOrganization().getId().equals(org.getId()))
          .findFirst()
          .orElseGet(
              () -> productCategoryRepository.save(
                  ProductCategory.builder()
                      .name(catName)
                      .description(catName + " category")
                      .organization(org)
                      .build()));
      categoriesByName.put(catName, cat);
    }

    for (Object[] data : productData) {
      String name = (String) data[0];
      BigDecimal price = new BigDecimal((String) data[1]);
      String desc = (String) data[2];
      com.dut.erp.enums.CogsMethod method = (com.dut.erp.enums.CogsMethod) data[3];
      String sku = (String) data[4];

      String categoryName = "General";
      if (name.contains("Laptop")
          || name.contains("MacBook")
          || name.contains("ThinkPad")
          || name.contains("Keyboard")
          || name.contains("Mouse")
          || name.contains("Docking")
          || name.contains("Headset")
          || name.contains("Camera")
          || name.contains("Trackpad")) {
        categoryName = "Electronics";
      } else if (name.contains("Chair")
          || name.contains("Lamp")
          || name.contains("Frame")
          || name.contains("Top")) {
        categoryName = "Office Supplies";
      } else if (name.contains("Monitor")) {
        categoryName = "Hardware";
      }
      ProductCategory category = categoriesByName.get(categoryName);

      Product product = productRepository.findAllByOrganizationId(org.getId()).stream()
          .filter(p -> name.equals(p.getName()))
          .findFirst()
          .orElseGet(
              () -> productRepository.save(
                  Product.builder()
                      .name(name)
                      .sku(sku)
                      .purchasePrice(price)
                      .salesPrice(price.multiply(new BigDecimal("1.25")).setScale(2, java.math.RoundingMode.HALF_UP))
                      .description(desc)
                      .cogsMethod(method)
                      .isArchived(false)
                      .organization(org)
                      .category(category)
                      .build()));
      products.add(product);
    }
    return products;
  }

  private List<Partner> seedPartners(Organization org) {
    List<Partner> partners = new ArrayList<>();
    Object[][] partnerData = {
        {
            "Da Nang High-Tech Software Park",
            "info@dntechpark.example.com",
            "0901234567",
            "2 Quang Trung Street, Hai Chau I Ward, Hai Chau District, Da Nang, Vietnam",
            "TAX-DN-TECH",
            com.dut.erp.enums.PartnerType.COMPANY,
            "Glenn",
            "glenn@dntechpark.example.com",
            "0901234568",
            "Purchasing Manager"
        },
        {
            "Song Han Logistics JSC",
            "contact@songhanlog.example.com",
            "0907654321",
            "15 Bach Dang Street, Thach Thang Ward, Hai Chau District, Da Nang, Vietnam",
            "TAX-SONGHAN",
            com.dut.erp.enums.PartnerType.COMPANY,
            "Nemo",
            "nemo@songhanlog.example.com",
            "0907654322",
            "Operations Lead"
        },
        {
            "VinaTech Solutions",
            "procurement@vinatech.example.com",
            "0911223344",
            "789 Dien Bien Phu Street, Thanh Khe District, Da Nang, Vietnam",
            "TAX-VINATECH",
            com.dut.erp.enums.PartnerType.COMPANY,
            "Miles Dyson",
            "mdyson@vinatech.example.com",
            "0911223345",
            "Director of R&D"
        },
        {
            "Da Nang Power Joint Stock Company",
            "info@dpc.example.com",
            "0922334455",
            "321 Tran Hung Dao Street, Son Tra District, Da Nang, Vietnam",
            "TAX-DPC",
            com.dut.erp.enums.PartnerType.COMPANY,
            "Sarah",
            "sarah@dpc.example.com",
            "0922334456",
            "General Procurement"
        },
        {
            "Han River Retail Group",
            "sales@hanretail.example.com",
            "0933445566",
            "159 Le Loi Street, Hai Chau District, Da Nang, Vietnam",
            "TAX-HANRETAIL",
            com.dut.erp.enums.PartnerType.COMPANY,
            "Eric",
            "eric@hanretail.example.com",
            "0933445567",
            "Store Manager"
        },
        {
            "Lien Chieu Port Corporation",
            "purchasing@lcport.example.com",
            "0944556677",
            "987 Nguyen Luong Bang Street, Lien Chieu District, Da Nang, Vietnam",
            "TAX-LCPORT",
            com.dut.erp.enums.PartnerType.COMPANY,
            "Sam",
            "sam@lcport.example.com",
            "0944556678",
            "Procurement Officer"
        },
        {
            "Da Nang Medical Equipment JSC",
            "office@dnmed.example.com",
            "0955667788",
            "147 Quang Trung Street, Hai Chau District, Da Nang, Vietnam",
            "TAX-DNMED",
            com.dut.erp.enums.PartnerType.COMPANY,
            "Dr. Alice",
            "alice@dnmed.example.com",
            "0955667789",
            "Lab Director"
        },
        {
            "Green Space Landscaping",
            "billing@greenspace.example.com",
            "0966778899",
            "369 Nguyen Huu Tho Street, Cam Le District, Da Nang, Vietnam",
            "TAX-GREENSPACE",
            com.dut.erp.enums.PartnerType.COMPANY,
            "Bob",
            "bob@greenspace.example.com",
            "0966778900",
            "Finance Lead"
        },
        {
            "Cam Le Electronics Assembly",
            "orders@camleelec.example.com",
            "0977889900",
            "258 Cam Le Industrial Zone, Cam Le District, Da Nang, Vietnam",
            "TAX-CAMLE",
            com.dut.erp.enums.PartnerType.COMPANY,
            "Isaac",
            "isaac@camleelec.example.com",
            "0977889901",
            "Inventory Mgr"
        },
        {
            "Da Nang Steel Manufacturing",
            "supply@dnsteel.example.com",
            "0988990011",
            "951 Hoa Khanh Industrial Park, Lien Chieu District, Da Nang, Vietnam",
            "TAX-DNSTEEL",
            com.dut.erp.enums.PartnerType.COMPANY,
            "Jane",
            "jane@dnsteel.example.com",
            "0988990012",
            "Supply Chain Director"
        },
        {
            "Nam Nguyen",
            "nam.nguyen@gmail.com",
            "0900111222",
            "111 Elm Street, Cam Le District, Da Nang, Vietnam",
            null,
            com.dut.erp.enums.PartnerType.INDIVIDUAL,
            null,
            null,
            null,
            null
        },
        {
            "Hoa Tran",
            "hoa.tran@gmail.com",
            "0900222333",
            "222 Oak Street, Son Tra District, Da Nang, Vietnam",
            null,
            com.dut.erp.enums.PartnerType.INDIVIDUAL,
            null,
            null,
            null,
            null
        },
        {
            "Minh Pham",
            "minh.pham@gmail.com",
            "0900333444",
            "333 Ngo Quyen Street, Son Tra District, Da Nang, Vietnam",
            null,
            com.dut.erp.enums.PartnerType.INDIVIDUAL,
            "Alfred",
            "alfred@waynecorp.com",
            "0900333445",
            "Butler/Advisor"
        },
        {
            "Hoang Le",
            "hoang.le@gmail.com",
            "0900444555",
            "444 Dien Bien Phu Street, Thanh Khe District, Da Nang, Vietnam",
            null,
            com.dut.erp.enums.PartnerType.INDIVIDUAL,
            "Pepper Potts",
            "pepper@starkindustries.com",
            "0900444556",
            "CEO/Admin"
        },
        {
            "Duy Tran",
            "duy.tran@gmail.com",
            "0900555666",
            "555 Ton Duc Thang Street, Lien Chieu District, Da Nang, Vietnam",
            null,
            com.dut.erp.enums.PartnerType.INDIVIDUAL,
            null,
            null,
            null,
            null
        }
    };

    for (Object[] data : partnerData) {
      String name = (String) data[0];
      String email = (String) data[1];
      String phone = (String) data[2];
      String address = (String) data[3];
      String taxCode = (String) data[4];
      com.dut.erp.enums.PartnerType type = (com.dut.erp.enums.PartnerType) data[5];

      Partner partner = partnerRepository.findAllByOrganizationId(org.getId()).stream()
          .filter(p -> email.equals(p.getEmail()))
          .findFirst()
          .orElseGet(
              () -> partnerRepository.save(
                  Partner.builder()
                      .name(name)
                      .email(email)
                      .phone(phone)
                      .address(address)
                      .taxCode(taxCode)
                      .partnerType(type)
                      .organization(org)
                      .build()));

      String contactName = (String) data[6];
      if (contactName != null) {
        String contactEmail = (String) data[7];
        String contactPhone = (String) data[8];
        String contactPosition = (String) data[9];

        boolean contactExists = partnerContactRepository.findAll().stream()
            .anyMatch(
                c -> c.getPartner().getId().equals(partner.getId())
                    && contactName.equals(c.getName()));
        if (!contactExists) {
          partnerContactRepository.save(
              PartnerContact.builder()
                  .name(contactName)
                  .email(contactEmail)
                  .phone(contactPhone)
                  .jobPosition(contactPosition)
                  .partner(partner)
                  .build());
        }
      }
      partners.add(partner);
    }
    return partners;
  }

  private List<SaleTeam> seedSaleTeams(Organization org, User salesMgr, List<User> salesAgents) {
    List<SaleTeam> teams = new ArrayList<>();
    String[][] teamData = {
        { "Enterprise Sales Team", "sales.mgr@erp.local" },
        { "Retail Sales Team", "sales.agent1@erp.local" }
    };

    for (String[] data : teamData) {
      String name = data[0];
      String leaderEmail = data[1];
      User leader = leaderEmail.equals(salesMgr.getEmail()) ? salesMgr : salesAgents.get(0);

      boolean exists = saleTeamRepository.existsByOrganizationIdAndName(org.getId(), name);
      SaleTeam team;
      if (!exists) {
        team = saleTeamRepository.save(
            SaleTeam.builder()
                .name(name)
                .organization(org)
                .leader(leader)
                .members(new HashSet<>(salesAgents))
                .isArchived(false)
                .build());
      } else {
        team = saleTeamRepository.findAll().stream()
            .filter(
                t -> t.getOrganization().getId().equals(org.getId()) && name.equals(t.getName()))
            .findFirst()
            .orElseThrow();
      }
      teams.add(team);
    }
    return teams;
  }

  private List<Lead> seedLeads(
      Organization org, List<Partner> partners, List<User> salesAgents, List<SaleTeam> teams) {
    List<Lead> leads = new ArrayList<>();
    Object[][] leadData = {
        {
            "Song Han Logistics Cloud Migration",
            "TAX-SONGHAN",
            "contact@songhanlog.example.com",
            "0907654321",
            "15 Bach Dang Street, Thach Thang Ward, Hai Chau District, Da Nang, Vietnam",
            "Moving legacy systems to AWS",
            "45000.00",
            com.dut.erp.enums.LeadStage.NEW,
            "10.00",
            0,
            0,
            1
        },
        {
            "Lien Chieu Port Modernization",
            "TAX-LCPORT",
            "purchasing@lcport.example.com",
            "0944556677",
            "987 Nguyen Luong Bang Street, Lien Chieu District, Da Nang, Vietnam",
            "Upgrading warehouse logistics",
            "120000.00",
            com.dut.erp.enums.LeadStage.NEW,
            "15.00",
            1,
            0,
            5
        },
        {
            "Green Space Corporate Security",
            "TAX-GREENSPACE",
            "billing@greenspace.example.com",
            "0966778899",
            "369 Nguyen Huu Tho Street, Cam Le District, Da Nang, Vietnam",
            "Installing corporate firewalls",
            "35000.00",
            com.dut.erp.enums.LeadStage.QUALIFIED,
            "30.00",
            0,
            1,
            7
        },
        {
            "Da Nang Medical Tech Refresh",
            "TAX-DNMED",
            "office@dnmed.example.com",
            "0955667788",
            "147 Quang Trung Street, Hai Chau District, Da Nang, Vietnam",
            "Replacing lab computers",
            "25000.00",
            com.dut.erp.enums.LeadStage.QUALIFIED,
            "40.00",
            1,
            1,
            6
        },
        {
            "Da Nang Tech Park Workstation Proposal",
            "TAX-DN-TECH",
            "info@dntechpark.example.com",
            "0901234567",
            "2 Quang Trung Street, Hai Chau I Ward, Hai Chau District, Da Nang, Vietnam",
            "Custom developer workstations",
            "50000.00",
            com.dut.erp.enums.LeadStage.PROPOSAL,
            "65.00",
            0,
            0,
            0
        },
        {
            "Da Nang Power Grid Infrastructure",
            "TAX-DPC",
            "info@dpc.example.com",
            "0922334455",
            "321 Tran Hung Dao Street, Son Tra District, Da Nang, Vietnam",
            "Grid management servers",
            "95000.00",
            com.dut.erp.enums.LeadStage.PROPOSAL,
            "70.00",
            1,
            0,
            3
        },
        {
            "VinaTech Core Server Upgrade",
            "TAX-VINATECH",
            "procurement@vinatech.example.com",
            "0911223344",
            "789 Dien Bien Phu Street, Thanh Khe District, Da Nang, Vietnam",
            "Upgrading core AI servers",
            "150000.00",
            com.dut.erp.enums.LeadStage.LOST,
            "0.00",
            0,
            0,
            2
        },
        {
            "Da Nang Steel Logistics Automation",
            "TAX-DNSTEEL",
            "supply@dnsteel.example.com",
            "0988990011",
            "951 Hoa Khanh Industrial Park, Lien Chieu District, Da Nang, Vietnam",
            "Factory logistics automation",
            "80000.00",
            com.dut.erp.enums.LeadStage.LOST,
            "0.00",
            1,
            1,
            9
        },
        {
            "Minh Pham IT Procurement",
            null,
            "minh.pham@gmail.com",
            "0900333444",
            "333 Ngo Quyen Street, Son Tra District, Da Nang, Vietnam",
            "Confidential hardware supply",
            "200000.00",
            com.dut.erp.enums.LeadStage.WON,
            "100.00",
            0,
            0,
            12
        },
        {
            "Hoang Le Lab Equipment",
            null,
            "hoang.le@gmail.com",
            "0900444555",
            "444 Dien Bien Phu Street, Thanh Khe District, Da Nang, Vietnam",
            "Clean room tech refresh",
            "180000.00",
            com.dut.erp.enums.LeadStage.WON,
            "100.00",
            1,
            0,
            13
        }
    };

    for (Object[] data : leadData) {
      String name = (String) data[0];
      String taxCode = (String) data[1];
      String email = (String) data[2];
      String phone = (String) data[3];
      String address = (String) data[4];
      String notes = (String) data[5];
      BigDecimal revenue = new BigDecimal((String) data[6]);
      com.dut.erp.enums.LeadStage stage = (com.dut.erp.enums.LeadStage) data[7];
      BigDecimal prob = new BigDecimal((String) data[8]);
      User agent = salesAgents.get((int) data[9]);
      SaleTeam team = teams.get((int) data[10]);
      Partner partner = partners.get((int) data[11]);

      Lead lead = leadRepository.findAll().stream()
          .filter(
              l -> l.getOrganization().getId().equals(org.getId()) && name.equals(l.getName()))
          .findFirst()
          .orElseGet(
              () -> leadRepository.save(
                  Lead.builder()
                      .organization(org)
                      .name(name)
                      .taxCode(taxCode)
                      .email(email)
                      .phone(phone)
                      .address(address)
                      .notes(notes)
                      .expectedRevenue(revenue)
                      .stage(stage)
                      .probability(prob)
                      .salePerson(agent)
                      .saleTeam(team)
                      .partner(partner)
                      .build()));
      leads.add(lead);
    }
    return leads;
  }

  private List<Warehouse> seedWarehouses(Organization org, User keeperMgr, List<User> keeperStaff) {
    List<Warehouse> warehouses = new ArrayList<>();
    Object[][] whData = {
        {
            "Central Da Nang Warehouse",
            "WH-DAD-CENTRAL",
            "254 Nguyen Van Linh Street, Thac Gian Ward, Thanh Khe District, Da Nang, Vietnam",
            "Main distribution hub in central Da Nang"
        },
        {
            "Lien Chieu Port Warehouse",
            "WH-DAD-LIENCHIEU",
            "45 Ton Duc Thang Street, Hoa Khanh Nam Ward, Lien Chieu District, Da Nang, Vietnam",
            "Northern regional warehouse near Lien Chieu port"
        },
        {
            "Son Tra Logistics Warehouse",
            "WH-DAD-SONTRA",
            "12 Hoang Sa Street, Tho Quang Ward, Son Tra District, Da Nang, Vietnam",
            "Eastern warehouse near Son Tra port"
        }
    };

    for (Object[] data : whData) {
      String name = (String) data[0];
      String code = (String) data[1];
      String address = (String) data[2];
      String desc = (String) data[3];

      Warehouse wh = warehouseRepository.findAllByOrganizationId(org.getId()).stream()
          .filter(w -> code.equals(w.getCode()))
          .findFirst()
          .orElseGet(
              () -> warehouseRepository.save(
                  Warehouse.builder()
                      .organization(org)
                      .name(name)
                      .code(code)
                      .address(address)
                      .description(desc)
                      .isActive(true)
                      .manager(keeperMgr)
                      .staff(new ArrayList<>(keeperStaff))
                      .build()));
      warehouses.add(wh);
    }
    return warehouses;
  }

  private void seedInboundInventory(
      Organization org, List<Warehouse> warehouses, List<Product> products) {
    for (Warehouse wh : warehouses) {
      String docName = wh.getCode() + "/IN/2026/0001";
      boolean docExists = inventoryDocumentRepository.existsByName(docName);
      if (docExists) {
        continue;
      }

      InventoryDocument doc = inventoryDocumentRepository.save(
          InventoryDocument.builder()
              .warehouse(wh)
              .name(docName)
              .documentType(com.dut.erp.enums.DocumentType.RECEIPT)
              .referenceType(com.dut.erp.enums.ReferenceType.MANUAL)
              .documentStatus(com.dut.erp.enums.DocumentStatus.COMPLETED)
              .notes("Initial warehouse stock seeding")
              .scheduledDate(Instant.now().minus(java.time.Duration.ofDays(10)))
              .dateDone(Instant.now().minus(java.time.Duration.ofDays(10)))
              .build());

      List<InventoryDocumentLine> lines = new ArrayList<>();
      for (Product prod : products) {
        BigDecimal qty = BigDecimal.valueOf(
            wh.getCode().contains("CENTRAL") ? 100 : (wh.getCode().contains("LIENCHIEU") ? 80 : 50));

        // Create low stock items for ROP/Warning test scenarios
        if ("MacBook Pro M3 Max".equals(prod.getName())
            || "Mechanical Keyboard RGB".equals(prod.getName())) {
          qty = BigDecimal.valueOf(wh.getCode().contains("CENTRAL") ? 2 : 0);
        } else if ("ThinkPad X1 Carbon".equals(prod.getName())
            || "Active Noise Cancelling Headset".equals(prod.getName())) {
          qty = BigDecimal.valueOf(wh.getCode().contains("CENTRAL") ? 4 : 1);
        }

        BigDecimal unitCost = prod.getPurchasePrice()
            .multiply(BigDecimal.valueOf(0.70))
            .setScale(4, java.math.RoundingMode.HALF_UP);
        BigDecimal valuation = qty.multiply(unitCost);

        InventoryDocumentLine line = inventoryDocumentLineRepository.save(
            InventoryDocumentLine.builder()
                .inventoryDocument(doc)
                .product(prod)
                .quantity(qty)
                .unitCost(unitCost)
                .valuation(valuation)
                .remainingQuantity(qty)
                .build());
        lines.add(line);

        inventoryBalanceRepository.save(
            InventoryBalance.builder().warehouse(wh).product(prod).quantity(qty).build());

        stockValuationRepository.save(
            StockValuation.builder()
                .inventoryDocumentLine(line)
                .product(prod)
                .quantity(qty)
                .unitCost(unitCost)
                .totalValuation(valuation)
                .method(prod.getCogsMethod())
                .build());
      }
      doc.getLines().clear();
      doc.getLines().addAll(lines);
      inventoryDocumentRepository.save(doc);
    }
  }

  private void seedOrdersAndInvoices(
      Organization org,
      List<Partner> partners,
      List<Product> products,
      List<Lead> leads,
      List<User> salesAgents,
      Warehouse centralWh,
      Tax vat10,
      Tax vat5) {

    for (int i = 1; i <= 10; i++) {
      String orderNumber = "QUO-2026-" + String.format("%04d", i);
      if (orderRepository.existsByOrganizationIdAndOrderNumber(org.getId(), orderNumber)) {
        continue;
      }
      createSampleOrder(
          org,
          orderNumber,
          com.dut.erp.enums.OrderStatus.DRAFT,
          partners.get(i % partners.size()),
          products,
          vat10,
          vat5,
          leads.get(i % leads.size()));
    }

    for (int i = 11; i <= 15; i++) {
      String orderNumber = "QUO-2026-" + String.format("%04d", i);
      if (orderRepository.existsByOrganizationIdAndOrderNumber(org.getId(), orderNumber)) {
        continue;
      }
      createSampleOrder(
          org,
          orderNumber,
          com.dut.erp.enums.OrderStatus.SENT,
          partners.get(i % partners.size()),
          products,
          vat10,
          vat5,
          leads.get(i % leads.size()));
    }

    for (int i = 1; i <= 5; i++) {
      String orderNumber = "SO-2026-" + String.format("%04d", i);
      if (orderRepository.existsByOrganizationIdAndOrderNumber(org.getId(), orderNumber)) {
        continue;
      }
      createSampleOrder(
          org,
          orderNumber,
          com.dut.erp.enums.OrderStatus.CONFIRMED,
          partners.get(i % partners.size()),
          products,
          vat10,
          vat5,
          leads.get(i % leads.size()));
    }

    for (int i = 16; i <= 20; i++) {
      String orderNumber = "SO-2026-" + String.format("%04d", i);
      if (orderRepository.existsByOrganizationIdAndOrderNumber(org.getId(), orderNumber)) {
        continue;
      }
      Order order = createSampleOrder(
          org,
          orderNumber,
          com.dut.erp.enums.OrderStatus.CANCELLED,
          partners.get(i % partners.size()),
          products,
          vat10,
          vat5,
          leads.get(i % leads.size()));
      createSampleInvoice(org, order, "INV-2026-C" + i, com.dut.erp.enums.InvoiceStatus.CANCELLED);
    }

    for (int i = 21; i <= 25; i++) {
      String orderNumber = "SO-2026-" + String.format("%04d", i);
      if (orderRepository.existsByOrganizationIdAndOrderNumber(org.getId(), orderNumber)) {
        continue;
      }
      createSampleOrder(
          org,
          orderNumber,
          com.dut.erp.enums.OrderStatus.WAITING_FOR_STOCK,
          partners.get(i % partners.size()),
          products,
          vat10,
          vat5,
          leads.get(i % leads.size()));
    }

    for (int i = 6; i <= 15; i++) {
      String orderNumber = "SO-2026-" + String.format("%04d", i);
      if (orderRepository.existsByOrganizationIdAndOrderNumber(org.getId(), orderNumber)) {
        continue;
      }

      Order order = createSampleOrder(
          org,
          orderNumber,
          com.dut.erp.enums.OrderStatus.COMPLETED,
          partners.get(i % partners.size()),
          products,
          vat10,
          vat5,
          leads.get(i % leads.size()));

      String outDocName = centralWh.getCode() + "/OUT/2026/" + String.format("%04d", i);
      if (!inventoryDocumentRepository.existsByName(outDocName)) {
        InventoryDocument outDoc = inventoryDocumentRepository.save(
            InventoryDocument.builder()
                .warehouse(centralWh)
                .name(outDocName)
                .documentType(com.dut.erp.enums.DocumentType.ISSUE)
                .referenceType(com.dut.erp.enums.ReferenceType.SALES_ORDER)
                .referenceId(order.getId())
                .documentStatus(com.dut.erp.enums.DocumentStatus.COMPLETED)
                .notes("Stock issue for order " + orderNumber)
                .scheduledDate(Instant.now().minus(java.time.Duration.ofDays(2)))
                .dateDone(Instant.now().minus(java.time.Duration.ofDays(2)))
                .build());

        List<InventoryDocumentLine> outLines = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
          Product prod = item.getProduct();
          BigDecimal qty = item.getQuantity();

          Optional<InventoryDocumentLine> receiptLineOpt = inventoryDocumentLineRepository.findAll().stream()
              .filter(
                  l -> l.getInventoryDocument().getName().equals(centralWh.getCode() + "/IN/2026/0001")
                      && l.getProduct().getId().equals(prod.getId()))
              .findFirst();

          BigDecimal unitCost = prod.getPurchasePrice().multiply(BigDecimal.valueOf(0.70));
          if (receiptLineOpt.isPresent()) {
            InventoryDocumentLine receiptLine = receiptLineOpt.get();
            unitCost = receiptLine.getUnitCost();

            BigDecimal newRemaining = receiptLine.getRemainingQuantity().subtract(qty);
            if (newRemaining.compareTo(BigDecimal.ZERO) < 0) {
              newRemaining = BigDecimal.ZERO;
            }
            receiptLine.setRemainingQuantity(newRemaining);
            inventoryDocumentLineRepository.save(receiptLine);
          }

          BigDecimal valuation = qty.multiply(unitCost);

          InventoryDocumentLine outLine = inventoryDocumentLineRepository.save(
              InventoryDocumentLine.builder()
                  .inventoryDocument(outDoc)
                  .product(prod)
                  .quantity(qty)
                  .unitCost(unitCost)
                  .valuation(valuation)
                  .remainingQuantity(BigDecimal.ZERO)
                  .build());
          outLines.add(outLine);

          Optional<InventoryBalance> balanceOpt = inventoryBalanceRepository.findByWarehouseIdAndProductId(
              centralWh.getId(), prod.getId());
          if (balanceOpt.isPresent()) {
            InventoryBalance balance = balanceOpt.get();
            BigDecimal newQty = balance.getQuantity().subtract(qty);
            if (newQty.compareTo(BigDecimal.ZERO) < 0) {
              newQty = BigDecimal.ZERO;
            }
            balance.setQuantity(newQty);
            inventoryBalanceRepository.save(balance);
          }

          stockValuationRepository.save(
              StockValuation.builder()
                  .inventoryDocumentLine(outLine)
                  .product(prod)
                  .quantity(qty)
                  .unitCost(unitCost)
                  .totalValuation(valuation)
                  .method(prod.getCogsMethod())
                  .build());
        }

        outDoc.getLines().clear();
        outDoc.getLines().addAll(outLines);
        inventoryDocumentRepository.save(outDoc);
      }

      createSampleInvoice(
          org, order, "INV-2026-" + String.format("%04d", i), com.dut.erp.enums.InvoiceStatus.PAID);
    }
  }

  private Order createSampleOrder(
      Organization org,
      String orderNumber,
      com.dut.erp.enums.OrderStatus status,
      Partner partner,
      List<Product> products,
      Tax vat10,
      Tax vat5,
      Lead lead) {

    Order order = Order.builder()
        .organization(org)
        .partner(partner)
        .orderNumber(orderNumber)
        .status(status)
        .lead(lead)
        .deliveryDate(Instant.now().plus(java.time.Duration.ofDays(5)))
        .expirationDate(Instant.now().plus(java.time.Duration.ofDays(15)))
        .totalAmount(BigDecimal.ZERO)
        .build();

    order = orderRepository.save(order);

    int itemCount = 2 + (Math.abs(orderNumber.hashCode()) % 3);
    List<OrderItem> items = new ArrayList<>();
    BigDecimal totalOrderAmt = BigDecimal.ZERO;

    for (int i = 0; i < itemCount; i++) {
      Product prod = products.get(Math.abs(orderNumber.hashCode() + i) % products.size());
      Tax tax = (i % 2 == 0) ? vat10 : vat5;
      BigDecimal qty = BigDecimal.valueOf(1 + Math.abs(orderNumber.hashCode() * (i + 1)) % 5);
      BigDecimal unitPrice = prod.getSalesPrice();
      BigDecimal subtotal = qty.multiply(unitPrice);

      OrderItem item = OrderItem.builder()
          .organization(org)
          .order(order)
          .product(prod)
          .tax(tax)
          .quantity(qty)
          .unitPrice(unitPrice)
          .subtotal(subtotal)
          .build();

      items.add(item);
      totalOrderAmt = totalOrderAmt.add(subtotal);
    }

    order.setItems(items);
    order.setTotalAmount(totalOrderAmt);
    return orderRepository.save(order);
  }

  private Invoice createSampleInvoice(
      Organization org, Order order, String invoiceNumber, com.dut.erp.enums.InvoiceStatus status) {

    BigDecimal total = order.getTotalAmount();
    BigDecimal paid = status == com.dut.erp.enums.InvoiceStatus.PAID ? total : BigDecimal.ZERO;

    return invoiceRepository.save(
        Invoice.builder()
            .organization(org)
            .order(order)
            .partner(order.getPartner())
            .invoiceNumber(invoiceNumber)
            .dueDate(Instant.now().plus(java.time.Duration.ofDays(30)))
            .totalAmount(total)
            .paidAmount(paid)
            .status(status)
            .build());
  }

  private void seedReplenishmentRequests(List<Warehouse> warehouses) {
    int index = 1;
    for (Warehouse wh : warehouses) {
      Optional<InventoryDocument> docOpt = inventoryDocumentRepository.findAll().stream()
          .filter(
              d -> d.getWarehouse().getId().equals(wh.getId())
                  && d.getDocumentType() == com.dut.erp.enums.DocumentType.RECEIPT)
          .findFirst();

      if (docOpt.isPresent()) {
        InventoryDocument receiptDoc = docOpt.get();

        boolean exists = replenishmentRequestRepository.findAllByWarehouseId(wh.getId()).size() > 0;
        if (!exists) {
          com.dut.erp.enums.ReplenishmentStatus status = receiptDoc.getDocumentStatus() == com.dut.erp.enums.DocumentStatus.COMPLETED
              ? com.dut.erp.enums.ReplenishmentStatus.RESOLVED
              : com.dut.erp.enums.ReplenishmentStatus.OPEN;

          ReplenishmentRequest req = replenishmentRequestRepository.save(
              ReplenishmentRequest.builder()
                  .warehouse(wh)
                  .inventoryDocument(receiptDoc)
                  .notes("Auto replenishment triggered by low stock alert. Check item levels.")
                  .status(status)
                  .build());

          if (status == com.dut.erp.enums.ReplenishmentStatus.RESOLVED) {
            receiptDoc.setReferenceType(com.dut.erp.enums.ReferenceType.REPLENISHMENT);
            receiptDoc.setReferenceId(req.getId());
            inventoryDocumentRepository.save(receiptDoc);
          }
          index++;
        }
      }
    }
  }

  private void seedModulesAndPermissions() {
    Map<String, ErpModule> modulesByCode = new HashMap<>();
    for (ErpModule module : erpModuleRepository.findAll()) {
      modulesByCode.put(module.getCode(), module);
    }

    Map<String, Permission> permissionsByCode = new HashMap<>();
    for (Permission permission : permissionRepository.findAll()) {
      permissionsByCode.put(permission.getCode(), permission);
    }

    // Products module
    ErpModule productsModule = getOrCreateModule(modulesByCode, "Products", "products", "Product management");
    createPermissionIfNotExists(
        permissionsByCode, productsModule, "products:read", "Read and list products");
    createPermissionIfNotExists(
        permissionsByCode, productsModule, "products:select", "Select product details");
    createPermissionIfNotExists(
        permissionsByCode, productsModule, "products:create", "Create products");
    createPermissionIfNotExists(
        permissionsByCode, productsModule, "products:write", "Update products");
    createPermissionIfNotExists(
        permissionsByCode, productsModule, "products:delete", "Delete products");

    // Partners module
    ErpModule partnersModule = getOrCreateModule(modulesByCode, "Partners", "partners", "Partner management");
    createPermissionIfNotExists(
        permissionsByCode, partnersModule, "partners:create", "Create partners");
    createPermissionIfNotExists(
        permissionsByCode, partnersModule, "partners:read", "Read and list partners");
    createPermissionIfNotExists(
        permissionsByCode, partnersModule, "partners:select", "Select partner details");
    createPermissionIfNotExists(
        permissionsByCode, partnersModule, "partners:write", "Update partners");
    createPermissionIfNotExists(
        permissionsByCode, partnersModule, "partners:delete", "Delete partners");

    // Roles module
    ErpModule rolesModule = getOrCreateModule(modulesByCode, "Roles", "roles", "Role management");
    createPermissionIfNotExists(permissionsByCode, rolesModule, "roles:create", "Create roles");
    createPermissionIfNotExists(
        permissionsByCode, rolesModule, "roles:read", "Read and list roles");
    createPermissionIfNotExists(
        permissionsByCode, rolesModule, "roles:select", "Select role details");
    createPermissionIfNotExists(permissionsByCode, rolesModule, "roles:write", "Update roles");
    createPermissionIfNotExists(permissionsByCode, rolesModule, "roles:delete", "Delete roles");

    // Organizations module
    ErpModule organizationsModule = getOrCreateModule(
        modulesByCode, "Organizations", "organizations", "Organization management");
    createPermissionIfNotExists(
        permissionsByCode, organizationsModule, "organizations:write", "Update organizations");

    // ERP Modules module
    ErpModule erpModule = getOrCreateModule(modulesByCode, "ERP Modules", "erp_module", "ERP module management");
    createPermissionIfNotExists(permissionsByCode, erpModule, "erp_module:read", "Read modules");

    // Users module
    ErpModule usersModule = getOrCreateModule(modulesByCode, "Users", "users", "User management");
    createPermissionIfNotExists(permissionsByCode, usersModule, "users:read", "Read users");
    createPermissionIfNotExists(permissionsByCode, usersModule, "users:select", "Select users");

    // Leads module
    ErpModule leadsModule = getOrCreateModule(modulesByCode, "Leads", "leads", "Lead management");
    createPermissionIfNotExists(permissionsByCode, leadsModule, "leads:create", "Create leads");
    createPermissionIfNotExists(
        permissionsByCode, leadsModule, "leads:read", "Read and list leads");
    createPermissionIfNotExists(
        permissionsByCode, leadsModule, "leads:read_all", "Read all leads in the organization");
    createPermissionIfNotExists(
        permissionsByCode, leadsModule, "leads:select", "Select lead details");
    createPermissionIfNotExists(permissionsByCode, leadsModule, "leads:write", "Update leads");
    createPermissionIfNotExists(
        permissionsByCode, leadsModule, "leads:write_all", "Update all leads in the organization");
    createPermissionIfNotExists(permissionsByCode, leadsModule, "leads:delete", "Delete leads");

    // Taxes module
    ErpModule taxesModule = getOrCreateModule(
        modulesByCode, "Taxes", "taxes", "Taxes configurations and rates management");
    createPermissionIfNotExists(permissionsByCode, taxesModule, "taxes:create", "Create taxes");
    createPermissionIfNotExists(
        permissionsByCode, taxesModule, "taxes:read", "Read and list taxes");
    createPermissionIfNotExists(
        permissionsByCode, taxesModule, "taxes:select", "Select tax details");
    createPermissionIfNotExists(permissionsByCode, taxesModule, "taxes:write", "Update taxes");
    createPermissionIfNotExists(permissionsByCode, taxesModule, "taxes:delete", "Delete taxes");

    // Orders module
    ErpModule ordersModule = getOrCreateModule(
        modulesByCode, "Orders", "orders", "Sales orders and quotation management");
    createPermissionIfNotExists(permissionsByCode, ordersModule, "orders:create", "Create orders");
    createPermissionIfNotExists(
        permissionsByCode, ordersModule, "orders:read", "Read and list orders");
    createPermissionIfNotExists(
        permissionsByCode, ordersModule, "orders:read_all", "Read all orders in the organization");
    createPermissionIfNotExists(
        permissionsByCode, ordersModule, "orders:select", "Select order details");
    createPermissionIfNotExists(permissionsByCode, ordersModule, "orders:write", "Update orders");
    createPermissionIfNotExists(
        permissionsByCode, ordersModule, "orders:write_all", "Update all orders in the organization");
    createPermissionIfNotExists(permissionsByCode, ordersModule, "orders:delete", "Delete orders");

    // Invoices module
    ErpModule invoicesModule = getOrCreateModule(modulesByCode, "Invoices", "invoices", "Invoices management");
    createPermissionIfNotExists(
        permissionsByCode, invoicesModule, "invoices:create", "Create invoices");
    createPermissionIfNotExists(
        permissionsByCode, invoicesModule, "invoices:read", "Read and list invoices");
    createPermissionIfNotExists(
        permissionsByCode, invoicesModule, "invoices:select", "Select invoice details");
    createPermissionIfNotExists(
        permissionsByCode, invoicesModule, "invoices:write", "Update invoices");
    createPermissionIfNotExists(
        permissionsByCode, invoicesModule, "invoices:delete", "Delete invoices");

    // Warehouses module
    ErpModule warehousesModule = getOrCreateModule(modulesByCode, "Warehouses", "warehouses", "Warehouse management");
    createPermissionIfNotExists(
        permissionsByCode, warehousesModule, "warehouses:create", "Create warehouses");
    createPermissionIfNotExists(
        permissionsByCode, warehousesModule, "warehouses:read", "Read and list warehouses");
    createPermissionIfNotExists(
        permissionsByCode, warehousesModule, "warehouses:read_all", "Read all warehouses in the organization");
    createPermissionIfNotExists(
        permissionsByCode, warehousesModule, "warehouses:select", "Select warehouse details");
    createPermissionIfNotExists(
        permissionsByCode, warehousesModule, "warehouses:write", "Update warehouses");
    createPermissionIfNotExists(
        permissionsByCode, warehousesModule, "warehouses:write_all", "Update all warehouses in the organization");
    createPermissionIfNotExists(
        permissionsByCode, warehousesModule, "warehouses:delete", "Delete warehouses");

    // Inventory Documents module
    ErpModule invDocsModule = getOrCreateModule(modulesByCode, "Inventory Documents", "inventory-documents", "Inventory documents management");
    createPermissionIfNotExists(permissionsByCode, invDocsModule, "inventory-documents:create", "Create inventory documents");
    createPermissionIfNotExists(permissionsByCode, invDocsModule, "inventory-documents:read", "Read and list inventory documents");
    createPermissionIfNotExists(permissionsByCode, invDocsModule, "inventory-documents:select", "Select inventory document details");
    createPermissionIfNotExists(permissionsByCode, invDocsModule, "inventory-documents:write", "Update inventory documents");
    createPermissionIfNotExists(permissionsByCode, invDocsModule, "inventory-documents:delete", "Delete inventory documents");

    // Inventory Transactions module
    ErpModule invTransModule = getOrCreateModule(modulesByCode, "Inventory Transactions", "inventory-transactions", "Inventory transactions management");
    createPermissionIfNotExists(permissionsByCode, invTransModule, "inventory-transactions:create", "Create inventory transactions");
    createPermissionIfNotExists(permissionsByCode, invTransModule, "inventory-transactions:read", "Read and list inventory transactions");
    createPermissionIfNotExists(permissionsByCode, invTransModule, "inventory-transactions:select", "Select inventory transaction details");
    createPermissionIfNotExists(permissionsByCode, invTransModule, "inventory-transactions:write", "Update inventory transactions");
    createPermissionIfNotExists(permissionsByCode, invTransModule, "inventory-transactions:delete", "Delete inventory transactions");

    // Replenishment Requests module
    ErpModule replenishmentModule = getOrCreateModule(modulesByCode, "Replenishment Requests", "replenishment-requests", "Replenishment requests management");
    createPermissionIfNotExists(permissionsByCode, replenishmentModule, "replenishment-requests:create", "Create replenishment requests");
    createPermissionIfNotExists(permissionsByCode, replenishmentModule, "replenishment-requests:read", "Read and list replenishment requests");
    createPermissionIfNotExists(permissionsByCode, replenishmentModule, "replenishment-requests:select", "Select replenishment request details");
    createPermissionIfNotExists(permissionsByCode, replenishmentModule, "replenishment-requests:write", "Update replenishment requests");
    createPermissionIfNotExists(permissionsByCode, replenishmentModule, "replenishment-requests:delete", "Delete replenishment requests");

    // Stock Valuations module
    ErpModule stockValuationsModule = getOrCreateModule(modulesByCode, "Stock Valuations", "stock-valuations", "Stock valuations management");
    createPermissionIfNotExists(permissionsByCode, stockValuationsModule, "stock-valuations:create", "Create stock valuations");
    createPermissionIfNotExists(permissionsByCode, stockValuationsModule, "stock-valuations:read", "Read and list stock valuations");
    createPermissionIfNotExists(permissionsByCode, stockValuationsModule, "stock-valuations:select", "Select stock valuation details");
    createPermissionIfNotExists(permissionsByCode, stockValuationsModule, "stock-valuations:write", "Update stock valuations");
    createPermissionIfNotExists(permissionsByCode, stockValuationsModule, "stock-valuations:delete", "Delete stock valuations");

    // Sale Teams module
    ErpModule saleTeamsModule = getOrCreateModule(modulesByCode, "Sale Teams", "sale_teams", "Sale teams management");
    createPermissionIfNotExists(
        permissionsByCode, saleTeamsModule, "sale_teams:create", "Create sale teams");
    createPermissionIfNotExists(
        permissionsByCode, saleTeamsModule, "sale_teams:read", "Read and list sale teams");
    createPermissionIfNotExists(
        permissionsByCode, saleTeamsModule, "sale_teams:select", "Select sale team details");
    createPermissionIfNotExists(
        permissionsByCode, saleTeamsModule, "sale_teams:write", "Update sale teams");
    createPermissionIfNotExists(
        permissionsByCode, saleTeamsModule, "sale_teams:delete", "Delete sale teams");
  }

  private Map<String, Organization> loadOrganizationsByName() {
    Map<String, Organization> organizationsByName = new HashMap<>();
    for (Organization organization : organizationRepository.findAll()) {
      organizationsByName.putIfAbsent(organization.getName(), organization);
    }
    return organizationsByName;
  }

  private Map<String, Organization> loadOrganizationsByTaxCode() {
    Map<String, Organization> organizationsByTaxCode = new HashMap<>();
    for (Organization organization : organizationRepository.findAll()) {
      if (organization.getTaxCode() != null) {
        organizationsByTaxCode.putIfAbsent(organization.getTaxCode(), organization);
      }
    }
    return organizationsByTaxCode;
  }

  private Organization getOrCreateOrganization(
      Map<String, Organization> organizationsByName,
      Map<String, Organization> organizationsByTaxCode,
      String name,
      String description,
      String address,
      String hotline,
      String taxCode) {
    Organization existing = organizationsByName.get(name);
    if (existing == null && taxCode != null) {
      existing = organizationsByTaxCode.get(taxCode);
    }
    if (existing != null) {
      return existing;
    }

    Organization created = organizationRepository.save(
        Organization.builder()
            .name(name)
            .description(description)
            .address(address)
            .hotline(hotline)
            .taxCode(taxCode)
            .build());
    organizationsByName.put(name, created);
    if (taxCode != null) {
      organizationsByTaxCode.put(taxCode, created);
    }
    return created;
  }

  private ErpModule getOrCreateModule(
      Map<String, ErpModule> modulesByCode, String name, String code, String description) {
    ErpModule existing = modulesByCode.get(code);
    if (existing != null) {
      return existing;
    }

    ErpModule created = erpModuleRepository.save(
        ErpModule.builder().name(name).code(code).description(description).build());
    modulesByCode.put(code, created);
    return created;
  }

  private void createPermissionIfNotExists(
      Map<String, Permission> permissionsByCode,
      ErpModule module,
      String code,
      String description) {
    if (permissionsByCode.containsKey(code)) {
      return;
    }

    Permission created = permissionRepository.save(
        Permission.builder()
            .code(code)
            .name(description)
            .description(description)
            .module(module)
            .build());
    permissionsByCode.put(code, created);
  }

  private Role getOrCreateRole(
      String roleName, Organization organization, Set<Permission> permissions) {
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
            () -> roleRepository.save(
                Role.builder()
                    .name(roleName)
                    .organization(organization)
                    .permissions(new HashSet<>(permissions))
                    .build()));
  }

  private User getOrCreateUser(
      String email, String rawPassword, String firstName, String lastName) {
    return userRepository
        .findByEmail(email)
        .orElseGet(
            () -> userRepository.save(
                User.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .roles(new HashSet<>())
                    .organizations(new HashSet<>())
                    .build()));
  }

  private void seedHistoricalSales(
      Organization org,
      List<Partner> partners,
      List<Product> products,
      Tax vat10,
      Tax vat5,
      Warehouse centralWh) {
    
    // Kiểm tra xem đã có dữ liệu lịch sử chưa
    boolean hasHistoricalOrders = orderRepository.existsByOrganizationIdAndOrderNumber(org.getId(), "HIST-SO-180-1");
    if (hasHistoricalOrders) {
      return;
    }

    Random rand = new Random(42);
    Instant now = Instant.now();

    for (int day = 180; day >= 1; day--) {
      int relativeDay = 180 - day; // Ngày 0 là cách đây 180 ngày, ngày 179 là cách đây 1 ngày
      
      // 1. Trend: Tăng trưởng tuyến tính từ 0.8 đến 1.4 (tăng 75% sau 180 ngày)
      double trend = 0.8 + (relativeDay / 180.0) * 0.6;
      
      // 2. Mùa vụ chu kỳ 28 ngày (4 tuần): mô phỏng lương/khuyến mãi hàng tháng
      int dayOfCycle = relativeDay % 28;
      double seasonal = 1.0;
      if (dayOfCycle < 7) {
        seasonal = 1.35; // Tuần 1: Đầu tháng mua sắm mạnh
      } else if (dayOfCycle < 14) {
        seasonal = 0.85; // Tuần 2: Giảm nhẹ
      } else if (dayOfCycle < 21) {
        seasonal = 1.20; // Tuần 3: Đợt khuyến mãi giữa tháng
      } else {
        seasonal = 0.60; // Tuần 4: Cuối tháng chi tiêu tiết kiệm
      }
      
      // 3. Nhiễu ngẫu nhiên nhỏ +/- 5% để tạo nhấp nhô tự nhiên nhưng ổn định
      double noise = 0.95 + rand.nextDouble() * 0.10;
      
      double demandMultiplier = trend * seasonal * noise;
      
      // Tạo đơn hàng đều đặn mỗi ngày (không skip ngày) để dữ liệu không bị gãy chuỗi
      int ordersOnThisDay = 2; // Cố định 2 đơn hàng/ngày để ổn định quy mô doanh số
      for (int oNum = 1; oNum <= ordersOnThisDay; oNum++) {
        String orderNum = "HIST-SO-" + day + "-" + oNum;
        Instant orderDate = now.minus(java.time.Duration.ofDays(day))
            .minus(java.time.Duration.ofHours(rand.nextInt(12)))
            .minus(java.time.Duration.ofMinutes(rand.nextInt(60)));

        Order order = Order.builder()
            .organization(org)
            .partner(partners.get(rand.nextInt(partners.size())))
            .orderNumber(orderNum)
            .status(com.dut.erp.enums.OrderStatus.COMPLETED)
            .deliveryDate(orderDate.plus(java.time.Duration.ofDays(1)))
            .expirationDate(orderDate.plus(java.time.Duration.ofDays(7)))
            .totalAmount(BigDecimal.ZERO)
            .build();

        order = orderRepository.save(order);

        int itemCount = 2; // Mỗi đơn hàng có 2 sản phẩm
        List<OrderItem> items = new ArrayList<>();
        BigDecimal totalOrderAmt = BigDecimal.ZERO;

        for (int itemIdx = 0; itemIdx < itemCount; itemIdx++) {
          Product prod = products.get(rand.nextInt(products.size()));
          Tax tax = (rand.nextBoolean()) ? vat10 : vat5;
          // Số lượng ổn định nhân với hệ số nhu cầu động
          BigDecimal qty = BigDecimal.valueOf(Math.max(1, (int)(2 * demandMultiplier)));
          // Giả lập biến động giá bán thực tế ±5% do chiết khấu/khuyến mãi nhẹ
          double salesFluctuation = 0.95 + rand.nextDouble() * 0.10;
          BigDecimal unitPrice = prod.getSalesPrice()
              .multiply(BigDecimal.valueOf(salesFluctuation))
              .setScale(2, java.math.RoundingMode.HALF_UP);
          BigDecimal subtotal = qty.multiply(unitPrice);

          OrderItem item = OrderItem.builder()
              .organization(org)
              .order(order)
              .product(prod)
              .tax(tax)
              .quantity(qty)
              .unitPrice(unitPrice)
              .subtotal(subtotal)
              .build();

          items.add(item);
          totalOrderAmt = totalOrderAmt.add(subtotal);
        }
        order.setItems(items);
        order.setTotalAmount(totalOrderAmt);
        order = orderRepository.save(order);

        // Tạo tài liệu xuất kho ISSUE tương ứng
        String outDocName = centralWh.getCode() + "/OUT/HIST/" + day + "-" + oNum;
        InventoryDocument outDoc = inventoryDocumentRepository.save(
            InventoryDocument.builder()
                .warehouse(centralWh)
                .name(outDocName)
                .documentType(com.dut.erp.enums.DocumentType.ISSUE)
                .referenceType(com.dut.erp.enums.ReferenceType.SALES_ORDER)
                .referenceId(order.getId())
                .documentStatus(com.dut.erp.enums.DocumentStatus.COMPLETED)
                .notes("Historical stock issue for order " + orderNum)
                .scheduledDate(orderDate)
                .dateDone(orderDate)
                .build());

        List<InventoryDocumentLine> outLines = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
          Product prod = item.getProduct();
          BigDecimal qty = item.getQuantity();
          // Giả lập biến động giá mua vào ±15% từ nhà cung cấp
          double purchaseFluctuation = 0.85 + rand.nextDouble() * 0.30;
          BigDecimal unitCost = prod.getPurchasePrice()
              .multiply(BigDecimal.valueOf(purchaseFluctuation))
              .setScale(4, java.math.RoundingMode.HALF_UP);
          BigDecimal valuation = qty.multiply(unitCost);

          InventoryDocumentLine outLine = inventoryDocumentLineRepository.save(
              InventoryDocumentLine.builder()
                  .inventoryDocument(outDoc)
                  .product(prod)
                  .quantity(qty)
                  .unitCost(unitCost)
                  .valuation(valuation)
                  .remainingQuantity(BigDecimal.ZERO)
                  .build());
          outLines.add(outLine);

          stockValuationRepository.save(
              StockValuation.builder()
                  .inventoryDocumentLine(outLine)
                  .product(prod)
                  .quantity(qty)
                  .unitCost(unitCost)
                  .totalValuation(valuation)
                  .method(prod.getCogsMethod())
                  .build());
        }
        outDoc.setLines(outLines);
        inventoryDocumentRepository.save(outDoc);

        // Tạo hóa đơn tương ứng
        Invoice invoice = invoiceRepository.save(
            Invoice.builder()
                .organization(org)
                .order(order)
                .partner(order.getPartner())
                .invoiceNumber("INV-HIST-" + day + "-" + oNum)
                .dueDate(orderDate.plus(java.time.Duration.ofDays(30)))
                .totalAmount(totalOrderAmt)
                .paidAmount(totalOrderAmt)
                .status(com.dut.erp.enums.InvoiceStatus.PAID)
                .build());

        // Ép Hibernate flush các câu lệnh INSERT xuống DB trước khi chạy JDBC UPDATE
        orderRepository.flush();
        invoiceRepository.flush();
        inventoryDocumentRepository.flush();

        // Cập nhật ngày tạo và cập nhật thủ công qua JDBC Template để bypass JPA Auditing
        java.sql.Timestamp orderDateTimestamp = new java.sql.Timestamp(orderDate.toEpochMilli());
        jdbcTemplate.update("UPDATE orders SET created_at = ?, updated_at = ? WHERE id = ?", 
            orderDateTimestamp, orderDateTimestamp, order.getId());
        jdbcTemplate.update("UPDATE invoices SET created_at = ?, updated_at = ? WHERE id = ?", 
            orderDateTimestamp, orderDateTimestamp, invoice.getId());
        jdbcTemplate.update("UPDATE inventory_documents SET created_at = ?, updated_at = ? WHERE id = ?", 
            orderDateTimestamp, orderDateTimestamp, outDoc.getId());
      }
    }
  }
}
