'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { ScrollArea } from '@/components/ui/scroll-area';
import {
  LayoutDashboard,
  ShoppingCart,
  Package,
  Users,
  Building2,
  Settings,
  ChevronLeft,
  ChevronRight,
  Link as LinkIcon,
  BarChart3
} from 'lucide-react';

interface SidebarProps {
  className?: string;
}

const menuItems = [
  {
    title: 'Dashboard',
    href: '/dashboard',
    icon: LayoutDashboard,
    description: 'Overview and analytics'
  },
  {
    title: 'Sales & CRM',
    href: '/sales',
    icon: ShoppingCart,
    description: 'Customer management and sales'
  },
  {
    title: 'Inventory',
    href: '/inventory',
    icon: Package,
    description: 'Stock and warehouse management'
  },
  {
    title: 'Administration',
    href: '/administration',
    icon: Users,
    description: 'System administration',
    children: [
      { title: 'Organizations', href: '/administration/organizations' },
      { title: 'Roles & Permissions', href: '/administration/roles' },
      { title: 'Users', href: '/administration/users' }
    ]
  },
  {
    title: 'Blockchain Audit',
    href: '/blockchain',
    icon: LinkIcon,
    description: 'Traceability and audit trail'
  },
  {
    title: 'Reports',
    href: '/reports',
    icon: BarChart3,
    description: 'Analytics and reporting'
  },
  {
    title: 'Settings',
    href: '/settings',
    icon: Settings,
    description: 'System configuration'
  }
];

export function Sidebar({ className }: SidebarProps) {
  const [collapsed, setCollapsed] = useState(false);
  const [expandedItems, setExpandedItems] = useState<string[]>(['Administration']);
  const pathname = usePathname();

  const toggleExpanded = (title: string) => {
    setExpandedItems(prev =>
      prev.includes(title)
        ? prev.filter(item => item !== title)
        : [...prev, title]
    );
  };

  const isActive = (href: string) => {
    if (href === '/administration') {
      return pathname.startsWith('/administration');
    }
    return pathname === href;
  };

  const isChildActive = (children?: { href: string }[]) => {
    return children?.some(child => pathname === child.href);
  };

  return (
    <div className={cn(
      "flex flex-col h-full bg-gray-50 border-r border-gray-200 transition-all duration-300",
      collapsed ? "w-16" : "w-64",
      className
    )}>
      {/* Header */}
      <div className="flex items-center justify-between p-4 border-b border-gray-200">
        {!collapsed && (
          <div className="flex items-center space-x-2">
            <Building2 className="h-8 w-8 text-blue-600" />
            <span className="font-bold text-lg text-gray-900">ERP Platform</span>
          </div>
        )}
        <Button
          variant="ghost"
          size="sm"
          onClick={() => setCollapsed(!collapsed)}
          className="h-8 w-8 p-0"
        >
          {collapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
        </Button>
      </div>

      {/* Navigation */}
      <ScrollArea className="flex-1 px-3 py-4">
        <nav className="space-y-2">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const active = isActive(item.href);
            const childActive = isChildActive(item.children);
            const isExpanded = expandedItems.includes(item.title);

            return (
              <div key={item.title}>
                <Link href={item.href}>
                  <Button
                    variant={active || childActive ? "secondary" : "ghost"}
                    className={cn(
                      "w-full justify-start h-10 px-3",
                      collapsed ? "px-2" : "px-3",
                      (active || childActive) && "bg-blue-50 text-blue-700 hover:bg-blue-100"
                    )}
                    onClick={(e) => {
                      if (item.children) {
                        e.preventDefault();
                        toggleExpanded(item.title);
                      }
                    }}
                  >
                    <Icon className={cn("h-5 w-5", collapsed ? "mr-0" : "mr-3")} />
                    {!collapsed && (
                      <>
                        <span className="flex-1 text-left">{item.title}</span>
                        {item.children && (
                          <ChevronRight
                            className={cn(
                              "h-4 w-4 transition-transform",
                              isExpanded && "rotate-90"
                            )}
                          />
                        )}
                      </>
                    )}
                  </Button>
                </Link>

                {/* Submenu */}
                {item.children && isExpanded && !collapsed && (
                  <div className="ml-6 mt-1 space-y-1">
                    {item.children.map((child) => (
                      <Link key={child.href} href={child.href}>
                        <Button
                          variant={pathname === child.href ? "secondary" : "ghost"}
                          size="sm"
                          className={cn(
                            "w-full justify-start h-8 px-3 text-sm",
                            pathname === child.href && "bg-blue-50 text-blue-700 hover:bg-blue-100"
                          )}
                        >
                          {child.title}
                        </Button>
                      </Link>
                    ))}
                  </div>
                )}
              </div>
            );
          })}
        </nav>
      </ScrollArea>

      {/* Footer */}
      <div className="p-4 border-t border-gray-200">
        {!collapsed && (
          <div className="text-xs text-gray-500 text-center">
            ERP Platform v1.0.0
            <br />
            <a href="#" className="text-blue-600 hover:underline">Help & Support</a>
          </div>
        )}
      </div>
    </div>
  );
}
