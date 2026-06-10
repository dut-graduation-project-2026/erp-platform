import { useState, useCallback, useEffect } from 'react';
import { toast } from 'sonner';
import { organizationMemberService, OrganizationMemberResponse } from '../services/organizationMemberService';

export const useOrganizationMember = (organizationId: string, userId: string) => {
  const [member, setMember] = useState<OrganizationMemberResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  const fetchMember = useCallback(async () => {
    if (!organizationId || !userId) return;

    try {
      setLoading(true);
      setError(null);
      const data = await organizationMemberService.getMemberById(organizationId, userId);
      setMember(data);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err);
      } else {
        setError(new Error('Unknown error'));
      }
      toast.error('Failed to load member details');
    } finally {
      setLoading(false);
    }
  }, [organizationId, userId]);

  useEffect(() => {
    fetchMember();
  }, [fetchMember]);

  const updateRoles = async (roleIds: string[]) => {
    try {
      setSaving(true);
      const updatedMember = await organizationMemberService.updateMemberRoles(organizationId, userId, roleIds);
      setMember(updatedMember);
      toast.success('Roles updated successfully');
      return true;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: unknown) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const axiosErr = err as any;
      toast.error(axiosErr.response?.data?.message || 'Failed to update roles');
      return false;
    } finally {
      setSaving(false);
    }
  };

  const removeMember = async () => {
    try {
      setSaving(true);
      await organizationMemberService.removeMember(organizationId, userId);
      toast.success('Member removed successfully');
      return true;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: unknown) {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const axiosErr = err as any;
      toast.error(axiosErr.response?.data?.message || 'Failed to remove member');
      return false;
    } finally {
      setSaving(false);
    }
  };

  return {
    member,
    loading,
    saving,
    error,
    refresh: fetchMember,
    updateRoles,
    removeMember,
  };
};
