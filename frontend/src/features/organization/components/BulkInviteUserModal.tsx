import React, { useState, useEffect } from 'react';
import { X, AlertCircle, CheckCircle2 } from 'lucide-react';
import { useRoles } from '../hooks/useRoles';
import { useInvitations } from '../hooks/useInvitations';

interface BulkInviteUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  orgId: string;
  onSuccess: () => void;
}

export const BulkInviteUserModal: React.FC<BulkInviteUserModalProps> = ({ isOpen, onClose, orgId, onSuccess }) => {
  const [textInput, setTextInput] = useState('');
  const [roleId, setRoleId] = useState('');
  const [validEmails, setValidEmails] = useState<string[]>([]);
  const [invalidEmails, setInvalidEmails] = useState<string[]>([]);
  
  const { roles, loading: loadingRoles } = useRoles(orgId);
  const { bulkInviteUsers, isInviting } = useInvitations(orgId);

  useEffect(() => {
    if (!textInput.trim()) {
      setValidEmails([]);
      setInvalidEmails([]);
      return;
    }

    const tokens = textInput.split(/[\s,;\n]+/);
    const valid: string[] = [];
    const invalid: string[] = [];
    
    // Very simple email regex for initial frontend validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    
    tokens.forEach(token => {
      const clean = token.trim();
      if (!clean) return;
      
      if (emailRegex.test(clean)) {
        if (!valid.includes(clean)) valid.push(clean);
      } else {
        if (!invalid.includes(clean)) invalid.push(clean);
      }
    });

    setValidEmails(valid);
    setInvalidEmails(invalid);
  }, [textInput]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (validEmails.length === 0 || !roleId) return;

    // Based on user feedback: ignore invalid emails, only submit valid ones
    const success = await bulkInviteUsers({ emails: validEmails, roleId });
    if (success) {
      setTextInput('');
      setRoleId('');
      setValidEmails([]);
      setInvalidEmails([]);
      onSuccess();
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 font-['Segoe_UI',_sans-serif]">
      <div className="bg-white rounded-[8px] shadow-[0px_12px_28px_rgba(0,0,0,0.30)] w-full max-w-[550px] max-h-[90vh] flex flex-col overflow-hidden">
        <div className="flex items-center justify-between px-6 py-4 border-b border-[#e0e0e0] shrink-0">
          <div>
            <h2 className="text-[24px] font-semibold text-[#242424]">Bulk Import Users</h2>
            <p className="text-[13px] text-[#898989] mt-1">Paste a list of emails to invite multiple users at once.</p>
          </div>
          <button onClick={onClose} className="text-[#898989] hover:text-[#242424] transition-colors self-start mt-1">
            <X className="w-6 h-6" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 flex flex-col gap-5 overflow-y-auto">
          <div className="flex flex-col gap-1">
            <label htmlFor="role" className="text-[14px] font-semibold text-[#242424]">
              Assign Role <span className="text-[#dc3545]">*</span>
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
          </div>

          <div className="flex flex-col gap-1">
            <label htmlFor="emails" className="text-[14px] font-semibold text-[#242424]">
              Email Addresses <span className="text-[#dc3545]">*</span>
            </label>
            <textarea
              id="emails"
              required
              value={textInput}
              onChange={(e) => setTextInput(e.target.value)}
              placeholder="e.g. user1@example.com, user2@example.com&#10;user3@example.com"
              className="px-3 py-2 border border-[#d0d0d0] rounded-[4px] text-[14px] text-[#242424] placeholder-[#898989] focus:outline-none focus:border-[#0066cc] min-h-[120px] resize-y"
            />
            <p className="text-[12px] text-[#898989] mt-1">
              Separate multiple emails with commas, spaces, or new lines.
            </p>
          </div>

          {(validEmails.length > 0 || invalidEmails.length > 0) && (
            <div className="flex flex-col gap-3 p-4 rounded-[4px] border border-[#e0e0e0] bg-[#f8f8f8]">
              <div className="flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 text-[#28a745]" />
                <span className="text-[13px] font-[600] text-[#242424]">{validEmails.length} valid emails found</span>
              </div>
              
              {invalidEmails.length > 0 && (
                <div className="flex flex-col gap-2 pt-3 border-t border-[#e0e0e0]">
                  <div className="flex items-start gap-2 text-[#dc3545]">
                    <AlertCircle className="w-4 h-4 mt-0.5 shrink-0" />
                    <div className="flex flex-col">
                      <span className="text-[13px] font-[600]">
                        {invalidEmails.length} invalid entries found
                      </span>
                      <span className="text-[12px] opacity-90">
                        These entries will be ignored. Please correct them or proceed to invite only the valid emails.
                      </span>
                    </div>
                  </div>
                  <div className="bg-white border border-[#ffcdd2] rounded-[4px] p-2 text-[12px] text-[#dc3545] font-mono break-all max-h-[80px] overflow-y-auto">
                    {invalidEmails.join(', ')}
                  </div>
                </div>
              )}
            </div>
          )}

          <div className="flex items-center justify-end gap-2 mt-2 pt-4 border-t border-[#e0e0e0]">
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
              disabled={isInviting || validEmails.length === 0 || !roleId}
              className="px-4 py-[10px] bg-[#0066cc] text-white rounded-[4px] text-[14px] font-semibold hover:bg-[#004499] transition-colors disabled:bg-[#e0e0e0] disabled:text-[#999999]"
            >
              {isInviting ? 'Importing...' : `Import ${validEmails.length} User${validEmails.length !== 1 ? 's' : ''}`}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
