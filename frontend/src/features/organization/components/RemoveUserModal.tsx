import React, { useState } from 'react';
import { X, AlertTriangle } from 'lucide-react';
import { organizationMemberService } from '../services/organizationMemberService';

interface RemoveUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  orgId: string;
  user: {
    id: string;
    name: string;
  } | null;
  onSuccess: () => void;
}

export const RemoveUserModal: React.FC<RemoveUserModalProps> = ({ isOpen, onClose, orgId, user, onSuccess }) => {
  const [isRemoving, setIsRemoving] = useState(false);

  if (!isOpen || !user) return null;

  const handleRemove = async () => {
    if (!user) return;
    try {
      setIsRemoving(true);
      await organizationMemberService.removeMember(orgId, user.id);
      onSuccess();
      onClose();
    } catch (err) {
      console.error(err);
    } finally {
      setIsRemoving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center bg-black/50 font-['Segoe_UI',_sans-serif]">
      <div className="bg-white rounded-[8px] shadow-[0px_12px_28px_rgba(0,0,0,0.30)] w-full max-w-[400px] overflow-hidden">
        <div className="flex items-center justify-between px-6 py-4 border-b border-[#e0e0e0]">
          <h2 className="text-[20px] font-semibold text-[#dc3545] flex items-center gap-2">
            <AlertTriangle className="w-5 h-5" />
            Remove User
          </h2>
          <button onClick={onClose} className="text-[#898989] hover:text-[#242424] transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6">
          <p className="text-[14px] text-[#242424] leading-[1.5]">
            Are you sure you want to remove <span className="font-semibold">{user.name}</span> from the organization? 
            They will lose all access to this organization&apos;s resources immediately.
          </p>
          <p className="text-[13px] text-[#898989] mt-3">
            This action cannot be undone.
          </p>
        </div>

        <div className="flex items-center justify-end gap-2 px-6 py-4 border-t border-[#e0e0e0] bg-[#f8f8f8]">
          <button
            type="button"
            onClick={onClose}
            disabled={isRemoving}
            className="px-4 py-[8px] bg-white border border-[#d0d0d0] rounded-[4px] text-[#242424] text-[14px] font-semibold hover:bg-[#e0e0e0] transition-colors disabled:opacity-50"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={handleRemove}
            disabled={isRemoving}
            className="px-4 py-[8px] bg-[#dc3545] text-white rounded-[4px] text-[14px] font-semibold hover:bg-[#c82333] transition-colors disabled:bg-[#e0e0e0] disabled:text-[#999999]"
          >
            {isRemoving ? 'Removing...' : 'Remove User'}
          </button>
        </div>
      </div>
    </div>
  );
};
