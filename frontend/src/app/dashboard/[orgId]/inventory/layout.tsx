'use client';

import { ReactNode, use } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { 
  FileText, 
  Warehouse, 
  Package, 
  Activity, 
  ArrowUpRight, 
  DollarSign, 
  Boxes 
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { PermissionGuard } from '@/components/rbac/PermissionGuard';
import { PERMISSIONS } from '@/config/permissions';

export default function InventoryLayout({
  children,
  params,
}: {
  children: ReactNode;
  params: Promise<{ orgId: string }>;
}) {
  const pathname = usePathname();
  const { orgId } = use(params);
  const basePath = `/dashboard/${orgId}/inventory`;

  const operationsItems = [
    { name: 'Stock Moves', href: `${basePath}/documents`, icon: FileText },
    { name: 'Replenishments', href: `${basePath}/replenishments`, icon: Boxes },
  ];

  const masterDataItems = [
    { name: 'Warehouses', href: `${basePath}/warehouses`, icon: Warehouse },
    { name: 'Stock Balances', href: `${basePath}/balances`, icon: Activity },
  ];

  const reportItems = [
    { name: 'COGS & Valuations', href: `${basePath}/valuations`, icon: DollarSign },
  ];

  const renderNavItem = (item: { name: string; href: string; icon: React.ElementType }) => {
    const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
    const Icon = item.icon;
    return (
      <Link
        key={item.name}
        href={item.href}
        className={cn(
          "flex items-center space-x-3 px-3 py-2.5 rounded-[6px] text-[13px] font-[500] transition-all duration-200 select-none",
          isActive 
            ? "bg-[#f0f4ff] text-[#0066cc] font-[600]" 
            : "text-[#4a4a4a] hover:bg-[#f5f5f5] hover:text-[#242424]"
        )}
      >
        <Icon className={cn("w-4 h-4 shrink-0", isActive ? "text-[#0066cc]" : "text-[#898989]")} />
        <span>{item.name}</span>
      </Link>
    );
  };

  return (
    <div className="flex h-[calc(100vh-4rem)] bg-[#f8f8f8] overflow-hidden">
      {/* Left Sidebar */}
      <aside className="w-[240px] bg-white border-r border-[#e0e0e0] flex flex-col justify-between shrink-0 h-full font-['Segoe_UI']">
        <div className="flex flex-col flex-1 overflow-y-auto">
          {/* Header section */}
          <div className="p-4 border-b border-[#e0e0e0] flex items-center space-x-3 bg-white sticky top-0 z-10">
            <div className="w-8 h-8 bg-[#fff3e0] text-[#ff9800] rounded-lg flex items-center justify-center shrink-0">
              <Package className="w-4.5 h-4.5" />
            </div>
            <div className="overflow-hidden">
              <h2 className="text-sm font-bold text-[#242424] truncate">Logistics & Stock</h2>
              <span className="text-[10px] text-[#898989] font-medium block uppercase tracking-wider">Inventory</span>
            </div>
          </div>

          {/* Navigation Links */}
          <div className="p-3 space-y-5">
            {/* Category: Operations */}
            <div>
              <span className="px-3 text-[10px] font-bold text-[#898989] tracking-wider uppercase block mb-1.5">Operations</span>
              <div className="space-y-0.5">
                {operationsItems.map(renderNavItem)}
              </div>
            </div>

            {/* Category: Master Data */}
            <div>
              <span className="px-3 text-[10px] font-bold text-[#898989] tracking-wider uppercase block mb-1.5">Master Data</span>
              <div className="space-y-0.5">
                {masterDataItems.map(renderNavItem)}
              </div>
            </div>

            {/* Category: Reporting */}
            <div>
              <span className="px-3 text-[10px] font-bold text-[#898989] tracking-wider uppercase block mb-1.5">Reporting</span>
              <div className="space-y-0.5">
                {reportItems.map(renderNavItem)}
              </div>
            </div>
          </div>
        </div>

        {/* Sidebar Footer Widget - Quick Alert */}
        <div className="p-4 border-t border-[#e0e0e0] bg-[#fafafa] select-none">
          <div className="flex justify-between items-center mb-1">
            <span className="text-[11px] font-semibold text-[#898989]">Warehouse Health</span>
            <span className="text-[11px] font-bold text-[#28a745]">100%</span>
          </div>
          <div className="w-full bg-[#e0e0e0] h-1.5 rounded-full overflow-hidden mb-2">
            <div className="bg-[#28a745] h-full w-full rounded-full"></div>
          </div>
          <div className="flex justify-between items-center text-[10px] text-[#898989]">
            <span>System Audited</span>
            <span className="bg-[#28a745]/10 text-[#28a745] px-1.5 py-0.5 rounded font-bold">Secure</span>
          </div>
        </div>
      </aside>

      {/* Main Module Content */}
      <div className="flex-1 min-w-0 h-full overflow-hidden flex flex-col bg-[#f8f8f8]">
        <PermissionGuard 
          permission={PERMISSIONS.INVENTORY.ACCESS}
          fallback={
            <div className="flex-1 flex items-center justify-center text-red-500 font-medium bg-white">
              Access Denied. You do not have permission to access Inventory.
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
