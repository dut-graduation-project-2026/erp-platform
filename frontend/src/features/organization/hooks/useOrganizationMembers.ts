import { useState, useCallback, useEffect } from 'react';
import { toast } from 'sonner';
import { organizationMemberService, OrganizationMemberResponse } from '../services/organizationMemberService';

interface UseOrganizationMembersProps {
  organizationId: string;
  query?: string;
  page?: number;
  limit?: number;
}

export const useOrganizationMembers = ({
  organizationId,
  query = '',
  page = 1,
  limit = 10,
}: UseOrganizationMembersProps) => {
  const [members, setMembers] = useState<OrganizationMemberResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchMembers = useCallback(async () => {
    if (!organizationId) return;

    try {
      setLoading(true);
      setError(null);
      const response = await organizationMemberService.getMembers(organizationId, query, page, limit);
      
      setMembers(response.data || []);
      setTotalElements(response.pagination?.totalItems || 0);
      setTotalPages(response.pagination?.totalPages || 0);
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err);
      } else {
        setError(new Error('Unknown error'));
      }
      toast.error('Failed to load organization members');
    } finally {
      setLoading(false);
    }
  }, [organizationId, query, page, limit]);

  useEffect(() => {
    fetchMembers();
  }, [fetchMembers]);

  return {
    members,
    loading,
    error,
    totalElements,
    totalPages,
    refresh: fetchMembers,
  };
};
