import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { CrmLead } from '../types';
import { CheckCircle2, Clock, Star } from 'lucide-react';
import { cn } from '@/lib/utils';
import { updateLeadStage } from '../services/crmService';
import { usePermissions } from '@/hooks/use-permissions';
import { PERMISSIONS } from '@/config/permissions';

interface Props {
  leads: CrmLead[];
  orgId: string;
}

const COLUMNS = [
  { id: 'NEW', name: 'New' },
  { id: 'QUALIFIED', name: 'Qualified' },
  { id: 'PROPOSAL', name: 'Proposal' },
  { id: 'WON', name: 'Won' },
  { id: 'LOST', name: 'Lost' },
];

export function CrmKanbanBoard({ leads: initialLeads, orgId }: Props) {
  const router = useRouter();
  const { hasPermission } = usePermissions();
  const [leads, setLeads] = useState<CrmLead[]>(initialLeads);

  useEffect(() => {
    setLeads(initialLeads);
  }, [initialLeads]);

  const handleDragStart = (e: React.DragEvent, leadId: string) => {
    e.dataTransfer.setData('leadId', leadId);
    e.currentTarget.classList.add('opacity-50');
  };

  const handleDragEnd = (e: React.DragEvent) => {
    e.currentTarget.classList.remove('opacity-50');
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  const handleDrop = async (e: React.DragEvent, targetColId: string) => {
    e.preventDefault();
    const leadId = e.dataTransfer.getData('leadId');
    if (!leadId) return;

    const leadToMove = leads.find(l => l.id === leadId);
    if (!leadToMove) return;

    // Optimistic update
    const previousLeads = [...leads];
    setLeads(leads.map(l => {
      if (l.id === leadId) {
        return {
          ...l,
          stage: targetColId
        } as any;
      }
      return l;
    }));

    try {
      await updateLeadStage(orgId, leadId, targetColId);
    } catch (err) {
      console.error("Backend update failed, rolling back", err);
      setLeads(previousLeads);
    }
  };

  const getColumnLeads = (colId: string) => {
    return leads.filter(lead => {
      const currentStage = lead.stage || 'NEW';
      return currentStage.toUpperCase() === colId.toUpperCase();
    });
  };

  return (
    <div className="flex h-full gap-4 overflow-x-auto pb-4 font-['Segoe_UI']">
      {COLUMNS.map((col) => {
        const colLeads = getColumnLeads(col.id);
        const colTotal = colLeads.reduce((sum, l) => sum + (l.expectedRevenue || 0), 0);

        return (
          <div 
            key={col.id} 
            className="flex flex-col w-[300px] shrink-0 bg-[#f8f8f8] rounded-[8px]"
            onDragOver={handleDragOver}
            onDrop={(e) => handleDrop(e, col.id)}
          >
            <div className="p-3 border-b border-transparent group">
              <div className="flex justify-between items-center mb-1">
                <h3 className="font-semibold text-[#242424]">{col.name}</h3>
                <span className="text-[#898989] text-xs font-medium bg-[#e0e0e0] px-2 py-0.5 rounded-full">
                  {colLeads.length}
                </span>
              </div>
              <div className="text-sm text-[#898989]">
                ₫{colTotal.toLocaleString()}
              </div>
            </div>

            <div className="flex-1 overflow-y-auto p-2 space-y-3 min-h-[150px]">
              {colLeads.map((lead) => (
                <div 
                  key={lead.id} 
                  draggable={hasPermission(PERMISSIONS.LEADS.WRITE)}
                  onDragStart={(e) => handleDragStart(e, lead.id)}
                  onDragEnd={handleDragEnd}
                  onClick={() => router.push(`/dashboard/${orgId}/crm/leads/${lead.id}`)}
                  className={cn("bg-white p-3 rounded-[4px] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] border border-transparent hover:border-[#0066cc] hover:shadow-[0px_2px_8px_rgba(0,0,0,0.15)] transition-all", hasPermission(PERMISSIONS.LEADS.WRITE) ? "cursor-pointer active:cursor-grabbing" : "cursor-pointer")}
                >
                  <div className="flex justify-between items-start mb-2">
                    <span className="font-semibold text-[#242424] text-[14px] leading-tight">
                      {lead.name}
                    </span>
                    {lead.probability >= 80 ? (
                      <span className="flex items-center text-[#28a745] bg-[#28a745]/10 px-1.5 py-0.5 rounded text-[10px] font-bold">
                        <CheckCircle2 className="w-3 h-3 mr-1" /> Hot
                      </span>
                    ) : lead.probability > 40 ? (
                      <span className="flex items-center text-[#ffc107] bg-[#ffc107]/10 px-1.5 py-0.5 rounded text-[10px] font-bold">
                        <Clock className="w-3 h-3 mr-1" /> Warm
                      </span>
                    ) : null}
                  </div>
                  
                  <div className="text-[#242424] text-[13px] font-medium mb-3">
                    ₫{lead.expectedRevenue?.toLocaleString() || 0}
                  </div>

                  <div className="flex justify-between items-center text-[#898989] text-xs">
                    <div className="flex items-center space-x-1">
                      <Star className={cn("w-3.5 h-3.5", lead.probability >= 80 ? "fill-[#ffc107] text-[#ffc107]" : "")} />
                      <Star className={cn("w-3.5 h-3.5", lead.probability >= 80 ? "fill-[#ffc107] text-[#ffc107]" : "")} />
                      <Star className={cn("w-3.5 h-3.5", lead.probability === 100 ? "fill-[#ffc107] text-[#ffc107]" : "")} />
                    </div>
                    {lead.partner?.name ? (
                      <span className="truncate max-w-[120px]" title={lead.partner.name}>
                        {lead.partner.name}
                      </span>
                    ) : (
                      <span>No partner</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
}
