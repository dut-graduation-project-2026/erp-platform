'use client';

import React, { useEffect, useState, use } from 'react';
import { getSaleInvoiceById, getOrderItems, updateInvoiceStatus, registerPayment } from '@/features/sales/services/salesService';
import { SaleInvoice, OrderItem } from '@/features/sales/types';
import { Button } from '@/components/ui/button';
import { ChevronRight, CheckCircle, FileText, XCircle, Calendar, Clock, Activity, Printer } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { usePermissions } from '@/hooks/use-permissions';
import { toast } from 'sonner';
import Link from 'next/link';
import { cn } from '@/lib/utils';
import { RegisterPaymentModal } from '@/features/sales/components/RegisterPaymentModal';

import { PERMISSIONS } from '@/config/permissions';
import { APP_ROUTES } from '@/config/constants';

export default function InvoiceDetailPage({ params }: { params: Promise<{ orgId: string, id: string }> }) {
  const router = useRouter();
  const { orgId, id } = use(params);
  const [invoice, setInvoice] = useState<SaleInvoice | null>(null);
  const [orderItems, setOrderItems] = useState<OrderItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isPaymentModalOpen, setIsPaymentModalOpen] = useState(false);
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

  const handleRegisterPayment = async (amount: number) => {
    try {
      await registerPayment(orgId, id, amount);
      toast.success(`Payment of $${amount.toLocaleString()} registered successfully`);
      await fetchInvoiceAndItems();
    } catch (err: any) {
      throw err; // Let the modal handle the error
    }
  };

  if (isLoading) {
    return <div className="p-6 text-[#898989] font-['Segoe_UI'] flex justify-center items-center h-full">Loading Invoice...</div>;
  }

  if (!invoice) {
    return <div className="p-6 text-red-500 font-['Segoe_UI'] flex justify-center items-center h-full">Invoice not found.</div>;
  }

  const canWrite = hasPermission(PERMISSIONS.INVOICES.WRITE);

  const netTotal = orderItems.reduce((s, l) => s + Number(l.quantity) * Number(l.unitPrice), 0);
  const taxTotal = orderItems.reduce((s, l) => {
    const tax = l.tax;
    if (!tax) return s;
    const base = Number(l.quantity) * Number(l.unitPrice);
    return s + (tax.computation === 'PERCENTAGE' ? (base * tax.amount) / 100 : tax.amount);
  }, 0);
  const grandTotal = invoice.totalAmount ?? (netTotal + taxTotal);

  return (
    <div className="h-full flex flex-col font-['Segoe_UI'] bg-[#f8f8f8]">
      {/* Breadcrumb bar */}
      <div className="bg-white border-b border-[#e0e0e0] px-6 h-12 flex items-center shrink-0 justify-between">
        <div className="flex items-center text-[14px]">
          <Link href={APP_ROUTES.SALES.INVOICES(orgId)} className="text-[#898989] hover:text-[#242424]">
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
          {invoice.status !== 'DRAFT' && invoice.status !== 'CANCELLED' && (
            <Button asChild variant="outline" className="border-[#0066cc] text-[#0066cc] hover:bg-[#f0f4ff] h-10 px-4 rounded-[4px]">
              <Link href={APP_ROUTES.SALES.INVOICE_PRINT(orgId, id)} target="_blank" rel="noopener noreferrer">
                <Printer className="w-4 h-4 mr-2" /> Print Invoice
              </Link>
            </Button>
          )}
          {canWrite && (invoice.status === 'POSTED' || invoice.status === 'DRAFT' || invoice.status === 'PARTIAL_PAID') && (
            <Button className="bg-[#28a745] hover:bg-[#218838] text-white h-10 px-4 rounded-[4px]" onClick={() => setIsPaymentModalOpen(true)}>
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
        <div className="max-w-[1400px] mx-auto grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
          
          {/* Left: Invoice Sheet Container (2/3 width) */}
          <div className="lg:col-span-2 bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_4px_16px_rgba(0,0,0,0.05)] p-8 md:p-12">
            
            {/* Header Block */}
            <div className="mb-8">
              <span className="text-[12px] font-[600] text-[#898989] uppercase tracking-wider block mb-1">
                Customer Invoice
              </span>
              <h1 className="text-[28px] font-[700] text-[#111111] leading-none mb-1">
                {invoice.invoiceNumber}
              </h1>
              <div className="flex flex-col space-y-1 mt-2 text-[13px] text-[#898989]">
                {invoice.saleOrder?.id && (
                  <div>
                    Source Document:{" "}
                    <Link 
                      href={APP_ROUTES.SALES.ORDER_DETAIL(orgId, invoice.saleOrder.id)} 
                      className="text-[#0066cc] hover:underline font-medium"
                    >
                      {invoice.saleOrder.orderNumber || invoice.saleOrder.code}
                    </Link>
                  </div>
                )}
                {invoice.organization && (
                  <div>
                    Organization: <span className="text-[#242424] font-medium">{invoice.organization.name}</span>
                  </div>
                )}
              </div>
            </div>

            {/* Key-Value metadata fields in 2 columns */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-x-12 gap-y-4 pb-8 border-b border-[#e0e0e0]">
              
              {/* Left Column: Customer details */}
              <div className="space-y-2">
                <div className="flex text-[14px]">
                  <span className="w-28 shrink-0 text-[#898989] font-[600]">Customer</span>
                  <span className="text-[#242424] font-[600]">{invoice.partner?.name || '—'}</span>
                </div>
                {invoice.partner?.taxCode && (
                  <div className="flex text-[14px]">
                    <span className="w-28 shrink-0 text-[#898989] font-[600]">Tax Code</span>
                    <span className="text-[#242424] font-mono">{invoice.partner.taxCode}</span>
                  </div>
                )}
                {invoice.partner?.email && (
                  <div className="flex text-[14px]">
                    <span className="w-28 shrink-0 text-[#898989] font-[600]">Email</span>
                    <a href={`mailto:${invoice.partner.email}`} className="text-[#0066cc] hover:underline">
                      {invoice.partner.email}
                    </a>
                  </div>
                )}
                {invoice.partner?.phone && (
                  <div className="flex text-[14px]">
                    <span className="w-28 shrink-0 text-[#898989] font-[600]">Phone</span>
                    <span className="text-[#242424]">{invoice.partner.phone}</span>
                  </div>
                )}
                {invoice.partner?.address && (
                  <div className="flex text-[14px]">
                    <span className="w-28 shrink-0 text-[#898989] font-[600]">Address</span>
                    <span className="text-[#606060] italic leading-relaxed">{invoice.partner.address}</span>
                  </div>
                )}
              </div>

              {/* Right Column: Invoice dates and payment info */}
              <div className="space-y-2">
                <div className="flex text-[14px]">
                  <span className="w-32 shrink-0 text-[#898989] font-[600]">Invoice Date</span>
                  <span className="text-[#242424]">
                    {invoice.createdAt ? new Date(invoice.createdAt).toLocaleDateString() : '—'}
                  </span>
                </div>
                <div className="flex text-[14px]">
                  <span className="w-32 shrink-0 text-[#898989] font-[600]">Due Date</span>
                  <span className="text-[#242424]">
                    {invoice.dueDate ? new Date(invoice.dueDate).toLocaleDateString() : '—'}
                  </span>
                </div>
                <div className="flex text-[14px]">
                  <span className="w-32 shrink-0 text-[#898989] font-[600]">Paid Amount</span>
                  <span className="text-[#242424] font-mono">${invoice.paidAmount.toLocaleString()}</span>
                </div>
                <div className="flex text-[14px]">
                  <span className="w-32 shrink-0 text-[#898989] font-[600]">Amount Due</span>
                  <span className="text-[#dc3545] font-mono font-semibold">
                    ${(invoice.totalAmount - invoice.paidAmount).toLocaleString()}
                  </span>
                </div>
              </div>

            </div>

            {/* Tabs header */}
            <div className="mt-8 border-b border-[#e0e0e0] flex space-x-6 text-[14px]">
              <button className="border-b-2 border-[#0066cc] pb-2 font-[600] text-[#0066cc]">
                Invoice Lines
              </button>
        
            </div>

            {/* Line items table */}
            <div className="mt-4">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-[#e0e0e0] text-[13px] font-[600] text-[#242424]">
                    <th className="py-3 pr-4">Product</th>
                    <th className="py-3 px-4 w-[180px]">Tax</th>
                    <th className="py-3 px-4 w-[100px] text-right">Qty</th>
                    <th className="py-3 px-4 w-[150px] text-right">Unit Price</th>
                    <th className="py-3 pl-4 w-[150px] text-right">Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {orderItems.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="py-6 text-center text-[#898989] italic">
                        No items found.
                      </td>
                    </tr>
                  ) : (
                    orderItems.map((item, idx) => (
                      <tr key={idx} className="border-b border-[#f0f0f0] text-[13px] text-[#242424] hover:bg-[#fafafa]">
                        <td className="py-3 pr-4">
                          <span className="font-[600]">{item.product?.name || '—'}</span>
                          {item.product?.code && (
                            <span className="text-[11px] text-[#898989] font-mono block mt-0.5">
                              Code: {item.product.code}
                            </span>
                          )}
                        </td>
                        <td className="py-3 px-4">
                          {item.tax ? (
                            <span className="bg-[#f0f4ff] text-[#0066cc] text-[11px] px-2 py-0.5 rounded font-[500]">
                              {item.tax.name}
                            </span>
                          ) : (
                            <span className="text-[#898989] italic">No Tax</span>
                          )}
                        </td>
                        <td className="py-3 px-4 text-right font-medium">{item.quantity}</td>
                        <td className="py-3 px-4 text-right font-mono">
                          ${Number(item.unitPrice).toLocaleString()}
                        </td>
                        <td className="py-3 pl-4 text-right font-mono font-[600] text-[#0066cc]">
                          ${(Number(item.quantity) * Number(item.unitPrice)).toLocaleString()}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>

              {/* Totals Section */}
              <div className="mt-6 flex justify-end">
                <div className="w-full max-w-[320px] space-y-2 text-[14px]">
                  <div className="flex justify-between">
                    <span className="text-[#898989]">Untaxed Amount</span>
                    <span className="font-mono text-[#242424]">${netTotal.toLocaleString()}</span>
                  </div>
                  <div className="flex justify-between border-b border-dashed border-[#e0e0e0] pb-2">
                    <span className="text-[#898989]">Taxes</span>
                    <span className="font-mono text-[#242424]">${taxTotal.toLocaleString()}</span>
                  </div>
                  <div className="flex justify-between py-2">
                    <span className="font-[700] text-[#111111] text-[16px]">Total</span>
                    <span className="font-[700] text-[#0066cc] text-[20px] font-mono">
                      ${grandTotal.toLocaleString()}
                    </span>
                  </div>
                </div>
              </div>
            </div>

          </div>

          {/* Right: Chatter Sidebar (1/3 width) */}
          <div className="space-y-4 lg:sticky lg:top-6">
            {/* Tabs */}
            <div className="flex border border-[#e0e0e0] rounded-t-[4px] overflow-hidden bg-[#fafafa]">
              <button className="flex-1 py-2.5 font-[600] text-[#242424] text-[13px] bg-white border-r border-[#e0e0e0] flex items-center justify-center gap-1.5">
                <FileText className="w-4 h-4 text-[#898989]" /> Log Note
              </button>
              <button className="flex-1 py-2.5 text-[#898989] text-[13px] hover:text-[#242424] flex items-center justify-center gap-1.5 cursor-not-allowed" disabled>
                <Calendar className="w-4 h-4 text-[#898989]" /> Schedule Activity
              </button>
            </div>

            {/* Timeline Content */}
            <div className="border border-[#e0e0e0] border-t-0 rounded-b-[4px] p-6 bg-[#fcfcfc] space-y-6 relative min-h-[300px]">
              {/* Vertical line */}
              <div className="absolute left-[39px] top-6 bottom-6 w-[1px] bg-[#e0e0e0] z-0" />

              {invoice.createdAt && (
                <div className="relative flex items-start z-10 gap-3">
                  {/* Timeline node: Avatar */}
                  <div className="w-8 h-8 rounded-full bg-[#0066cc] text-white font-bold flex items-center justify-center text-[12px] shrink-0 shadow-sm">
                    {invoice.createdBy?.firstName?.[0] || 'U'}
                  </div>
                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-center text-[13px] mb-1">
                      <span className="font-[600] text-[#242424] truncate">
                        {invoice.createdBy?.firstName} {invoice.createdBy?.lastName}
                      </span>
                      <span className="text-[11px] text-[#898989] shrink-0">Original</span>
                    </div>
                    <div className="bg-white border border-[#e0e0e0] rounded-[4px] p-3 shadow-[0px_1px_2px_rgba(0,0,0,0.02)] text-[13px]">
                      <p className="text-[#242424] font-medium mb-1">Invoice Issued</p>
                      <p className="text-[#606060] text-[12px]">
                        Invoice <span className="font-mono text-[#0066cc]">{invoice.invoiceNumber}</span> was registered in draft mode.
                      </p>
                      <div className="text-[10px] text-[#898989] mt-2 font-mono">
                        {new Date(invoice.createdAt).toLocaleString()}
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {invoice.updatedAt && invoice.updatedBy && (
                <div className="relative flex items-start z-10 gap-3">
                  {/* Timeline node: Clock icon in a white circle */}
                  <div className="w-8 h-8 rounded-full bg-white border border-[#e0e0e0] text-[#898989] flex items-center justify-center shrink-0 shadow-sm">
                    <Clock className="w-4 h-4" />
                  </div>
                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-center text-[13px] mb-1">
                      <span className="font-[600] text-[#242424] truncate">
                        {invoice.updatedBy?.firstName} {invoice.updatedBy?.lastName}
                      </span>
                      <span className="text-[11px] text-[#898989] shrink-0">Recent Update</span>
                    </div>
                    <div className="bg-white border border-[#e0e0e0] rounded-[4px] p-3 shadow-[0px_1px_2px_rgba(0,0,0,0.02)] text-[13px]">
                      <p className="text-[#242424] font-medium mb-1">Status Transition</p>
                      <p className="text-[#606060] text-[12px]">
                        Invoice status updated to <span className="font-[600] text-[#242424]">{invoice.status}</span>.
                      </p>
                      <div className="text-[10px] text-[#898989] mt-2 font-mono">
                        {new Date(invoice.updatedAt).toLocaleString()}
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>

        </div>
      </div>

      <RegisterPaymentModal
        isOpen={isPaymentModalOpen}
        onClose={() => setIsPaymentModalOpen(false)}
        onConfirm={handleRegisterPayment}
        remainingBalance={invoice.totalAmount - invoice.paidAmount}
      />
    </div>
  );
}
