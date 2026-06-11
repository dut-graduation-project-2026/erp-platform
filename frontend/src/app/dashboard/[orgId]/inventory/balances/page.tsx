'use client';

import React, { useEffect, useState, use } from 'react';
import { getWarehouses, getInventoryBalances } from '@/features/inventory/services/inventoryService';
import { Warehouse, InventoryBalance } from '@/features/inventory/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Search, Filter, RefreshCcw, ChevronLeft, ChevronRight, AlertTriangle, CheckCircle } from 'lucide-react';
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
        // compute total pages if totalPages is not returned
        setTotalPages(res.totalPages || Math.ceil((res.total || 1) / limit) || 1);
      })
      .catch(err => {
        console.error(err);
        toast.error('Failed to fetch stock balances');
      })
      .finally(() => setIsLoading(false));
  };

  useEffect(() => {
    fetchBalances();
  }, [orgId, selectedWarehouseId, page]);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setPage(1);
    fetchBalances();
  };

  const handleWarehouseChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedWarehouseId(e.target.value);
    setPage(1);
  };

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white">
      {/* Top Controls */}
      <div className="flex justify-between items-center mb-6 shrink-0">
        <div>
          <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Real-time Stock Levels</h1>
          <span className="text-[14px] text-[#898989]">View current physical balances and check item availability</span>
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
        </div>
      </div>

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
          <div className="mt-4 bg-white border border-[#e0e0e0] rounded-[4px] px-4 py-3 flex items-center justify-between text-[13px] text-[#64748b]">
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
    </div>
  );
}
