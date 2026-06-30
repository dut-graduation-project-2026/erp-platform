'use client';

import React, { useEffect, useState, useMemo, use } from 'react';
import { getQuotations } from '@/features/sales/services/salesService';
import { SaleOrder } from '@/features/sales/types';
import { Button } from '@/components/ui/button';
import { Plus, Search } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { usePermissions } from '@/hooks/use-permissions';
import { cn } from '@/lib/utils';
import { Input } from '@/components/ui/input';
import { PERMISSIONS } from '@/config/permissions';
import { APP_ROUTES } from '@/config/constants';
import { TablePagination } from '@/components/ui/table-pagination';
import { getSaleTeams, getMySaleTeams, SaleTeamResponse } from '@/features/crm/services/crmService';

export default function QuotationsListPage({ params }: { params: Promise<{ orgId: string }> }) {
  const router = useRouter();
  const { orgId } = use(params);
  const [orders, setOrders] = useState<SaleOrder[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [appliedSearch, setAppliedSearch] = useState('');

  const { hasPermission } = usePermissions();

  // Sales Teams State
  const [saleTeams, setSaleTeams] = useState<SaleTeamResponse[]>([]);
  const [selectedSaleTeamId, setSelectedSaleTeamId] = useState<string>('');

  // Pagination State
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [totalItems, setTotalItems] = useState(0);
  const [limit, setLimit] = useState(10);

  useEffect(() => {
    const fetchSaleTeams = async () => {
      try {
        if (hasPermission(PERMISSIONS.ORDERS.READ_ALL)) {
          const res = await getSaleTeams(orgId, { limit: 100 });
          setSaleTeams(res.data || []);
        } else {
          const res = await getMySaleTeams(orgId);
          setSaleTeams(res || []);
        }
      } catch (err) {
        console.error('Error fetching sale teams:', err);
      }
    };
    if (orgId) {
      fetchSaleTeams();
    }
  }, [orgId]);

  useEffect(() => {
    setIsLoading(true);
    getQuotations(orgId, {
      search: appliedSearch.trim(),
      page,
      limit,
      saleTeamId: selectedSaleTeamId || undefined
    })
      .then(res => {
        setOrders(res.data || []);
        setTotalItems(res.pagination?.totalItems || res.total || 0);
        setTotalPages(res.pagination?.totalPages || res.totalPages || Math.ceil((res.total || 1) / limit) || 1);
      })
      .catch(console.error)
      .finally(() => setIsLoading(false));
  }, [orgId, page, appliedSearch, limit, selectedSaleTeamId]);

  const filteredOrders = useMemo(() => {
    return orders;
  }, [orders]);

  return (
    <div className="p-6 h-full flex flex-col min-h-0 overflow-hidden font-['Segoe_UI'] bg-white">
      <div className="flex justify-between items-center mb-6 shrink-0">
        <div>
          <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Quotations</h1>
          <span className="text-[14px] text-[#898989]">Manage your sales orders and quotes</span>
        </div>
        <div className="flex space-x-3">
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
              placeholder="Search by ID or customer..."
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
              className="pl-9 h-10 w-[250px] border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
            />
          </div>

          <select
            value={selectedSaleTeamId}
            onChange={(e) => {
              setSelectedSaleTeamId(e.target.value);
              setPage(1);
            }}
            className="h-10 border border-[#d0d0d0] hover:border-[#a0a0a0] focus:border-[#0066cc] rounded-[4px] px-3 text-[13px] outline-none bg-white transition-colors min-w-[160px]"
          >
            <option value="">All Sales Teams</option>
            {saleTeams.map((team) => (
              <option key={team.id} value={team.id}>
                {team.name}
              </option>
            ))}
          </select>

          {hasPermission(PERMISSIONS.ORDERS.CREATE) && (
            <Button
              onClick={() => router.push(APP_ROUTES.SALES.QUOTATION_NEW(orgId))}
              className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px] font-[600]"
            >
              <Plus className="w-4 h-4 mr-2" /> New Quotation
            </Button>
          )}
        </div>
      </div>

      <div className="flex-1 min-h-0 border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden flex flex-col">
        <div className="flex-1 overflow-auto">
          <table className="w-full text-left border-collapse">
            <thead className="bg-[#f8f8f8] sticky top-0 z-10 shadow-[0px_1px_0px_#e0e0e0]">
              <tr>
                <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[120px]">Number</th>
                <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0]">Customer</th>
                <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[150px]">Sales Team</th>
                <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[150px]">Order Date</th>
                <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[150px] text-right">Total</th>
                <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] w-[120px] text-center">Status</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr><td colSpan={6} className="p-4 text-center text-[#898989]">Loading...</td></tr>
              ) : filteredOrders.length === 0 ? (
                <tr><td colSpan={6} className="p-4 text-center text-[#898989]">No quotations found</td></tr>
              ) : (
                filteredOrders.map((order, idx) => (
                  <tr
                    key={order.id}
                    onClick={() => router.push(APP_ROUTES.SALES.QUOTATION_DETAIL(orgId, order.id))}
                    className={cn("border-b border-[#e0e0e0] hover:bg-[#f0f4ff] cursor-pointer", idx % 2 === 0 ? "bg-white" : "bg-[#fafafa]")}
                  >
                    <td className="px-4 py-3 text-[13px] text-[#242424] font-[600] border-r border-[#e0e0e0]">{order.orderNumber || order.code}</td>
                    <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0]">{order.partner?.name ?? order.lead?.name ?? '—'}</td>
                    <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0]">{order.saleTeamName ?? order.lead?.saleTeam?.name ?? '—'}</td>
                    <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0]">{order.createdAt?.split('T')[0] || '—'}</td>
                    <td className="px-4 py-3 text-[13px] text-[#242424] text-right font-mono border-r border-[#e0e0e0]">${Number(order.totalAmount ?? 0).toLocaleString()}</td>
                    <td className="px-4 py-3 text-center">
                      <span className={cn("px-2 py-1 text-[11px] font-[600] rounded uppercase",
                        order.status === 'CONFIRMED' ? "bg-[#0066cc]/10 text-[#0066cc]" :
                          order.status === 'COMPLETED' ? "bg-[#28a745]/10 text-[#28a745]" :
                            order.status === 'DRAFT' ? "bg-[#898989]/10 text-[#898989]" :
                              "bg-[#0066cc]/10 text-[#0066cc]"
                      )}>
                        {order.status === 'CONFIRMED' ? 'PENDING FULFILLMENT' : order.status}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
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
          className="mt-6"
        />
      )}
    </div>
  );
}
