'use client';

import { logoutApi } from '@/features/auth/services/authService';

import React, { useState } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import {
  Search,
  Settings,
  LogOut,
  User,
  Building2,
  ChevronDown,
  LayoutGrid
} from 'lucide-react';
import { useAuthStore } from '@/store/use-auth-store';
import { APP_MODULES } from '@/config/modules';
import { useErpModules } from '@/features/organization/hooks/useErpModules';
import { PermissionGuard } from '@/components/rbac/PermissionGuard';
import { NotificationPopover } from '@/features/notification/components/NotificationPopover';
import { useNotificationStore } from '@/features/notification/store/use-notification-store';

interface HeaderProps {
  className?: string;
}

export function Header({ className }: HeaderProps) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, organizations, currentOrgId, logout } = useAuthStore();
  const currentOrg = organizations.find(org => org.id === currentOrgId);

  const [isAppLauncherOpen, setIsAppLauncherOpen] = useState(false);

  const { initializeSSE, cleanupSSE } = useNotificationStore();

  React.useEffect(() => {
    if (user) {
      initializeSSE();
    }
    return () => {
      cleanupSSE();
    };
  }, [user, initializeSSE, cleanupSSE]);
  React.useEffect(() => {
    setIsAppLauncherOpen(false);
  }, [pathname]);

  // Fetch modules for the App Launcher Overlay
  const { modules: backendModules } = useErpModules(currentOrgId || '');

  // Determine if we are on the App Launcher or inside a module
  const segments = pathname.split('/').filter(Boolean);
  const isAppLauncher = segments.length === 2 && segments[0] === 'dashboard'; // e.g. /dashboard/orgId
  const currentModuleRoute = segments.length > 2 ? '/' + segments[2] : null; // e.g. /attendance-machine
  
  const currentModule = currentModuleRoute 
    ? APP_MODULES.find(m => m.route === currentModuleRoute) 
    : null;

  const handleLogout = async () => {
    try {
      logout();
      document.cookie = 'currentOrgId=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT; secure';
      document.cookie = 'userOrgIds=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT; secure';
      document.cookie = 'clientSession=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT; secure';
      await logoutApi();
      window.location.href = '/login';
    } catch (error) {
      console.error('Logout error:', error);
      window.location.href = '/login';
    }
  };

  // Filter APP_MODULES config based on what backend returned
  const accessibleModules = React.useMemo(() => {
    if (!backendModules) return [];
    return APP_MODULES.filter(configModule => 
      backendModules.some(backendModule => {
        const backendCode = backendModule.code.toLowerCase().replace(/_/g, '-');
        const configCode = configModule.id.toLowerCase().replace(/_/g, '-');
        return backendCode === configCode;
      })
    );
  }, [backendModules]);

  const handleModuleClick = (route: string) => {
    router.push(`/dashboard/${currentOrgId}${route}`);
  };

  return (
    <>
      <header className={cn(
        "h-16 bg-gradient-to-r from-blue-600 to-blue-800 border-b border-blue-700 flex items-center justify-between px-6 text-white relative z-50",
        className
      )}>
        {/* Left Section - Odoo Style Navigation */}
        <div className="flex items-center space-x-4">
          {!isAppLauncher && currentOrgId && (
            <button 
              onClick={() => setIsAppLauncherOpen(!isAppLauncherOpen)}
              className={cn(
                "flex items-center text-white hover:bg-white/10 p-2 rounded-md transition-colors",
                isAppLauncherOpen && "bg-white/20"
              )}
            >
              <LayoutGrid className="h-5 w-5" />
            </button>
          )}
          
          {currentModule && !isAppLauncherOpen ? (
            <div className="flex items-center space-x-4">
              <span className="text-[18px] font-semibold text-white">{currentModule.name}</span>
              {/* Odoo Sub-navigation would go here, mapped by module */}
              <div className="hidden md:flex space-x-1 ml-4 border-l border-white/20 pl-4">
                 <Button variant="ghost" className="text-white hover:bg-white/10 h-8 text-sm px-3">
                   Overview
                 </Button>
                 <Button variant="ghost" className="text-white/70 hover:text-white hover:bg-white/10 h-8 text-sm px-3">
                   Configuration
                 </Button>
              </div>
            </div>
          ) : (
            <div className="text-[18px] font-semibold text-white">App Launcher</div>
          )}
        </div>

        {/* Center Section - Search */}
        <div className="flex-1 max-w-md mx-8">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
            <Input
              placeholder="Search organizations, users, roles..."
              className="pl-10 bg-white/10 border-white/20 text-white placeholder:text-white/70 focus:bg-white focus:text-gray-900"
            />
          </div>
        </div>

        {/* Right Section - Actions */}
        <div className="flex items-center space-x-4">
          {/* Organization Switcher */}
          {currentOrg && (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="text-white hover:bg-white/10">
                  <Building2 className="h-4 w-4 mr-2" />
                  <span className="hidden sm:inline">{currentOrg.name}</span>
                  <ChevronDown className="h-4 w-4 ml-2" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-56">
                <DropdownMenuLabel>Switch Organization</DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem>Tech Corp</DropdownMenuItem>
                <DropdownMenuItem>Manufacturing Inc</DropdownMenuItem>
                <DropdownMenuItem>Retail Solutions</DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          )}

          {/* Notifications */}
          <NotificationPopover />

          {/* User Menu */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" className="text-white hover:bg-white/10 p-1">
                <Avatar className="h-8 w-8">
                  <AvatarImage src={user?.avatarUrl} />
                  <AvatarFallback className="bg-white/20 text-white">
                    {user?.firstName?.[0]}{user?.lastName?.[0]}
                  </AvatarFallback>
                </Avatar>
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-64 p-1.5 shadow-[0px_2px_8px_rgba(0,0,0,0.15)] border border-[#e0e0e0] rounded-[4px] font-['Segoe_UI'] bg-white">
              <DropdownMenuLabel className="flex flex-col py-2.5 px-3">
                <span className="text-[14px] font-bold text-[#242424] leading-tight">
                  {user?.firstName} {user?.lastName}
                </span>
                <span className="text-[11px] font-normal text-[#898989] mt-1 break-all">
                  {user?.email}
                </span>
              </DropdownMenuLabel>
              <DropdownMenuSeparator className="bg-[#f5f5f5] my-1" />
              <DropdownMenuItem 
                onClick={() => currentOrgId && router.push(`/dashboard/${currentOrgId}/profile`)}
                className="flex items-center text-[13px] text-[#242424] hover:bg-[#f0f4ff] hover:text-[#0066cc] cursor-pointer py-2 px-3 rounded-[2px] focus:bg-[#f0f4ff] focus:text-[#0066cc] transition-colors"
              >
                <User className="mr-2.5 h-4 w-4 text-[#898989]" />
                Profile
              </DropdownMenuItem>
              <DropdownMenuItem 
                onClick={() => currentOrgId && router.push(`/dashboard/${currentOrgId}/profile?tab=settings`)}
                className="flex items-center text-[13px] text-[#242424] hover:bg-[#f0f4ff] hover:text-[#0066cc] cursor-pointer py-2 px-3 rounded-[2px] focus:bg-[#f0f4ff] focus:text-[#0066cc] transition-colors"
              >
                <Settings className="mr-2.5 h-4 w-4 text-[#898989]" />
                Settings
              </DropdownMenuItem>
              <DropdownMenuSeparator className="bg-[#f5f5f5] my-1" />
              <DropdownMenuItem 
                onClick={handleLogout} 
                className="flex items-center text-[13px] text-[#dc3545] hover:bg-[#fff5f5] hover:text-[#dc3545] cursor-pointer py-2 px-3 rounded-[2px] focus:bg-[#fff5f5] focus:text-[#dc3545] transition-colors"
              >
                <LogOut className="mr-2.5 h-4 w-4 text-[#dc3545]" />
                Logout
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </header>

      {/* App Launcher Full Screen Overlay */}
      {isAppLauncherOpen && (
        <div className="fixed inset-0 top-16 bg-white z-40 overflow-auto animate-in fade-in duration-200">
          <div className="min-h-full p-8 font-['Segoe_UI',_sans-serif]">
            <div className="flex flex-wrap gap-[32px] justify-center max-w-5xl mx-auto mt-12">
              {accessibleModules.map((module) => {
                const Icon = module.icon;
                
                return (
                  <PermissionGuard key={module.id} permission={module.permission}>
                    <button 
                      onClick={() => handleModuleClick(module.route)}
                      className="group flex flex-col items-center w-[120px] hover:translate-y-[-2px] transition-transform duration-200"
                    >
                      {/* App Icon Box */}
                      <div 
                        className={`w-[80px] h-[80px] rounded-[16px] flex items-center justify-center text-white shadow-[0px_2px_8px_rgba(0,0,0,0.15)] group-hover:shadow-[0px_8px_20px_rgba(0,0,0,0.25)] transition-shadow duration-200 ${module.bgColor}`}
                      >
                        <Icon className="w-[36px] h-[36px]" strokeWidth={1.5} />
                      </div>
                      
                      {/* App Name */}
                      <span className="mt-[16px] text-[14px] font-[500] text-[#242424] text-center leading-[1.4] group-hover:text-[#0066cc] transition-colors">
                        {module.name}
                      </span>
                    </button>
                  </PermissionGuard>
                );
              })}
            </div>
          </div>
        </div>
      )}
    </>
  );
}
