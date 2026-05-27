'use client';

import { ReactNode, use } from 'react';
import Link from 'next/link';
import { FileText, BarChart2, Package, Users, Receipt } from 'lucide-react';
import { cn } from '@/lib/utils';
import { usePathname } from 'next/navigation';

export default function SalesLayout({
  children,
  params,
}: {
  children: ReactNode;
  params: Promise<{ orgId: string }>;
}) {
  const pathname = usePathname();
  const { orgId } = use(params);
  const basePath = `/dashboard/${orgId}/sales`;

  const navItems = [
    { name: 'Quotations', href: `${basePath}/quotations`, icon: FileText },
    { name: 'Invoices', href: `${basePath}/invoices`, icon: Receipt },
    { name: 'Customers', href: `${basePath}/customers`, icon: Users },
    { name: 'Products', href: `${basePath}/products`, icon: Package },
    { name: 'Analytics', href: `${basePath}/analytics`, icon: BarChart2 },
  ];

  return (
    <div className="flex flex-col h-full bg-[#f8f8f8]">
      {/* Odoo Style Sub-Navigation */}
      <div className="bg-white border-b border-[#e0e0e0] px-6 flex items-center h-12 shrink-0">
        <div className="flex items-center space-x-1">
          {navItems.map((item) => {
            const isActive = pathname === item.href;
            const Icon = item.icon;
            return (
              <Link
                key={item.name}
                href={item.href}
                className={cn(
                  "flex items-center space-x-2 px-3 py-1.5 rounded-md text-sm transition-colors",
                  isActive 
                    ? "bg-[#f0f4ff] text-[#0066cc] font-semibold" 
                    : "text-[#242424] hover:bg-[#f8f8f8]"
                )}
              >
                <Icon className="w-4 h-4" />
                <span>{item.name}</span>
              </Link>
            );
          })}
        </div>
      </div>
      
      {/* Main Module Content */}
      <div className="flex-1 overflow-auto">
        {children}
      </div>
    </div>
  );
}
