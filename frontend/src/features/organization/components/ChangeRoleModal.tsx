import React, { useState, useEffect } from 'react';
import { X, Shield } from 'lucide-react';
import { useRoles } from '../hooks/useRoles';
import { organizationMemberService } from '../services/organizationMemberService';

interface ChangeRoleModalProps {
  isOpen: boolean;
  onClose: () => void;
  orgId: string;
  user: {
    id: string;
    name: string;
    email: string;
    roleIds?: string[];
  } | null;
  onSuccess: () => void;
}

export const ChangeRoleModal: React.FC<ChangeRoleModalProps> = ({ isOpen, onClose, orgId, user, onSuccess }) => {
  const [selectedRoleIds, setSelectedRoleIds] = useState<string[]>([]);
  const [isUpdating, setIsUpdating] = useState(false);
  
  const { roles, loading: loadingRoles } = useRoles(orgId);

  useEffect(() => {
    if (user?.roleIds) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setSelectedRoleIds(user.roleIds);
    } else {
      setSelectedRoleIds([]);
    }
  }, [user]);

  if (!isOpen || !user) return null;

  const handleToggleRole = (roleId: string) => {
    setSelectedRoleIds(prev => 
      prev.includes(roleId) 
        ? prev.filter(id => id !== roleId)
        : [...prev, roleId]
    );
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (selectedRoleIds.length === 0 || !user) return;

    try {
      setIsUpdating(true);
      await organizationMemberService.updateMemberRoles(orgId, user.id, selectedRoleIds);
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
      <div className="bg-white rounded-[8px] shadow-[0px_12px_28px_rgba(0,0,0,0.30)] w-full max-w-[500px] overflow-hidden flex flex-col max-h-[90vh]">
        <div className="flex items-center justify-between px-6 py-4 border-b border-[#e0e0e0] shrink-0">
          <h2 className="text-[24px] font-semibold text-[#242424]">Assign Roles</h2>
          <button onClick={onClose} className="text-[#898989] hover:text-[#242424] transition-colors">
            <X className="w-6 h-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 flex flex-col gap-6 overflow-y-auto">
          <div className="flex items-center gap-4 p-4 bg-[#f8f8f8] rounded-[6px] border border-[#e0e0e0]">
            <div className="w-12 h-12 rounded-full bg-[#e2e8f0] flex items-center justify-center text-[#0066cc] font-bold text-[18px]">
              {user.name.charAt(0).toUpperCase()}
            </div>
            <div className="flex flex-col">
              <span className="text-[15px] font-[600] text-[#242424]">{user.name}</span>
              <span className="text-[13px] text-[#898989]">{user.email}</span>
            </div>
          </div>

          <div className="flex flex-col gap-3">
            <label className="text-[14px] font-semibold text-[#242424]">
              Select Roles <span className="text-[#dc3545]">*</span>
            </label>
            
            {loadingRoles ? (
              <p className="text-[13px] text-[#0066cc] py-4 text-center">Loading available roles...</p>
            ) : (
              <div className="flex flex-col gap-2 max-h-[300px] overflow-y-auto pr-2">
                {roles.map((role) => {
                  const isSelected = selectedRoleIds.includes(role.id);
                  return (
                    <div 
                      key={role.id}
                      onClick={() => handleToggleRole(role.id)}
                      className={`flex items-start gap-3 p-3 rounded-[6px] border transition-colors cursor-pointer ${
                        isSelected ? 'border-[#0066cc] bg-[#f0f4ff]' : 'border-[#d0d0d0] hover:border-[#898989] bg-white'
                      }`}
                    >
                      <div className={`mt-0.5 flex-shrink-0 w-5 h-5 rounded border flex items-center justify-center transition-colors ${
                        isSelected ? 'border-[#0066cc] bg-[#0066cc]' : 'border-[#d0d0d0] bg-white'
                      }`}>
                        {isSelected && <svg className="w-3.5 h-3.5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" /></svg>}
                      </div>
                      <div className="flex flex-col">
                        <span className={`text-[14px] font-[600] ${isSelected ? 'text-[#0066cc]' : 'text-[#242424]'}`}>
                          {role.name}
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          <div className="flex items-center justify-end gap-2 pt-4 border-t border-[#e0e0e0] shrink-0 mt-auto">
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
              disabled={isUpdating || selectedRoleIds.length === 0}
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
