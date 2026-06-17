'use client';

import React, { useEffect, useState, use } from 'react';
import {
  getWarehouses,
  getInventoryBalances,
  getAiInventoryAnalysis,
  getAiReorderRecommendations,
  confirmAiReorders,
  ProductAbcXyz,
  AiInventoryAnalysisResponse,
  AiReorderItem
} from '@/features/inventory/services/inventoryService';
import { Warehouse, InventoryBalance } from '@/features/inventory/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Search, Filter, RefreshCcw, ChevronLeft, ChevronRight, AlertTriangle, CheckCircle, Brain, ShoppingCart, Check, Loader2 } from 'lucide-react';
import { cn } from '@/lib/utils';
import { toast } from 'sonner';

export default function BalancesListPage({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = use(params);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState<string>('');
  const [balances, setBalances] = useState<InventoryBalance[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  // Pagination State
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  const limit = 10;

  // Actionable AI States
  const [activeTab, setActiveTab] = useState<'balances' | 'ai-analysis' | 'ai-reorder'>('balances');
  const [aiAnalysis, setAiAnalysis] = useState<AiInventoryAnalysisResponse | null>(null);
  const [isLoadingAnalysis, setIsLoadingAnalysis] = useState(false);
  const [reorderRecs, setReorderRecs] = useState<AiReorderItem[]>([]);
  const [isLoadingRecs, setIsLoadingRecs] = useState(false);
  const [isConfirmingReorder, setIsConfirmingReorder] = useState(false);

  // Load warehouses first
  useEffect(() => {
    getWarehouses(orgId)
      .then(res => {
        setWarehouses(res.data || []);
        if (res.data && res.data.length > 0) {
          setSelectedWarehouseId(res.data[0].id);
        }
      })
      .catch(err => {
        console.error(err);
        toast.error('Failed to load warehouses');
      });
  }, [orgId]);

  // Load balances when selectedWarehouseId, page, or searchQuery changes
  const fetchBalances = () => {
    if (!selectedWarehouseId) return;

    setIsLoading(true);
    getInventoryBalances(orgId, selectedWarehouseId, {
      search: searchQuery.trim(),
      page,
      limit
    })
      .then(res => {
        setBalances(res.data || []);
        setTotalItems(res.total || 0);
        setTotalPages(res.totalPages || Math.ceil((res.total || 1) / limit) || 1);
      })
      .catch(err => {
        console.error(err);
        toast.error('Failed to fetch stock balances');
      })
      .finally(() => setIsLoading(false));
  };

  const fetchAiAnalysis = async (force: boolean = false) => {
    setIsLoadingAnalysis(true);
    try {
      const data = await getAiInventoryAnalysis(orgId, force);
      setAiAnalysis(data);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoadingAnalysis(false);
    }
  };

  const fetchReorderRecommendations = async () => {
    setIsLoadingRecs(true);
    try {
      const data = await getAiReorderRecommendations(orgId);
      setReorderRecs(data.recommendations || []);
    } catch (err) {
      console.error(err)
    } finally {
      setIsLoadingRecs(false);
    }
  };

  const handleConfirmReorders = async () => {
    if (!selectedWarehouseId) {
      toast.error('Please select a warehouse to reorder');
      return;
    }
    if (reorderRecs.length === 0) {
      toast.info('No recommendations to approve');
      return;
    }

    setIsConfirmingReorder(true);
    try {
      await confirmAiReorders(orgId, selectedWarehouseId, reorderRecs);
      toast.success('Successfully approved and created automatic goods receipt!');
      fetchReorderRecommendations();
    } catch (err) {
      console.error(err);
      toast.error('Error while approving automatic goods receipt');
    } finally {
      setIsConfirmingReorder(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'balances') {
      fetchBalances();
    } else if (activeTab === 'ai-analysis') {
      fetchAiAnalysis(false);
    } else if (activeTab === 'ai-reorder') {
      fetchReorderRecommendations();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [orgId, selectedWarehouseId, page, activeTab]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(1);
    fetchBalances();
  };

  const handleWarehouseChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedWarehouseId(e.target.value);
    setPage(1);
  };

  const [aiSearchQuery, setAiSearchQuery] = useState('');

  const filteredAbcXyz = aiAnalysis?.abc_xyz_matrix?.filter(item =>
    item.productName.toLowerCase().includes(aiSearchQuery.toLowerCase()) ||
    item.productId.toLowerCase().includes(aiSearchQuery.toLowerCase())
  ) || [];

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white overflow-y-auto">
      {/* Top Controls */}
      <div className="flex justify-between items-center mb-6 shrink-0">
        <div>
          <h1 className="text-[24px] font-[600] text-[#242424] mb-1">
            {activeTab === 'balances' && 'Real-time Stock Levels'}
            {activeTab === 'ai-analysis' && 'AI Inventory & ABC-XYZ Analysis'}
            {activeTab === 'ai-reorder' && 'AI Automated Reorder Recommendations'}
          </h1>
          <span className="text-[14px] text-[#898989]">
            {activeTab === 'balances' && 'View current physical balances and check item availability'}
            {activeTab === 'ai-analysis' && 'Inventory classification by value (ABC) and sales frequency (XYZ) powered by Gemma-31B-Reasoning'}
            {activeTab === 'ai-reorder' && 'Approve goods receipt based on Reorder Point (ROP) and Economic Order Quantity (EOQ)'}
          </span>
        </div>
        <div className="flex space-x-3 items-center">
          {/* Warehouse Selector */}
          <div className="flex items-center space-x-2">
            <span className="text-[13px] font-[600] text-[#4a4a4a] whitespace-nowrap">Warehouse:</span>
            <select
              value={selectedWarehouseId}
              onChange={handleWarehouseChange}
              className="h-10 border border-[#d0d0d0] rounded-[4px] px-3 text-[13px] bg-white focus:outline-none focus:border-[#0066cc]"
            >
              {warehouses.length === 0 ? (
                <option value="">No Warehouses Available</option>
              ) : (
                warehouses.map(wh => (
                  <option key={wh.id} value={wh.id}>
                    [{wh.code}] {wh.name}
                  </option>
                ))
              )}
            </select>
          </div>

          {activeTab === 'balances' && (
            <>
              <form onSubmit={handleSearchSubmit} className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
                <Input
                  placeholder="Search by product name/SKU..."
                  value={searchQuery}
                  onChange={e => setSearchQuery(e.target.value)}
                  className="pl-9 h-10 w-[260px] border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                />
              </form>

              <Button
                onClick={fetchBalances}
                variant="outline"
                className="border-[#d0d0d0] text-[#242424] h-10 px-3 bg-white rounded-[4px]"
              >
                <RefreshCcw className="w-4 h-4" />
              </Button>
            </>
          )}

          {activeTab === 'ai-analysis' && (
            <Button
              onClick={() => fetchAiAnalysis(true)}
              disabled={isLoadingAnalysis}
              className="bg-[#0066cc] text-white hover:bg-[#0052a3] h-10 px-4 rounded-[4px] font-[600] flex items-center gap-1.5"
            >
              {isLoadingAnalysis ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <RefreshCcw className="w-4 h-4" />
              )}
              Re-analyze with AI
            </Button>
          )}

          {activeTab === 'ai-reorder' && (
            <Button
              onClick={fetchReorderRecommendations}
              disabled={isLoadingRecs}
              variant="outline"
              className="border-[#d0d0d0] text-[#242424] h-10 px-3 bg-white rounded-[4px]"
            >
              <RefreshCcw className="w-4 h-4" />
            </Button>
          )}
        </div>
      </div>

      {/* Tab Switcher */}
      <div className="flex space-x-1 border-b border-[#e0e0e0] mb-6 shrink-0">
        <button
          onClick={() => setActiveTab('balances')}
          className={cn(
            "pb-3 px-4 text-[13px] font-[600] border-b-2 transition-all",
            activeTab === 'balances'
              ? "border-[#0066cc] text-[#0066cc]"
              : "border-transparent text-[#898989] hover:text-[#242424]"
          )}
        >
          Actual Inventory Balance
        </button>
        <button
          onClick={() => setActiveTab('ai-analysis')}
          className={cn(
            "pb-3 px-4 text-[13px] font-[600] border-b-2 transition-all flex items-center gap-1.5",
            activeTab === 'ai-analysis'
              ? "border-[#0066cc] text-[#0066cc]"
              : "border-transparent text-[#898989] hover:text-[#242424]"
          )}
        >
          <Brain className="w-4.5 h-4.5" /> ABC-XYZ Analysis (Inventory Optimization)
        </button>
        <button
          onClick={() => setActiveTab('ai-reorder')}
          className={cn(
            "pb-3 px-4 text-[13px] font-[600] border-b-2 transition-all flex items-center gap-1.5",
            activeTab === 'ai-reorder'
              ? "border-[#0066cc] text-[#0066cc]"
              : "border-transparent text-[#898989] hover:text-[#242424]"
          )}
        >
          <ShoppingCart className="w-4.5 h-4.5" /> AI Reorder Recommendations
          {reorderRecs.length > 0 && (
            <span className="bg-[#dc3545] text-white text-[10px] font-bold px-1.5 py-0.5 rounded-full ml-1 animate-pulse">
              {reorderRecs.length}
            </span>
          )}
        </button>
      </div>

      {/* ────────────────── TAB 1: REAL-TIME BALANCES ────────────────── */}
      {activeTab === 'balances' && (
        <div className="flex-1 overflow-auto bg-[#f8f8f8] p-4 -mx-6 -mb-6 border-t border-[#e0e0e0] flex flex-col justify-between">
          <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-white border-b border-[#e0e0e0]">
                  <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">SKU Code</th>
                  <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Product Name</th>
                  <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">Unit Price</th>
                  <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">Physical Stock</th>
                  <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-center">Status</th>
                  <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Last Updated</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr>
                    <td colSpan={6} className="py-12 text-center text-[#898989] text-[13px]">
                      <RefreshCcw className="w-6 h-6 animate-spin mx-auto mb-2 text-[#0066cc]" />
                      Fetching inventory balances...
                    </td>
                  </tr>
                ) : balances.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="py-12 text-center text-[#898989] text-[13px]">
                      No stock balance records found in this warehouse.
                    </td>
                  </tr>
                ) : (
                  balances.map((bal) => {
                    const qty = bal.quantity || 0;
                    const isLowStock = qty <= 5;
                    const formattedDate = new Date(bal.updatedAt).toLocaleString();

                    return (
                      <tr
                        key={bal.id}
                        className="border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f9fafb] transition-colors"
                      >
                        <td className="py-3.5 px-4 font-mono text-[12px] font-[600] text-[#0066cc]">
                          {bal.product?.sku || 'N/A'}
                        </td>
                        <td className="py-3.5 px-4 text-[13px] font-[500] text-[#242424]">
                          {bal.product?.name || 'Unknown Product'}
                        </td>
                        <td className="py-3.5 px-4 text-[13px] text-right font-[500] text-[#4a4a4a]">
                          ${(bal.product?.price || 0).toLocaleString(undefined, { minimumFractionDigits: 2 })}
                        </td>
                        <td className="py-3.5 px-4 text-right">
                          <span className={cn(
                            "text-[14px] font-[700]",
                            isLowStock ? "text-[#dc3545]" : "text-[#242424]"
                          )}>
                            {qty.toLocaleString()}
                          </span>
                        </td>
                        <td className="py-3.5 px-4 text-center">
                          <span className={cn(
                            "inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-[4px] min-w-[110px] justify-center text-[11px] font-[600]",
                            qty === 0
                              ? "bg-[#fbe5d6] text-[#c65911]"
                              : isLowStock
                                ? "bg-[#fff2cc] text-[#d68100]"
                                : "bg-[#e2f0d9] text-[#385723]"
                          )}>
                            {qty === 0 ? (
                              <>Out of Stock</>
                            ) : isLowStock ? (
                              <><AlertTriangle className="w-3 h-3 mr-1" /> Low Stock</>
                            ) : (
                              <><CheckCircle className="w-3 h-3 mr-1" /> In Stock</>
                            )}
                          </span>
                        </td>
                        <td className="py-3.5 px-4 text-[12px] text-[#898989]">
                          {formattedDate}
                          {bal.updatedBy && (
                            <span className="block text-[10px] text-[#b0b0b0]">
                              by {bal.updatedBy.firstName} {bal.updatedBy.lastName}
                            </span>
                          )}
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

          {/* Pagination Footer */}
          {!isLoading && totalItems > 0 && (
            <div className="mt-4 bg-white border border-[#e0e0e0] rounded-[4px] px-4 py-3 flex items-center justify-between text-[13px] text-[#64748b] shrink-0 shadow-sm">
              <span>
                Showing {((page - 1) * limit) + 1} to {Math.min(page * limit, totalItems)} of {totalItems} items
              </span>
              <div className="flex items-center gap-1">
                <button
                  onClick={() => setPage(p => Math.max(1, p - 1))}
                  disabled={page === 1}
                  className="p-1 hover:bg-[#f8f8f8] hover:text-[#242424] rounded disabled:opacity-50 disabled:hover:bg-transparent"
                >
                  <ChevronLeft className="w-4 h-4" />
                </button>
                <div className="flex items-center gap-1">
                  {Array.from({ length: totalPages }).map((_, i) => (
                    <button
                      key={i}
                      className={cn(
                        "w-7 h-7 rounded flex items-center justify-center font-medium",
                        page === i + 1 ? "bg-[#0066cc] text-white" : "hover:bg-[#f8f8f8] text-[#242424]"
                      )}
                      onClick={() => setPage(i + 1)}
                    >
                      {i + 1}
                    </button>
                  ))}
                </div>
                <button
                  onClick={() => setPage(p => Math.min(totalPages, p + 1))}
                  disabled={page === totalPages}
                  className="p-1 hover:bg-[#f8f8f8] hover:text-[#242424] rounded disabled:opacity-50 disabled:hover:bg-transparent"
                >
                  <ChevronRight className="w-4 h-4" />
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* ────────────────── TAB 2: AI ABC-XYZ ANALYSIS ────────────────── */}
      {activeTab === 'ai-analysis' && (
        <div className="flex-1 bg-[#f8f8f8] p-5 -mx-6 -mb-6 border-t border-[#e0e0e0] space-y-6 overflow-y-auto">
          {isLoadingAnalysis ? (
            <div className="flex flex-col items-center justify-center py-24 bg-white border border-[#e0e0e0] rounded-[4px]">
              <Loader2 className="w-8 h-8 animate-spin text-[#0066cc] mb-3" />
              <span className="text-[14px] text-[#898989] font-medium">AI is calculating ABC-XYZ inventory matrix...</span>
            </div>
          ) : !aiAnalysis ? (
            <div className="text-center py-20 bg-white border border-[#e0e0e0] rounded-[4px]">
              <Brain className="w-10 h-10 text-[#898989] mx-auto mb-2" />
              <p className="text-[14px] text-[#4a4a4a] mb-4">No AI inventory analysis data available.</p>
              <Button onClick={() => fetchAiAnalysis(true)} className="bg-[#0066cc] text-white">
                Start Analysis
              </Button>
            </div>
          ) : (
            <>
              {/* Summary Dashboard Card */}
              <div className="border border-[#0066cc]/20 rounded-[4px] p-5 bg-[#0066cc]/[0.02] shadow-sm hover:border-[#0066cc]/40 transition-all duration-300">
                <div className="flex items-start space-x-3 mb-4">
                  <div className="w-9 h-9 rounded-[4px] bg-[#f0f4ff] flex items-center justify-center text-[#0066cc] font-semibold text-[16px] shrink-0 border border-blue-100 shadow-sm">
                    🤖
                  </div>
                  <div>
                    <h3 className="text-[15px] font-[700] text-[#242424]">AI Inventory Recommendation Summary</h3>
                    <p className="text-[12px] text-[#898989]">In-depth insights into product lines and safety stock</p>
                  </div>
                </div>

                <p className="text-[13px] leading-relaxed text-[#4a4a4a] italic bg-white p-3 rounded border border-gray-100 mb-4">
                  &quot;{aiAnalysis.summary}&quot;
                </p>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 border-t border-dashed border-[#e0e0e0] pt-4">
                  <div>
                    <span className="text-[11px] font-[600] text-[#898989] uppercase tracking-wider block mb-2">Recommended Action Optimizations</span>
                    <ul className="space-y-1.5">
                      {aiAnalysis.recommendations?.map((rec, idx) => (
                        <li key={idx} className="text-[12.5px] text-[#4a4a4a] flex items-start">
                          <span className="text-[#0066cc] mr-1.5 font-bold">•</span>
                          {rec}
                        </li>
                      ))}
                    </ul>
                  </div>
                  <div className="bg-white p-4 rounded border border-[#e0e0e0] flex flex-col justify-between">
                    <div>
                      <span className="text-[11px] font-[600] text-[#898989] uppercase tracking-wider block mb-1">Items Needing Urgent Attention</span>
                      <p className="text-[12px] text-[#898989] mb-3">Stock below Reorder Point (ROP)</p>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-[28px] font-[800] text-[#dc3545]">
                        {aiAnalysis.critical_stock_count} SKUs
                      </span>
                      {aiAnalysis.critical_stock_count > 0 && (
                        <Button
                          onClick={() => setActiveTab('ai-reorder')}
                          className="bg-[#dc3545] hover:bg-[#c82333] text-white text-[12px] h-8 rounded-[4px] px-3 font-[600]"
                        >
                          Process Reorder Immediately
                        </Button>
                      )}
                    </div>
                  </div>
                </div>
              </div>

              {/* ABC-XYZ Matrix Description Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="bg-white border border-[#e0e0e0] p-4 rounded-[4px]">
                  <h4 className="text-[13px] font-[700] text-[#242424] mb-2 flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-[#0066cc]"></span>
                    ABC Classification Rules (By Sales Value)
                  </h4>
                  <ul className="text-[12px] text-[#4a4a4a] space-y-1 pl-3.5 list-disc">
                    <li><strong>Category A (High Value):</strong> Accounts for ~70-80% of sales value but only 10-20% of SKUs. Needs tight control.</li>
                    <li><strong>Category B (Medium Value):</strong> Accounts for ~15-20% of sales value and ~30% of SKUs.</li>
                    <li><strong>Category C (Low Value):</strong> Accounts for ~5-10% of value but most SKUs (~50%). Simple periodic ordering.</li>
                  </ul>
                </div>
                <div className="bg-white border border-[#e0e0e0] p-4 rounded-[4px]">
                  <h4 className="text-[13px] font-[700] text-[#242424] mb-2 flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-[#ffb703]"></span>
                    XYZ Classification Rules (By Demand Predictability)
                  </h4>
                  <ul className="text-[12px] text-[#4a4a4a] space-y-1 pl-3.5 list-disc">
                    <li><strong>Category X (Stable Demand):</strong> Regular demand, very easy to predict. Safety stock level can be low.</li>
                    <li><strong>Category Y (Fluctuating Demand):</strong> Demand fluctuates by season or cycle. Moderate safety stock needed.</li>
                    <li><strong>Category Z (Erratic Demand):</strong> Hard to predict or occurs randomly. High safety stock needed to avoid stockouts.</li>
                  </ul>
                </div>
              </div>

              {/* Matrix Table */}
              <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-sm overflow-hidden">
                <div className="p-4 border-b border-[#e0e0e0] flex justify-between items-center bg-gray-50">
                  <span className="text-[13px] font-[700] text-[#242424]">AI Product Classification Matrix (ABC-XYZ)</span>
                  <div className="relative">
                    <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-[#898989]" />
                    <Input
                      placeholder="Search analyzed products..."
                      value={aiSearchQuery}
                      onChange={e => setAiSearchQuery(e.target.value)}
                      className="pl-8 h-8 w-[220px] text-[12px] border-[#d0d0d0] rounded-[4px]"
                    />
                  </div>
                </div>
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-white border-b border-[#e0e0e0]">
                      <th className="py-2.5 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Product Name</th>
                      <th className="py-2.5 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-center">ABC Category</th>
                      <th className="py-2.5 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-center">XYZ Category</th>
                      <th className="py-2.5 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">Current Stock</th>
                      <th className="py-2.5 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">Reorder Point (ROP)</th>
                      <th className="py-2.5 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">Economic Order Qty (EOQ)</th>
                      <th className="py-2.5 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-center">AI Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredAbcXyz.length === 0 ? (
                      <tr>
                        <td colSpan={7} className="py-10 text-center text-[#898989] text-[13px]">
                          No products found matching the search.
                        </td>
                      </tr>
                    ) : (
                      filteredAbcXyz.map((item) => (
                        <tr key={item.productId} className="border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f9fafb] text-[13px]">
                          <td className="py-2.5 px-4 font-medium text-[#242424]">{item.productName}</td>
                          <td className="py-2.5 px-4 text-center">
                            <span className={cn(
                              "px-2 py-0.5 rounded text-[11px] font-bold",
                              item.abcClass === 'A' ? "bg-red-50 text-red-600 border border-red-200" :
                                item.abcClass === 'B' ? "bg-blue-50 text-blue-600 border border-blue-200" :
                                  "bg-gray-50 text-gray-500 border border-gray-200"
                            )}>
                              Cat {item.abcClass}
                            </span>
                          </td>
                          <td className="py-2.5 px-4 text-center">
                            <span className={cn(
                              "px-2 py-0.5 rounded text-[11px] font-bold",
                              item.xyzClass === 'X' ? "bg-emerald-50 text-emerald-600 border border-emerald-200" :
                                item.xyzClass === 'Y' ? "bg-amber-50 text-amber-600 border border-amber-200" :
                                  "bg-purple-50 text-purple-600 border border-purple-200"
                            )}>
                              Cat {item.xyzClass}
                            </span>
                          </td>
                          <td className="py-2.5 px-4 text-right font-semibold text-[#242424]">{item.currentStock}</td>
                          <td className="py-2.5 px-4 text-right font-mono text-[#898989]">{item.rop}</td>
                          <td className="py-2.5 px-4 text-right font-mono text-[#898989]">{item.eoq}</td>
                          <td className="py-2.5 px-4 text-center">
                            <span className={cn(
                              "inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-[600]",
                              item.status === 'CRITICAL' ? "bg-red-100 text-red-800" :
                                item.status === 'WARNING' ? "bg-amber-100 text-amber-800" :
                                  "bg-green-100 text-green-800"
                            )}>
                              {item.status}
                            </span>
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>
      )}

      {/* ────────────────── TAB 3: AI REORDER RECOMMENDATIONS ────────────────── */}
      {activeTab === 'ai-reorder' && (
        <div className="flex-1 bg-[#f8f8f8] p-5 -mx-6 -mb-6 border-t border-[#e0e0e0] space-y-6 overflow-y-auto">
          {isLoadingRecs ? (
            <div className="flex flex-col items-center justify-center py-24 bg-white border border-[#e0e0e0] rounded-[4px]">
              <Loader2 className="w-8 h-8 animate-spin text-[#0066cc] mb-3" />
              <span className="text-[14px] text-[#898989] font-medium">AI is analyzing low stock products and calculating EOQ...</span>
            </div>
          ) : reorderRecs.length === 0 ? (
            <div className="text-center py-24 bg-white border border-[#e0e0e0] rounded-[4px]">
              <Check className="w-10 h-10 text-green-500 mx-auto mb-2" />
              <h4 className="text-[14px] font-bold text-[#242424]">Stock at Safe Levels!</h4>
              <p className="text-[12px] text-[#898989] mt-1">Currently, no products are below their Reorder Point (ROP).</p>
            </div>
          ) : (
            <>
              {/* Batch Action Banner */}
              <div className="border border-[#28a745]/20 rounded-[4px] p-5 bg-[#28a745]/[0.02] shadow-sm flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div className="flex items-start space-x-3">
                  <div className="w-9 h-9 rounded-[4px] bg-[#e2f0d9] flex items-center justify-center text-[#28a745] font-semibold text-[16px] shrink-0 border border-green-100 shadow-sm">
                    💡
                  </div>
                  <div>
                    <h3 className="text-[15px] font-[700] text-[#242424]">Immediate Action: Create Goods Receipt from AI Proposal</h3>
                    <p className="text-[12px] text-[#898989]">Approve and automatically create Draft Goods Receipt for the products below.</p>
                  </div>
                </div>
                <Button
                  onClick={handleConfirmReorders}
                  disabled={isConfirmingReorder}
                  className="bg-[#28a745] hover:bg-[#218838] text-white h-10 px-5 rounded-[4px] font-[600] flex items-center gap-1.5 shadow-sm transition-all"
                >
                  {isConfirmingReorder ? (
                    <Loader2 className="w-4 h-4 animate-spin" />
                  ) : (
                    <Check className="w-4 h-4" />
                  )}
                  Approve Reorder for {reorderRecs.length} Items
                </Button>
              </div>

              {/* Recommendations Table */}
              <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-sm overflow-hidden">
                <div className="p-4 border-b border-[#e0e0e0] bg-gray-50">
                  <span className="text-[13px] font-[700] text-[#242424]">List of Recommended Reorder Items</span>
                </div>
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-white border-b border-[#e0e0e0]">
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Product</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Warehouse</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">Current Stock</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">ROP Point</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">Recommended (EOQ)</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-center">Urgency</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">AI Notes</th>
                    </tr>
                  </thead>
                  <tbody>
                    {reorderRecs.map((item) => (
                      <tr key={item.productId} className="border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f9fafb] text-[13px]">
                        <td className="py-3.5 px-4 font-semibold text-[#242424]">{item.productName}</td>
                        <td className="py-3.5 px-4 text-[#4a4a4a]">{item.warehouseName}</td>
                        <td className="py-3.5 px-4 text-right font-mono font-bold text-red-500">{item.currentStock}</td>
                        <td className="py-3.5 px-4 text-right font-mono text-[#898989]">{item.rop}</td>
                        <td className="py-3.5 px-4 text-right font-mono font-bold text-[#28a745]">{item.recommendedQuantity}</td>
                        <td className="py-3.5 px-4 text-center">
                          <span className={cn(
                            "px-2 py-0.5 rounded text-[11px] font-bold",
                            item.urgency === 'HIGH' ? "bg-red-100 text-red-800" :
                              item.urgency === 'MEDIUM' ? "bg-amber-100 text-amber-800" :
                                "bg-blue-100 text-blue-800"
                          )}>
                            {item.urgency}
                          </span>
                        </td>
                        <td className="py-3.5 px-4 text-[#898989] text-[12px] italic">{item.notes}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>
      )}
    </div>
  );
}
