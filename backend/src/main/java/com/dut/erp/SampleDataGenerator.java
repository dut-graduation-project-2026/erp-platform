package com.dut.erp;

import com.dut.erp.entity.CrmAppointment;
import com.dut.erp.entity.CrmLead;
import com.dut.erp.entity.CrmStage;
import com.dut.erp.entity.ErpModule;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.PartnerContact;
import com.dut.erp.entity.Permission;
import com.dut.erp.entity.Product;
import com.dut.erp.entity.ProductTemplate;
import com.dut.erp.entity.Role;
import com.dut.erp.entity.SaleInvoice;
import com.dut.erp.entity.SaleOrder;
import com.dut.erp.entity.SaleOrderLine;
import com.dut.erp.entity.SalePartner;
import com.dut.erp.entity.SalesTeam;
import com.dut.erp.entity.StockInventory;
import com.dut.erp.entity.StockInventoryLine;
import com.dut.erp.entity.StockLocation;
import com.dut.erp.entity.StockLot;
import com.dut.erp.entity.StockMove;
import com.dut.erp.entity.StockPicking;
import com.dut.erp.entity.StockQuant;
import com.dut.erp.entity.StockValuation;
import com.dut.erp.entity.User;
import com.dut.erp.entity.Warehouse;
import com.dut.erp.enums.AppointmentStatus;
import com.dut.erp.enums.AppointmentType;
import com.dut.erp.enums.CostMethod;
import com.dut.erp.enums.InvoiceStatus;
import com.dut.erp.enums.LeadType;
import com.dut.erp.enums.LocationType;
import com.dut.erp.enums.PartnerStatus;
import com.dut.erp.enums.PartnerType;
import com.dut.erp.enums.PickingType;
import com.dut.erp.enums.SaleOrderStatus;
import com.dut.erp.enums.StockInventoryState;
import com.dut.erp.enums.StockMoveState;
import com.dut.erp.enums.StockPickingState;
import com.dut.erp.repository.CrmAppointmentRepository;
import com.dut.erp.repository.CrmLeadRepository;
import com.dut.erp.repository.CrmStageRepository;
import com.dut.erp.repository.ErpModuleRepository;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.PartnerContactRepository;
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.ProductTemplateRepository;
import com.dut.erp.repository.RoleRepository;
import com.dut.erp.repository.SaleInvoiceRepository;
import com.dut.erp.repository.SaleOrderLineRepository;
import com.dut.erp.repository.SaleOrderRepository;
import com.dut.erp.repository.SalePartnerRepository;
import com.dut.erp.repository.SalesTeamRepository;
import com.dut.erp.repository.StockInventoryLineRepository;
import com.dut.erp.repository.StockInventoryRepository;
import com.dut.erp.repository.StockLocationRepository;
import com.dut.erp.repository.StockLotRepository;
import com.dut.erp.repository.StockMoveRepository;
import com.dut.erp.repository.StockPickingRepository;
import com.dut.erp.repository.StockQuantRepository;
import com.dut.erp.repository.StockValuationRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.repository.WarehouseRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class SampleDataGenerator implements CommandLineRunner {

  // ── Auth / Organization ──────────────────────────────────────────────────────
  private static final String ADMIN_ROLE_NAME        = "ADMIN";
  private static final String SALES_ROLE_NAME        = "SALES";
  private static final String WAREHOUSE_ROLE_NAME    = "WAREHOUSE";
  private static final String NO_PERMISSION_ROLE_NAME = "NO_PERMISSION";

  private static final String ORG_A_NAME     = "DUT Technology JSC";
  private static final String ORG_A_TAX_CODE = "SEED-TAX-0001";
  private static final String ORG_B_NAME     = "DUT Trading Co.";
  private static final String ORG_B_TAX_CODE = "SEED-TAX-0002";

  private static final String ADMIN_EMAIL    = "admin@dut.com";
  private static final String ADMIN_PASSWORD = "Admin@123";
  private static final String SALES_EMAIL    = "sales@dut.com";
  private static final String SALES_PASSWORD = "Sales@123";
  private static final String WH_EMAIL       = "warehouse@dut.com";
  private static final String WH_PASSWORD    = "Warehouse@123";
  private static final String LIMITED_EMAIL  = "testuser@dut.com";
  private static final String LIMITED_PASSWORD = "testuser@123";

  // ── Repositories ─────────────────────────────────────────────────────────────
  private final OrganizationRepository      organizationRepository;
  private final UserRepository              userRepository;
  private final RoleRepository              roleRepository;
  private final PermissionRepository        permissionRepository;
  private final ErpModuleRepository         erpModuleRepository;
  private final SalePartnerRepository       salePartnerRepository;
  private final PartnerContactRepository    partnerContactRepository;
  private final ProductTemplateRepository   productTemplateRepository;
  private final ProductRepository           productRepository;
  private final SalesTeamRepository         salesTeamRepository;
  private final CrmStageRepository          crmStageRepository;
  private final CrmLeadRepository           crmLeadRepository;
  private final CrmAppointmentRepository    crmAppointmentRepository;
  private final SaleOrderRepository         saleOrderRepository;
  private final SaleOrderLineRepository     saleOrderLineRepository;
  private final SaleInvoiceRepository       saleInvoiceRepository;
  private final WarehouseRepository         warehouseRepository;
  private final StockLocationRepository     stockLocationRepository;
  private final StockLotRepository          stockLotRepository;
  private final StockPickingRepository      stockPickingRepository;
  private final StockMoveRepository         stockMoveRepository;
  private final StockQuantRepository        stockQuantRepository;
  private final StockValuationRepository    stockValuationRepository;
  private final StockInventoryRepository    stockInventoryRepository;
  private final StockInventoryLineRepository stockInventoryLineRepository;
  private final PasswordEncoder             passwordEncoder;

  // ──────────────────────────────────────────────────────────────────────────────

  @Override
  @Transactional
  public void run(String... args) {
    log.info("=== SampleDataGenerator: starting seed ===");

    // 1. Organizations
    Organization orgA = getOrCreateOrganization(ORG_A_NAME, "Công ty TNHH Công nghệ DUT",
        "234 Nguyễn Văn Linh, Đà Nẵng", "02363812345", ORG_A_TAX_CODE);
    Organization orgB = getOrCreateOrganization(ORG_B_NAME, "Công ty Thương mại DUT",
        "12 Lê Duẩn, Đà Nẵng", "02363900001", ORG_B_TAX_CODE);

    // 2. ERP Modules & Permissions
    ErpModule crmModule  = getOrCreateModule("CRM",       "crm",       "Quản lý quan hệ khách hàng");
    ErpModule salesModule = getOrCreateModule("Sales",    "sales",     "Quản lý bán hàng");
    ErpModule stockModule = getOrCreateModule("Inventory","inventory", "Quản lý kho hàng");

    Permission crmView    = getOrCreatePermission("CRM View",    "crm.view",    "Xem CRM",          crmModule);
    Permission crmManage  = getOrCreatePermission("CRM Manage",  "crm.manage",  "Quản lý CRM",      crmModule);
    Permission salesView  = getOrCreatePermission("Sales View",  "sales.view",  "Xem bán hàng",     salesModule);
    Permission salesManage = getOrCreatePermission("Sales Manage","sales.manage","Quản lý bán hàng", salesModule);
    Permission stockView  = getOrCreatePermission("Stock View",  "stock.view",  "Xem kho",          stockModule);
    Permission stockManage = getOrCreatePermission("Stock Manage","stock.manage","Quản lý kho",      stockModule);

    Set<Permission> allPermissions    = new HashSet<>(permissionRepository.findAll());
    Set<Permission> salesPermissions  = Set.of(crmView, crmManage, salesView, salesManage);
    Set<Permission> stockPermissions  = Set.of(stockView, stockManage);

    // 3. Roles per organisation
    Role adminRoleA   = getOrCreateRole(ADMIN_ROLE_NAME,        orgA, allPermissions);
    Role adminRoleB   = getOrCreateRole(ADMIN_ROLE_NAME,        orgB, allPermissions);
    Role salesRoleA   = getOrCreateRole(SALES_ROLE_NAME,        orgA, salesPermissions);
    Role warehRoleA   = getOrCreateRole(WAREHOUSE_ROLE_NAME,    orgA, stockPermissions);
    getOrCreateRole(NO_PERMISSION_ROLE_NAME, orgA, new HashSet<>());
    getOrCreateRole(NO_PERMISSION_ROLE_NAME, orgB, new HashSet<>());

    // 4. Users
    User adminUser  = getOrCreateUser(ADMIN_EMAIL,   ADMIN_PASSWORD,   "System",    "Admin");
    User salesUser  = getOrCreateUser(SALES_EMAIL,   SALES_PASSWORD,   "Nguyen",    "Sales");
    User whUser     = getOrCreateUser(WH_EMAIL,      WH_PASSWORD,      "Tran",      "Warehouse");
    User limitedUser = getOrCreateUser(LIMITED_EMAIL, LIMITED_PASSWORD, "Test",      "User");

    adminUser.getOrganizations().addAll(Set.of(orgA, orgB));
    adminUser.getRoles().addAll(Set.of(adminRoleA, adminRoleB));

    salesUser.getOrganizations().add(orgA);
    salesUser.getRoles().add(salesRoleA);

    whUser.getOrganizations().add(orgA);
    whUser.getRoles().add(warehRoleA);

    limitedUser.getOrganizations().add(orgA);
    userRepository.saveAll(List.of(adminUser, salesUser, whUser, limitedUser));

    // 5. Partners (Org A)
    SalePartner customer1 = getOrCreatePartner(orgA, "KH-001", "Công ty ABC",
        PartnerType.COMPANY, "1234567890", "abc@company.vn", "0901234567", "Hà Nội");
    SalePartner customer2 = getOrCreatePartner(orgA, "KH-002", "Công ty XYZ",
        PartnerType.COMPANY, "9876543210", "xyz@company.vn", "0907654321", "TP. Hồ Chí Minh");
    SalePartner supplier1 = getOrCreatePartner(orgA, "NCC-001", "Nhà cung cấp Alpha",
        PartnerType.COMPANY, "5555000111", "alpha@supplier.vn", "0912345678", "Đà Nẵng");
    SalePartner supplier2 = getOrCreatePartner(orgA, "NCC-002", "Nhà cung cấp Beta",
        PartnerType.COMPANY, "5555000222", "beta@supplier.vn", "0987654321", "Cần Thơ");
    SalePartner both1 = getOrCreatePartner(orgA, "PH-001", "Đối tác Gamma",
        PartnerType.INDIVIDUAL, "3333111222", "gamma@partner.vn", "0933111222", "Huế");

    // Partner contacts
    seedContactIfAbsent(customer1, "Nguyễn Văn An", "nguyenvanan@abc.vn",   "0900000001", "Giám đốc", true);
    seedContactIfAbsent(customer1, "Trần Thị Bình", "tranthibibh@abc.vn",   "0900000002", "Kế toán",  false);
    seedContactIfAbsent(customer2, "Lê Văn Cường",  "levancuong@xyz.vn",    "0900000003", "Giám đốc", true);
    seedContactIfAbsent(supplier1, "Phạm Thị Dung", "phamthidung@alpha.vn", "0900000004", "Sale Rep", true);

    // 6. Product Templates & Products (Org A)
    ProductTemplate tmplElectronics = getOrCreateTemplate(orgA, "Sản phẩm điện tử",
        "Mẫu danh mục điện tử", "Electronics", "Cái", CostMethod.FIFO);
    ProductTemplate tmplFurniture = getOrCreateTemplate(orgA, "Nội thất văn phòng",
        "Mẫu danh mục nội thất", "Furniture", "Bộ", CostMethod.AVERAGE);

    Product laptop   = getOrCreateProduct(orgA, tmplElectronics, "SKU-LAPTOP-001", "Laptop Dell XPS 15",
        "8888001001", new BigDecimal("35000000"), new BigDecimal("28000000"),
        new BigDecimal("2.5"), new BigDecimal("0.010"), "Laptop cao cấp cho doanh nghiệp", new BigDecimal("5"));
    Product monitor  = getOrCreateProduct(orgA, tmplElectronics, "SKU-MON-001", "Màn hình LG 27\" 4K",
        "8888002001", new BigDecimal("12000000"), new BigDecimal("9500000"),
        new BigDecimal("5.0"), new BigDecimal("0.025"), "Màn hình 4K chuyên dụng",         new BigDecimal("10"));
    Product keyboard = getOrCreateProduct(orgA, tmplElectronics, "SKU-KB-001",   "Bàn phím cơ Logitech",
        "8888003001", new BigDecimal("2500000"),  new BigDecimal("1800000"),
        new BigDecimal("1.0"), new BigDecimal("0.005"), "Bàn phím cơ chuyên nghiệp",       new BigDecimal("20"));
    Product desk     = getOrCreateProduct(orgA, tmplFurniture,   "SKU-DESK-001", "Bàn làm việc gỗ sồi",
        "7777001001", new BigDecimal("8000000"),  new BigDecimal("5500000"),
        new BigDecimal("30.0"), new BigDecimal("0.5"), "Bàn làm việc cao cấp",             new BigDecimal("3"));
    Product chair    = getOrCreateProduct(orgA, tmplFurniture,   "SKU-CHAIR-001","Ghế công thái học Ergonomic",
        "7777002001", new BigDecimal("6500000"),  new BigDecimal("4200000"),
        new BigDecimal("15.0"), new BigDecimal("0.3"), "Ghế văn phòng cao cấp",            new BigDecimal("5"));

    // 7. Sales Teams & CRM Stages (Org A)
    SalesTeam teamNorth = getOrCreateSalesTeam(orgA, "Nhóm Miền Bắc", salesUser);
    SalesTeam teamSouth = getOrCreateSalesTeam(orgA, "Nhóm Miền Nam", adminUser);

    CrmStage stageNew        = getOrCreateCrmStage(orgA, "Mới",           1);
    CrmStage stageQualified  = getOrCreateCrmStage(orgA, "Đủ điều kiện", 2);
    CrmStage stageProposal   = getOrCreateCrmStage(orgA, "Báo giá",      3);
    CrmStage stageWon        = getOrCreateCrmStage(orgA, "Đã chốt",      4);
    CrmStage stageLost       = getOrCreateCrmStage(orgA, "Thất bại",     5);

    // 8. CRM Leads / Opportunities
    CrmLead lead1 = getOrCreateLead(orgA, customer1, LeadType.LEAD,
        "Cơ hội cung cấp laptop cho ABC",
        new BigDecimal("100000000"), new BigDecimal("70.00"),
        stageQualified, salesUser, teamNorth);
    CrmLead lead2 = getOrCreateLead(orgA, customer2, LeadType.OPPORTUNITY,
        "Dự án trang bị văn phòng XYZ",
        new BigDecimal("250000000"), new BigDecimal("85.00"),
        stageProposal, salesUser, teamSouth);
    CrmLead lead3 = getOrCreateLead(orgA, both1, LeadType.LEAD,
        "Khảo sát nhu cầu Gamma",
        new BigDecimal("50000000"), new BigDecimal("40.00"),
        stageNew, salesUser, teamNorth);

    // Appointments
    Instant now = Instant.now();
    seedAppointmentIfAbsent(orgA, lead1,
        AppointmentType.MEETING, "Họp đánh giá nhu cầu ABC",
        "Thảo luận yêu cầu chi tiết", now.plus(2, ChronoUnit.DAYS),
        now.plus(2, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS),
        "Văn phòng DUT", AppointmentStatus.SCHEDULED);
    seedAppointmentIfAbsent(orgA, lead2,
        AppointmentType.DEMO, "Demo sản phẩm cho XYZ",
        "Trình bày sản phẩm văn phòng", now.plus(5, ChronoUnit.DAYS),
        now.plus(5, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS),
        "Văn phòng XYZ", AppointmentStatus.SCHEDULED);

    // 9. Warehouses & Locations (Org A)
    Warehouse whMain = getOrCreateWarehouse(orgA, "Kho chính Đà Nẵng", "WH-DN",
        "234 Nguyễn Văn Linh, Đà Nẵng");
    Warehouse whHCM  = getOrCreateWarehouse(orgA, "Kho TP. Hồ Chí Minh", "WH-HCM",
        "100 Nguyễn Trãi, Quận 1, TP. HCM");

    StockLocation locMainIn      = getOrCreateLocation(whMain, null, "Khu nhập hàng",      "IN",     LocationType.TRANSIT);
    StockLocation locMainOut     = getOrCreateLocation(whMain, null, "Khu xuất hàng",      "OUT",    LocationType.TRANSIT);
    StockLocation locMainStorage = getOrCreateLocation(whMain, null, "Khu lưu trữ chính",  "STORE",  LocationType.INTERNAL);
    StockLocation locMainA1      = getOrCreateLocation(whMain, locMainStorage, "Kệ A-01",  "A1",     LocationType.INTERNAL);
    StockLocation locMainA2      = getOrCreateLocation(whMain, locMainStorage, "Kệ A-02",  "A2",     LocationType.INTERNAL);
    StockLocation locMainB1      = getOrCreateLocation(whMain, locMainStorage, "Kệ B-01",  "B1",     LocationType.INTERNAL);
    StockLocation locMainVirtual = getOrCreateLocation(whMain, null, "Ảo (hao hụt)",       "VIRT",   LocationType.INVENTORY);
    StockLocation locMainCustomer = getOrCreateLocation(whMain, null, "Khách hàng",        "CUST",   LocationType.CUSTOMER);
    StockLocation locMainSupplier = getOrCreateLocation(whMain, null, "Nhà cung cấp",      "SUPP",   LocationType.SUPPLIER);

    StockLocation locHcmIn      = getOrCreateLocation(whHCM, null, "Khu nhập HCM",    "IN",    LocationType.TRANSIT);
    StockLocation locHcmStorage = getOrCreateLocation(whHCM, null, "Khu lưu trữ HCM", "STORE", LocationType.INTERNAL);
    StockLocation locHcmOut     = getOrCreateLocation(whHCM, null, "Khu xuất HCM",    "OUT",   LocationType.TRANSIT);

    // 10. Lots
    StockLot lotLaptop1  = getOrCreateLot(orgA, laptop,   "LOT-LAPTOP-2024-01", now.plus(365, ChronoUnit.DAYS));
    StockLot lotLaptop2  = getOrCreateLot(orgA, laptop,   "LOT-LAPTOP-2024-02", now.plus(300, ChronoUnit.DAYS));
    StockLot lotMonitor1 = getOrCreateLot(orgA, monitor,  "LOT-MON-2024-01",    now.plus(400, ChronoUnit.DAYS));
    StockLot lotKeyboard1 = getOrCreateLot(orgA, keyboard,"LOT-KB-2024-01",     now.plus(500, ChronoUnit.DAYS));
    StockLot lotDesk1    = getOrCreateLot(orgA, desk,     "LOT-DESK-2024-01",   null);
    StockLot lotChair1   = getOrCreateLot(orgA, chair,    "LOT-CHAIR-2024-01",  null);

    // 11. Receipt pickings (Supplier → WH)
    StockPicking receipt1 = getOrCreatePicking(orgA, "WH/IN/001", PickingType.INCOMING,
        locMainSupplier, locMainA1, supplier1, null, StockPickingState.DONE,
        now.minus(30, ChronoUnit.DAYS), now.minus(30, ChronoUnit.DAYS));

    StockMove move1a = seedMove(receipt1, laptop,   locMainSupplier, locMainA1, lotLaptop1,  new BigDecimal("20"), new BigDecimal("20"), StockMoveState.DONE);
    StockMove move1b = seedMove(receipt1, monitor,  locMainSupplier, locMainA1, lotMonitor1, new BigDecimal("15"), new BigDecimal("15"), StockMoveState.DONE);
    StockMove move1c = seedMove(receipt1, keyboard, locMainSupplier, locMainA1, lotKeyboard1,new BigDecimal("30"), new BigDecimal("30"), StockMoveState.DONE);

    StockPicking receipt2 = getOrCreatePicking(orgA, "WH/IN/002", PickingType.INCOMING,
        locMainSupplier, locMainB1, supplier2, null, StockPickingState.DONE,
        now.minus(15, ChronoUnit.DAYS), now.minus(15, ChronoUnit.DAYS));

    StockMove move2a = seedMove(receipt2, desk,    locMainSupplier, locMainB1, lotDesk1,   new BigDecimal("10"), new BigDecimal("10"), StockMoveState.DONE);
    StockMove move2b = seedMove(receipt2, chair,   locMainSupplier, locMainB1, lotChair1,  new BigDecimal("10"), new BigDecimal("10"), StockMoveState.DONE);
    StockMove move2c = seedMove(receipt2, laptop,  locMainSupplier, locMainB1, lotLaptop2, new BigDecimal("5"),  new BigDecimal("5"),  StockMoveState.DONE);

    // 12. Stock Quants (on-hand after receipts)
    seedQuant(laptop,   locMainA1, lotLaptop1,  new BigDecimal("20"));
    seedQuant(laptop,   locMainB1, lotLaptop2,  new BigDecimal("5"));
    seedQuant(monitor,  locMainA1, lotMonitor1, new BigDecimal("15"));
    seedQuant(keyboard, locMainA1, lotKeyboard1,new BigDecimal("30"));
    seedQuant(desk,     locMainB1, lotDesk1,    new BigDecimal("10"));
    seedQuant(chair,    locMainB1, lotChair1,   new BigDecimal("10"));

    // 13. Stock Valuations (FIFO layers)
    seedValuation(laptop,   move1a, new BigDecimal("20"), new BigDecimal("28000000"), new BigDecimal("560000000"), new BigDecimal("20"), new BigDecimal("560000000"), CostMethod.FIFO);
    seedValuation(monitor,  move1b, new BigDecimal("15"), new BigDecimal("9500000"),  new BigDecimal("142500000"), new BigDecimal("15"), new BigDecimal("142500000"), CostMethod.FIFO);
    seedValuation(keyboard, move1c, new BigDecimal("30"), new BigDecimal("1800000"),  new BigDecimal("54000000"),  new BigDecimal("30"), new BigDecimal("54000000"),  CostMethod.FIFO);
    seedValuation(desk,     move2a, new BigDecimal("10"), new BigDecimal("5500000"),  new BigDecimal("55000000"),  new BigDecimal("10"), new BigDecimal("55000000"),  CostMethod.AVERAGE);
    seedValuation(chair,    move2b, new BigDecimal("10"), new BigDecimal("4200000"),  new BigDecimal("42000000"),  new BigDecimal("10"), new BigDecimal("42000000"),  CostMethod.AVERAGE);
    seedValuation(laptop,   move2c, new BigDecimal("5"),  new BigDecimal("28000000"), new BigDecimal("140000000"), new BigDecimal("5"),  new BigDecimal("140000000"), CostMethod.FIFO);

    // 14. Sale Orders
    SaleOrder so1 = getOrCreateSaleOrder(orgA, customer1, lead2, salesUser, "SO-2024-001",
        now.minus(20, ChronoUnit.DAYS), SaleOrderStatus.CONFIRMED, new BigDecimal("82500000"));
    seedOrderLine(so1, laptop,   new BigDecimal("2"),  new BigDecimal("35000000"), BigDecimal.ZERO,        new BigDecimal("70000000"));
    seedOrderLine(so1, keyboard, new BigDecimal("5"),  new BigDecimal("2500000"),  BigDecimal.ZERO,        new BigDecimal("12500000"));

    SaleOrder so2 = getOrCreateSaleOrder(orgA, customer2, null, salesUser, "SO-2024-002",
        now.minus(10, ChronoUnit.DAYS), SaleOrderStatus.CONFIRMED, new BigDecimal("70000000"));
    seedOrderLine(so2, desk,  new BigDecimal("5"),  new BigDecimal("8000000"),  new BigDecimal("5.00"), new BigDecimal("38000000"));
    seedOrderLine(so2, chair, new BigDecimal("5"),  new BigDecimal("6500000"),  new BigDecimal("5.00"), new BigDecimal("30875000"));

    SaleOrder so3 = getOrCreateSaleOrder(orgA, customer1, null, salesUser, "SO-2024-003",
        now.minus(3, ChronoUnit.DAYS), SaleOrderStatus.DRAFT, new BigDecimal("12000000"));
    seedOrderLine(so3, monitor, new BigDecimal("1"), new BigDecimal("12000000"), BigDecimal.ZERO, new BigDecimal("12000000"));

    // 15. Delivery Pickings (WH → Customer)
    StockPicking delivery1 = getOrCreatePicking(orgA, "WH/OUT/001", PickingType.OUTGOING,
        locMainA1, locMainCustomer, customer1, so1, StockPickingState.DONE,
        now.minus(18, ChronoUnit.DAYS), now.minus(18, ChronoUnit.DAYS));
    seedMove(delivery1, laptop,   locMainA1, locMainCustomer, lotLaptop1,  new BigDecimal("2"), new BigDecimal("2"), StockMoveState.DONE);
    seedMove(delivery1, keyboard, locMainA1, locMainCustomer, lotKeyboard1,new BigDecimal("5"), new BigDecimal("5"), StockMoveState.DONE);

    StockPicking delivery2 = getOrCreatePicking(orgA, "WH/OUT/002", PickingType.OUTGOING,
        locMainB1, locMainCustomer, customer2, so2, StockPickingState.DONE,
        now.minus(8, ChronoUnit.DAYS), now.minus(8, ChronoUnit.DAYS));
    seedMove(delivery2, desk,  locMainB1, locMainCustomer, lotDesk1,  new BigDecimal("5"), new BigDecimal("5"), StockMoveState.DONE);
    seedMove(delivery2, chair, locMainB1, locMainCustomer, lotChair1, new BigDecimal("5"), new BigDecimal("5"), StockMoveState.DONE);

    // Internal transfer (Kệ A1 → Kệ A2)
    StockPicking internalTransfer = getOrCreatePicking(orgA, "WH/INT/001", PickingType.INTERNAL,
        locMainA1, locMainA2, null, null, StockPickingState.DONE,
        now.minus(5, ChronoUnit.DAYS), now.minus(5, ChronoUnit.DAYS));
    seedMove(internalTransfer, monitor, locMainA1, locMainA2, lotMonitor1, new BigDecimal("5"), new BigDecimal("5"), StockMoveState.DONE);

    // Draft picking (chưa xử lý)
    StockPicking draftPicking = getOrCreatePicking(orgA, "WH/OUT/003", PickingType.OUTGOING,
        locMainA1, locMainCustomer, customer1, so3, StockPickingState.DRAFT,
        now.plus(2, ChronoUnit.DAYS), null);
    seedMove(draftPicking, monitor, locMainA1, locMainCustomer, lotMonitor1, new BigDecimal("1"), BigDecimal.ZERO, StockMoveState.DRAFT);

    // 16. Sale Invoices
    seedInvoiceIfAbsent(orgA, so1, customer1, "INV-2024-001",
        now.minus(18, ChronoUnit.DAYS), now.minus(18, ChronoUnit.DAYS).plus(30, ChronoUnit.DAYS),
        new BigDecimal("82500000"), new BigDecimal("82500000"), InvoiceStatus.PAID);
    seedInvoiceIfAbsent(orgA, so2, customer2, "INV-2024-002",
        now.minus(8, ChronoUnit.DAYS), now.minus(8, ChronoUnit.DAYS).plus(30, ChronoUnit.DAYS),
        new BigDecimal("70000000"), new BigDecimal("35000000"), InvoiceStatus.PARTIAL_PAID);

    // 17. Stock Inventory (kiểm kê kho)
    seedInventoryIfAbsent(orgA, locMainA1, "Kiểm kê kho tháng 5/2024",
        now.minus(7, ChronoUnit.DAYS), StockInventoryState.DONE,
        laptop, lotLaptop1, new BigDecimal("18"), new BigDecimal("18"),
        monitor, lotMonitor1, new BigDecimal("10"), new BigDecimal("10"));

    log.info("=== SampleDataGenerator: seed completed successfully ===");
  }

  // ── Organizations ─────────────────────────────────────────────────────────────

  private Organization getOrCreateOrganization(String name, String description,
      String address, String hotline, String taxCode) {
    return organizationRepository.findAll().stream()
        .filter(o -> o.getName().equals(name))
        .findFirst()
        .orElseGet(() -> {
          log.info("Creating organization: {}", name);
          return organizationRepository.save(Organization.builder()
              .name(name).description(description).address(address)
              .hotline(hotline).taxCode(taxCode).build());
        });
  }

  // ── Modules & Permissions ─────────────────────────────────────────────────────

  private ErpModule getOrCreateModule(String name, String code, String description) {
    return erpModuleRepository.findAll().stream()
        .filter(m -> m.getCode().equals(code))
        .findFirst()
        .orElseGet(() -> {
          log.info("Creating ERP module: {}", code);
          return erpModuleRepository.save(ErpModule.builder()
              .name(name).code(code).description(description).build());
        });
  }

  private Permission getOrCreatePermission(String name, String code, String description, ErpModule module) {
    return permissionRepository.findAll().stream()
        .filter(p -> p.getCode().equals(code))
        .findFirst()
        .orElseGet(() -> {
          log.info("Creating permission: {}", code);
          return permissionRepository.save(Permission.builder()
              .name(name).code(code).description(description).module(module).build());
        });
  }

  // ── Roles ─────────────────────────────────────────────────────────────────────

  private Role getOrCreateRole(String roleName, Organization organization, Set<Permission> permissions) {
    return roleRepository.findByNameAndOrganizationId(roleName, organization.getId())
        .map(existingRole -> {
          if (!existingRole.getPermissions().equals(permissions)) {
            existingRole.setPermissions(new HashSet<>(permissions));
            return roleRepository.save(existingRole);
          }
          return existingRole;
        })
        .orElseGet(() -> {
          log.info("Creating role '{}' for org '{}'", roleName, organization.getName());
          return roleRepository.save(Role.builder()
              .name(roleName).organization(organization)
              .permissions(new HashSet<>(permissions)).build());
        });
  }

  // ── Users ─────────────────────────────────────────────────────────────────────

  private User getOrCreateUser(String email, String rawPassword, String firstName, String lastName) {
    return userRepository.findByEmail(email)
        .orElseGet(() -> {
          log.info("Creating user: {}", email);
          return userRepository.save(User.builder()
              .email(email).password(passwordEncoder.encode(rawPassword))
              .firstName(firstName).lastName(lastName).build());
        });
  }

  // ── Partners ──────────────────────────────────────────────────────────────────

  private SalePartner getOrCreatePartner(Organization org, String code, String name,
      PartnerType type, String taxCode, String email, String phone, String address) {
    return salePartnerRepository.findByCodeAndOrganizationId(code, org.getId())
        .orElseGet(() -> {
          log.info("Creating partner: {}", code);
          return salePartnerRepository.save(SalePartner.builder()
              .organization(org).code(code).name(name).partnerType(type)
              .taxCode(taxCode).email(email).phone(phone).address(address)
              .status(PartnerStatus.ACTIVE).build());
        });
  }

  private void seedContactIfAbsent(SalePartner partner, String name, String email,
      String phone, String position, boolean isPrimary) {
    boolean exists = partnerContactRepository.findAll().stream()
        .anyMatch(c -> c.getPartner().getId().equals(partner.getId()) && c.getEmail().equals(email));
    if (!exists) {
      log.info("Creating contact '{}' for partner '{}'", name, partner.getName());
      partnerContactRepository.save(PartnerContact.builder()
          .partner(partner).name(name).email(email)
          .phone(phone).position(position).isPrimary(isPrimary).build());
    }
  }

  // ── Product Templates & Products ──────────────────────────────────────────────

  private ProductTemplate getOrCreateTemplate(Organization org, String name, String description,
      String category, String uom, CostMethod valuationMethod) {
    return productTemplateRepository.findAll().stream()
        .filter(t -> t.getOrganization().getId().equals(org.getId()) && t.getName().equals(name))
        .findFirst()
        .orElseGet(() -> {
          log.info("Creating product template: {}", name);
          return productTemplateRepository.save(ProductTemplate.builder()
              .organization(org).name(name).description(description)
              .category(category).uom(uom).valuationMethod(valuationMethod).build());
        });
  }

  private Product getOrCreateProduct(Organization org, ProductTemplate template, String sku,
      String name, String barcode, BigDecimal price, BigDecimal cost,
      BigDecimal weight, BigDecimal volume, String description, BigDecimal minStock) {
    return productRepository.findBySkuAndOrganizationId(sku, org.getId())
        .orElseGet(() -> {
          log.info("Creating product: {}", sku);
          return productRepository.save(Product.builder()
              .organization(org).productTemplate(template).sku(sku).name(name)
              .barcode(barcode).price(price).cost(cost).weight(weight)
              .volume(volume).description(description).minStock(minStock).build());
        });
  }

  // ── Sales Teams ───────────────────────────────────────────────────────────────

  private SalesTeam getOrCreateSalesTeam(Organization org, String name, User leader) {
    return salesTeamRepository.findAllByOrganizationId(org.getId()).stream()
        .filter(t -> t.getName().equals(name))
        .findFirst()
        .orElseGet(() -> {
          log.info("Creating sales team: {}", name);
          return salesTeamRepository.save(SalesTeam.builder()
              .organization(org).name(name).leader(leader).build());
        });
  }

  // ── CRM Stages ────────────────────────────────────────────────────────────────

  private CrmStage getOrCreateCrmStage(Organization org, String name, int sequence) {
    return crmStageRepository.findAllByOrganizationIdOrderBySequence(org.getId()).stream()
        .filter(s -> s.getName().equals(name))
        .findFirst()
        .orElseGet(() -> {
          log.info("Creating CRM stage: {}", name);
          return crmStageRepository.save(CrmStage.builder()
              .organization(org).name(name).sequence(sequence).build());
        });
  }

  // ── CRM Leads ─────────────────────────────────────────────────────────────────

  private CrmLead getOrCreateLead(Organization org, SalePartner partner, LeadType type,
      String name, BigDecimal expectedRevenue, BigDecimal probability,
      CrmStage stage, User salesperson, SalesTeam salesTeam) {
    return crmLeadRepository.findAll().stream()
        .filter(l -> l.getOrganization().getId().equals(org.getId()) && l.getName().equals(name))
        .findFirst()
        .orElseGet(() -> {
          log.info("Creating CRM lead: {}", name);
          return crmLeadRepository.save(CrmLead.builder()
              .organization(org).partner(partner).type(type).name(name)
              .expectedRevenue(expectedRevenue).probability(probability)
              .stage(stage).salesperson(salesperson).salesTeam(salesTeam).build());
        });
  }

  private void seedAppointmentIfAbsent(Organization org, CrmLead lead, AppointmentType type,
      String title, String description, Instant startTime, Instant endTime,
      String location, AppointmentStatus status) {
    boolean exists = crmAppointmentRepository.findAll().stream()
        .anyMatch(a -> a.getLead().getId().equals(lead.getId()) && a.getTitle().equals(title));
    if (!exists) {
      log.info("Creating appointment: {}", title);
      crmAppointmentRepository.save(CrmAppointment.builder()
          .organization(org).lead(lead).type(type).title(title)
          .description(description).startTime(startTime).endTime(endTime)
          .location(location).status(status).build());
    }
  }

  // ── Warehouses & Locations ────────────────────────────────────────────────────

  private Warehouse getOrCreateWarehouse(Organization org, String name, String code, String address) {
    return warehouseRepository.findByCodeAndOrganizationId(code, org.getId())
        .orElseGet(() -> {
          log.info("Creating warehouse: {}", code);
          return warehouseRepository.save(Warehouse.builder()
              .organization(org).name(name).code(code).address(address).build());
        });
  }

  private StockLocation getOrCreateLocation(Warehouse warehouse, StockLocation parent,
      String name, String code, LocationType type) {
    return stockLocationRepository.findByCodeAndWarehouseId(code, warehouse.getId())
        .orElseGet(() -> {
          log.info("Creating stock location: {}/{}", warehouse.getCode(), code);
          return stockLocationRepository.save(StockLocation.builder()
              .warehouse(warehouse).parent(parent).name(name)
              .code(code).locationType(type).build());
        });
  }

  // ── Lots ──────────────────────────────────────────────────────────────────────

  private StockLot getOrCreateLot(Organization org, Product product, String lotNumber, Instant expirationDate) {
    return stockLotRepository.findByLotNumberAndProductIdAndOrganizationId(
            lotNumber, product.getId(), org.getId())
        .orElseGet(() -> {
          log.info("Creating lot: {} for product {}", lotNumber, product.getSku());
          return stockLotRepository.save(StockLot.builder()
              .organization(org).product(product)
              .lotNumber(lotNumber).expirationDate(expirationDate).build());
        });
  }

  // ── Pickings & Moves ──────────────────────────────────────────────────────────

  private StockPicking getOrCreatePicking(Organization org, String name, PickingType type,
      StockLocation location, StockLocation locationDest,
      SalePartner partner, SaleOrder saleOrder,
      StockPickingState state, Instant scheduledDate, Instant dateDone) {
    return stockPickingRepository.findAll().stream()
        .filter(p -> p.getName().equals(name))
        .findFirst()
        .orElseGet(() -> {
          log.info("Creating stock picking: {}", name);
          return stockPickingRepository.save(StockPicking.builder()
              .organization(org).name(name).pickingType(type)
              .location(location).locationDest(locationDest)
              .partner(partner).saleOrder(saleOrder).state(state)
              .scheduledDate(scheduledDate).dateDone(dateDone).build());
        });
  }

  private StockMove seedMove(StockPicking picking, Product product,
      StockLocation location, StockLocation locationDest, StockLot lot,
      BigDecimal demandQty, BigDecimal doneQty, StockMoveState state) {
    // Always create moves; duplicates are avoided by only calling when picking is freshly created
    boolean exists = stockMoveRepository.findAll().stream()
        .anyMatch(m -> m.getPicking().getId().equals(picking.getId())
            && m.getProduct().getId().equals(product.getId())
            && (lot == null ? m.getLot() == null : lot.getId().equals(
                m.getLot() != null ? m.getLot().getId() : null)));
    if (exists) {
      return stockMoveRepository.findAll().stream()
          .filter(m -> m.getPicking().getId().equals(picking.getId())
              && m.getProduct().getId().equals(product.getId()))
          .findFirst().orElse(null);
    }
    log.info("Creating stock move: {} → {} for {}", location.getCode(), locationDest.getCode(), product.getSku());
    return stockMoveRepository.save(StockMove.builder()
        .picking(picking).product(product)
        .location(location).locationDest(locationDest)
        .lot(lot).productUomQty(demandQty).quantityDone(doneQty).state(state).build());
  }

  // ── Quants ────────────────────────────────────────────────────────────────────

  private void seedQuant(Product product, StockLocation location, StockLot lot, BigDecimal qty) {
    stockQuantRepository.findByProductIdLocationIdAndLotId(
            product.getId(), location.getId(), lot != null ? lot.getId() : null)
        .ifPresentOrElse(
            q -> { /* already exists */ },
            () -> {
              log.info("Creating stock quant: {} at {}", product.getSku(), location.getCode());
              stockQuantRepository.save(StockQuant.builder()
                  .product(product).location(location).lot(lot).quantity(qty).build());
            });
  }

  // ── Valuations ────────────────────────────────────────────────────────────────

  private void seedValuation(Product product, StockMove move, BigDecimal qty,
      BigDecimal unitValue, BigDecimal totalValue,
      BigDecimal remainingQty, BigDecimal remainingValue, CostMethod method) {
    boolean exists = stockValuationRepository.findAll().stream()
        .anyMatch(v -> v.getMove().getId().equals(move.getId()));
    if (!exists) {
      log.info("Creating stock valuation for move of product {}", product.getSku());
      stockValuationRepository.save(StockValuation.builder()
          .product(product).move(move).quantity(qty).unitValue(unitValue)
          .totalValue(totalValue).remainingQty(remainingQty)
          .remainingValue(remainingValue).method(method)
          .createdAt(Instant.now()).build());
    }
  }

  // ── Sale Orders ───────────────────────────────────────────────────────────────

  private SaleOrder getOrCreateSaleOrder(Organization org, SalePartner partner,
      CrmLead opportunity, User salesperson, String orderNumber,
      Instant orderDate, SaleOrderStatus status, BigDecimal totalAmount) {
    return saleOrderRepository.findByOrderNumberAndOrganizationId(orderNumber, org.getId())
        .orElseGet(() -> {
          log.info("Creating sale order: {}", orderNumber);
          return saleOrderRepository.save(SaleOrder.builder()
              .organization(org).partner(partner).opportunity(opportunity)
              .salesperson(salesperson).orderNumber(orderNumber)
              .orderDate(orderDate).status(status).totalAmount(totalAmount).build());
        });
  }

  private void seedOrderLine(SaleOrder order, Product product,
      BigDecimal quantity, BigDecimal unitPrice, BigDecimal discountPercent, BigDecimal subtotal) {
    boolean exists = saleOrderLineRepository.findAll().stream()
        .anyMatch(l -> l.getOrder().getId().equals(order.getId())
            && l.getProduct().getId().equals(product.getId()));
    if (!exists) {
      log.info("Creating order line: {} × {} for order {}", product.getSku(), quantity, order.getOrderNumber());
      saleOrderLineRepository.save(SaleOrderLine.builder()
          .order(order).product(product).quantity(quantity)
          .unitPrice(unitPrice).discountPercent(discountPercent).subtotal(subtotal).build());
    }
  }

  // ── Invoices ──────────────────────────────────────────────────────────────────

  private void seedInvoiceIfAbsent(Organization org, SaleOrder order, SalePartner partner,
      String invoiceNumber, Instant invoiceDate, Instant dueDate,
      BigDecimal totalAmount, BigDecimal paidAmount, InvoiceStatus status) {
    if (saleInvoiceRepository.findByInvoiceNumberAndOrganizationId(invoiceNumber, org.getId()).isEmpty()) {
      log.info("Creating invoice: {}", invoiceNumber);
      saleInvoiceRepository.save(SaleInvoice.builder()
          .organization(org).order(order).partner(partner)
          .invoiceNumber(invoiceNumber).invoiceDate(invoiceDate).dueDate(dueDate)
          .totalAmount(totalAmount).paidAmount(paidAmount).status(status).build());
    }
  }

  // ── Inventory ─────────────────────────────────────────────────────────────────

  private void seedInventoryIfAbsent(Organization org, StockLocation location,
      String name, Instant inventoryDate, StockInventoryState state,
      Product product1, StockLot lot1, BigDecimal theoretical1, BigDecimal checked1,
      Product product2, StockLot lot2, BigDecimal theoretical2, BigDecimal checked2) {
    boolean exists = stockInventoryRepository.findAll().stream()
        .anyMatch(i -> i.getName().equals(name) && i.getOrganization().getId().equals(org.getId()));
    if (!exists) {
      log.info("Creating stock inventory: {}", name);
      StockInventory inventory = stockInventoryRepository.save(StockInventory.builder()
          .organization(org).location(location).name(name)
          .inventoryDate(inventoryDate).state(state).build());

      stockInventoryLineRepository.save(StockInventoryLine.builder()
          .inventory(inventory).product(product1).location(location)
          .lot(lot1).theoreticalQty(theoretical1).checkedQty(checked1).build());
      stockInventoryLineRepository.save(StockInventoryLine.builder()
          .inventory(inventory).product(product2).location(location)
          .lot(lot2).theoreticalQty(theoretical2).checkedQty(checked2).build());
    }
  }
}
