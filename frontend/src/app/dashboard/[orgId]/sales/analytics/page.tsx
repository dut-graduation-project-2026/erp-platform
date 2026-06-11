'use client';

import React, { useState, use } from 'react';
import { 
  Download, 
  TrendingUp, 
  ArrowUpRight, 
  ArrowDownRight, 
  Award, 
  Target, 
  DollarSign, 
  Users,
  Calendar
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { 
  AreaChart, 
  Area, 
  BarChart, 
  Bar, 
  PieChart, 
  Pie, 
  Cell, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  ResponsiveContainer 
} from 'recharts';
import { toast } from 'sonner';
import { cn } from '@/lib/utils';

export default function SalesAnalyticsPage({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = use(params);
  const [timePeriod, setTimePeriod] = useState('Last 6 Months');

  // Rich, realistic ERP seed data
  const revenueTrendData = [
    { name: 'Jan', sales: 42000, margin: 13500 },
    { name: 'Feb', sales: 58000, margin: 19000 },
    { name: 'Mar', sales: 51000, margin: 16200 },
    { name: 'Apr', sales: 88000, margin: 28500 },
    { name: 'May', sales: 74000, margin: 24000 },
    { name: 'Jun', sales: 95000, margin: 31200 },
  ];

  const categoryShareData = [
    { name: 'Licenses', value: 55000, color: '#0066cc' },
    { name: 'SLA Support', value: 28000, color: '#898989' },
    { name: 'Consulting', value: 15000, color: '#4a4a4a' },
    { name: 'SaaS Storage', value: 10000, color: '#242424' },
  ];

  const topProducts = [
    { name: 'Enterprise Core License v4', qty: 145, revenue: 580000, margin: 32, trend: 'up' },
    { name: 'Premium 24/7 Support SLA', qty: 98, revenue: 294000, margin: 25, trend: 'up' },
    { name: 'Cloud Dedicated Storage 50TB', qty: 42, revenue: 168000, margin: 28, trend: 'down' },
    { name: 'Solutions Architect Consulting', qty: 35, revenue: 105000, margin: 40, trend: 'up' },
  ];

  const handleExport = () => {
    toast.success('Sales report exported to PDF');
  };

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white overflow-y-auto">
      {/* Header */}
      <div className="flex justify-between items-center mb-6 shrink-0">
        <div>
          <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Sales Intelligence</h1>
          <span className="text-[14px] text-[#898989]">Live revenue tracking, funnel analysis, and profitability diagnostics</span>
        </div>
        <Button 
          onClick={handleExport}
          variant="outline" 
          className="border-[#d0d0d0] text-[#242424] h-10 px-4 bg-white rounded-[4px] font-[600] text-[13px]"
        >
          <Download className="w-4 h-4 mr-2 text-[#898989]" /> Export PDF Report
        </Button>
      </div>

      {/* Mini KPI Cards row */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-5 mb-6 shrink-0">
        <div className="border border-[#e0e0e0] rounded-[4px] p-4 bg-white shadow-sm flex items-center justify-between">
          <div>
            <span className="text-[11px] font-[600] text-[#898989] uppercase tracking-wider block mb-1">YTD Net Revenue</span>
            <span className="text-[22px] font-[700] text-[#242424]">$1,148,000</span>
          </div>
          <div className="w-10 h-10 bg-[#f0f4ff] rounded-[4px] flex items-center justify-center text-[#0066cc]">
            <DollarSign className="w-5 h-5" />
          </div>
        </div>

        <div className="border border-[#e0e0e0] rounded-[4px] p-4 bg-white shadow-sm flex items-center justify-between">
          <div>
            <span className="text-[11px] font-[600] text-[#898989] uppercase tracking-wider block mb-1">Avg Deal Size</span>
            <span className="text-[22px] font-[700] text-[#242424]">$8,450</span>
          </div>
          <div className="w-10 h-10 bg-[#f5f5f5] rounded-[4px] flex items-center justify-center text-[#898989]">
            <Target className="w-5 h-5" />
          </div>
        </div>

        <div className="border border-[#e0e0e0] rounded-[4px] p-4 bg-white shadow-sm flex items-center justify-between">
          <div>
            <span className="text-[11px] font-[600] text-[#898989] uppercase tracking-wider block mb-1">Quota Attainment</span>
            <span className="text-[22px] font-[700] text-[#28a745]">104.2%</span>
          </div>
          <div className="w-10 h-10 bg-[#e2f0d9] rounded-[4px] flex items-center justify-center text-[#385723]">
            <Award className="w-5 h-5" />
          </div>
        </div>

        <div className="border border-[#e0e0e0] rounded-[4px] p-4 bg-white shadow-sm flex items-center justify-between">
          <div>
            <span className="text-[11px] font-[600] text-[#898989] uppercase tracking-wider block mb-1">Active Sales Reps</span>
            <span className="text-[22px] font-[700] text-[#242424]">12 Representatives</span>
          </div>
          <div className="w-10 h-10 bg-[#f5f5f5] rounded-[4px] flex items-center justify-center text-[#898989]">
            <Users className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Main Charts area */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
        
        {/* Sales Performance Area Chart */}
        <div className="lg:col-span-2 border border-[#e0e0e0] rounded-[4px] p-5 bg-white shadow-sm">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h3 className="text-[14px] font-[700] text-[#242424]">Revenue & Margin Trends</h3>
              <p className="text-[12px] text-[#898989]">Monthly gross sales compared to net profitability margins</p>
            </div>
            <select 
              value={timePeriod}
              onChange={e => setTimePeriod(e.target.value)}
              className="h-9 border border-[#d0d0d0] rounded-[4px] text-[13px] text-[#242424] px-2 bg-white focus:outline-none"
            >
              <option>Last 6 Months</option>
              <option>This Year</option>
            </select>
          </div>
          
          <div className="h-[260px]">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={revenueTrendData} margin={{ top: 10, right: 5, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorSales" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#0066cc" stopOpacity={0.15}/>
                    <stop offset="95%" stopColor="#0066cc" stopOpacity={0.0}/>
                  </linearGradient>
                  <linearGradient id="colorMargin" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#898989" stopOpacity={0.1}/>
                    <stop offset="95%" stopColor="#898989" stopOpacity={0.0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e8e8e8" />
                <XAxis dataKey="name" stroke="#898989" fontSize={11} tickLine={false} />
                <YAxis stroke="#898989" fontSize={11} tickLine={false} />
                <Tooltip formatter={(value) => [`$${value.toLocaleString()}`, '']} />
                <Area type="monotone" dataKey="sales" name="Gross Sales" stroke="#0066cc" strokeWidth={2} fillOpacity={1} fill="url(#colorSales)" />
                <Area type="monotone" dataKey="margin" name="Net Margin" stroke="#898989" strokeWidth={2} fillOpacity={1} fill="url(#colorMargin)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Conversion Funnel card */}
        <div className="border border-[#e0e0e0] rounded-[4px] p-5 bg-white shadow-sm flex flex-col justify-between">
          <div>
            <h3 className="text-[14px] font-[700] text-[#242424] mb-1">Quote Conversion Funnel</h3>
            <p className="text-[12px] text-[#898989] mb-6">Stage-to-stage conversion efficiency totals</p>
            
            <div className="space-y-4">
              <div>
                <div className="flex justify-between text-[13px] mb-1">
                  <span className="font-semibold text-[#242424]">1. Quotation Drafts</span>
                  <span className="font-mono text-[#898989]">95 draft quotes</span>
                </div>
                <div className="w-full bg-[#f2f2f2] h-2 rounded-full overflow-hidden">
                  <div className="bg-[#898989] h-full w-[100%]"></div>
                </div>
              </div>

              <div>
                <div className="flex justify-between text-[13px] mb-1">
                  <span className="font-semibold text-[#242424]">2. Shared / Sent</span>
                  <span className="font-mono text-[#0066cc]">64 orders (67.3%)</span>
                </div>
                <div className="w-full bg-[#f2f2f2] h-2 rounded-full overflow-hidden">
                  <div className="bg-[#0066cc] h-full w-[67.3%]"></div>
                </div>
              </div>

              <div>
                <div className="flex justify-between text-[13px] mb-1">
                  <span className="font-semibold text-[#242424]">3. Confirmed & Invoiced</span>
                  <span className="font-mono text-[#28a745]">36 completed (37.8%)</span>
                </div>
                <div className="w-full bg-[#f2f2f2] h-2 rounded-full overflow-hidden">
                  <div className="bg-[#28a745] h-full w-[37.8%]"></div>
                </div>
              </div>
            </div>
          </div>

          <div className="border-t border-[#f5f5f5] pt-4 mt-6 flex justify-between items-center text-[13px] text-[#4a4a4a]">
            <span>Opportunity Conversion:</span>
            <strong className="text-[#28a745] flex items-center font-[700]">
              <TrendingUp className="w-4 h-4 mr-1" />
              37.8% Overall
            </strong>
          </div>
        </div>

      </div>

      {/* Top Performing products and category distribution */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Products Table */}
        <div className="lg:col-span-2 border border-[#e0e0e0] rounded-[4px] overflow-hidden shadow-sm">
          <div className="px-5 py-4 border-b border-[#e0e0e0] bg-[#fafafa]">
            <h3 className="text-[14px] font-[700] text-[#242424]">Top Performing SKUs</h3>
          </div>
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-white border-b border-[#e0e0e0]">
                <th className="py-2.5 px-4 text-[11px] font-bold text-[#898989] uppercase tracking-wider">Product Description</th>
                <th className="py-2.5 px-4 text-[11px] font-bold text-[#898989] uppercase tracking-wider text-right">Qty Sold</th>
                <th className="py-2.5 px-4 text-[11px] font-bold text-[#898989] uppercase tracking-wider text-right">Gross Sales</th>
                <th className="py-2.5 px-4 text-[11px] font-bold text-[#898989] uppercase tracking-wider text-center">Avg Margin</th>
                <th className="py-2.5 px-4 text-[11px] font-bold text-[#898989] uppercase tracking-wider text-center">Trend</th>
              </tr>
            </thead>
            <tbody>
              {topProducts.map((p, idx) => (
                <tr key={idx} className="border-b border-[#f5f5f5] last:border-b-0 hover:bg-[#fafafa]">
                  <td className="py-3 px-4 text-[13px] font-[600] text-[#242424]">{p.name}</td>
                  <td className="py-3 px-4 text-[13px] text-right font-mono">{p.qty}</td>
                  <td className="py-3 px-4 text-[13px] text-right font-mono font-[600] text-[#0066cc]">
                    ${p.revenue.toLocaleString()}
                  </td>
                  <td className="py-3 px-4 text-[13px] text-center font-[500] text-[#28a745]">{p.margin}%</td>
                  <td className="py-3 px-4 text-center">
                    <span className={cn(
                      "inline-flex items-center px-2 py-0.5 rounded-[12px] text-[10px] font-bold uppercase",
                      p.trend === 'up' ? "bg-[#e2f0d9] text-[#385723]" : "bg-[#fbe5d6] text-[#c65911]"
                    )}>
                      {p.trend === 'up' ? (
                        <><ArrowUpRight className="w-3 h-3 mr-0.5" /> Growth</>
                      ) : (
                        <><ArrowDownRight className="w-3 h-3 mr-0.5" /> Decline</>
                      )}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Category Share Donut */}
        <div className="border border-[#e0e0e0] rounded-[4px] p-5 bg-white shadow-sm">
          <h3 className="text-[14px] font-[700] text-[#242424] mb-1">Sales Category Share</h3>
          <p className="text-[12px] text-[#898989] mb-4">Revenue distribution across service lines</p>

          <div className="h-[170px] flex justify-center items-center">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={categoryShareData}
                  cx="50%"
                  cy="50%"
                  innerRadius={40}
                  outerRadius={60}
                  paddingAngle={3}
                  dataKey="value"
                >
                  {categoryShareData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip formatter={(value) => `$${value.toLocaleString()}`} />
              </PieChart>
            </ResponsiveContainer>
          </div>

          <div className="space-y-2 text-[12px] text-[#4a4a4a]">
            {categoryShareData.map((item, idx) => (
              <div key={idx} className="flex justify-between items-center border-t border-[#f5f5f5] pt-1.5 first:border-0 first:pt-0">
                <div className="flex items-center space-x-2">
                  <span className="w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: item.color }}></span>
                  <span>{item.name}</span>
                </div>
                <strong className="font-mono">${item.value.toLocaleString()}</strong>
              </div>
            ))}
          </div>
        </div>

      </div>
    </div>
  );
}
