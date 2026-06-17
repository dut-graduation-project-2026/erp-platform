'use client';

import React, { useState, useEffect, use } from 'react';
import { 
  Download, 
  TrendingUp, 
  Award, 
  Target, 
  DollarSign, 
  Users,
  Loader2
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { 
  AreaChart, 
  Area, 
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
import {
  getSalesSummary,
  getRevenueTrend,
  getSalesConversionFunnel,
  getSalesTopProducts,
  getSalesCategoryDistribution,
  getAiSalesForecast,
  SalesSummaryResponse,
  RevenueTrendPoint,
  OrderStatusCount,
  TopProductResponse,
  CategorySalesDistribution,
  AiSalesForecastResponse
} from '@/features/sales/services/salesService';

export default function SalesAnalyticsPage({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = use(params);
  const [timePeriod, setTimePeriod] = useState('Last 6 Months');
  const [isLoading, setIsLoading] = useState(true);

  // States for API data
  const [summary, setSummary] = useState<SalesSummaryResponse | null>(null);
  const [revenueTrend, setRevenueTrend] = useState<RevenueTrendPoint[]>([]);
  const [conversionFunnel, setConversionFunnel] = useState<OrderStatusCount[]>([]);
  const [topProducts, setTopProducts] = useState<TopProductResponse[]>([]);
  const [categoryDistribution, setCategoryDistribution] = useState<CategorySalesDistribution[]>([]);

  // Actionable AI states
  const [aiForecast, setAiForecast] = useState<AiSalesForecastResponse | null>(null);
  const [showForecast, setShowForecast] = useState(false);

  useEffect(() => {
    const fetchAnalytics = async () => {
      setIsLoading(true);
      try {
        const periodType = timePeriod === 'Last 6 Months' ? 'MONTH' : 'YEAR';
        const months = timePeriod === 'Last 6 Months' ? 6 : 12;

        const [summaryRes, trendRes, funnelRes, productsRes, categoryRes] = await Promise.all([
          getSalesSummary(orgId, { periodType }),
          getRevenueTrend(orgId, { months }),
          getSalesConversionFunnel(orgId, { periodType }),
          getSalesTopProducts(orgId, { limit: 10 }),
          getSalesCategoryDistribution(orgId)
        ]);

        setSummary(summaryRes);
        setRevenueTrend(trendRes);
        setConversionFunnel(funnelRes);
        setTopProducts(productsRes);
        setCategoryDistribution(categoryRes);

        // Call AI sales forecast API (wrapped in a separate try-catch to avoid blocking the whole dashboard)
        try {
          const forecastRes = await getAiSalesForecast(orgId);
          setAiForecast(forecastRes);
        } catch (aiErr) {
          console.error('Failed to load AI Sales Forecast:', aiErr);
        }
      } catch (error) {
        console.error('Error fetching sales analytics:', error);
        toast.error('Failed to load sales intelligence data.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchAnalytics();
  }, [orgId, timePeriod]);

  const handleExport = () => {
    toast.success('Sales report exported to PDF');
  };

  // 1. Quota Attainment calculation
  const salesTarget = 5000000; // Mock $5,000,000 yearly sales target
  const ytdNetRevenue = summary?.ytdNetRevenue || 0;
  const quotaAttainment = salesTarget > 0 ? (ytdNetRevenue / salesTarget) * 100 : 0;

  // 2. Format Trend Data for AreaChart
  const chartData = revenueTrend.map(point => {
    const date = new Date(point.date);
    const monthName = date.toLocaleDateString('en-US', { month: 'short' });
    return {
      name: `${monthName} ${date.getFullYear()}`,
      sales: point.grossSales,
      margin: point.netMargin
    };
  });

  // Actionable AI: Map Forecast Points
  const forecastChartData = aiForecast?.forecast_points?.map(p => {
    const d = new Date(p.date);
    const label = isNaN(d.getTime()) ? p.date : `${d.getDate()}/${d.getMonth() + 1}`;
    return {
      name: label,
      historical: p.historical_revenue,
      predicted: p.predicted_revenue
    };
  }) || [];

  // 3. Process conversion funnel data
  const getFunnelData = () => {
    const draft = conversionFunnel.find(c => c.status === 'DRAFT')?.count || 0;
    const sent = conversionFunnel.find(c => c.status === 'SENT')?.count || 0;
    const confirmed = conversionFunnel.find(c => c.status === 'CONFIRMED')?.count || 0;
    const completed = conversionFunnel.find(c => c.status === 'COMPLETED')?.count || 0;
    const waiting = conversionFunnel.find(c => c.status === 'WAITING_FOR_STOCK')?.count || 0;

    const totalDraft = draft;
    const totalSent = sent + confirmed + completed + waiting;
    const totalConfirmed = confirmed + completed;

    const draftToSentRate = totalDraft > 0 ? (totalSent / totalDraft) * 100 : 0;
    const sentToConfirmedRate = totalSent > 0 ? (totalConfirmed / totalSent) * 100 : 0;
    const overallRate = (totalDraft + totalSent) > 0 ? (totalConfirmed / (totalDraft + totalSent)) * 100 : 0;

    return {
      draft: totalDraft,
      sent: totalSent,
      confirmed: totalConfirmed,
      draftToSentRate,
      sentToConfirmedRate,
      overallRate
    };
  };

  const funnelStats = getFunnelData();

  // 4. Category share distribution
  const COLORS = ['#0066cc', '#898989', '#4a4a4a', '#242424', '#00b4d8', '#ffb703', '#fb8500'];
  const categoryShareData = categoryDistribution.map((item, idx) => ({
    name: item.categoryName || 'General',
    value: item.totalRevenue,
    color: COLORS[idx % COLORS.length]
  }));

  if (isLoading) {
    return (
      <div className="flex h-full w-full items-center justify-center bg-white">
        <div className="flex flex-col items-center space-y-2">
          <Loader2 className="h-8 w-8 animate-spin text-[#0066cc]" />
          <span className="text-[14px] text-[#898989]">Loading Sales Intelligence...</span>
        </div>
      </div>
    );
  }

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
            <span className="text-[22px] font-[700] text-[#242424]">${(summary?.ytdNetRevenue || 0).toLocaleString()}</span>
          </div>
          <div className="w-10 h-10 bg-[#f0f4ff] rounded-[4px] flex items-center justify-center text-[#0066cc]">
            <DollarSign className="w-5 h-5" />
          </div>
        </div>

        <div className="border border-[#e0e0e0] rounded-[4px] p-4 bg-white shadow-sm flex items-center justify-between">
          <div>
            <span className="text-[11px] font-[600] text-[#898989] uppercase tracking-wider block mb-1">Avg Deal Size</span>
            <span className="text-[22px] font-[700] text-[#242424]">${(summary?.avgDealSize || 0).toLocaleString(undefined, { maximumFractionDigits: 0 })}</span>
          </div>
          <div className="w-10 h-10 bg-[#f5f5f5] rounded-[4px] flex items-center justify-center text-[#898989]">
            <Target className="w-5 h-5" />
          </div>
        </div>

        <div className="border border-[#e0e0e0] rounded-[4px] p-4 bg-white shadow-sm flex items-center justify-between">
          <div>
            <span className="text-[11px] font-[600] text-[#898989] uppercase tracking-wider block mb-1">Quota Attainment</span>
            <span className="text-[22px] font-[700] text-[#28a745]">{quotaAttainment.toFixed(1)}%</span>
          </div>
          <div className="w-10 h-10 bg-[#e2f0d9] rounded-[4px] flex items-center justify-center text-[#385723]">
            <Award className="w-5 h-5" />
          </div>
        </div>

        <div className="border border-[#e0e0e0] rounded-[4px] p-4 bg-white shadow-sm flex items-center justify-between">
          <div>
            <span className="text-[11px] font-[600] text-[#898989] uppercase tracking-wider block mb-1">Active Sales Reps</span>
            <span className="text-[22px] font-[700] text-[#242424]">{summary?.activeSalesReps || 0} Representatives</span>
          </div>
          <div className="w-10 h-10 bg-[#f5f5f5] rounded-[4px] flex items-center justify-center text-[#898989]">
            <Users className="w-5 h-5" />
          </div>
        </div>
      </div>

      {/* Actionable AI: AI Assistant Panel */}
      {aiForecast && (
        <div className="border border-[#0066cc]/20 rounded-[4px] p-5 bg-[#0066cc]/[0.02] mb-6 shadow-sm hover:border-[#0066cc]/40 transition-all duration-300 font-['Segoe_UI']">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div className="flex items-start space-x-3">
              <div className="w-9 h-9 rounded-[4px] bg-[#f0f4ff] flex items-center justify-center text-[#0066cc] font-semibold text-[16px] shrink-0 shadow-sm border border-blue-100">
                🤖
              </div>
              <div>
                <h2 className="text-[15px] font-[700] text-[#242424] flex items-center gap-2">
                  AI Sales Forecast Assistant (Actionable AI)
                  <span className="text-[10px] bg-[#0066cc]/10 text-[#0066cc] px-2 py-0.5 rounded-[2px] font-bold uppercase tracking-wider">
                    Gemma-31B-Reasoning
                  </span>
                </h2>
                <p className="text-[12px] text-[#898989]">Automatic sales trend analysis and market demand forecasting</p>
              </div>
            </div>
            <div className="flex items-center space-x-2 self-end md:self-auto">
              <Button 
                onClick={() => setShowForecast(!showForecast)} 
                variant={showForecast ? "default" : "outline"}
                className={`h-9 px-4 rounded-[4px] font-[600] text-[12px] transition-all ${
                  showForecast 
                    ? "bg-[#0066cc] text-white hover:bg-[#0052a3]" 
                    : "border-[#d0d0d0] text-[#242424] bg-white hover:bg-gray-50"
                }`}
              >
                {showForecast ? "📊 View Actual Trend" : "🔮 Activate 30-Day AI Forecast"}
              </Button>
            </div>
          </div>
          
          <div className="mt-4 grid grid-cols-1 md:grid-cols-3 gap-5 border-t border-dashed border-[#e0e0e0] pt-4">
            <div className="md:col-span-2">
              <span className="text-[11px] font-[600] text-[#898989] uppercase tracking-wider block mb-1">AI Analysis & Insights</span>
              <p className="text-[13px] leading-relaxed text-[#4a4a4a] italic bg-white/50 p-3 rounded border border-gray-100">
                &quot;{aiForecast.summary || ''}&quot;
              </p>
            </div>
            <div>
              <span className="text-[11px] font-[600] text-[#898989] uppercase tracking-wider block mb-1">Actionable Recommendations</span>
              <ul className="text-[12px] text-[#4a4a4a] space-y-1.5 list-disc pl-4">
                {aiForecast.insights?.map((insight, idx) => (
                  <li key={idx} className="font-[500]">{insight}</li>
                ))}
              </ul>
            </div>
          </div>
        </div>
      )}

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
            {showForecast && forecastChartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={forecastChartData} margin={{ top: 10, right: 5, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="colorHist" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#0066cc" stopOpacity={0.15}/>
                      <stop offset="95%" stopColor="#0066cc" stopOpacity={0.0}/>
                    </linearGradient>
                    <linearGradient id="colorPred" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="#e67e22" stopOpacity={0.15}/>
                      <stop offset="95%" stopColor="#e67e22" stopOpacity={0.0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e8e8e8" />
                  <XAxis dataKey="name" stroke="#898989" fontSize={10} tickLine={false} />
                  <YAxis stroke="#898989" fontSize={11} tickLine={false} />
                  <Tooltip formatter={(value: number) => [`$${value.toLocaleString()}`, '']} />
                  <Area type="monotone" dataKey="historical" name="Actual Revenue" stroke="#0066cc" strokeWidth={2} fillOpacity={1} fill="url(#colorHist)" />
                  <Area type="monotone" dataKey="predicted" name="AI Predicted Revenue" stroke="#e67e22" strokeWidth={2} strokeDasharray="4 4" fillOpacity={1} fill="url(#colorPred)" />
                </AreaChart>
              </ResponsiveContainer>
            ) : chartData.length === 0 ? (
              <div className="flex h-full items-center justify-center text-[#898989]">No revenue data available</div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData} margin={{ top: 10, right: 5, left: -20, bottom: 0 }}>
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
                  <Tooltip formatter={(value: number) => [`$${value.toLocaleString()}`, '']} />
                  <Area type="monotone" dataKey="sales" name="Gross Sales" stroke="#0066cc" strokeWidth={2} fillOpacity={1} fill="url(#colorSales)" />
                  <Area type="monotone" dataKey="margin" name="Net Margin" stroke="#898989" strokeWidth={2} fillOpacity={1} fill="url(#colorMargin)" />
                </AreaChart>
              </ResponsiveContainer>
            )}
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
                  <span className="font-mono text-[#898989]">{funnelStats.draft} draft quotes</span>
                </div>
                <div className="w-full bg-[#f2f2f2] h-2 rounded-full overflow-hidden">
                  <div className="bg-[#898989] h-full w-[100%]"></div>
                </div>
              </div>

              <div>
                <div className="flex justify-between text-[13px] mb-1">
                  <span className="font-semibold text-[#242424]">2. Shared / Sent</span>
                  <span className="font-mono text-[#0066cc]">{funnelStats.sent} orders ({funnelStats.draftToSentRate.toFixed(1)}%)</span>
                </div>
                <div className="w-full bg-[#f2f2f2] h-2 rounded-full overflow-hidden">
                  <div className="bg-[#0066cc] h-full" style={{ width: `${Math.min(100, funnelStats.draftToSentRate)}%` }}></div>
                </div>
              </div>

              <div>
                <div className="flex justify-between text-[13px] mb-1">
                  <span className="font-semibold text-[#242424]">3. Confirmed & Invoiced</span>
                  <span className="font-mono text-[#28a745]">{funnelStats.confirmed} completed ({funnelStats.overallRate.toFixed(1)}%)</span>
                </div>
                <div className="w-full bg-[#f2f2f2] h-2 rounded-full overflow-hidden">
                  <div className="bg-[#28a745] h-full" style={{ width: `${Math.min(100, funnelStats.overallRate)}%` }}></div>
                </div>
              </div>
            </div>
          </div>

          <div className="border-t border-[#f5f5f5] pt-4 mt-6 flex justify-between items-center text-[13px] text-[#4a4a4a]">
            <span>Opportunity Conversion:</span>
            <strong className="text-[#28a745] flex items-center font-[700]">
              <TrendingUp className="w-4 h-4 mr-1" />
              {funnelStats.overallRate.toFixed(1)}% Overall
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
                <th className="py-2.5 px-4 text-[11px] font-bold text-[#898989] uppercase tracking-wider">Category</th>
                <th className="py-2.5 px-4 text-[11px] font-bold text-[#898989] uppercase tracking-wider text-right">Orders</th>
                <th className="py-2.5 px-4 text-[11px] font-bold text-[#898989] uppercase tracking-wider text-right">Qty Sold</th>
                <th className="py-2.5 px-4 text-[11px] font-bold text-[#898989] uppercase tracking-wider text-right">Gross Sales</th>
              </tr>
            </thead>
            <tbody>
              {topProducts.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-4 text-center text-[13px] text-[#898989]">No product sales record found</td>
                </tr>
              ) : (
                topProducts.map((p, idx) => (
                  <tr key={idx} className="border-b border-[#f5f5f5] last:border-b-0 hover:bg-[#fafafa]">
                    <td className="py-3 px-4 text-[13px] font-[600] text-[#242424]">{p.productName}</td>
                    <td className="py-3 px-4 text-[13px] text-[#898989]">{p.categoryName || 'General'}</td>
                    <td className="py-3 px-4 text-[13px] text-right font-mono">{p.orderCount}</td>
                    <td className="py-3 px-4 text-[13px] text-right font-mono">{p.quantitySold}</td>
                    <td className="py-3 px-4 text-[13px] text-right font-mono font-[600] text-[#0066cc]">
                      ${p.totalRevenue.toLocaleString()}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Category Share Donut */}
        <div className="border border-[#e0e0e0] rounded-[4px] p-5 bg-white shadow-sm">
          <h3 className="text-[14px] font-[700] text-[#242424] mb-1">Sales Category Share</h3>
          <p className="text-[12px] text-[#898989] mb-4">Revenue distribution across service lines</p>

          <div className="h-[170px] flex justify-center items-center">
            {categoryShareData.length === 0 ? (
              <div className="text-[13px] text-[#898989]">No category share data</div>
            ) : (
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
                  <Tooltip formatter={(value: number) => `$${value.toLocaleString()}`} />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>

          <div className="space-y-2 text-[12px] text-[#4a4a4a] max-h-[120px] overflow-y-auto">
            {categoryShareData.map((item, idx) => (
              <div key={idx} className="flex justify-between items-center border-t border-[#f5f5f5] pt-1.5 first:border-0 first:pt-0">
                <div className="flex items-center space-x-2">
                  <span className="w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: item.color }}></span>
                  <span className="truncate max-w-[120px]">{item.name}</span>
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
