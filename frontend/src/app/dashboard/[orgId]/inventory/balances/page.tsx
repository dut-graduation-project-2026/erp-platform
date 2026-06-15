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
      toast.error('Không thể tải phân tích tồn kho AI');
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
      console.error(err);
      toast.error('Không thể tải khuyến nghị nhập kho AI');
    } finally {
      setIsLoadingRecs(false);
    }
  };

  const handleConfirmReorders = async () => {
    if (!selectedWarehouseId) {
      toast.error('Vui lòng chọn kho để nhập hàng');
      return;
    }
    if (reorderRecs.length === 0) {
      toast.info('Không có khuyến nghị nào cần duyệt');
      return;
    }

    setIsConfirmingReorder(true);
    try {
      await confirmAiReorders(orgId, selectedWarehouseId, reorderRecs);
      toast.success('Đã duyệt và tạo phiếu nhập kho tự động thành công!');
      fetchReorderRecommendations();
    } catch (err) {
      console.error(err);
      toast.error('Lỗi khi phê duyệt lệnh nhập kho tự động');
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
            {activeTab === 'ai-analysis' && 'Phân Tích Tồn Kho & ABC-XYZ AI'}
            {activeTab === 'ai-reorder' && 'Khuyến Nghị Nhập Kho Tự Động AI'}
          </h1>
          <span className="text-[14px] text-[#898989]">
            {activeTab === 'balances' && 'View current physical balances and check item availability'}
            {activeTab === 'ai-analysis' && 'Phân loại tồn kho theo giá trị (ABC) và tần suất bán hàng (XYZ) từ Gemma-31B-Reasoning'}
            {activeTab === 'ai-reorder' && 'Phê duyệt phiếu nhập kho dựa trên Điểm đặt hàng lại (ROP) và Lượng đặt tối ưu (EOQ)'}
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

      {/* Main Table */}
      <div className="flex-1 overflow-auto bg-[#f8f8f8] p-4 -mx-6 -mb-6 border-t border-[#e0e0e0] flex flex-col justify-between">
        <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-white border-b border-[#e0e0e0]">
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">SKU Code</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Product Name</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">Unit Price</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">Physical Stock</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Last Updated</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan={5} className="py-12 text-center text-[#898989] text-[13px]">
                    <RefreshCcw className="w-6 h-6 animate-spin mx-auto mb-2 text-[#0066cc]" />
                    Fetching inventory balances...
                  </td>
                </tr>
              ) : balances.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-12 text-center text-[#898989] text-[13px]">
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
              Phân Tích Lại Bằng AI
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
          📋 Số Dư Tồn Kho Thực Tế
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
          <Brain className="w-4.5 h-4.5" /> Phân Tích ABC-XYZ (Tối Ưu Tồn Kho)
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
          <ShoppingCart className="w-4.5 h-4.5" /> Khuyến Nghị Nhập Kho AI
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
                            "inline-flex items-center space-x-1 px-2.5 py-0.5 rounded-[12px] text-[11px] font-[600]",
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
              <span className="text-[14px] text-[#898989] font-medium">AI đang tính toán ma trận tồn kho ABC-XYZ...</span>
            </div>
          ) : !aiAnalysis ? (
            <div className="text-center py-20 bg-white border border-[#e0e0e0] rounded-[4px]">
              <Brain className="w-10 h-10 text-[#898989] mx-auto mb-2" />
              <p className="text-[14px] text-[#4a4a4a] mb-4">Chưa có dữ liệu phân tích tồn kho AI.</p>
              <Button onClick={() => fetchAiAnalysis(true)} className="bg-[#0066cc] text-white">
                Bắt đầu phân tích
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
                    <h3 className="text-[15px] font-[700] text-[#242424]">Tóm Tắt Khuyến Nghị Tồn Kho AI</h3>
                    <p className="text-[12px] text-[#898989]">Nhận định chuyên sâu về dòng sản phẩm và an toàn lưu kho</p>
                  </div>
                </div>

                <p className="text-[13px] leading-relaxed text-[#4a4a4a] italic bg-white p-3 rounded border border-gray-100 mb-4">
                  &quot;{aiAnalysis.summary}&quot;
                </p>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 border-t border-dashed border-[#e0e0e0] pt-4">
                  <div>
                    <span className="text-[11px] font-[600] text-[#898989] uppercase tracking-wider block mb-2">Đề xuất tối ưu hóa hành động</span>
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
                      <span className="text-[11px] font-[600] text-[#898989] uppercase tracking-wider block mb-1">Mặt hàng cần chú ý khẩn cấp</span>
                      <p className="text-[12px] text-[#898989] mb-3">Tồn kho dưới điểm an toàn (Reorder Point)</p>
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
                          Xử lý nhập hàng ngay
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
                    Quy Tắc Phân Nhóm ABC (Theo Giá Trị Hàng Bán)
                  </h4>
                  <ul className="text-[12px] text-[#4a4a4a] space-y-1 pl-3.5 list-disc">
                    <li><strong>Nhóm A (Giá Trị Cao):</strong> Chiếm ~70-80% giá trị bán hàng nhưng chỉ chiếm 10-20% số lượng SKU. Cần kiểm soát chặt chẽ.</li>
                    <li><strong>Nhóm B (Giá Trị Trung Bình):</strong> Chiếm ~15-20% giá trị bán hàng và ~30% SKU.</li>
                    <li><strong>Nhóm C (Giá Trị Thấp):</strong> Chiếm ~5-10% giá trị nhưng chiếm phần lớn SKU (~50%). Đặt hàng định kỳ đơn giản.</li>
                  </ul>
                </div>
                <div className="bg-white border border-[#e0e0e0] p-4 rounded-[4px]">
                  <h4 className="text-[13px] font-[700] text-[#242424] mb-2 flex items-center gap-1.5">
                    <span className="w-2 h-2 rounded-full bg-[#ffb703]"></span>
                    Quy Tắc Phân Nhóm XYZ (Theo Tính Dự Báo Nhu Cầu)
                  </h4>
                  <ul className="text-[12px] text-[#4a4a4a] space-y-1 pl-3.5 list-disc">
                    <li><strong>Nhóm X (Nhu Cầu Ổn Định):</strong> Nhu cầu đều đặn, rất dễ dự báo. Mức tồn kho an toàn có thể để thấp.</li>
                    <li><strong>Nhóm Y (Nhu Cầu Biến Động):</strong> Nhu cầu dao động theo mùa vụ hoặc chu kỳ. Cần dự trữ an toàn vừa phải.</li>
                    <li><strong>Nhóm Z (Nhu Cầu Thất Thường):</strong> Khó dự báo hoặc phát sinh ngẫu nhiên. Cần tồn kho dự phòng cao để tránh đứt hàng.</li>
                  </ul>
                </div>
              </div>

              {/* Matrix Table */}
              <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-sm overflow-hidden">
                <div className="p-4 border-b border-[#e0e0e0] flex justify-between items-center bg-gray-50">
                  <span className="text-[13px] font-[700] text-[#242424]">Bảng Đánh Giá Phân Lớp Sản Phẩm AI (ABC-XYZ)</span>
                  <div className="relative">
                    <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-[#898989]" />
                    <Input 
                      placeholder="Tìm kiếm sản phẩm phân tích..." 
                      value={aiSearchQuery}
                      onChange={e => setAiSearchQuery(e.target.value)}
                      className="pl-8 h-8 w-[220px] text-[12px] border-[#d0d0d0] rounded-[4px]" 
                    />
                  </div>
                </div>
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-white border-b border-[#e0e0e0]">
                      <th className="py-2.5 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Tên Sản Phẩm</th>
                      <th className="py-2.5 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-center">Nhóm ABC</th>
                      <th className="py-2.5 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-center">Nhóm XYZ</th>
                      <th className="py-2.5 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">Tồn Kho Hiện Tại</th>
                      <th className="py-2.5 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">Điểm Đặt Hàng Lại (ROP)</th>
                      <th className="py-2.5 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">Lượng Đặt Tối Ưu (EOQ)</th>
                      <th className="py-2.5 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-center">Trạng Thái AI</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredAbcXyz.length === 0 ? (
                      <tr>
                        <td colSpan={7} className="py-10 text-center text-[#898989] text-[13px]">
                          Không tìm thấy sản phẩm nào khớp với tìm kiếm.
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
                              Nhóm {item.abcClass}
                            </span>
                          </td>
                          <td className="py-2.5 px-4 text-center">
                            <span className={cn(
                              "px-2 py-0.5 rounded text-[11px] font-bold",
                              item.xyzClass === 'X' ? "bg-emerald-50 text-emerald-600 border border-emerald-200" :
                              item.xyzClass === 'Y' ? "bg-amber-50 text-amber-600 border border-amber-200" :
                              "bg-purple-50 text-purple-600 border border-purple-200"
                            )}>
                              Nhóm {item.xyzClass}
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
              <span className="text-[14px] text-[#898989] font-medium">AI đang phân tích các sản phẩm có tồn kho thấp và tính toán EOQ...</span>
            </div>
          ) : reorderRecs.length === 0 ? (
            <div className="text-center py-24 bg-white border border-[#e0e0e0] rounded-[4px]">
              <Check className="w-10 h-10 text-green-500 mx-auto mb-2" />
              <h4 className="text-[14px] font-bold text-[#242424]">Tồn Kho Đạt Mức An Toàn!</h4>
              <p className="text-[12px] text-[#898989] mt-1">Hiện không có sản phẩm nào có số lượng thấp dưới điểm đặt hàng lại (ROP).</p>
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
                    <h3 className="text-[15px] font-[700] text-[#242424]">Hành Động Tức Thời: Tạo Phiếu Nhập Kho Từ Đề Xuất AI</h3>
                    <p className="text-[12px] text-[#898989]">Duyệt và tự động tạo phiếu nhập kho Nháp (RECEIPT Draft) cho các sản phẩm bên dưới.</p>
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
                  Phê Duyệt Nhập {reorderRecs.length} Mặt Hàng
                </Button>
              </div>

              {/* Recommendations Table */}
              <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-sm overflow-hidden">
                <div className="p-4 border-b border-[#e0e0e0] bg-gray-50">
                  <span className="text-[13px] font-[700] text-[#242424]">Danh Sách Mặt Hàng Đề Xuất Nhập Hàng</span>
                </div>
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-white border-b border-[#e0e0e0]">
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Sản phẩm</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Kho hàng</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">Tồn hiện tại</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">Điểm ROP</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-right">Khuyên Nhập (EOQ)</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-center">Độ Khẩn Cấp</th>
                      <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Ghi chú từ AI</th>
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
