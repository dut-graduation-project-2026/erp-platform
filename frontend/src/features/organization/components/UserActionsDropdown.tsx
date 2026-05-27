import React from 'react';
import { MoreHorizontal, Mail, ShieldAlert, Trash2 } from 'lucide-react';
import { useInvitations } from '../hooks/useInvitations';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

interface UserActionsDropdownProps {
  user: {
    id: string;
    name: string;
    status?: string;
    roleId?: string;
  };
  orgId: string;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onChangeRole: (user: any) => void;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onRemove: (user: any) => void;
}

export const UserActionsDropdown: React.FC<UserActionsDropdownProps> = ({ user, orgId, onChangeRole, onRemove }) => {
  const { resendInvitation, isInviting } = useInvitations(orgId);

  const handleResendInvite = async (e: Event) => {
    e.preventDefault();
    if (user.status !== 'Pending') return;
    await resendInvitation(user.id);
  };

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <button 
          onClick={(e) => e.stopPropagation()}
          className="text-[#898989] hover:text-[#0066cc] p-1.5 rounded hover:bg-[#e0e0e0] transition-colors focus:outline-none focus:ring-2 focus:ring-[#0066cc] focus:ring-offset-1"
        >
          <MoreHorizontal className="w-4 h-4" />
        </button>
      </DropdownMenuTrigger>

      <DropdownMenuContent align="end" className="w-48 bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_8px_20px_rgba(0,0,0,0.25)] p-1 font-['Segoe_UI',_sans-serif]">
        {user.status === 'Pending' && (
          <DropdownMenuItem
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            onClick={(e: any) => handleResendInvite(e)}
            disabled={isInviting}
            className="flex items-center gap-2 px-3 py-2 text-[13px] text-[#242424] hover:bg-[#f8f8f8] cursor-pointer rounded-sm"
          >
            <Mail className="w-4 h-4 text-[#898989]" />
            Resend Invitation
          </DropdownMenuItem>
        )}
        
        <DropdownMenuItem
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          onClick={(e: any) => {
            e.stopPropagation();
            onChangeRole(user);
          }}
          className="flex items-center gap-2 px-3 py-2 text-[13px] text-[#242424] hover:bg-[#f8f8f8] cursor-pointer rounded-sm"
        >
          <ShieldAlert className="w-4 h-4 text-[#898989]" />
          Change Role
        </DropdownMenuItem>
        
        <DropdownMenuSeparator className="bg-[#e0e0e0] my-1" />
        
        <DropdownMenuItem
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          onClick={(e: any) => {
            e.stopPropagation();
            onRemove(user);
          }}
          className="flex items-center gap-2 px-3 py-2 text-[13px] text-[#dc3545] focus:text-[#dc3545] hover:bg-[#fff5f5] focus:bg-[#fff5f5] cursor-pointer rounded-sm"
        >
          <Trash2 className="w-4 h-4" />
          Remove User
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
};
