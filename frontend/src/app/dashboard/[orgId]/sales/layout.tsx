'use client';

import { ReactNode, use, useState, useEffect } from 'react';
import Link from 'next/link';
import { FileText, BarChart2, Package, Users, Receipt, ShoppingCart, Percent, ChevronLeft, ChevronRight } from 'lucide-react';
import { cn } from '@/lib/utils';
import { usePathname } from 'next/navigation';
import { PermissionGuard } from '@/components/rbac/PermissionGuard';
import { PERMISSIONS } from '@/config/permissions';
import { APP_ROUTES } from '@/config/constants';

export default function SalesLayout({
  children,
  params,
}: {
  children: ReactNode;
  params: Promise<{ orgId: string }>;
}) {
  const pathname = usePathname();
  const { orgId } = use(params);

  const [isCollapsed, setIsCollapsed] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem('sidebar-collapsed-sales');
    if (stored === 'true') {
      setIsCollapsed(true);
    }
  }, []);

  const toggleSidebar = () => {
    const next = !isCollapsed;
    setIsCollapsed(next);
    localStorage.setItem('sidebar-collapsed-sales', String(next));
  };

  const docItems = [
    { name: 'Quotations', href: APP_ROUTES.SALES.QUOTATIONS(orgId), icon: FileText },
    { name: 'Orders', href: APP_ROUTES.SALES.ORDERS(orgId), icon: ShoppingCart },
    { name: 'Invoices', href: APP_ROUTES.SALES.INVOICES(orgId), icon: Receipt },
  ];

  const masterItems = [
    { name: 'Customers', href: APP_ROUTES.SALES.CUSTOMERS(orgId), icon: Users },
    { name: 'Products', href: APP_ROUTES.SALES.PRODUCTS(orgId), icon: Package },
    { name: 'Taxes', href: APP_ROUTES.SALES.TAXES(orgId), icon: Percent },
  ];

  const reportItems = [
    { name: 'Analytics', href: APP_ROUTES.SALES.ANALYTICS(orgId), icon: BarChart2 },
  ];

  const renderNavItem = (item: { name: string; href: string; icon: React.ElementType }) => {
    const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
    const Icon = item.icon;
    return (
      <Link
        key={item.name}
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
              <ShoppingCart className="w-4.5 h-4.5" />
            </div>
            <div className={cn(
              "overflow-hidden transition-all duration-300 ease-in-out flex flex-col",
              isCollapsed ? "w-0 opacity-0 scale-95 ml-0" : "w-auto opacity-100 scale-100 ml-3"
            )}>
              <h2 className="text-sm font-bold text-[#242424] truncate">Sales Operations</h2>
              <span className="text-[10px] text-[#898989] font-medium block uppercase tracking-wider">Commercial</span>
            </div>
          </div>

          {/* Navigation Links */}
          <div className="p-3 space-y-5">
            {/* Category: Documents */}
            <div>
              {!isCollapsed && (
                <span className="px-3 text-[10px] font-bold text-[#898989] tracking-wider uppercase block mb-1.5 transition-all duration-300">Documents</span>
              )}
              <div className="space-y-0.5">
                {docItems.map(renderNavItem)}
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

        {/* Sidebar Footer Widget - Monthly Goal Progress */}
        {!isCollapsed ? (
          <div className="p-4 border-t border-[#e0e0e0] bg-[#fafafa] select-none transition-all duration-300">
            <div className="flex justify-between items-center mb-1">
              <span className="text-[11px] font-semibold text-[#898989]">Monthly Sales Target</span>
              <span className="text-[11px] font-bold text-[#28a745]">75%</span>
            </div>
            <div className="w-full bg-[#e0e0e0] h-1.5 rounded-full overflow-hidden mb-2">
              <div className="bg-[#28a745] h-full w-[75%] rounded-full transition-all duration-500"></div>
            </div>
            <div className="flex justify-between items-center text-[10px] text-[#898989]">
              <span className="font-mono">$750M / $1.0B</span>
              <span className="bg-[#28a745]/10 text-[#28a745] px-1.5 py-0.5 rounded font-bold">On track</span>
            </div>
          </div>
        ) : (
          <div className="p-3 border-t border-[#e0e0e0] bg-[#fafafa] flex justify-center text-[#28a745]" title="Sales Target: 75%">
            <Percent className="w-4.5 h-4.5 animate-pulse" />
          </div>
        )}
      </aside>

      {/* Main Module Content */}
      <div className="flex-1 min-w-0 h-full overflow-hidden flex flex-col bg-[#f8f8f8]">
        <PermissionGuard
          permission={PERMISSIONS.SALES.READ}
          fallback={
            <div className="flex-1 flex items-center justify-center text-red-500 font-medium bg-white">
              Access Denied. You do not have permission to access Sales.
            </div>
          }
        >
          <div className="flex-1 overflow-auto">
            {children}
          </div>
        </PermissionGuard>
      </div>
    </div>
  );
}
