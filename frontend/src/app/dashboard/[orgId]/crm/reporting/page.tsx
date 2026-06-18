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

  // Filter state
  const [filterSalesperson, setFilterSalesperson] = useState<string>('ALL');
  const [filterStage, setFilterStage] = useState<string>('ALL');
  const [filterMinRevenue, setFilterMinRevenue] = useState<number>(0);
  const [isFilterOpen, setIsFilterOpen] = useState(false);

  // Pivot settings state
  const [pivotBy, setPivotBy] = useState<'salesperson' | 'stage' | 'salesTeam'>('salesperson');
  const [pivotMetric, setPivotMetric] = useState<'totalExpectedRevenue' | 'weightedRevenue' | 'leadCount'>('totalExpectedRevenue');
  const [showStageDist, setShowStageDist] = useState(true);
  const [isPivotSettingsOpen, setIsPivotSettingsOpen] = useState(false);

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

  // Derived filter calculations
  const hasActiveFilters = filterSalesperson !== 'ALL' || filterStage !== 'ALL' || filterMinRevenue > 0;

  const uniqueSalespersons = Array.from(new Set(leads.map(lead => 
    lead.salesperson ? `${lead.salesperson.firstName} ${lead.salesperson.lastName}` : 'Unassigned'
  ))).filter(Boolean);

  const filteredLeads = leads.filter(lead => {
    const spName = lead.salesperson ? `${lead.salesperson.firstName} ${lead.salesperson.lastName}` : 'Unassigned';
    const matchesSalesperson = filterSalesperson === 'ALL' || spName === filterSalesperson;
    const matchesStage = filterStage === 'ALL' || lead.stage === filterStage;
    const matchesRevenue = (lead.expectedRevenue || 0) >= filterMinRevenue;
    return matchesSalesperson && matchesStage && matchesRevenue;
  });

  // Dynamic Chart & Funnel Data based on filters
  const displayLeadFunnel = hasActiveFilters
    ? ['NEW', 'QUALIFIED', 'PROPOSAL', 'WON', 'LOST'].map(stage => {
        const count = filteredLeads.filter(l => l.stage === stage).length;
        return { stage, count };
      })
    : leadFunnel;

  const displayPipelineSummary = hasActiveFilters
    ? ['NEW', 'QUALIFIED', 'PROPOSAL', 'WON', 'LOST'].map(stage => {
        const stageLeads = filteredLeads.filter(l => l.stage === stage);
        const count = stageLeads.length;
        const totalExpectedRevenue = stageLeads.reduce((sum, l) => sum + (l.expectedRevenue || 0), 0);
        const avgProbability = count > 0 
          ? stageLeads.reduce((sum, l) => sum + (l.probability || 0), 0) / count 
          : 0;
        const weightedRevenue = stageLeads.reduce((sum, l) => sum + ((l.expectedRevenue || 0) * (l.probability || 0)) / 100, 0);
        
        return {
          stage,
          leadCount: count,
          totalExpectedRevenue,
          weightedRevenue,
          averageProbability: avgProbability
        };
      })
    : pipelineSummary;

  // Aggregate dynamic Pivot Data
  const pivotMap = new Map<string, { value: number; leadCount: number; stages: Record<string, number> }>();
  
  filteredLeads.forEach(lead => {
    let key = 'Unassigned';
    if (pivotBy === 'salesperson') {
      key = lead.salesperson ? `${lead.salesperson.firstName} ${lead.salesperson.lastName}` : 'Unassigned';
    } else if (pivotBy === 'stage') {
      key = lead.stage || 'Unknown';
    } else if (pivotBy === 'salesTeam') {
      key = lead.salesTeam?.name || 'No Team';
    }
    
    const revenue = lead.expectedRevenue || 0;
    const probability = lead.probability || 0;
    const weighted = (revenue * probability) / 100;
    const stageName = lead.stage || 'Unknown';

    if (!pivotMap.has(key)) {
      pivotMap.set(key, { value: 0, leadCount: 0, stages: {} });
    }
    
    const stats = pivotMap.get(key)!;
    if (pivotMetric === 'totalExpectedRevenue') {
      stats.value += revenue;
    } else if (pivotMetric === 'weightedRevenue') {
      stats.value += weighted;
    } else {
      stats.value += 1;
    }
    stats.leadCount += 1;
    stats.stages[stageName] = (stats.stages[stageName] || 0) + 1;
  });

  const pivotData = Array.from(pivotMap.entries());

  const getPivotDimensionLabel = () => {
    if (pivotBy === 'salesperson') return 'Salesperson';
    if (pivotBy === 'stage') return 'Pipeline Stage';
    return 'Sales Team';
  };

  const getPivotMetricLabel = () => {
    if (pivotMetric === 'totalExpectedRevenue') return 'Total Expected Revenue';
    if (pivotMetric === 'weightedRevenue') return 'Total Weighted Revenue';
    return 'Total Lead Count';
  };

  const formatPivotValue = (val: number) => {
    if (pivotMetric === 'leadCount') return val.toString();
    return `$${val.toLocaleString()}`;
  };

  // CSV Export Action
  const handleExportCSV = () => {
    if (filteredLeads.length === 0) {
      toast.info("No data to export");
      return;
    }
    
    const headers = ["Lead Name", "Type", "Expected Revenue", "Probability (%)", "Weighted Revenue", "Stage", "Salesperson", "Created At"];
    const csvContent = [
      headers.join(','),
      ...filteredLeads.map(lead => {
        const weighted = ((lead.expectedRevenue || 0) * (lead.probability || 0)) / 100;
        return [
          `"${(lead.name || '').replace(/"/g, '""')}"`,
          `"${lead.type || 'OPPORTUNITY'}"`,
          lead.expectedRevenue || 0,
          lead.probability || 0,
          weighted,
          `"${lead.stage || ''}"`,
          `"${lead.salesperson ? `${lead.salesperson.firstName} ${lead.salesperson.lastName}` : 'Unassigned'}"`,
          `"${lead.createdAt ? new Date(lead.createdAt).toLocaleDateString() : ''}"`
        ].join(',');
      })
    ].join('\n');
    
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", `CRM_Reporting_${orgId}_${new Date().toISOString().slice(0, 10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    toast.success("CSV exported successfully");
  };

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
          <Button 
            variant="outline" 
            onClick={() => setIsFilterOpen(!isFilterOpen)}
            className={cn("border-[#d0d0d0] text-[#242424] h-9 px-3 rounded-[4px] font-[500] text-[13px]", isFilterOpen && "bg-[#f0f4ff] border-[#0066cc] text-[#0066cc]")}
          >
            <Filter className="w-4 h-4 mr-2 text-[#898989]" /> Filter
          </Button>
          <Button 
            variant="outline" 
            onClick={handleExportCSV}
            className="border-[#d0d0d0] text-[#242424] h-9 px-3 rounded-[4px] font-[500] text-[13px]"
          >
            <Download className="w-4 h-4 mr-2 text-[#898989]" /> Export
          </Button>
        </div>
      </div>

      {/* Interactive Filter Panel */}
      {isFilterOpen && (
        <div className="bg-[#fafafa] border border-[#e0e0e0] rounded-[4px] p-4 mb-6 grid grid-cols-1 md:grid-cols-3 gap-4 items-end animate-in fade-in slide-in-from-top-2 duration-200">
          <div>
            <label className="block text-[12px] font-[600] text-[#606060] mb-1">Salesperson</label>
            <select 
              value={filterSalesperson}
              onChange={(e) => setFilterSalesperson(e.target.value)}
              className="w-full bg-white border border-[#d0d0d0] rounded-[4px] px-3 h-9 text-[13px] text-[#242424] focus:outline-none focus:border-[#0066cc]"
            >
              <option value="ALL">All Salespersons</option>
              <option value="Unassigned">Unassigned</option>
              {uniqueSalespersons.map(sp => (
                <option key={sp} value={sp}>{sp}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-[12px] font-[600] text-[#606060] mb-1">Pipeline Stage</label>
            <select 
              value={filterStage}
              onChange={(e) => setFilterStage(e.target.value)}
              className="w-full bg-white border border-[#d0d0d0] rounded-[4px] px-3 h-9 text-[13px] text-[#242424] focus:outline-none focus:border-[#0066cc]"
            >
              <option value="ALL">All Stages</option>
              <option value="NEW">New</option>
              <option value="QUALIFIED">Qualified</option>
              <option value="PROPOSAL">Proposal</option>
              <option value="WON">Won</option>
              <option value="LOST">Lost</option>
            </select>
          </div>
          <div>
            <label className="block text-[12px] font-[600] text-[#606060] mb-1">Min Expected Revenue ($)</label>
            <div className="flex gap-2">
              <input 
                type="number"
                value={filterMinRevenue || ''}
                onChange={(e) => setFilterMinRevenue(Number(e.target.value))}
                placeholder="0"
                className="w-full bg-white border border-[#d0d0d0] rounded-[4px] px-3 h-9 text-[13px] text-[#242424] focus:outline-none focus:border-[#0066cc]"
              />
              <Button 
                variant="outline" 
                className="border-[#d0d0d0] text-[#242424] h-9 px-3 rounded-[4px] text-[13px] hover:bg-[#f2f2f2]"
                onClick={() => {
                  setFilterSalesperson('ALL');
                  setFilterStage('ALL');
                  setFilterMinRevenue(0);
                }}
              >
                Reset
              </Button>
            </div>
          </div>
        </div>
      )}

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
              {displayPipelineSummary.length === 0 ? (
                <div className="flex h-full items-center justify-center text-[#898989]">No pipeline deals found</div>
              ) : (
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={displayPipelineSummary} margin={{ top: 5, right: 5, left: -10, bottom: 5 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e8e8e8" />
                    <XAxis dataKey="stage" stroke="#898989" fontSize={11} tickLine={false} />
                    <YAxis stroke="#898989" fontSize={11} tickLine={false} />
                    <Tooltip formatter={formatDollar} />
                    <Legend iconSize={10} wrapperStyle={{ fontSize: 11 }} />
                    <Bar dataKey="totalExpectedRevenue" name="Expected Revenue" fill="#0066cc" radius={[2, 2, 0, 0]} />
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
                {displayLeadFunnel.length === 0 ? (
                  <div className="text-[13px] text-[#898989] py-8 text-center">No lead status data</div>
                ) : (
                  displayLeadFunnel.map((item, idx) => {
                    const maxCount = Math.max(...displayLeadFunnel.map(f => f.count), 1);
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

        {/* Pivot Table: Dynamic Performance Analysis */}
        <div className="border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] bg-white overflow-hidden">
           <div className="bg-[#f8f8f8] px-4 py-3 border-b border-[#e0e0e0] flex items-center justify-between">
              <h2 className="text-[14px] font-[600] text-[#242424]">Revenue & Performance Analysis</h2>
              <Button 
                variant="ghost" 
                size="sm" 
                onClick={() => setIsPivotSettingsOpen(true)}
                className="h-7 px-2 text-[#0066cc] text-[12px]"
              >
                <Settings className="w-3.5 h-3.5 mr-1"/> Pivot Settings
              </Button>
           </div>
           <div className="overflow-x-auto">
             <table className="w-full text-left border-collapse min-w-[800px]">
                <thead className="bg-[#f8f8f8]">
                  <tr className="border-b border-[#e0e0e0]">
                    <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[200px]">
                      {getPivotDimensionLabel()}
                    </th>
                    <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] text-right">
                      {getPivotMetricLabel()}
                    </th>
                    <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] text-right">
                      Active Leads
                    </th>
                    {showStageDist && (
                      <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] text-center">
                        Stage Distribution
                      </th>
                    )}
                  </tr>
                </thead>
                <tbody>
                  {pivotData.length === 0 ? (
                    <tr>
                      <td colSpan={showStageDist ? 4 : 3} className="p-4 text-center text-[#898989]">
                        No records available under current filters
                      </td>
                    </tr>
                  ) : (
                    pivotData.map(([name, stats], idx) => (
                      <tr key={name} className={cn("border-b border-[#e0e0e0] hover:bg-[#f0f4ff]", idx % 2 === 0 ? "bg-white" : "bg-[#fafafa]")}>
                        <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0] font-[500]">{name}</td>
                        <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0] text-right font-mono">
                           {formatPivotValue(stats.value)}
                        </td>
                        <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0] text-right font-mono">
                           {stats.leadCount}
                        </td>
                        {showStageDist && (
                          <td className="px-4 py-3 text-[13px] text-[#242424]">
                             <div className="flex items-center space-x-2 text-[12px] overflow-x-auto">
                               {Object.entries(stats.stages).map(([stage, count]) => (
                                  <span key={stage} className="bg-[#e0e0e0] px-2 py-0.5 rounded-[2px] whitespace-nowrap">{stage}: {count}</span>
                               ))}
                             </div>
                          </td>
                        )}
                      </tr>
                    ))
                  )}
                </tbody>
             </table>
           </div>
        </div>
      </div>

      {/* Pivot Settings Modal */}
      {isPivotSettingsOpen && (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-center justify-center">
          <div className="bg-white rounded-[4px] border border-[#e0e0e0] w-[450px] overflow-hidden shadow-lg animate-in fade-in zoom-in-95 duration-200">
            <div className="bg-[#f8f8f8] px-4 py-3 border-b border-[#e0e0e0] flex items-center justify-between">
              <h3 className="text-[14px] font-[600] text-[#242424]">Pivot Settings</h3>
              <button 
                onClick={() => setIsPivotSettingsOpen(false)}
                className="text-[#898989] hover:text-[#242424] text-[18px] font-bold"
              >
                &times;
              </button>
            </div>
            <div className="p-5 space-y-4">
              <div>
                <label className="block text-[12px] font-[600] text-[#606060] mb-1">Pivot Row Dimension (Group By)</label>
                <select 
                  value={pivotBy}
                  onChange={(e) => setPivotBy(e.target.value as any)}
                  className="w-full bg-white border border-[#d0d0d0] rounded-[4px] px-3 h-9 text-[13px] text-[#242424] focus:outline-none focus:border-[#0066cc]"
                >
                  <option value="salesperson">Salesperson</option>
                  <option value="stage">Pipeline Stage</option>
                  <option value="salesTeam">Sales Team</option>
                </select>
              </div>
              
              <div>
                <label className="block text-[12px] font-[600] text-[#606060] mb-1">Aggregate Metric Value</label>
                <select 
                  value={pivotMetric}
                  onChange={(e) => setPivotMetric(e.target.value as any)}
                  className="w-full bg-white border border-[#d0d0d0] rounded-[4px] px-3 h-9 text-[13px] text-[#242424] focus:outline-none focus:border-[#0066cc]"
                >
                  <option value="totalExpectedRevenue">Total Expected Revenue</option>
                  <option value="weightedRevenue">Total Weighted Revenue</option>
                  <option value="leadCount">Total Lead Count</option>
                </select>
              </div>

              <div className="flex items-center space-x-2 pt-2">
                <input 
                  type="checkbox"
                  id="showStageDist"
                  checked={showStageDist}
                  onChange={(e) => setShowStageDist(e.target.checked)}
                  className="rounded border-[#d0d0d0] text-[#0066cc] focus:ring-[#0066cc]"
                />
                <label htmlFor="showStageDist" className="text-[13px] text-[#242424] select-none cursor-pointer">
                  Show Stage Distribution column
                </label>
              </div>
            </div>
            <div className="bg-[#f8f8f8] px-4 py-3 border-t border-[#e0e0e0] flex justify-end space-x-2">
              <Button 
                className="bg-[#0066cc] hover:bg-[#0052a3] text-white h-8 px-4 rounded-[4px] text-[12px]"
                onClick={() => setIsPivotSettingsOpen(false)}
              >
                Apply Settings
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
