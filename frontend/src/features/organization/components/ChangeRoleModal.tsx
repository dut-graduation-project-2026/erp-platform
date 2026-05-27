import React, { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { useRoles } from '../hooks/useRoles';
import { organizationMemberService } from '../services/organizationMemberService';

interface ChangeRoleModalProps {
  isOpen: boolean;
  onClose: () => void;
  orgId: string;
  user: {
    id: string;
    name: string;
    roleId?: string;
  } | null;
  onSuccess: () => void;
}

export const ChangeRoleModal: React.FC<ChangeRoleModalProps> = ({ isOpen, onClose, orgId, user, onSuccess }) => {
  const [roleId, setRoleId] = useState('');
  const [isUpdating, setIsUpdating] = useState(false);
  
  const { roles, loading: loadingRoles } = useRoles(orgId);

  useEffect(() => {
    if (user?.roleId) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setRoleId(user.roleId);
    } else {
      setRoleId('');
    }
  }, [user]);

  if (!isOpen || !user) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!roleId || !user) return;

    try {
      setIsUpdating(true);
      await organizationMemberService.updateMemberRoles(orgId, user.id, [roleId]);
      onSuccess();
      onClose();
    } catch (err: unknown) {
      console.error(err);
    } finally {
      setIsUpdating(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center bg-black/50 font-['Segoe_UI',_sans-serif]">
      <div className="bg-white rounded-[8px] shadow-[0px_12px_28px_rgba(0,0,0,0.30)] w-full max-w-[480px] overflow-hidden">
        <div className="flex items-center justify-between px-6 py-4 border-b border-[#e0e0e0]">
          <h2 className="text-[24px] font-semibold text-[#242424]">Change User Role</h2>
          <button onClick={onClose} className="text-[#898989] hover:text-[#242424] transition-colors">
            <X className="w-6 h-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 flex flex-col gap-4">
          <div className="flex flex-col gap-1">
            <span className="text-[14px] text-[#898989]">User</span>
            <span className="text-[14px] font-medium text-[#242424]">{user.name}</span>
          </div>

          <div className="flex flex-col gap-1">
            <label htmlFor="change-role" className="text-[14px] font-semibold text-[#242424]">
              New Role <span className="text-[#dc3545]">*</span>
            </label>
            <select
              id="change-role"
              required
              value={roleId}
              onChange={(e) => setRoleId(e.target.value)}
              disabled={loadingRoles || isUpdating}
              className="px-3 py-2 border border-[#d0d0d0] rounded-[4px] text-[14px] text-[#242424] bg-white focus:outline-none focus:border-[#0066cc] disabled:bg-[#e0e0e0] disabled:text-[#999999]"
            >
              <option value="" disabled>Select a role</option>
              {roles.map((role) => (
                <option key={role.id} value={role.id}>
                  {role.name}
                </option>
              ))}
            </select>
            {loadingRoles && <p className="text-[12px] text-[#0066cc] mt-1">Loading roles...</p>}
          </div>

          <div className="flex items-center justify-end gap-2 mt-4 pt-4 border-t border-[#e0e0e0]">
            <button
              type="button"
              onClick={onClose}
              disabled={isUpdating}
              className="px-4 py-[10px] bg-white border border-[#d0d0d0] rounded-[4px] text-[#242424] text-[14px] font-semibold hover:bg-[#f8f8f8] transition-colors disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isUpdating || !roleId}
              className="px-4 py-[10px] bg-[#0066cc] text-white rounded-[4px] text-[14px] font-semibold hover:bg-[#004499] transition-colors disabled:bg-[#e0e0e0] disabled:text-[#999999]"
            >
              {isUpdating ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
