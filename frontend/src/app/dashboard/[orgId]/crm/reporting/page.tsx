'use client';

import React, { useEffect, useState, use } from 'react';
import { getLeads } from '@/features/crm/services/crmService';
import { CrmLead } from '@/features/crm/types';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Download, Filter, Settings } from 'lucide-react';

export default function CrmReportingPage({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = use(params);
  const [leads, setLeads] = useState<CrmLead[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    getLeads(orgId, { limit: 500 })
      .then(res => setLeads(res.data || []))
      .catch(console.error)
      .finally(() => setIsLoading(false));
  }, [orgId]);

  // Aggregate data for Pivot Table (Mock aggregation based on real leads)
  // We'll group by salesperson and stage
  const salespersonMap = new Map<string, { totalRevenue: number; leadCount: number; stages: Record<string, number> }>();
  
  leads.forEach(lead => {
    const spName = lead.salesperson ? `${lead.salesperson.firstName} ${lead.salesperson.lastName}` : 'Unassigned';
    const stageName = lead.stage || 'Unknown';
    const revenue = lead.expectedRevenue || 0;

    if (!salespersonMap.has(spName)) {
      salespersonMap.set(spName, { totalRevenue: 0, leadCount: 0, stages: {} });
    }
    
    const stats = salespersonMap.get(spName)!;
    stats.totalRevenue += revenue;
    stats.leadCount += 1;
    stats.stages[stageName] = (stats.stages[stageName] || 0) + 1;
  });

  const pivotData = Array.from(salespersonMap.entries());

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white">
      {/* Header Actions */}
      <div className="flex justify-between items-center mb-6 shrink-0">
        <div>
           <h1 className="text-[24px] font-[600] text-[#242424] leading-tight mb-1">
             CRM Analytics & Configuration
           </h1>
           <span className="text-[14px] text-[#898989]">Deep dive into pipeline metrics and system settings</span>
        </div>
        <div className="flex space-x-2">
          <Button variant="outline" className="border-[#d0d0d0] text-[#242424] h-9 px-3 rounded-[4px] font-[500] text-[13px]">
            <Filter className="w-4 h-4 mr-2 text-[#898989]" /> Filter
          </Button>
          <Button variant="outline" className="border-[#d0d0d0] text-[#242424] h-9 px-3 rounded-[4px] font-[500] text-[13px]">
            <Download className="w-4 h-4 mr-2 text-[#898989]" /> Export
          </Button>
        </div>
      </div>

      <div className="flex flex-col flex-1 overflow-hidden space-y-6">
        
        {/* Top Half: Pivot Table */}
        <div className="flex-1 flex flex-col min-h-0 border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] bg-white overflow-hidden">
           <div className="bg-[#f8f8f8] px-4 py-3 border-b border-[#e0e0e0] flex items-center justify-between">
              <h2 className="text-[14px] font-[600] text-[#242424]">Revenue by Salesperson</h2>
              <Button variant="ghost" size="sm" className="h-7 px-2 text-[#0066cc] text-[12px]"><Settings className="w-3.5 h-3.5 mr-1"/> Pivot Settings</Button>
           </div>
           <div className="flex-1 overflow-auto">
             <table className="w-full text-left border-collapse min-w-[800px]">
               <thead className="bg-[#f8f8f8] sticky top-0 z-10 shadow-[0px_1px_0px_#e0e0e0]">
                 <tr>
                   <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[200px]">Salesperson</th>
                   <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] text-right">Total Revenue</th>
                   <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] text-right">Active Leads</th>
                   <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] text-center">Stage Distribution</th>
                 </tr>
               </thead>
               <tbody>
                 {isLoading ? (
                    <tr><td colSpan={4} className="p-4 text-center text-[#898989]">Loading Analytics...</td></tr>
                 ) : pivotData.length === 0 ? (
                    <tr><td colSpan={4} className="p-4 text-center text-[#898989]">No data available</td></tr>
                 ) : (
                   pivotData.map(([name, stats], idx) => (
                     <tr key={name} className={cn("border-b border-[#e0e0e0] hover:bg-[#f0f4ff]", idx % 2 === 0 ? "bg-white" : "bg-[#fafafa]")}>
                       <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0] font-[500]">{name}</td>
                       <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0] text-right font-mono">
                          ₫{stats.totalRevenue.toLocaleString()}
                       </td>
                       <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0] text-right font-mono">
                          {stats.leadCount}
                       </td>
                       <td className="px-4 py-3 text-[13px] text-[#242424]">
                          <div className="flex items-center space-x-2 text-[12px]">
                            {Object.entries(stats.stages).map(([stage, count]) => (
                               <span key={stage} className="bg-[#e0e0e0] px-2 py-0.5 rounded-[2px]">{stage}: {count}</span>
                            ))}
                          </div>
                       </td>
                     </tr>
                   ))
                 )}
               </tbody>
             </table>
           </div>
        </div>

        {/* Bottom Half: Configuration Nook */}
        <div className="h-[250px] shrink-0 border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] bg-white flex flex-col overflow-hidden">
           <div className="bg-[#f8f8f8] px-4 py-3 border-b border-[#e0e0e0]">
              <h2 className="text-[14px] font-[600] text-[#242424]">Lost Reasons Configuration</h2>
           </div>
           <div className="flex-1 overflow-auto">
             <table className="w-full text-left">
                <tbody>
                  {[
                    { id: 1, reason: 'Too Expensive', selected: false },
                    { id: 2, reason: 'No Budget', selected: true },
                    { id: 3, reason: 'Competitor Chosen', selected: false },
                    { id: 4, reason: 'Missing Feature', selected: false },
                  ].map(row => (
                    <tr 
                      key={row.id} 
                      className={cn(
                        "border-b border-[#e0e0e0] cursor-pointer hover:bg-[#f8f8f8]",
                        row.selected ? "bg-[#f0f4ff] border-l-[3px] border-l-[#0066cc]" : "border-l-[3px] border-l-transparent"
                      )}
                    >
                      <td className="px-4 py-3 text-[13px] text-[#242424]">{row.reason}</td>
                      <td className="px-4 py-3 text-right">
                         <span className="text-[12px] text-[#898989]">Active</span>
                      </td>
                    </tr>
                  ))}
                </tbody>
             </table>
           </div>
        </div>

      </div>
    </div>
  );
}
