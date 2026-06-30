'use client';

import React, { useEffect, useState, use } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { getWarehouses, getReplenishmentRequests } from '@/features/inventory/services/inventoryService';
import { Warehouse, ReplenishmentRequest } from '@/features/inventory/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { RefreshCw, Boxes, Calendar, FileText, User, Search } from 'lucide-react';
import { cn } from '@/lib/utils';
import { toast } from 'sonner';
import { APP_ROUTES } from '@/config/constants';
import { TablePagination } from '@/components/ui/table-pagination';

export default function ReplenishmentsListPage({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = use(params);
  const router = useRouter();

  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState<string>('');
  const [requests, setRequests] = useState<ReplenishmentRequest[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  
  // Search State
  const [searchQuery, setSearchQuery] = useState('');
  const [appliedSearch, setAppliedSearch] = useState('');

  // Tabs filter state
  const [activeTab, setActiveTab] = useState<'ALL' | 'OPEN' | 'RESOLVED' | 'CANCELED'>('ALL');

  // Pagination
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  const [limit, setLimit] = useState(10);

  // Fetch warehouses
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

  const fetchReplenishments = () => {
    if (!selectedWarehouseId) return;

    setIsLoading(true);
    getReplenishmentRequests(orgId, selectedWarehouseId, { 
      page, 
      limit,
      status: activeTab,
      search: appliedSearch || undefined 
    })
      .then(res => {
        setRequests(res.data || []);
        setTotalItems(res.pagination?.totalItems || res.total || 0);
        setTotalPages(res.pagination?.totalPages || res.totalPages || Math.ceil((res.total || 1) / limit) || 1);
      })
      .catch(err => {
        console.error(err);
        toast.error('Failed to fetch replenishment tickets');
      })
      .finally(() => setIsLoading(false));
  };

  useEffect(() => {
    fetchReplenishments();
  }, [orgId, selectedWarehouseId, page, appliedSearch, activeTab, limit]);

  const filteredRequests = requests;

  return (
    <div className="p-6 h-full flex flex-col min-h-0 overflow-hidden font-['Segoe_UI'] bg-white">
      {/* Header controls */}
      <div className="flex justify-between items-center mb-5 shrink-0">
        <div>
          <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Stock Replenishment Requests</h1>
          <span className="text-[14px] text-[#898989]">View requests submitted when stock levels were insufficient</span>
        </div>
        <div className="flex space-x-3 items-center">
          <div className="relative">
            <button 
              onClick={() => {
                setAppliedSearch(searchQuery);
                setPage(1);
              }}
              className="absolute left-3 top-1/2 -translate-y-1/2 text-[#898989] hover:text-[#0066cc] focus:outline-none transition-colors"
            >
              <Search className="w-4 h-4" />
            </button>
            <Input 
              placeholder="Search replenishment..." 
              value={searchQuery}
              onChange={e => {
                setSearchQuery(e.target.value);
                if (e.target.value === '') {
                  setAppliedSearch('');
                  setPage(1);
                }
              }}
              onKeyDown={e => {
                if (e.key === 'Enter') {
                  setAppliedSearch(searchQuery);
                  setPage(1);
                }
              }}
              className="pl-9 h-10 w-[240px] border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]" 
            />
          </div>

          <div className="flex items-center space-x-2">
            <span className="text-[13px] font-[600] text-[#4a4a4a] whitespace-nowrap">Warehouse:</span>
            <select
              value={selectedWarehouseId}
              onChange={e => {
                setSelectedWarehouseId(e.target.value);
                setPage(1);
              }}
              className="h-10 border border-[#d0d0d0] rounded-[4px] px-3 text-[13px] bg-white focus:outline-none focus:border-[#0066cc]"
            >
              {warehouses.map(wh => (
                <option key={wh.id} value={wh.id}>
                  [{wh.code}] {wh.name}
                </option>
              ))}
            </select>
          </div>

          <Button 
            onClick={fetchReplenishments}
            variant="outline" 
            className="border-[#d0d0d0] text-[#242424] h-10 px-3 bg-white rounded-[4px]"
          >
            <RefreshCw className="w-4 h-4" />
          </Button>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex space-x-1 border-b border-[#e0e0e0] mb-4 shrink-0">
        {(['ALL', 'OPEN', 'RESOLVED', 'CANCELED'] as const).map(tab => (
          <button
            key={tab}
            onClick={() => {
              setActiveTab(tab);
              setPage(1);
            }}
            className={cn(
              "px-4 py-2 text-[13px] font-[600] border-b-2 transition-all",
              activeTab === tab 
                ? "border-[#0066cc] text-[#0066cc]" 
                : "border-transparent text-[#64748b] hover:text-[#242424]"
            )}
          >
            {tab}
          </button>
        ))}
      </div>

      {/* Main Table */}
      <div className="flex-1 min-h-0 overflow-hidden bg-[#f8f8f8] p-4 -mx-6 -mb-6 border-t border-[#e0e0e0] flex flex-col">
        <div className="flex-1 overflow-auto bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)]">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-white border-b border-[#e0e0e0]">
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Warehouse</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Linked Stock Move</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Order No.</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Requested By</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Reason / Notes</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-center">Status</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Request Date</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-[#898989] text-[13px]">
                    <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-[#0066cc]" />
                    Loading requests...
                  </td>
                </tr>
              ) : filteredRequests.length === 0 ? (
                <tr>
                  <td colSpan={7} className="py-12 text-center text-[#898989] text-[13px]">
                    <div className="bg-[#f8f8f8] w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-3">
                      <Boxes className="w-6 h-6 text-[#d0d0d0]" />
                    </div>
                    No {activeTab !== 'ALL' ? activeTab.toLowerCase() : ''} replenishment requests found.
                  </td>
                </tr>
              ) : (
                filteredRequests.map((req) => {
                  const dateStr = new Date(req.createdAt).toLocaleString();

                  return (
                    <tr 
                      key={req.id} 
                      onClick={() => router.push(`${APP_ROUTES.INVENTORY.DOCUMENT_DETAIL(orgId, req.inventoryDocumentId)}?whId=${req.warehouseId}`)}
                      className="border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f0f4ff] transition-colors cursor-pointer group"
                    >
                      <td className="py-3.5 px-4 text-[13px] font-[500] text-[#242424]">
                        {req.warehouseName}
                      </td>
                      <td className="py-3.5 px-4 text-[13px] font-mono font-[700] text-[#0066cc] group-hover:underline">
                        <div className="flex items-center">
                          <FileText className="w-4 h-4 mr-1.5 text-[#898989]" />
                          {req.inventoryDocumentName}
                        </div>
                      </td>
                      <td className="py-3.5 px-4 text-[13px] text-[#4a4a4a] font-medium" onClick={(e) => e.stopPropagation()}>
                        {req.orderNumber && req.referenceId ? (
                          <Link 
                            href={APP_ROUTES.SALES.ORDER_DETAIL(orgId, req.referenceId)}
                            className="inline-flex items-center justify-center min-w-[110px] text-center px-2 py-0.5 rounded-[4px] text-[11px] font-[600] uppercase bg-[#e8f4fd] text-[#1b75bb] border border-[#d0e8fc] hover:bg-[#d0e8fc] hover:text-[#004499] transition-colors"
                          >
                            {req.orderNumber}
                          </Link>
                        ) : (
                          <span className="text-[#898989] italic">-</span>
                        )}
                      </td>
                      <td className="py-3.5 px-4 text-[13px] text-[#4a4a4a]">
                        <div className="flex items-center">
                          <User className="w-3.5 h-3.5 mr-1 text-[#898989]" />
                          {req.requestedBy ? `${req.requestedBy.firstName} ${req.requestedBy.lastName}` : 'System'}
                        </div>
                      </td>
                      <td className="py-3.5 px-4 text-[13px] text-[#898989] max-w-[250px] truncate">
                        {req.notes || 'No description provided.'}
                      </td>
                      <td className="py-3.5 px-4 text-center">
                        <span className={cn(
                          "inline-block px-2.5 py-0.5 rounded-[4px] min-w-[80px] text-center text-[11px] font-[600] uppercase",
                          req.status === 'OPEN' 
                            ? "bg-[#fff2cc] text-[#d68100]" 
                            : req.status === 'RESOLVED'
                              ? "bg-[#e2f0d9] text-[#385723]"
                              : "bg-[#f8d7da] text-[#721c24]"
                        )}>
                          {req.status}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 text-[12px] text-[#64748b]">
                        <div className="flex items-center">
                          <Calendar className="w-3.5 h-3.5 mr-1 text-[#898989]" />
                          {dateStr}
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
        {!isLoading && totalItems > 0 && (
          <TablePagination
            page={page}
            limit={limit}
            totalItems={totalItems}
            totalPages={totalPages}
            onPageChange={setPage}
            onLimitChange={(newLimit) => {
              setLimit(newLimit);
              setPage(1);
            }}
            className="mt-4 bg-white"
          />
        )}
      </div>
    </div>
  );
}
