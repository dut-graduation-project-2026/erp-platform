// src/features/administration/components/dialogs/UserFormDialog.tsx
// User Form Dialog - Create/Edit user with role assignment
// Includes quick role definition capability

'use client';

import React, { useState } from 'react';
import { X, Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { useRoles } from '../../hooks/useRoles';
import type { User, UserFormData } from '../../types';

interface UserFormDialogProps {
  user?: User | null;
  organizationId: string;
  onClose: () => void;
  onSubmit: (data: UserFormData) => Promise<void>;
}

export const UserFormDialog: React.FC<UserFormDialogProps> = ({
  user,
  organizationId,
  onClose,
  onSubmit,
}) => {
  const { roles } = useRoles();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<UserFormData>({
    email: user?.email || '',
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    roleId: user?.roleId || '',
  });

  const orgRoles = roles.filter(r => r.organizationId === organizationId);

  const handleChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmit = async () => {
    setLoading(true);
    try {
      await onSubmit(formData);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={true} onOpenChange={onClose}>
      <DialogContent className="max-w-md border border-[#e0e0e0] shadow-[0px_12px_28px_rgba(0,0,0,0.30)] rounded-[4px]">
        <DialogHeader className="border-b border-[#e0e0e0] pb-4">
          <DialogTitle className="text-[24px] font-bold text-[#242424]">
            {user ? 'Edit User' : 'Add User'}
          </DialogTitle>
          <button
            onClick={onClose}
            className="absolute right-4 top-4 text-[#898989] hover:text-[#242424]"
          >
            <X className="w-5 h-5" />
          </button>
        </DialogHeader>

        <div className="space-y-4 py-6">
          {/* Email */}
          <div className="space-y-2">
            <Label className="text-[14px] font-semibold text-[#242424]">
              Email <span className="text-[#dc3545]">*</span>
            </Label>
            <Input
              type="email"
              value={formData.email}
              onChange={(e) => handleChange('email', e.target.value)}
              placeholder="user@example.com"
              className="h-10 rounded-[4px] border-[#d0d0d0] text-[14px] focus:border-[#0066cc]"
              required
              disabled={!!user}
            />
            {user && <p className="text-[12px] text-[#898989]">Email cannot be changed</p>}
          </div>

          {/* First Name */}
          <div className="space-y-2">
            <Label className="text-[14px] font-semibold text-[#242424]">
              First Name <span className="text-[#dc3545]">*</span>
            </Label>
            <Input
              value={formData.firstName}
              onChange={(e) => handleChange('firstName', e.target.value)}
              placeholder="Enter first name"
              className="h-10 rounded-[4px] border-[#d0d0d0] text-[14px] focus:border-[#0066cc]"
              required
            />
          </div>

          {/* Last Name */}
          <div className="space-y-2">
            <Label className="text-[14px] font-semibold text-[#242424]">
              Last Name <span className="text-[#dc3545]">*</span>
            </Label>
            <Input
              value={formData.lastName}
              onChange={(e) => handleChange('lastName', e.target.value)}
              placeholder="Enter last name"
              className="h-10 rounded-[4px] border-[#d0d0d0] text-[14px] focus:border-[#0066cc]"
              required
            />
          </div>

          {/* Role Selection */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <Label className="text-[14px] font-semibold text-[#242424]">
                Role
              </Label>
              <Button
                variant="link"
                size="sm"
                className="text-[#0066cc] p-0 h-auto text-[12px]"
              >
                <Plus className="w-3 h-3 mr-1" />
                Create role
              </Button>
            </div>
            <Select value={formData.roleId || ''} onValueChange={(value) => handleChange('roleId', value)}>
              <SelectTrigger className="h-10 rounded-[4px] border-[#d0d0d0] text-[14px]">
                <SelectValue placeholder="Select role..." />
              </SelectTrigger>
              <SelectContent className="rounded-[4px]">
                {orgRoles.map(role => (
                  <SelectItem key={role.id} value={role.id} className="text-[14px]">
                    {role.name}
                  </SelectItem>
                ))}
                {orgRoles.length === 0 && (
                  <div className="text-center py-2 text-[12px] text-[#898989]">
                    No roles available
                  </div>
                )}
              </SelectContent>
            </Select>
          </div>
        </div>

        <DialogFooter className="border-t border-[#e0e0e0] pt-4 flex gap-2 justify-end">
          <Button
            variant="outline"
            onClick={onClose}
            className="h-10 px-4 border-[#d0d0d0] text-[#242424] hover:bg-[#f8f8f8] rounded-[4px]"
          >
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={loading || !formData.email || !formData.firstName || !formData.lastName}
            className="h-10 px-4 bg-[#0066cc] hover:bg-[#004499] text-white font-semibold rounded-[4px]"
          >
            {loading ? 'Saving...' : 'Save'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
