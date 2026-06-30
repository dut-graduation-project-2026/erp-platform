'use client';

import { useEffect, useState, use } from 'react';
import { CrmLeadForm } from '@/features/crm/components/CrmLeadForm';
import { getLeadById } from '@/features/crm/services/crmService';
import { CrmLead } from '@/features/crm/types';

export default function LeadDetailPage({ params }: { params: Promise<{ orgId: string, id: string }> }) {
  const { orgId, id } = use(params);
  const [lead, setLead] = useState<CrmLead | null>(null);
  const [isLoading, setIsLoading] = useState(id !== 'new');

  useEffect(() => {
    if (id === 'new') {
      return;
    }

    getLeadById(orgId, id)
      .then(data => setLead(data))
      .catch(console.error)
      .finally(() => setIsLoading(false));
  }, [orgId, id]);

  if (isLoading) {
    return <div className="p-6 text-[#898989] font-['Segoe_UI'] flex justify-center items-center h-full">Loading Form...</div>;
  }

  return (
    <div className="h-full">
      <CrmLeadForm lead={lead} orgId={orgId} isNew={id === 'new'} />
    </div>
  );
}
