'use client';

import React, { useEffect, useState, use } from 'react';
import { getSaleOrders } from '@/features/sales/services/salesService';
import { SaleOrder } from '@/features/sales/types';
import { Button } from '@/components/ui/button';
import { Plus, Search } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { cn } from '@/lib/utils';
import { Input } from '@/components/ui/input';

export default function QuotationsListPage({ params }: { params: Promise<{ orgId: string }> }) {
  const router = useRouter();
  const { orgId } = use(params);
  const [orders, setOrders] = useState<SaleOrder[]>([]);
  const [filteredOrders, setFilteredOrders] = useState<SaleOrder[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    getSaleOrders(orgId)
      .then(res => {
        setOrders(res.data || []);
        setFilteredOrders(res.data || []);
      })
      .catch(console.error)
      .finally(() => setIsLoading(false));
  }, [orgId]);

  useEffect(() => {
    if (searchQuery.trim() === '') {
      setFilteredOrders(orders);
    } else {
      const q = searchQuery.toLowerCase();
      setFilteredOrders(orders.filter(o => 
        (o.code && o.code.toLowerCase().includes(q)) || 
        (o.partner?.name && o.partner.name.toLowerCase().includes(q))
      ));
    }
  }, [searchQuery, orders]);

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white">
      <div className="flex justify-between items-center mb-6 shrink-0">
         <div>
            <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Quotations</h1>
            <span className="text-[14px] text-[#898989]">Manage your sales orders and quotes</span>
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
            <Button 
              onClick={() => router.push(`/dashboard/${orgId}/sales/quotations/new`)}
              className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px] font-[600]"
            >
              <Plus className="w-4 h-4 mr-2" /> New
            </Button>
         </div>
      </div>

      <div className="flex-1 min-h-0 border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden flex flex-col">
         <div className="flex-1 overflow-auto">
            <table className="w-full text-left border-collapse">
              <thead className="bg-[#f8f8f8] sticky top-0 z-10 shadow-[0px_1px_0px_#e0e0e0]">
                <tr>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[120px]">Number</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0]">Customer</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[150px]">Order Date</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[150px] text-right">Total</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] w-[120px] text-center">Status</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr><td colSpan={5} className="p-4 text-center text-[#898989]">Loading...</td></tr>
                ) : filteredOrders.length === 0 ? (
                  <tr><td colSpan={5} className="p-4 text-center text-[#898989]">No quotations found</td></tr>
                ) : (
                  filteredOrders.map((order, idx) => (
                    <tr 
                      key={order.id} 
                      onClick={() => router.push(`/dashboard/${orgId}/sales/quotations/${order.id}`)}
                      className={cn("border-b border-[#e0e0e0] hover:bg-[#f0f4ff] cursor-pointer", idx % 2 === 0 ? "bg-white" : "bg-[#fafafa]")}
                    >
                      <td className="px-4 py-3 text-[13px] text-[#242424] font-[600] border-r border-[#e0e0e0]">{order.code}</td>
                      <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0]">{order.partner?.name}</td>
                      <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0]">{order.orderDate?.split('T')[0] || order.orderDate}</td>
                      <td className="px-4 py-3 text-[13px] text-[#242424] text-right font-mono border-r border-[#e0e0e0]">₫{order.totalAmount?.toLocaleString()}</td>
                      <td className="px-4 py-3 text-center">
                        <span className={cn("px-2 py-1 text-[11px] font-[600] rounded uppercase", 
                          order.status === 'CONFIRMED' ? "bg-[#28a745]/10 text-[#28a745]" : 
                          order.status === 'DRAFT' ? "bg-[#898989]/10 text-[#898989]" : 
                          "bg-[#0066cc]/10 text-[#0066cc]"
                        )}>
                          {order.status}
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
