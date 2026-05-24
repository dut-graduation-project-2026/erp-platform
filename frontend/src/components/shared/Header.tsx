'use client';

import { logoutApi } from '@/features/auth/services/authService';

import React from 'react';
import { usePathname } from 'next/navigation';
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
import { Badge } from '@/components/ui/badge';
import {
  Search,
  Bell,
  Settings,
  LogOut,
  User,
  Building2,
  ChevronDown,
  LayoutGrid
} from 'lucide-react';
import { useAuthStore } from '@/store/use-auth-store';
import { APP_MODULES } from '@/config/modules';
import Link from 'next/link';

interface HeaderProps {
  className?: string;
}

export function Header({ className }: HeaderProps) {
  const pathname = usePathname();
  const { user, organizations, currentOrgId, logout } = useAuthStore();
  const currentOrg = organizations.find(org => org.id === currentOrgId);

  // Determine if we are on the App Launcher or inside a module
  const segments = pathname.split('/').filter(Boolean);
  const isAppLauncher = segments.length === 2 && segments[0] === 'dashboard'; // e.g. /dashboard/orgId
  const currentModuleRoute = segments.length > 2 ? '/' + segments[2] : null; // e.g. /attendance-machine
  
  const currentModule = currentModuleRoute 
    ? APP_MODULES.find(m => m.route === currentModuleRoute) 
    : null;

  const handleLogout = async () => {
    try {
      // 1. Clear Zustand state
      logout();
      
      // 2. Clear frontend cookies
      document.cookie = 'currentOrgId=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
      document.cookie = 'userOrgIds=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
      
      // 3. Optional: Call backend to clear HttpOnly tokens
      await logoutApi();
      
      // 4. Redirect to login and reload to clear any residual memory
      window.location.href = '/login';
    } catch (error) {
      console.error('Logout error:', error);
      window.location.href = '/login';
    }
  };

  return (
    <header className={cn(
      "h-16 bg-gradient-to-r from-blue-600 to-blue-800 border-b border-blue-700 flex items-center justify-between px-6 text-white",
      className
    )}>
      {/* Left Section - Odoo Style Navigation */}
      <div className="flex items-center space-x-4">
        {!isAppLauncher && currentOrgId && (
          <Link href={`/dashboard/${currentOrgId}`} className="flex items-center text-white hover:bg-white/10 p-2 rounded-md transition-colors">
            <LayoutGrid className="h-5 w-5" />
          </Link>
        )}
        
        {currentModule ? (
          <div className="flex items-center space-x-4">
            <span className="text-[18px] font-semibold text-white">{currentModule.name}</span>
            {/* Odoo Sub-navigation would go here, mapped by module */}
            <div className="hidden md:flex space-x-1 ml-4 border-l border-white/20 pl-4">
               {/* Placeholder for sub-tabs */}
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
              {/* Mock organizations - replace with real data */}
              <DropdownMenuItem>Tech Corp</DropdownMenuItem>
              <DropdownMenuItem>Manufacturing Inc</DropdownMenuItem>
              <DropdownMenuItem>Retail Solutions</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        )}

        {/* Notifications */}
        <Button variant="ghost" size="sm" className="text-white hover:bg-white/10 relative">
          <Bell className="h-5 w-5" />
          <Badge
            variant="destructive"
            className="absolute -top-1 -right-1 h-5 w-5 flex items-center justify-center p-0 text-xs"
          >
            3
          </Badge>
        </Button>

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
          <DropdownMenuContent align="end" className="w-56">
            <DropdownMenuLabel>
              {user?.firstName} {user?.lastName}
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem>
              <User className="mr-2 h-4 w-4" />
              Profile
            </DropdownMenuItem>
            <DropdownMenuItem>
              <Settings className="mr-2 h-4 w-4" />
              Settings
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem onClick={handleLogout} className="text-red-600">
              <LogOut className="mr-2 h-4 w-4" />
              Logout
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  );
}
