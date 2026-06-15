'use client';

import React, { useEffect, useState, useMemo, use } from 'react';
import { getOrders } from '@/features/sales/services/salesService';
import { SaleOrder } from '@/features/sales/types';
import { Button } from '@/components/ui/button';
import { Plus, Search, Building } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { usePermissions } from '@/hooks/use-permissions';
import { cn } from '@/lib/utils';
import { Input } from '@/components/ui/input';
import { PERMISSIONS } from '@/config/permissions';
import { ORDER_STATUS, ORDER_STATUS_CONFIG, OrderStatus } from '@/config/constants';

export default function OrdersListPage({ params }: { params: Promise<{ orgId: string }> }) {
  const router = useRouter();
  const { orgId } = use(params);
  const [orders, setOrders] = useState<SaleOrder[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const { hasPermission } = usePermissions();

  useEffect(() => {
    getOrders(orgId)
      .then(res => {
        setOrders(res.data || []);
      })
      .catch(console.error)
      .finally(() => setIsLoading(false));
  }, [orgId]);

  const filteredOrders = useMemo(() => {
    let result = orders;
    
    if (statusFilter !== 'ALL') {
      result = result.filter(o => o.status === statusFilter);
    }

    if (searchQuery.trim() !== '') {
      const q = searchQuery.toLowerCase();
      result = result.filter(o => 
        (o.orderNumber && o.orderNumber.toLowerCase().includes(q)) || 
        (o.code && o.code.toLowerCase().includes(q)) || 
        (o.partner?.name && o.partner.name.toLowerCase().includes(q))
      );
    }
    
    return result;
  }, [searchQuery, statusFilter, orders]);

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white">
      <div className="flex justify-between items-center mb-6 shrink-0">
         <div>
            <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Sales Orders</h1>
            <span className="text-[14px] text-[#898989]">Manage your confirmed sales orders</span>
         </div>
         <div className="flex space-x-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
              <Input 
                placeholder="Search by ID or customer..." 
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                className="pl-9 h-10 w-[250px] border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]" 
              />
            </div>
            <select
              value={statusFilter}
              onChange={e => setStatusFilter(e.target.value)}
              className="h-10 px-3 border border-[#d0d0d0] rounded-[4px] text-[13px] text-[#242424] focus-visible:ring-0 focus-visible:border-[#0066cc] bg-white outline-none"
            >
              <option value="ALL">All Statuses</option>
              {Object.entries(ORDER_STATUS).map(([key, value]) => (
                <option key={key} value={value}>
                  {key.replace('_', ' ')}
                </option>
              ))}
            </select>
            {/* {hasPermission(PERMISSIONS.ORDERS.CREATE) && (
              <Button 
                onClick={() => router.push(`/dashboard/${orgId}/sales/orders/new`)}
                className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px] font-[600]"
              >
                <Plus className="w-4 h-4 mr-2" /> New Order
              </Button>
            )} */}
         </div>
      </div>

      <div className="flex-1 min-h-0 border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden flex flex-col">
         <div className="flex-1 overflow-auto">
            <table className="w-full text-left border-collapse">
              <thead className="bg-[#f8f8f8] sticky top-0 z-10 shadow-[0px_1px_0px_#e0e0e0]">
                <tr>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[120px]">Number</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0]">Customer</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[180px]">Warehouse</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[150px]">Order Date</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[150px] text-right">Total</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] w-[150px] text-center">Status</th>
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
                      onClick={() => router.push(`/dashboard/${orgId}/sales/orders/${order.id}`)}
                      className={cn("border-b border-[#e0e0e0] hover:bg-[#f0f4ff] cursor-pointer", idx % 2 === 0 ? "bg-white" : "bg-[#fafafa]")}
                    >
                      <td className="px-4 py-3 text-[13px] text-[#242424] font-[600] border-r border-[#e0e0e0]">{order.orderNumber || order.code}</td>
                      <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0]">{order.partner?.name ?? order.lead?.name ?? '—'}</td>
                      <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0] font-[500]">
                        {order.warehouseName ? (
                          <span className="flex items-center text-[#0369a1]">
                            <Building className="w-3.5 h-3.5 mr-1 text-[#0284c7] shrink-0" />
                            <span className="truncate max-w-[140px]">{order.warehouseName}</span>
                          </span>
                        ) : (
                          <span className="text-[#a0a0a0] italic text-[12px]">Unassigned</span>
                        )}
                      </td>
                      <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0]">{order.createdAt?.split('T')[0] || '—'}</td>
                      <td className="px-4 py-3 text-[13px] text-[#242424] text-right font-mono border-r border-[#e0e0e0]">₫{Number(order.totalAmount ?? 0).toLocaleString()}</td>
                      <td className="px-4 py-3 text-center">
                        <span className={cn("px-2 py-1 text-[11px] font-[600] rounded uppercase block text-center truncate border", 
                          ORDER_STATUS_CONFIG[order.status as OrderStatus]?.badgeClass || "bg-gray-50 text-gray-650 border-gray-200"
                        )}>
                          {ORDER_STATUS_CONFIG[order.status as OrderStatus]?.label || order.status}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
         </div>
      </div>
    </div>
  );
}
