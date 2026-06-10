'use client';

import React, { useEffect, useState, use } from 'react';
import { getSaleInvoiceById, getOrderItems, updateInvoiceStatus } from '@/features/sales/services/salesService';
import { SaleInvoice, OrderItem } from '@/features/sales/types';
import { Button } from '@/components/ui/button';
import { ChevronRight, CheckCircle, FileText, XCircle } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { usePermissions } from '@/hooks/use-permissions';
import { toast } from 'sonner';
import Link from 'next/link';
import { cn } from '@/lib/utils';

import { PERMISSIONS } from '@/config/permissions';

export default function InvoiceDetailPage({ params }: { params: Promise<{ orgId: string, id: string }> }) {
  const router = useRouter();
  const { orgId, id } = use(params);
  const [invoice, setInvoice] = useState<SaleInvoice | null>(null);
  const [orderItems, setOrderItems] = useState<OrderItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const { hasPermission } = usePermissions();

  const fetchInvoiceAndItems = async () => {
    try {
      const inv = await getSaleInvoiceById(orgId, id);
      setInvoice(inv);
      if (inv.saleOrder?.id) {
        const items = await getOrderItems(orgId, inv.saleOrder.id);
        setOrderItems(items);
      }
    } catch (err: any) {
      console.error(err);
      toast.error('Failed to load invoice.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchInvoiceAndItems();
  }, [orgId, id]);

  const handleUpdateStatus = async (status: string) => {
    try {
      await updateInvoiceStatus(orgId, id, status);
      toast.success(`Invoice marked as ${status}`);
      await fetchInvoiceAndItems();
    } catch (err: any) {
      // error toast already shown by api-client
    }
  };

  if (isLoading) {
    return <div className="p-6 text-[#898989] font-['Segoe_UI'] flex justify-center items-center h-full">Loading Invoice...</div>;
  }

  if (!invoice) {
    return <div className="p-6 text-red-500 font-['Segoe_UI'] flex justify-center items-center h-full">Invoice not found.</div>;
  }

  const canWrite = hasPermission(PERMISSIONS.INVOICES.WRITE);

  return (
    <div className="h-full flex flex-col font-['Segoe_UI'] bg-[#f8f8f8]">
      {/* Breadcrumb bar */}
      <div className="bg-white border-b border-[#e0e0e0] px-6 h-12 flex items-center shrink-0 justify-between">
        <div className="flex items-center text-[14px]">
          <Link href={`/dashboard/${orgId}/sales/invoices`} className="text-[#898989] hover:text-[#242424]">
            Invoices
          </Link>
          <ChevronRight className="w-4 h-4 text-[#898989] mx-2" />
          <span className="text-[#0066cc] font-[600]">{invoice.invoiceNumber}</span>
        </div>
        <span className={cn('px-3 py-1 rounded-[4px] text-[12px] font-[600] uppercase',
          invoice.status === 'PAID' ? 'bg-[#28a745]/10 text-[#28a745]' :
          invoice.status === 'POSTED' ? 'bg-[#17a2b8]/10 text-[#17a2b8]' :
          invoice.status === 'CANCELLED' ? 'bg-[#dc3545]/10 text-[#dc3545]' :
          'bg-[#898989]/10 text-[#898989]'
        )}>
          {invoice.status}
        </span>
      </div>

      {/* Action bar */}
      <div className="bg-white px-6 py-4 border-b border-[#e0e0e0] flex justify-between items-center shrink-0">
        <h1 className="text-[22px] font-[700] text-[#242424]">
          {invoice.invoiceNumber}
        </h1>
        <div className="flex space-x-2">
          {canWrite && invoice.status === 'DRAFT' && (
            <Button className="bg-[#17a2b8] hover:bg-[#138496] text-white h-10 px-4 rounded-[4px]" onClick={() => handleUpdateStatus('POSTED')}>
              <FileText className="w-4 h-4 mr-2" /> Issue Invoice
            </Button>
          )}
          {canWrite && (invoice.status === 'POSTED' || invoice.status === 'DRAFT' || invoice.status === 'PARTIAL_PAID') && (
            <Button className="bg-[#28a745] hover:bg-[#218838] text-white h-10 px-4 rounded-[4px]" onClick={() => handleUpdateStatus('PAID')}>
              <CheckCircle className="w-4 h-4 mr-2" /> Register Payment
            </Button>
          )}
          {canWrite && invoice.status !== 'PAID' && invoice.status !== 'CANCELLED' && (
            <Button variant="outline" className="border-red-300 text-red-600 hover:bg-red-50 h-10 px-4 rounded-[4px]" onClick={() => handleUpdateStatus('CANCELLED')}>
              <XCircle className="w-4 h-4 mr-2" /> Cancel Invoice
            </Button>
          )}
        </div>
      </div>

      {/* Content */}
      <div className="flex-1 overflow-auto p-6">
        <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
          {/* Header Info */}
          <div className="p-6 grid grid-cols-2 gap-8 border-b border-[#e0e0e0]">
            <div className="space-y-4">
              <div>
                <label className="block text-[13px] font-[600] text-[#898989] uppercase tracking-wider mb-1">Customer</label>
                <div className="text-[15px] text-[#242424] font-[500]">{invoice.partner?.name || '—'}</div>
              </div>
              <div>
                <label className="block text-[13px] font-[600] text-[#898989] uppercase tracking-wider mb-1">Source Document</label>
                <div className="text-[15px] text-[#0066cc] font-[500] cursor-pointer hover:underline" onClick={() => invoice.saleOrder?.id && router.push(`/dashboard/${orgId}/sales/orders/${invoice.saleOrder.id}`)}>
                  {invoice.saleOrder?.orderNumber || '—'}
                </div>
              </div>
            </div>
            <div className="space-y-4">
              <div>
                <label className="block text-[13px] font-[600] text-[#898989] uppercase tracking-wider mb-1">Invoice Date</label>
                <div className="text-[15px] text-[#242424] font-[500]">{invoice.createdAt ? new Date(invoice.createdAt).toLocaleDateString() : '—'}</div>
              </div>
              <div>
                <label className="block text-[13px] font-[600] text-[#898989] uppercase tracking-wider mb-1">Due Date</label>
                <div className="text-[15px] text-[#242424] font-[500]">{invoice.dueDate ? new Date(invoice.dueDate).toLocaleDateString() : '—'}</div>
              </div>
            </div>
          </div>

          {/* Invoice Lines */}
          <div className="p-0">
            <table className="w-full text-left border-collapse">
              <thead className="bg-[#f8f8f8]">
                <tr>
                  <th className="px-6 py-3 text-[13px] font-[600] text-[#242424] border-b border-[#e0e0e0]">Description</th>
                  <th className="px-6 py-3 text-[13px] font-[600] text-[#242424] border-b border-[#e0e0e0] w-[100px] text-right">Qty</th>
                  <th className="px-6 py-3 text-[13px] font-[600] text-[#242424] border-b border-[#e0e0e0] w-[150px] text-right">Unit Price</th>
                  <th className="px-6 py-3 text-[13px] font-[600] text-[#242424] border-b border-[#e0e0e0] w-[150px] text-right">Amount</th>
                </tr>
              </thead>
              <tbody>
                {orderItems.length === 0 ? (
                  <tr><td colSpan={4} className="p-6 text-center text-[#898989]">No items found.</td></tr>
                ) : (
                  orderItems.map((item, idx) => (
                    <tr key={idx} className="border-b border-[#e0e0e0] bg-white">
                      <td className="px-6 py-3 text-[13px] text-[#242424]">{item.product?.name || '—'}</td>
                      <td className="px-6 py-3 text-[13px] text-[#242424] text-right">{item.quantity}</td>
                      <td className="px-6 py-3 text-[13px] text-[#242424] text-right font-mono">₫{Number(item.unitPrice).toLocaleString()}</td>
                      <td className="px-6 py-3 text-[13px] text-[#242424] text-right font-mono font-[500]">
                        ₫{(Number(item.quantity) * Number(item.unitPrice)).toLocaleString()}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* Totals */}
          <div className="p-6 bg-[#fafafa] flex justify-end rounded-b-[4px]">
            <div className="w-[300px]">
              <div className="flex justify-between py-2 border-b border-[#e0e0e0]">
                <span className="text-[14px] font-[600] text-[#242424]">Untaxed Amount:</span>
                <span className="text-[14px] font-mono text-[#242424]">₫{Number(invoice.totalAmount).toLocaleString()}</span>
              </div>
              <div className="flex justify-between py-2 border-b border-[#e0e0e0]">
                <span className="text-[14px] font-[600] text-[#242424]">Taxes:</span>
                <span className="text-[14px] font-mono text-[#898989]">₫0 (Included)</span>
              </div>
              <div className="flex justify-between py-3">
                <span className="text-[18px] font-[700] text-[#242424]">Total Due:</span>
                <span className="text-[18px] font-[700] text-[#0066cc] font-mono">
                  ₫{(Number(invoice.totalAmount) - Number(invoice.paidAmount)).toLocaleString()}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
