'use client';

import { useEffect, useState, useMemo, use } from 'react';
import { getLeads } from '@/features/crm/services/crmService';
import { CrmLead } from '@/features/crm/types';
import { CrmKanbanBoard } from '@/features/crm/components/CrmKanbanBoard';
import { Button } from '@/components/ui/button';
import { Plus, Search } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { Input } from '@/components/ui/input';
import { usePermissions } from '@/hooks/use-permissions';
import { PERMISSIONS } from '@/config/permissions';
import { APP_ROUTES } from '@/config/constants';

export default function CrmPipelinePage({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = use(params);
  const router = useRouter();
  const [leads, setLeads] = useState<CrmLead[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const { hasPermission } = usePermissions();

  useEffect(() => {
    getLeads(orgId, { limit: 100 })
      .then(res => {
        setLeads(res.data || []);
      })
      .catch(err => console.error("Failed to load leads", err))
      .finally(() => setIsLoading(false));
  }, [orgId]);

  const filteredLeads = useMemo(() => {
    if (searchQuery.trim() === '') {
      return leads;
    } else {
      const q = searchQuery.toLowerCase();
      return leads.filter(l => l.name.toLowerCase().includes(q) || (l.partner?.name && l.partner.name.toLowerCase().includes(q)));
    }
  }, [searchQuery, leads]);

  const totalRevenue = filteredLeads.reduce((acc, l) => acc + (l.expectedRevenue || 0), 0);

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI']">
      <div className="flex justify-between items-center mb-6 shrink-0">
        <div>
          <h1 className="text-[24px] font-[600] text-[#242424]">Pipeline Dashboard</h1>
        </div>

        <div className="flex space-x-4 items-center">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
            <Input
              placeholder="Search leads..."
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              className="pl-9 h-10 w-[200px] border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
            />
          </div>

          {hasPermission(PERMISSIONS.LEADS.CREATE) && (
            <Button
              onClick={() => router.push(APP_ROUTES.CRM.LEADS_NEW(orgId))}
              className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px] font-[600]"
            >
              <Plus className="w-4 h-4 mr-2" /> New Lead
            </Button>
          )}

          {/* Metric Ribbon */}
          <div className="flex space-x-4 text-[14px] bg-white px-4 py-2 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] rounded-[4px] border border-[#e0e0e0]">
            <div className="flex flex-col">
              <span className="text-[#898989] text-[12px] uppercase tracking-wide font-semibold">Expected Revenue</span>
              <span className="font-[600] text-[#242424]">${totalRevenue.toLocaleString()}</span>
            </div>
            <div className="w-[1px] bg-[#e0e0e0] my-1"></div>
            <div className="flex flex-col">
              <span className="text-[#898989] text-[12px] uppercase tracking-wide font-semibold">Active Leads</span>
              <span className="font-[600] text-[#242424]">{filteredLeads.length}</span>
            </div>
          </div>
        </div>
      </div>

      <div className="flex-1 min-h-0">
        {isLoading ? (
          <div className="flex items-center justify-center h-full text-[#898989]">Loading Pipeline...</div>
        ) : (
          <CrmKanbanBoard leads={filteredLeads} orgId={orgId} />
        )}
      </div>
    </div>
  );
}
