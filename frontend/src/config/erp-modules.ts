/**
 * Constants for ERP Module Codes.
 * These codes must match exactly with the 'code' field of ErpModule entity in the backend.
 */
export const ERP_MODULE_CODES = {
  ERP_MODULE: "erp_module_v1",
  ROLES: "roles",
  USERS: "users",
  ORGANIZATIONS: "organizations",
  
  // Example future modules to match frontend's APP_MODULES
  ANNOUNCEMENT: "announcement",
  ATTENDANCE_MACHINE: "attendance_machine",
  DISCUSS: "discuss",
  CALENDAR: "calendar",
  WORKING_ATTENDANCE: "working_attendance",
  OVER_TIME: "over_time",
  EMPLOYEES: "employees",
  TIME_OFF: "time_off",
  SALES: "partners",
  CRM: "leads",
  INVENTORY: "warehouses",
} as const;

export type ErpModuleCode = typeof ERP_MODULE_CODES[keyof typeof ERP_MODULE_CODES];
