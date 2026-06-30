'use client';

import { ReactNode, use, useState, useEffect } from 'react';
import Link from 'next/link';
import { Target, BarChart2, Calendar, Users, ChevronLeft, ChevronRight } from 'lucide-react';
import { cn } from '@/lib/utils';
import { usePathname } from 'next/navigation';
import { PermissionGuard } from '@/components/rbac/PermissionGuard';
import { AccessDenied } from '@/components/shared/AccessDenied';
import { PERMISSIONS } from '@/config/permissions';
import { APP_ROUTES } from '@/config/constants';

export default function CrmLayout({
  children,
  params,
}: {
  children: ReactNode;
  params: Promise<{ orgId: string }>;
}) {
  const pathname = usePathname();
  const { orgId } = use(params);
  const basePath = APP_ROUTES.CRM.DASHBOARD(orgId);

  const [isCollapsed, setIsCollapsed] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem('sidebar-collapsed-crm');
    if (stored === 'true') {
      setIsCollapsed(true);
    }
  }, []);

  const toggleSidebar = () => {
    const next = !isCollapsed;
    setIsCollapsed(next);
    localStorage.setItem('sidebar-collapsed-crm', String(next));
  };

  const crmItems = [
    { name: 'Pipeline', href: basePath, icon: Target, permission: PERMISSIONS.CRM.READ },
    { name: 'Sales Teams', href: `${basePath}/teams`, icon: Calendar, permission: PERMISSIONS.SALE_TEAMS.READ },
  ];

  const masterItems = [
    { name: 'Customers', href: `${basePath}/customers`, icon: Users, permission: PERMISSIONS.PARTNERS.READ },
  ];

  const reportItems = [
    { name: 'Reporting', href: `${basePath}/reporting`, icon: BarChart2, permission: PERMISSIONS.CRM.READ },
  ];

  const allNavItems = [...crmItems, ...masterItems, ...reportItems];
  const activeNavItem = allNavItems
    .filter(item => item.href !== basePath)
    .find(item => pathname.startsWith(item.href));
  const requiredPermission = activeNavItem ? activeNavItem.permission : PERMISSIONS.CRM.READ;

  const renderNavItem = (item: { name: string; href: string; icon: React.ElementType; permission: string }) => {
    const isActive = item.href === basePath 
      ? pathname === basePath || pathname.startsWith(basePath + '/leads/')
      : pathname === item.href || pathname.startsWith(item.href + '/');
    const Icon = item.icon;
    return (
      <PermissionGuard key={item.name} permission={item.permission}>
        <Link
          href={item.href}
          title={isCollapsed ? item.name : undefined}
          className={cn(
            "flex items-center rounded-[6px] text-[13px] font-[500] transition-all duration-300 select-none",
            isCollapsed ? "justify-center p-2.5 mx-auto w-10 h-10" : "px-3 py-2.5 mx-1",
            isActive 
              ? "bg-[#f0f4ff] text-[#0066cc] font-[600]" 
              : "text-[#4a4a4a] hover:bg-[#f5f5f5] hover:text-[#242424]"
          )}
        >
          <Icon className={cn("w-4 h-4 shrink-0 transition-transform duration-300", isActive ? "text-[#0066cc]" : "text-[#898989]", isCollapsed && "scale-110")} />
          <span className={cn(
            "transition-all duration-300 ease-in-out truncate origin-left",
            isCollapsed ? "w-0 opacity-0 scale-95 pointer-events-none ml-0 hidden" : "w-auto opacity-100 scale-100 ml-3"
          )}>
            {item.name}
          </span>
        </Link>
      </PermissionGuard>
    );
  };

  return (
    <div className="flex h-[calc(100vh-4rem)] bg-[#f8f8f8] overflow-hidden">
      {/* Left Sidebar */}
      <aside className={cn(
        "bg-white border-r border-[#e0e0e0] flex flex-col justify-between shrink-0 h-full font-['Segoe_UI'] relative transition-all duration-300 ease-in-out",
        isCollapsed ? "w-[64px]" : "w-[240px]"
      )}>
        {/* Collapse Toggle Button */}
        <button
          onClick={toggleSidebar}
          className="absolute -right-3 top-6 w-6 h-6 bg-white border border-[#e0e0e0] hover:border-[#0066cc] rounded-full flex items-center justify-center shadow-[0_2px_5px_rgba(0,0,0,0.1)] hover:shadow-[0_2px_8px_rgba(0,0,0,0.15)] text-[#898989] hover:text-[#0066cc] z-30 transition-all cursor-pointer"
        >
          {isCollapsed ? <ChevronRight className="w-3.5 h-3.5" /> : <ChevronLeft className="w-3.5 h-3.5" />}
        </button>

        <div className="flex flex-col flex-1 overflow-y-auto">
          {/* Header section */}
          <div className="p-4 border-b border-[#e0e0e0] flex items-center bg-white sticky top-0 z-10">
            <div className="w-8 h-8 bg-[#f0f4ff] text-[#0066cc] rounded-lg flex items-center justify-center shrink-0">
              <Target className="w-4.5 h-4.5" />
            </div>
            <div className={cn(
              "overflow-hidden transition-all duration-300 ease-in-out flex flex-col",
              isCollapsed ? "w-0 opacity-0 scale-95 ml-0" : "w-auto opacity-100 scale-100 ml-3"
            )}>
              <h2 className="text-sm font-bold text-[#242424] truncate">CRM & Leads</h2>
              <span className="text-[10px] text-[#898989] font-medium block uppercase tracking-wider">Relationship</span>
            </div>
          </div>

          {/* Navigation Links */}
          <div className="p-3 space-y-5">
            {/* Category: CRM Operations */}
            <div>
              {!isCollapsed && (
                <span className="px-3 text-[10px] font-bold text-[#898989] tracking-wider uppercase block mb-1.5 transition-all duration-300">CRM</span>
              )}
              <div className="space-y-0.5">
                {crmItems.map(renderNavItem)}
              </div>
            </div>

            {/* Category: Master Data */}
            <div>
              {!isCollapsed ? (
                <span className="px-3 text-[10px] font-bold text-[#898989] tracking-wider uppercase block mb-1.5 transition-all duration-300">Master Data</span>
              ) : (
                <div className="h-[1px] bg-[#e0e0e0] my-3 mx-2" />
              )}
              <div className="space-y-0.5">
                {masterItems.map(renderNavItem)}
              </div>
            </div>

            {/* Category: Reporting */}
            <div>
              {!isCollapsed ? (
                <span className="px-3 text-[10px] font-bold text-[#898989] tracking-wider uppercase block mb-1.5 transition-all duration-300">Reporting</span>
              ) : (
                <div className="h-[1px] bg-[#e0e0e0] my-3 mx-2" />
              )}
              <div className="space-y-0.5">
                {reportItems.map(renderNavItem)}
              </div>
            </div>
          </div>
        </div>
      </aside>

      {/* Main Module Content */}
      <div className="flex-1 min-w-0 h-full overflow-hidden flex flex-col bg-[#f8f8f8] relative">
        <PermissionGuard 
          permission={requiredPermission}
          fallback={<AccessDenied title="Access denied" description="Your account does not have permission to access this feature." />}
        >
          <div className="flex-1 overflow-auto bg-white">
            {children}
          </div>
        </PermissionGuard>
      </div>
    </div>
  );
}
