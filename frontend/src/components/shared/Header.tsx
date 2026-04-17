'use client';

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
  ChevronDown
} from 'lucide-react';
import { useAuthStore } from '@/store/use-auth-store';

interface HeaderProps {
  className?: string;
}

export function Header({ className }: HeaderProps) {
  const pathname = usePathname();
  const { user, organizations, currentOrgId, logout } = useAuthStore();
  const currentOrg = organizations.find(org => org.id === currentOrgId);

  // Generate breadcrumbs from pathname
  const generateBreadcrumbs = () => {
    const segments = pathname.split('/').filter(Boolean);
    const breadcrumbs = [{ label: 'Home', href: '/dashboard' }];

    segments.forEach((segment, index) => {
      const href = '/' + segments.slice(0, index + 1).join('/');
      let label = segment.charAt(0).toUpperCase() + segment.slice(1);

      // Custom labels for specific routes
      if (segment === 'administration') label = 'Administration';
      if (segment === 'organizations') label = 'Organizations';
      if (segment === 'roles') label = 'Roles & Permissions';
      if (segment === 'users') label = 'Users';

      breadcrumbs.push({ label, href });
    });

    return breadcrumbs;
  };

  const breadcrumbs = generateBreadcrumbs();

  const handleLogout = () => {
    logout();
    // Redirect will be handled by auth context
  };

  return (
    <header className={cn(
      "h-16 bg-gradient-to-r from-blue-600 to-blue-800 border-b border-blue-700 flex items-center justify-between px-6 text-white",
      className
    )}>
      {/* Left Section - Breadcrumbs */}
      <div className="flex items-center space-x-4">
        <nav className="flex items-center space-x-2 text-sm">
          {breadcrumbs.map((crumb, index) => (
            <React.Fragment key={crumb.href}>
              {index > 0 && <span className="text-blue-300">/</span>}
              <a
                href={crumb.href}
                className={cn(
                  "hover:text-blue-200 transition-colors",
                  index === breadcrumbs.length - 1
                    ? "text-white font-medium"
                    : "text-blue-200"
                )}
              >
                {crumb.label}
              </a>
            </React.Fragment>
          ))}
        </nav>
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
