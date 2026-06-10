'use client';

import React, { useEffect, useState, use } from 'react';
import { getSalesDashboard } from '@/features/sales/services/salesService';
import { Download, ArrowUpRight, ArrowDownRight, TrendingUp } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { cn } from '@/lib/utils';

export default function SalesAnalyticsPage({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = use(params);
  const [data, setData] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    getSalesDashboard(orgId)
      .then(res => setData(res))
      .catch(console.error)
      .finally(() => setIsLoading(false));
  }, [orgId]);

  // Mock data if API fails or returns empty
  const mockChartData = [40, 60, 45, 80, 50, 90, 70];
  const maxVal = Math.max(...mockChartData);

  const topProducts = [
    { name: 'Enterprise Server License', qty: 120, revenue: 1800000000, trend: 'up' },
    { name: 'Premium Support SLA', qty: 95, revenue: 475000000, trend: 'up' },
    { name: 'Cloud Storage 10TB', qty: 50, revenue: 250000000, trend: 'down' },
    { name: 'Consulting Services', qty: 30, revenue: 150000000, trend: 'up' },
  ];

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-[#f8f8f8] overflow-auto">
      <div className="flex justify-between items-center mb-6 shrink-0">
         <div>
            <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Sales Analytics</h1>
            <span className="text-[14px] text-[#898989]">Advanced forecasting and performance metrics</span>
         </div>
         <Button variant="outline" className="border-[#d0d0d0] text-[#242424] h-9 px-3 bg-white rounded-[4px] font-[500] text-[13px]">
            <Download className="w-4 h-4 mr-2 text-[#898989]" /> Export PDF
         </Button>
      </div>

      {/* 3-Column Layout for Desktop */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Card 1: Bar Chart */}
        <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] p-6 col-span-1 lg:col-span-2 flex flex-col">
           <div className="flex justify-between items-start mb-6">
              <h2 className="text-[16px] font-[600] text-[#242424]">Sales Performance by Month</h2>
              <select className="h-8 border border-[#d0d0d0] rounded-[4px] text-[13px] text-[#242424] px-2 outline-none">
                 <option>Last 6 Months</option>
                 <option>This Year</option>
              </select>
           </div>
           
           <div className="flex-1 flex items-end justify-between space-x-4 min-h-[250px] relative">
              {/* Y-Axis lines mocked */}
              <div className="absolute inset-0 flex flex-col justify-between pointer-events-none">
                 {[4,3,2,1,0].map(i => (
                    <div key={i} className="w-full border-t border-dashed border-[#e0e0e0] flex items-center"></div>
                 ))}
              </div>
              
              {/* Bars */}
              {mockChartData.map((val, idx) => (
                 <div key={idx} className="relative z-10 w-full flex flex-col items-center justify-end h-full group">
                    <div className="text-[11px] text-[#0066cc] font-[600] mb-2 opacity-0 group-hover:opacity-100 transition-opacity">
                      ₫{val}M
                    </div>
                    <div 
                      className="w-full max-w-[40px] bg-[#0066cc] rounded-t-[2px] transition-all duration-500 hover:bg-[#004499]"
                      style={{ height: `${(val / maxVal) * 85}%` }}
                    ></div>
                    <div className="mt-3 text-[12px] text-[#898989] font-[500]">M{idx + 1}</div>
                 </div>
              ))}
           </div>
        </div>

        {/* Card 3: Workflow State List */}
        <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] p-6 flex flex-col">
           <h2 className="text-[16px] font-[600] text-[#242424] mb-6">Quote Conversion Funnel</h2>
           
           <div className="space-y-6 flex-1">
              <div className="flex justify-between items-center">
                 <div className="flex items-center">
                    <div className="w-8 h-8 rounded-full bg-[#f8f8f8] flex items-center justify-center text-[#898989] font-[600] text-[12px] border border-[#e0e0e0]">1</div>
                    <span className="ml-3 text-[14px] font-[600] text-[#242424]">Draft Quotes</span>
                 </div>
                 <span className="text-[18px] font-mono font-[700] text-[#242424]">45</span>
              </div>
              <div className="w-[2px] h-6 bg-[#e0e0e0] ml-[15px] -my-2"></div>
              
              <div className="flex justify-between items-center">
                 <div className="flex items-center">
                    <div className="w-8 h-8 rounded-full bg-[#f0f4ff] flex items-center justify-center text-[#0066cc] font-[600] text-[12px] border border-[#0066cc]">2</div>
                    <span className="ml-3 text-[14px] font-[600] text-[#242424]">Sent</span>
                 </div>
                 <span className="text-[18px] font-mono font-[700] text-[#0066cc]">28</span>
              </div>
              <div className="w-[2px] h-6 bg-[#e0e0e0] ml-[15px] -my-2"></div>
              
              <div className="flex justify-between items-center">
                 <div className="flex items-center">
                    <div className="w-8 h-8 rounded-full bg-[#28a745]/10 flex items-center justify-center text-[#28a745] font-[600] text-[12px] border border-[#28a745]">3</div>
                    <span className="ml-3 text-[14px] font-[600] text-[#242424]">Invoiced</span>
                 </div>
                 <span className="text-[18px] font-mono font-[700] text-[#28a745]">15</span>
              </div>
           </div>

           <div className="mt-auto pt-6 border-t border-[#e0e0e0]">
             <div className="flex justify-between items-center text-[13px]">
               <span className="text-[#898989]">Conversion Rate</span>
               <span className="text-[#28a745] font-[700] flex items-center"><TrendingUp className="w-3.5 h-3.5 mr-1"/> 33.3%</span>
             </div>
           </div>
        </div>

        {/* Card 2: Top Performing Products (Full Width below) */}
        <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] col-span-1 lg:col-span-3 overflow-hidden">
           <div className="px-6 py-4 border-b border-[#e0e0e0] flex justify-between items-center bg-[#f8f8f8]">
              <h2 className="text-[16px] font-[600] text-[#242424]">Top Performing Products</h2>
              <Button variant="ghost" size="sm" className="h-8 text-[#0066cc] text-[13px] font-[600]">View Full Report</Button>
           </div>
           <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead>
                  <tr className="border-b border-[#e0e0e0]">
                    <th className="px-6 py-3 text-[12px] uppercase tracking-wide font-[700] text-[#898989]">Product Name</th>
                    <th className="px-6 py-3 text-[12px] uppercase tracking-wide font-[700] text-[#898989] text-right">Units Sold</th>
                    <th className="px-6 py-3 text-[12px] uppercase tracking-wide font-[700] text-[#898989] text-right">Revenue</th>
                    <th className="px-6 py-3 text-[12px] uppercase tracking-wide font-[700] text-[#898989] text-center w-[120px]">Trend</th>
                  </tr>
                </thead>
                <tbody>
                  {topProducts.map((p, i) => (
                    <tr key={i} className="border-b border-[#e0e0e0] hover:bg-[#fafafa]">
                      <td className="px-6 py-3 text-[14px] font-[600] text-[#242424]">{p.name}</td>
                      <td className="px-6 py-3 text-[14px] font-mono text-[#242424] text-right">{p.qty}</td>
                      <td className="px-6 py-3 text-[14px] font-mono text-[#0066cc] font-[600] text-right">₫{p.revenue.toLocaleString()}</td>
                      <td className="px-6 py-3 text-center">
                         {p.trend === 'up' ? (
                            <span className="inline-flex items-center text-[#28a745] bg-[#28a745]/10 px-2 py-0.5 rounded text-[12px] font-[600]">
                              <ArrowUpRight className="w-3 h-3 mr-1" /> Up
                            </span>
                         ) : (
                            <span className="inline-flex items-center text-[#dc3545] bg-[#dc3545]/10 px-2 py-0.5 rounded text-[12px] font-[600]">
                              <ArrowDownRight className="w-3 h-3 mr-1" /> Down
                            </span>
                         )}
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
