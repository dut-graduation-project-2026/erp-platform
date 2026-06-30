// src/features/administration/components/dialogs/OrganizationFormDialog.tsx
// Organization Form Dialog - Create/Edit organization
// Follows DESIGN.md: Dialog, form styling, typography

'use client';

import React, { useState } from 'react';
import { X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import type { Organization, OrganizationFormData } from '../../types';

interface OrganizationFormDialogProps {
  organization?: Organization | null;
  onClose: () => void;
  onSubmit: (data: OrganizationFormData) => Promise<void>;
}

export const OrganizationFormDialog: React.FC<OrganizationFormDialogProps> = ({
  organization,
  onClose,
  onSubmit,
}) => {
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<OrganizationFormData>({
    name: organization?.name || '',
    code: organization?.code || '',
    logo: organization?.logo || '',
    contactEmail: organization?.contactEmail || '',
    contactPhone: organization?.contactPhone || '',
    address: organization?.address || '',
  });

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
            {organization ? 'Edit Organization' : 'Add Organization'}
          </DialogTitle>
          <button
            onClick={onClose}
            className="absolute right-4 top-4 text-[#898989] hover:text-[#242424]"
          >
            <X className="w-5 h-5" />
          </button>
        </DialogHeader>

        <div className="space-y-4 py-6">
          {/* Organization Name */}
          <div className="space-y-2">
            <Label className="text-[14px] font-semibold text-[#242424]">
              Organization Name <span className="text-[#dc3545]">*</span>
            </Label>
            <Input
              value={formData.name}
              onChange={(e) => handleChange('name', e.target.value)}
              placeholder="Enter organization name"
              className="h-10 rounded-[4px] border-[#d0d0d0] text-[14px] focus:border-[#0066cc]"
              required
            />
          </div>

          {/* Organization Code */}
          <div className="space-y-2">
            <Label className="text-[14px] font-semibold text-[#242424]">
              Code <span className="text-[#dc3545]">*</span>
            </Label>
            <Input
              value={formData.code}
              onChange={(e) => handleChange('code', e.target.value)}
              placeholder="e.g., ORG001"
              className="h-10 rounded-[4px] border-[#d0d0d0] text-[14px] focus:border-[#0066cc]"
              required
            />
          </div>

          {/* Contact Email */}
          <div className="space-y-2">
            <Label className="text-[14px] font-semibold text-[#242424]">
              Contact Email
            </Label>
            <Input
              type="email"
              value={formData.contactEmail}
              onChange={(e) => handleChange('contactEmail', e.target.value)}
              placeholder="contact@organization.com"
              className="h-10 rounded-[4px] border-[#d0d0d0] text-[14px] focus:border-[#0066cc]"
            />
          </div>

          {/* Contact Phone */}
          <div className="space-y-2">
            <Label className="text-[14px] font-semibold text-[#242424]">
              Phone Number
            </Label>
            <Input
              value={formData.contactPhone}
              onChange={(e) => handleChange('contactPhone', e.target.value)}
              placeholder="+84 XX XXXX XXXX"
              className="h-10 rounded-[4px] border-[#d0d0d0] text-[14px] focus:border-[#0066cc]"
            />
          </div>

          {/* Address */}
          <div className="space-y-2">
            <Label className="text-[14px] font-semibold text-[#242424]">
              Address
            </Label>
            <Textarea
              value={formData.address}
              onChange={(e) => handleChange('address', e.target.value)}
              placeholder="Enter organization address"
              className="h-24 rounded-[4px] border-[#d0d0d0] text-[14px] focus:border-[#0066cc]"
            />
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
            disabled={loading || !formData.name || !formData.code}
            className="h-10 px-4 bg-[#0066cc] hover:bg-[#004499] text-white font-semibold rounded-[4px]"
          >
            {loading ? 'Saving...' : 'Save'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
