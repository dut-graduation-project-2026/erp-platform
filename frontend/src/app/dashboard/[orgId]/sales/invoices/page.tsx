'use client';

import React, { useEffect, useState, useMemo, use } from 'react';
import { getSaleInvoices } from '@/features/sales/services/salesService';
import { SaleInvoice } from '@/features/sales/types';
import { Button } from '@/components/ui/button';
import { Plus, Search } from 'lucide-react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { usePermissions } from '@/hooks/use-permissions';
import { cn } from '@/lib/utils';
import { Input } from '@/components/ui/input';
import { PERMISSIONS } from '@/config/permissions';
import { INVOICE_STATUS } from '@/config/constants';

export default function InvoicesListPage({ params }: { params: Promise<{ orgId: string }> }) {
  const router = useRouter();
  const { orgId } = use(params);
  const [invoices, setInvoices] = useState<SaleInvoice[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const { hasPermission } = usePermissions();

  useEffect(() => {
    getSaleInvoices(orgId)
      .then(res => {
        setInvoices(res.data || []);
      })
      .catch(console.error)
      .finally(() => setIsLoading(false));
  }, [orgId]);

  const filteredInvoices = useMemo(() => {
    let result = invoices;
    
    if (statusFilter !== 'ALL') {
      result = result.filter(i => i.status === statusFilter);
    }

    if (searchQuery.trim() !== '') {
      const q = searchQuery.toLowerCase();
      result = result.filter(i => 
        (i.invoiceNumber && i.invoiceNumber.toLowerCase().includes(q)) || 
        (i.partner?.name && i.partner.name.toLowerCase().includes(q)) ||
        (i.saleOrder?.orderNumber && i.saleOrder.orderNumber.toLowerCase().includes(q))
      );
    }

    return result;
  }, [searchQuery, statusFilter, invoices]);

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white">
      <div className="flex justify-between items-center mb-6 shrink-0">
         <div>
            <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Invoices</h1>
            <span className="text-[14px] text-[#898989]">Manage your customer invoices and payments</span>
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
              {Object.entries(INVOICE_STATUS).map(([key, value]) => (
                <option key={key} value={value}>
                  {key.replace('_', ' ')}
                </option>
              ))}
            </select>
            {/* {hasPermission(PERMISSIONS.INVOICES.CREATE) && (
              <Button 
                onClick={() => alert("Invoice creation modal/page would open here.")}
                className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px] font-[600]"
              >
                <Plus className="w-4 h-4 mr-2" /> New Invoice
              </Button>
            )} */}
         </div>
      </div>

      <div className="flex-1 min-h-0 border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden flex flex-col">
         <div className="flex-1 overflow-auto">
            <table className="w-full text-left border-collapse">
              <thead className="bg-[#f8f8f8] sticky top-0 z-10 shadow-[0px_1px_0px_#e0e0e0]">
                <tr>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[140px]">Number</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[140px] text-center">Order No.</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0]">Customer</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[150px]">Invoice Date</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[150px] text-right">Total</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[150px] text-right">Amount Due</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] w-[120px] text-center">Status</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr><td colSpan={7} className="p-4 text-center text-[#898989]">Loading...</td></tr>
                ) : filteredInvoices.length === 0 ? (
                  <tr><td colSpan={7} className="p-4 text-center text-[#898989]">No invoices found</td></tr>
                ) : (
                  filteredInvoices.map((invoice, idx) => {
                    const displayDate = invoice.createdAt?.split('T')[0] || invoice.dueDate?.split('T')[0] || '';
                    const amtDue = invoice.totalAmount - (invoice.paidAmount ?? 0);
                    return (
                      <tr 
                        key={invoice.id} 
                        onClick={() => router.push(`/dashboard/${orgId}/sales/invoices/${invoice.id}`)}
                        className={cn("border-b border-[#e0e0e0] hover:bg-[#f0f4ff] cursor-pointer", idx % 2 === 0 ? "bg-white" : "bg-[#fafafa]")}
                      >
                        <td className="px-4 py-3 text-[13px] text-[#242424] font-[600] border-r border-[#e0e0e0]">{invoice.invoiceNumber}</td>
                        <td className="px-4 py-3 text-[13px] text-center border-r border-[#e0e0e0]" onClick={e => e.stopPropagation()}>
                          {invoice.saleOrder ? (
                            <Link
                              href={`/dashboard/${orgId}/sales/orders/${invoice.saleOrder.id}`}
                              className="inline-flex items-center justify-center min-w-[110px] text-center px-2 py-0.5 rounded-[4px] text-[11px] font-[600] uppercase bg-[#e8f4fd] text-[#1b75bb] border border-[#d0e8fc] hover:bg-[#d0e8fc] hover:text-[#004499] transition-colors"
                            >
                              {invoice.saleOrder.orderNumber}
                            </Link>
                          ) : (
                            <span className="text-[#898989] italic">-</span>
                          )}
                        </td>
                        <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0]">{invoice.partner?.name}</td>
                        <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0]">{displayDate}</td>
                        <td className="px-4 py-3 text-[13px] text-[#242424] text-right font-mono border-r border-[#e0e0e0]">₫{invoice.totalAmount?.toLocaleString()}</td>
                        <td className="px-4 py-3 text-[13px] text-[#242424] text-right font-mono border-r border-[#e0e0e0]">₫{amtDue.toLocaleString()}</td>
                        <td className="px-4 py-3 text-center">
                          <span className={cn(
                            "inline-flex items-center justify-center min-w-[110px] text-center px-2.5 py-0.5 rounded-[4px] text-[11px] font-[600] uppercase",
                            invoice.status === 'PAID' ? "bg-[#28a745]/10 text-[#28a745]" : 
                            invoice.status === 'POSTED' ? "bg-[#ffc107]/10 text-[#ffc107]" : 
                            "bg-[#dc3545]/10 text-[#dc3545]"
                          )}>
                            {invoice.status || 'DRAFT'}
                          </span>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
         </div>
      </div>
    </div>
  );
}
