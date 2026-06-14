'use client';

import { useState } from 'react';
import { useOrganizations } from '@/features/organization/hooks/useOrganizations';
import OrgCard from '@/features/organization/components/OrgCard';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import { Building2, Plus } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/use-auth-store';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { LogOut } from 'lucide-react';

/**
 * SelectOrgPage - Figma Design 2:5212
 * Executive Architect ERP - Organization Gate
 *
 * Features:
 * - Header with branding and user info
 * - Gradient title "Select Your Organization"
 * - 3-column grid of organization cards
 * - Cards with avatar, status badge, role info
 * - Contact admin section for new memberships
 * - Footer with company values
 */
export default function SelectOrgPage() {
  const { organizations, loading, error, createOrganization } = useOrganizations();
  const { user, logout, setCurrentOrgId, setPermissions } = useAuthStore();
  const router = useRouter();
  
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    taxCode: '',
    address: '',
    hotline: '',
    description: ''
  });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSelectOrg = async (orgId: string) => {
    try {
      // Gọi API lấy danh sách quyền chi tiết cho user trong tổ chức này
      const { fetchMyPermissionsApi } = await import('@/features/auth/services/authService');
      const permissions = await fetchMyPermissionsApi(orgId);
      
      // Lưu vào store
      setPermissions(permissions);
      setCurrentOrgId(orgId);
      
      // Đồng bộ cookies để Next.js Middleware có thể đọc được (Middleware không đọc được localStorage của Zustand)
      const orgIds = organizations.map(org => org.id).join(',');
      document.cookie = `currentOrgId=${orgId}; path=/; max-age=86400; secure; SameSite=Lax`;
      document.cookie = `userOrgIds=${orgIds}; path=/; max-age=86400; secure; SameSite=Lax`;
      
      router.push(`/dashboard/${orgId}`);
    } catch (err) {
      console.error("Failed to fetch permissions:", err);
      // Bạn có thể show toast lỗi ở đây nếu cần
    }
  };

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    const success = await createOrganization(formData);
    setIsSubmitting(false);
    if (success) {
      setIsDialogOpen(false);
      setFormData({ name: '', taxCode: '', address: '', hotline: '', description: '' });
    }
  };

  const handleLogout = async () => {
    try {
      const { logoutApi } = await import('@/features/auth/services/authService');
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

  const getInitials = (firstName?: string, lastName?: string) => {
    const f = firstName?.[0] || '';
    const l = lastName?.[0] || '';
    return (f + l).toUpperCase() || 'U';
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-[#fcf9f8] to-[#fcf9f8]">
        <div className="text-center">
          <Building2 className="h-12 w-12 text-[#004e9f] mx-auto mb-4" />
          <p className="text-[#414753] text-[18px] font-medium">Loading organizations...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-[#fcf9f8] to-[#fcf9f8]">
        <Card className="w-full max-w-md border-[#c1c6d5] shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
          <CardContent className="p-6">
            <p className="text-[#414753] mb-4">{error}</p>
            <Button onClick={() => window.location.reload()} className="bg-[#004e9f] hover:bg-[#003d7a] text-white">
              Try Again
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#fcf9f8] to-[#fcf9f8] relative">
      {/* Header */}
      <div className="backdrop-blur-sm bg-[#fcf9f8]/80 border-b border-[#c1c6d5]/30 sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-8 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <h1 className="text-[20px] font-bold text-[#1b1c1c]">Executive Architect ERP</h1>
              <div className="h-6 w-px bg-[#c1c6d5]/30" />
              <span className="text-[14px] font-medium text-[#5f5e5e]">Organization Gate</span>
            </div>
            <div className="flex items-center gap-4">
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <button className="flex items-center gap-3 text-left hover:bg-black/5 p-1 px-2 rounded-md transition-colors focus:outline-none">
                    <div className="text-right hidden sm:block">
                      <div className="text-[12px] font-bold text-[#1b1c1c]">
                        {user ? `${user.firstName} ${user.lastName}` : 'Guest'}
                      </div>
                      <div className="text-[10px] text-[#5f5e5e] max-w-[150px] truncate">
                        {user?.email || 'Not logged in'}
                      </div>
                    </div>
                    <Avatar className="w-9 h-9 border border-[#c1c6d5]/50">
                      <AvatarImage src={user?.avatarUrl} />
                      <AvatarFallback className="bg-[#004e9f] text-white text-sm font-bold flex items-center justify-center">
                        {getInitials(user?.firstName, user?.lastName)}
                      </AvatarFallback>
                    </Avatar>
                  </button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="w-56 bg-white border border-[#c1c6d5]/50 shadow-md rounded-md p-1">
                  <DropdownMenuLabel className="px-2 py-1.5 text-sm">
                    <div className="font-semibold text-foreground">
                      {user ? `${user.firstName} ${user.lastName}` : 'Guest'}
                    </div>
                    <div className="text-xs text-muted-foreground truncate">{user?.email}</div>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator className="my-1 bg-[#c1c6d5]/20" />
                  <DropdownMenuItem 
                    onClick={handleLogout} 
                    className="flex items-center px-2 py-1.5 text-sm text-red-600 rounded-sm hover:bg-red-50 focus:bg-red-50 focus:text-red-600 cursor-pointer transition-colors"
                  >
                    <LogOut className="mr-2 h-4 w-4" />
                    Logout
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </div>
        </div>
      </div>

      {/* Notification Banner */}
      {/* <div className="absolute left-8 bottom-64 z-10 max-w-sm">
        <Card className="border-l-4 border-[#af4900] bg-[#af4900] text-[#ffe3d6] shadow-lg">
          <CardContent className="p-4">
            <div className="flex items-start gap-3">
              <div className="w-6 h-6 text-[#ffe3d6] mt-0.5">⚠️</div>
              <div>
                <div className="text-[12px] font-bold uppercase tracking-wide mb-1">
                  ARCHITECTURE SUGGESTION
                </div>
                <div className="text-[11px] leading-relaxed">
                  Nexus Global has pending audit reports that require your immediate attention based on your Admin role.
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div> */}

      {/* Main Content */}
      <div className="flex items-center justify-center min-h-[calc(100vh-200px)] px-24 py-24 relative z-0">
        <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
          <div className="max-w-6xl w-full">
          {/* Title Section */}
          <div className="text-center mb-16">
            <h1 className="text-[48px] font-black text-[#1b1c1c] mb-4">
              Select Your{' '}
              <span className="bg-gradient-to-r from-[#004e9f] to-[#06c] bg-clip-text text-transparent">
                Organization
              </span>
            </h1>
            <p className="text-[18px] font-medium text-[#414753] max-w-2xl mx-auto leading-relaxed mb-6">
              Welcome back. Access your designated operational workspace to continue managing your enterprise architecture and supply chain logistics.
            </p>

            {organizations.length > 0 && (
              <DialogTrigger asChild>
                <Button className="bg-[#004e9f] hover:bg-[#003d7a] text-white">
                  <Plus className="h-4 w-4 mr-2" />
                  Create New Organization
                </Button>
              </DialogTrigger>
            )}
          </div>

          {/* Organizations Grid */}
          {organizations.length > 0 ? (
            <div className="grid grid-cols-3 gap-8 mb-16">
              {organizations.map((org) => (
                <OrgCard
                  key={org.id}
                  id={org.id}
                  name={org.name}
                  description={org.description}
                  role={org.role}
                  onSelect={handleSelectOrg}
                />
              ))}
            </div>
          ) : (
            <div className="mb-16">
              <Card className="border-2 border-dashed border-[#c1c6d5]/30 bg-[#fcf9f8]/50 p-12 text-center">
                <div className="max-w-md mx-auto">
                  <Building2 className="h-12 w-12 text-[#414753] mx-auto mb-4" />
                  <p className="text-[16px] font-medium text-[#414753] mb-6">
                    You haven&apos;t joined any organization yet. Please create a new one to get started.
                  </p>
                  
                  <DialogTrigger asChild>
                    <Button className="bg-[#004e9f] hover:bg-[#003d7a] text-white">
                      <Plus className="h-4 w-4 mr-2" />
                      Create Organization
                    </Button>
                  </DialogTrigger>
                </div>
              </Card>
            </div>
          )}

          {/* Shared Dialog Content for Create Organization */}
          <DialogContent className="sm:max-w-[500px]">
            <form onSubmit={handleCreateSubmit}>
              <DialogHeader>
                <DialogTitle>Create New Organization</DialogTitle>
                <DialogDescription>
                  Set up a new operational workspace. You will automatically become the System Admin.
                </DialogDescription>
              </DialogHeader>
              <div className="grid gap-4 py-4">
                <div className="space-y-2">
                  <Label htmlFor="name">Organization Name <span className="text-red-500">*</span></Label>
                  <Input 
                    id="name" 
                    placeholder="Enter organization name" 
                    value={formData.name}
                    onChange={(e) => setFormData({...formData, name: e.target.value})}
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="taxCode">Tax Code <span className="text-red-500">*</span></Label>
                  <Input 
                    id="taxCode" 
                    placeholder="Enter tax code (must be unique)" 
                    value={formData.taxCode}
                    onChange={(e) => setFormData({...formData, taxCode: e.target.value})}
                    required
                  />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="hotline">Hotline</Label>
                    <Input 
                      id="hotline" 
                      placeholder="e.g. 0912345678" 
                      value={formData.hotline}
                      onChange={(e) => setFormData({...formData, hotline: e.target.value.replace(/\D/g, '')})}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="address">Address</Label>
                    <Input 
                      id="address" 
                      placeholder="City, Country" 
                      value={formData.address}
                      onChange={(e) => setFormData({...formData, address: e.target.value})}
                    />
                  </div>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="description">Description</Label>
                  <Textarea 
                    id="description" 
                    placeholder="Brief description about your organization" 
                    value={formData.description}
                    onChange={(e) => setFormData({...formData, description: e.target.value})}
                    rows={3}
                  />
                </div>
              </div>
              <div className="flex justify-end gap-3 pt-4 border-t">
                <Button type="button" variant="outline" onClick={() => setIsDialogOpen(false)}>
                  Cancel
                </Button>
                <Button type="submit" disabled={isSubmitting} className="bg-[#004e9f] hover:bg-[#003d7a] text-white">
                  {isSubmitting ? 'Creating...' : 'Create Organization'}
                </Button>
              </div>
            </form>
          </DialogContent>
        </div>
        </Dialog>
      </div>

      {/* Footer */}
      <div className="bg-white border-t border-[#c1c6d5]/30 px-16 py-10">
        <div className="max-w-6xl mx-auto flex items-center justify-between">
          <div>
            <div className="flex items-center gap-4 mb-2">
              <span className="text-[12px] font-medium text-[#5f5e5e] uppercase tracking-wider">PRECISION</span>
              <div className="w-1 h-1 bg-[#c1c6d5] rounded-full" />
              <span className="text-[12px] font-medium text-[#5f5e5e] uppercase tracking-wider">PERFORMANCE</span>
              <div className="w-1 h-1 bg-[#c1c6d5] rounded-full" />
              <span className="text-[12px] font-medium text-[#5f5e5e] uppercase tracking-wider">SOVEREIGNTY</span>
            </div>
            <p className="text-[10px] text-[#414753]">
              © 2024 Executive Architectural Standard. All rights reserved. Tier-1 ERP Security Protocol Active.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
