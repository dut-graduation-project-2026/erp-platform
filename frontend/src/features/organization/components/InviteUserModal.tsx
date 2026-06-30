import React, { useState } from 'react';
import { X } from 'lucide-react';
import { useRoles } from '../hooks/useRoles';
import { useInvitations } from '../hooks/useInvitations';

interface InviteUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  orgId: string;
  onSuccess: () => void;
}

export const InviteUserModal: React.FC<InviteUserModalProps> = ({ isOpen, onClose, orgId, onSuccess }) => {
  const [email, setEmail] = useState('');
  const [roleId, setRoleId] = useState('');
  
  const { roles, loading: loadingRoles } = useRoles(orgId);
  const { inviteUser, isInviting } = useInvitations(orgId);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !roleId) return;

    const success = await inviteUser({ email, roleId });
    if (success) {
      setEmail('');
      setRoleId('');
      onSuccess();
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 font-['Segoe_UI',_sans-serif]">
      <div className="bg-white rounded-[8px] shadow-[0px_12px_28px_rgba(0,0,0,0.30)] w-full max-w-[480px] overflow-hidden">
        <div className="flex items-center justify-between px-6 py-4 border-b border-[#e0e0e0]">
          <h2 className="text-[24px] font-semibold text-[#242424]">Invite New User</h2>
          <button onClick={onClose} className="text-[#898989] hover:text-[#242424] transition-colors">
            <X className="w-6 h-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 flex flex-col gap-4">
          <div className="flex flex-col gap-1">
            <label htmlFor="email" className="text-[14px] font-semibold text-[#242424]">
              Email Address <span className="text-[#dc3545]">*</span>
            </label>
            <input
              id="email"
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="user@example.com"
              className="px-3 py-2 border border-[#d0d0d0] rounded-[4px] text-[14px] text-[#242424] placeholder-[#898989] focus:outline-none focus:border-[#0066cc]"
            />
            <p className="text-[12px] text-[#898989] mt-1">
              An invitation link will be sent to this email address.
            </p>
          </div>

          <div className="flex flex-col gap-1">
            <label htmlFor="role" className="text-[14px] font-semibold text-[#242424]">
              Role <span className="text-[#dc3545]">*</span>
            </label>
            <select
              id="role"
              required
              value={roleId}
              onChange={(e) => setRoleId(e.target.value)}
              disabled={loadingRoles}
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
              disabled={isInviting}
              className="px-4 py-[10px] bg-white border border-[#d0d0d0] rounded-[4px] text-[#242424] text-[14px] font-semibold hover:bg-[#f8f8f8] transition-colors disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isInviting || !email || !roleId}
              className="px-4 py-[10px] bg-[#0066cc] text-white rounded-[4px] text-[14px] font-semibold hover:bg-[#004499] transition-colors disabled:bg-[#e0e0e0] disabled:text-[#999999]"
            >
              {isInviting ? 'Inviting...' : 'Send Invitation'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
