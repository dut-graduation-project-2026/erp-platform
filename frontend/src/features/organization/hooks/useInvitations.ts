import { useState } from 'react';
import { 
  inviteUserApi, 
  bulkInviteUsersApi,
  resendInvitationApi, 
  OrganizationInvitationUserRequest,
  BulkOrganizationInvitationRequest,
  OrganizationInvitationResponse
} from '../services/invitationService';
import { toast } from 'sonner';

export const useInvitations = (orgId: string) => {
  const [isInviting, setIsInviting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const inviteUser = async (data: OrganizationInvitationUserRequest): Promise<OrganizationInvitationResponse | null> => {
    try {
      setIsInviting(true);
      setError(null);
      const res = await inviteUserApi(orgId, data);
      toast.success('User invited successfully!');
      return res;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Failed to invite user';
      setError(msg);
      return null;
    } finally {
      setIsInviting(false);
    }
  };

  const resendInvitation = async (invitationId: string) => {
    try {
      setIsInviting(true);
      setError(null);
      await resendInvitationApi(orgId, invitationId);
      toast.success('Invitation resent successfully!');
      return true;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Failed to resend invitation';
      setError(msg);
      return false;
    } finally {
      setIsInviting(false);
    }
  };

  const bulkInviteUsers = async (data: BulkOrganizationInvitationRequest): Promise<OrganizationInvitationResponse[] | null> => {
    try {
      setIsInviting(true);
      setError(null);
      const res = await bulkInviteUsersApi(orgId, data);
      toast.success(`Successfully invited ${res.length} users!`);
      return res;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      const msg = err.response?.data?.message || err.message || 'Failed to bulk invite users';
      setError(msg);
      return null;
    } finally {
      setIsInviting(false);
    }
  };

  return { 
    inviteUser, 
    bulkInviteUsers,
    resendInvitation, 
    isInviting, 
    error 

  };
};
