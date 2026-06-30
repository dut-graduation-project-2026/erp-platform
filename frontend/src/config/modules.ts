import { 
  Megaphone, 
  UserCheck, 
  MessageSquare, 
  Calendar, 
  Clock, 
  Timer, 
  Users, 
  CalendarOff,
  ShoppingCart,
  Package,
  Building2,
  Shield,
  AppWindow,
  Target
} from 'lucide-react';
import { PERMISSIONS, AppPermission } from './permissions';
import { ERP_MODULE_CODES } from './erp-modules';

export interface ModuleConfig {
  id: string;
  name: string;
  icon: React.ElementType;
  route: string;
  permission: AppPermission | string;
  bgColor: string;
}

export const APP_MODULES: ModuleConfig[] = [
  // -------------------------
  // ADMINISTRATIVE MODULES (Matches backend seed)
  // -------------------------
  {
    id: ERP_MODULE_CODES.ERP_MODULE,
    name: 'ERP Modules',
    icon: AppWindow,
    route: '/erp-modules',
    permission: PERMISSIONS.ERP_MODULE.READ,
    bgColor: 'bg-[#004e9f]',
  },
  {
    id: ERP_MODULE_CODES.ROLES,
    name: 'Roles & Permissions',
    icon: Shield,
    route: '/roles',
    permission: PERMISSIONS.ROLES.READ,
    bgColor: 'bg-red-800',
  },
  {
    id: ERP_MODULE_CODES.USERS,
    name: 'Users',
    icon: Users,
    route: '/users',
    permission: PERMISSIONS.USERS.WRITE,
    bgColor: 'bg-teal-700',
  },
  {
    id: ERP_MODULE_CODES.ORGANIZATIONS,
    name: 'Organizations',
    icon: Building2,
    route: '/organizations',
    permission: PERMISSIONS.ORGANIZATIONS.READ,
    bgColor: 'bg-slate-800',
  },

  // -------------------------
  // BUSINESS MODULES (From original design)
  // -------------------------
  {
    id: ERP_MODULE_CODES.ANNOUNCEMENT,
    name: 'Announcement',
    icon: Megaphone,
    route: '/announcement',
    permission: PERMISSIONS.ANNOUNCEMENT.ACCESS,
    bgColor: 'bg-blue-600',
  },
  {
    id: ERP_MODULE_CODES.ATTENDANCE_MACHINE,
    name: 'Attendance Machine',
    icon: UserCheck,
    route: '/attendance-machine',
    permission: PERMISSIONS.ATTENDANCE.ACCESS,
    bgColor: 'bg-slate-400',
  },
  {
    id: ERP_MODULE_CODES.DISCUSS,
    name: 'Discuss',
    icon: MessageSquare,
    route: '/discuss',
    permission: PERMISSIONS.DISCUSS.ACCESS,
    bgColor: 'bg-pink-500',
  },
  {
    id: ERP_MODULE_CODES.CALENDAR,
    name: 'Calendar',
    icon: Calendar,
    route: '/calendar',
    permission: PERMISSIONS.CALENDAR.ACCESS,
    bgColor: 'bg-yellow-500',
  },
  {
    id: ERP_MODULE_CODES.WORKING_ATTENDANCE,
    name: 'Working Attendance',
    icon: Clock,
    route: '/working-attendance',
    permission: PERMISSIONS.WORKING_ATTENDANCE.ACCESS,
    bgColor: 'bg-slate-600',
  },
  {
    id: ERP_MODULE_CODES.OVER_TIME,
    name: 'Over Time',
    icon: Timer,
    route: '/over-time',
    permission: PERMISSIONS.OVER_TIME.ACCESS,
    bgColor: 'bg-red-700',
  },
  {
    id: ERP_MODULE_CODES.EMPLOYEES,
    name: 'Employees',
    icon: Users,
    route: '/employees',
    permission: PERMISSIONS.EMPLOYEES.ACCESS,
    bgColor: 'bg-emerald-600',
  },
  {
    id: ERP_MODULE_CODES.TIME_OFF,
    name: 'Time Off',
    icon: CalendarOff,
    route: '/time-off',
    permission: PERMISSIONS.TIME_OFF.ACCESS,
    bgColor: 'bg-yellow-600',
  },
  {
    id: ERP_MODULE_CODES.CRM,
    name: 'CRM',
    icon: Target,
    route: '/crm',
    permission: PERMISSIONS.CRM.READ,
    bgColor: 'bg-indigo-600',
  },
  {
    id: ERP_MODULE_CODES.SALES,
    name: 'Sales',
    icon: ShoppingCart,
    route: '/sales',
    permission: PERMISSIONS.SALES.READ,
    bgColor: 'bg-emerald-700',
  },
  {
    id: ERP_MODULE_CODES.INVENTORY,
    name: 'Inventory',
    icon: Package,
    route: '/inventory',
    permission: PERMISSIONS.INVENTORY.ACCESS,
    bgColor: 'bg-amber-600',
  }
];
