'use client';

import React, { useEffect, useState, use } from 'react';
import { getLeads, getLeadStageFunnel, getPipelineSummary, LeadStageCount, PipelineStageSummary } from '@/features/crm/services/crmService';
import { CrmLead } from '@/features/crm/types';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Download, Filter, Settings, Loader2, TrendingUp, BarChart as BarChartIcon } from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer
} from 'recharts';
import { toast } from 'sonner';

export default function CrmReportingPage({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = use(params);
  const [leads, setLeads] = useState<CrmLead[]>([]);
  const [leadFunnel, setLeadFunnel] = useState<LeadStageCount[]>([]);
  const [pipelineSummary, setPipelineSummary] = useState<PipelineStageSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      setIsLoading(true);
      try {
        const [leadsRes, funnelRes, pipelineRes] = await Promise.all([
          getLeads(orgId, { limit: 500 }),
          getLeadStageFunnel(orgId),
          getPipelineSummary(orgId)
        ]);

        setLeads(leadsRes.data || []);
        setLeadFunnel(funnelRes || []);
        setPipelineSummary(pipelineRes || []);
      } catch (err) {
        console.error('Error loading CRM reporting:', err);
        toast.error('Failed to load CRM reports.');
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, [orgId]);

  // Aggregate data for Pivot Table (Salesperson performance)
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

  // Formatter for Recharts expected revenue tooltips
  const formatDollar = (value: number) => `$${value.toLocaleString()}`;

  if (isLoading) {
    return (
      <div className="flex h-full w-full items-center justify-center bg-white">
        <div className="flex flex-col items-center space-y-2">
          <Loader2 className="h-8 w-8 animate-spin text-[#0066cc]" />
          <span className="text-[14px] text-[#898989]">Loading CRM Analytics...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white overflow-y-auto">
      {/* Header Actions */}
      <div className="flex justify-between items-center mb-6 shrink-0">
        <div>
           <h1 className="text-[24px] font-[600] text-[#242424] leading-tight mb-1">
             CRM Analytics & Reporting
           </h1>
           <span className="text-[14px] text-[#898989]">Deep dive into pipeline metrics, conversion rates, and salesperson performance</span>
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

      <div className="flex flex-col space-y-6">
        
        {/* Top Section: Charts */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          
          {/* Pipeline Revenue by Stage BarChart */}
          <div className="lg:col-span-2 border border-[#e0e0e0] rounded-[4px] p-5 bg-white shadow-sm">
            <h3 className="text-[14px] font-[700] text-[#242424] flex items-center mb-1">
              <BarChartIcon className="w-4.5 h-4.5 mr-2 text-[#0066cc]" />
              Pipeline Value by Stage
            </h3>
            <p className="text-[12px] text-[#898989] mb-4">Total expected revenue compared to weighted probability revenue</p>
            
            <div className="h-[220px]">
              {pipelineSummary.length === 0 ? (
                <div className="flex h-full items-center justify-center text-[#898989]">No pipeline deals found</div>
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={pipelineSummary} margin={{ top: 5, right: 5, left: -10, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e8e8e8" />
                    <XAxis dataKey="stage" stroke="#898989" fontSize={11} tickLine={false} />
                    <YAxis stroke="#898989" fontSize={11} tickLine={false} />
                    <Tooltip formatter={formatDollar} />
                    <Legend iconSize={10} wrapperStyle={{ fontSize: 11 }} />
                    <Bar dataKey="revenue" name="Expected Revenue" fill="#0066cc" radius={[2, 2, 0, 0]} />
                    <Bar dataKey="weightedRevenue" name="Weighted Revenue" fill="#898989" radius={[2, 2, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              )}
            </div>
          </div>

          {/* Lead Stage Funnel */}
          <div className="border border-[#e0e0e0] rounded-[4px] p-5 bg-white shadow-sm flex flex-col justify-between">
            <div>
              <h3 className="text-[14px] font-[700] text-[#242424] flex items-center mb-1">
                <TrendingUp className="w-4.5 h-4.5 mr-2 text-[#28a745]" />
                Lead Stage Distribution
              </h3>
              <p className="text-[12px] text-[#898989] mb-4">Active leads grouped by pipeline stage</p>
              
              <div className="space-y-3 max-h-[180px] overflow-y-auto pr-1">
                {leadFunnel.length === 0 ? (
                  <div className="text-[13px] text-[#898989] py-8 text-center">No lead status data</div>
                ) : (
                  leadFunnel.map((item, idx) => {
                    const maxCount = Math.max(...leadFunnel.map(f => f.count), 1);
                    const widthPercent = (item.count / maxCount) * 100;
                    return (
                      <div key={idx}>
                        <div className="flex justify-between text-[12px] mb-1">
                          <span className="font-semibold text-[#242424]">{item.stage}</span>
                          <span className="font-mono text-[#898989]">{item.count} leads</span>
                        </div>
                        <div className="w-full bg-[#f2f2f2] h-2 rounded-full overflow-hidden">
                          <div 
                            className="bg-[#0066cc] h-full" 
                            style={{ 
                              width: `${widthPercent}%`,
                              backgroundColor: item.stage === 'WON' ? '#28a745' : item.stage === 'LOST' ? '#dc3545' : '#0066cc'
                            }}
                          ></div>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>
          </div>
          
        </div>

        {/* Pivot Table: Salesperson Performance */}
        <div className="border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] bg-white overflow-hidden">
           <div className="bg-[#f8f8f8] px-4 py-3 border-b border-[#e0e0e0] flex items-center justify-between">
              <h2 className="text-[14px] font-[600] text-[#242424]">Revenue by Salesperson</h2>
              <Button variant="ghost" size="sm" className="h-7 px-2 text-[#0066cc] text-[12px]"><Settings className="w-3.5 h-3.5 mr-1"/> Pivot Settings</Button>
           </div>
           <div className="overflow-x-auto">
             <table className="w-full text-left border-collapse min-w-[800px]">
               <thead className="bg-[#f8f8f8]">
                 <tr className="border-b border-[#e0e0e0]">
                   <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[200px]">Salesperson</th>
                   <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] text-right">Total Revenue</th>
                   <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] text-right">Active Leads</th>
                   <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] text-center">Stage Distribution</th>
                 </tr>
               </thead>
               <tbody>
                 {pivotData.length === 0 ? (
                   <tr><td colSpan={4} className="p-4 text-center text-[#898989]">No salesperson records available</td></tr>
                 ) : (
                   pivotData.map(([name, stats], idx) => (
                     <tr key={name} className={cn("border-b border-[#e0e0e0] hover:bg-[#f0f4ff]", idx % 2 === 0 ? "bg-white" : "bg-[#fafafa]")}>
                       <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0] font-[500]">{name}</td>
                       <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0] text-right font-mono">
                          ${stats.totalRevenue.toLocaleString()}
                       </td>
                       <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0] text-right font-mono">
                          {stats.leadCount}
                       </td>
                       <td className="px-4 py-3 text-[13px] text-[#242424]">
                          <div className="flex items-center space-x-2 text-[12px] overflow-x-auto">
                            {Object.entries(stats.stages).map(([stage, count]) => (
                               <span key={stage} className="bg-[#e0e0e0] px-2 py-0.5 rounded-[2px] whitespace-nowrap">{stage}: {count}</span>
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

        {/* Configuration Nook */}
        <div className="border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] bg-white flex flex-col overflow-hidden">
           <div className="bg-[#f8f8f8] px-4 py-3 border-b border-[#e0e0e0]">
              <h2 className="text-[14px] font-[600] text-[#242424]">Lost Reasons Configuration</h2>
           </div>
           <div className="overflow-y-auto max-h-[200px]">
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
