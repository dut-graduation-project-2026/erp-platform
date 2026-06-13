'use client';

import React, { useState, useEffect } from 'react';
import { OrderItem, Product, SaleOrder, SaleTax } from '../types';
import { Button } from '@/components/ui/button';
import { ORDER_STATUS, TAX_COMPUTATION } from '@/config/constants';
import { Input } from '@/components/ui/input';
import { Plus, Trash2, ChevronRight, Save, CheckCircle, XCircle, Receipt, Building, Mail, Phone, User, Calendar, ArrowLeft, Clock, Activity, FileText } from 'lucide-react';
import { cn } from '@/lib/utils';
import Link from 'next/link';
import {
  getProducts,
  getTaxes,
  createQuotation,
  updateQuotation,
  confirmQuotation,
  cancelQuotation,
  createOrderItem,
  updateOrderItem,
  deleteOrderItem,
  createInvoice,
} from '../services/salesService';
import { getLeads } from '@/features/crm/services/crmService';
import { useRouter, useSearchParams } from 'next/navigation';
import { usePermissions } from '@/hooks/use-permissions';
import { toast } from 'sonner';

import { PERMISSIONS } from '@/config/permissions';

interface Props {
  order: SaleOrder | null;
  orgId: string;
}

interface LocalLine {
  id?: string;           // undefined = not yet saved to backend
  productId: string;
  taxId: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
  productName?: string;
  taxAmount?: number;    // computed display only
}

export function SaleOrderForm({ order, orgId }: Props) {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { hasPermission } = usePermissions();

  // ─── Lookup data ─────────────────────────────────────────────────────────
  const [products, setProducts] = useState<Product[]>([]);
  const [taxes, setTaxes] = useState<SaleTax[]>([]);
  const [leads, setLeads] = useState<{ id: string; name: string }[]>([]);
  const [isLoadingMeta, setIsLoadingMeta] = useState(true);

  // ─── Form state ───────────────────────────────────────────────────────────
  const [leadId, setLeadId] = useState(order?.lead?.id || searchParams.get('leadId') || '');
  const [orderNumber, setOrderNumber] = useState(order?.orderNumber || '');
  const [deliveryDate, setDeliveryDate] = useState(
    order?.deliveryDate ? order.deliveryDate.split('T')[0] : ''
  );
  const [expirationDate, setExpirationDate] = useState(
    order?.expirationDate ? order.expirationDate.split('T')[0] : ''
  );
  const [lines, setLines] = useState<LocalLine[]>(
    (order?.items ?? []).map((it) => ({
      id: it.id,
      productId: it.productId ?? it.product?.id ?? '',
      taxId: it.taxId ?? it.tax?.id ?? '',
      quantity: Number(it.quantity),
      unitPrice: Number(it.unitPrice),
      subtotal: Number(it.subtotal),
      productName: it.product?.name,
    }))
  );

  const [isSaving, setIsSaving] = useState(false);
  const [localStatus, setLocalStatus] = useState(order?.status ?? ORDER_STATUS.DRAFT);

  // ─── Load metadata ────────────────────────────────────────────────────────
  useEffect(() => {
    Promise.all([
      getProducts(orgId, { limit: 200 }),
      getTaxes(orgId, { limit: 100 }),
      getLeads(orgId, { limit: 200 }),
    ])
      .then(([prodRes, taxRes, leadsRes]) => {
        setProducts(prodRes.data ?? []);
        setTaxes(taxRes.data ?? []);
        setLeads((leadsRes.data ?? []).map((l: any) => ({ id: l.id, name: l.name })));
      })
      .catch(console.error)
      .finally(() => setIsLoadingMeta(false));
  }, [orgId]);

  // ─── Calculations ─────────────────────────────────────────────────────────
  const netTotal = lines.reduce((s, l) => s + l.quantity * l.unitPrice, 0);
  const taxTotal = lines.reduce((s, l) => {
    const tax = taxes.find((t) => t.id === l.taxId);
    if (!tax) return s;
    const base = l.quantity * l.unitPrice;
    return s + (tax.computation === TAX_COMPUTATION.PERCENTAGE ? (base * tax.amount) / 100 : tax.amount);
  }, 0);
  const grandTotal = netTotal + taxTotal;

  // ─── Line manipulation ────────────────────────────────────────────────────
  const handleAddLine = () => {
    setLines((prev) => [
      ...prev,
      { productId: '', taxId: '', quantity: 1, unitPrice: 0, subtotal: 0 },
    ]);
  };

  const handleLineChange = (idx: number, field: keyof LocalLine, value: string | number) => {
    setLines((prev) => {
      const next = [...prev];
      const line = { ...next[idx], [field]: value };

      if (field === 'productId') {
        const prod = products.find((p) => p.id === value);
        if (prod) {
          line.unitPrice = prod.price;
          line.productName = prod.name;
        }
      }
      line.subtotal = line.quantity * line.unitPrice;
      next[idx] = line;
      return next;
    });
  };

  const handleRemoveLine = async (idx: number) => {
    const line = lines[idx];
    if (line.id && order?.id) {
      try {
        await deleteOrderItem(orgId, order.id, line.id);
      } catch {
        toast.error('Failed to delete item from backend.');
        return;
      }
    }
    setLines((prev) => prev.filter((_, i) => i !== idx));
  };

  // ─── Save quotation header ─────────────────────────────────────────────────
  const handleSave = async (): Promise<boolean> => {
    if (!leadId) {
      toast.error('Please select a CRM Lead.');
      return false;
    }

    setIsSaving(true);
    try {
      const payload = {
        leadId,
        orderNumber: orderNumber || undefined,
        deliveryDate: deliveryDate ? new Date(deliveryDate).toISOString() : undefined,
        expirationDate: expirationDate ? new Date(expirationDate).toISOString() : undefined,
      };

      let saved: SaleOrder;
      if (order?.id) {
        saved = await updateQuotation(orgId, order.id, payload);
      } else {
        saved = await createQuotation(orgId, payload);
      }

      // Persist line items via separate API
      const updatedLines = [...lines];
      for (let i = 0; i < updatedLines.length; i++) {
        const line = updatedLines[i];
        if (!line.productId) continue;
        const itemPayload = {
          productId: line.productId,
          taxId: line.taxId || undefined,
          quantity: line.quantity,
          unitPrice: line.unitPrice,
        };
        if (line.id) {
          await updateOrderItem(orgId, saved.id, line.id, itemPayload);
        } else {
          const created = await createOrderItem(orgId, saved.id, itemPayload);
          updatedLines[i] = { ...line, id: created.id };
        }
      }
      setLines(updatedLines);

      toast.success('Quotation saved successfully.');
      if (!order?.id) {
        router.push(`/dashboard/${orgId}/sales/quotations/${saved.id}`);
      }
      return true;
    } catch (err: any) {
      console.error(err);
      return false;
    } finally {
      setIsSaving(false);
    }
  };

  // ─── Confirm order ────────────────────────────────────────────────────────
  const handleConfirm = async () => {
    if (!order?.id) return toast.error('Save the quotation first.');
    const ok = await handleSave();
    if (!ok) return;

    try {
      await confirmQuotation(orgId, order.id);
      setLocalStatus('CONFIRMED');
      toast.success('Order confirmed successfully.');
      router.push(`/dashboard/${orgId}/sales/orders/${order.id}`);
    } catch {
      // error toast already shown by api-client interceptor
    }
  };

  // ─── Cancel order ─────────────────────────────────────────────────────────
  const handleCancel = async () => {
    if (!order?.id) return;
    try {
      await cancelQuotation(orgId, order.id);
      setLocalStatus('CANCELLED');
      toast.success('Quotation cancelled.');
    } catch {
      // error toast already shown
    }
  };

  // ─── Create invoice ───────────────────────────────────────────────────────
  const handleCreateInvoice = async () => {
    if (!order?.id) return;
    try {
      const invoice = await createInvoice(orgId, { orderId: order.id });
      toast.success('Invoice created successfully.');
      router.push(`/dashboard/${orgId}/sales/invoices/${invoice.id}`);
    } catch {
      // error toast handled
    }
  };

  const canWrite = order?.id ? hasPermission(PERMISSIONS.ORDERS.WRITE) : hasPermission(PERMISSIONS.ORDERS.CREATE);

  const isReadOnly = (localStatus === 'CONFIRMED' || localStatus === 'CANCELLED' || localStatus === 'COMPLETED') && order;

  if (isReadOnly && order) {
    return (
      <SaleOrderReadOnlyView
        order={order}
        orgId={orgId}
        localStatus={localStatus}
        handleCreateInvoice={handleCreateInvoice}
      />
    );
  }

  return (
    <div className="h-full flex flex-col font-['Segoe_UI'] bg-[#f8f8f8]">
      {/* Breadcrumb bar */}
      <div className="bg-white border-b border-[#e0e0e0] px-6 h-12 flex items-center shrink-0 justify-between">
        <div className="flex items-center text-[14px]">
          <Link href={`/dashboard/${orgId}/sales/quotations`} className="text-[#898989] hover:text-[#242424]">
            Quotations
          </Link>
          <ChevronRight className="w-4 h-4 text-[#898989] mx-2" />
          <span className="text-[#0066cc] font-[600]">{order?.orderNumber || order?.code || 'New Quotation'}</span>
        </div>
        <span className={cn('px-3 py-1 rounded-[4px] text-[12px] font-[600] uppercase',
          localStatus === 'CONFIRMED' ? 'bg-green-100 text-green-700' :
          localStatus === 'CANCELLED' ? 'bg-red-100 text-red-600' :
          'bg-gray-100 text-gray-600'
        )}>
          {localStatus}
        </span>
      </div>

      {/* Action bar */}
      <div className="bg-white px-6 py-4 border-b border-[#e0e0e0] flex justify-between items-center shrink-0">
        <h1 className="text-[22px] font-[700] text-[#242424]">
          {order?.orderNumber || order?.code || 'New Quotation'}
        </h1>
        <div className="flex space-x-2">
          {canWrite && localStatus === 'DRAFT' && (
            <Button variant="outline" className="border-[#d0d0d0] text-[#242424] h-10 px-4 rounded-[4px]" onClick={handleSave} disabled={isSaving}>
              <Save className="w-4 h-4 mr-2" />{isSaving ? 'Saving…' : 'Save'}
            </Button>
          )}
          {canWrite && order?.id && localStatus === 'DRAFT' && (
            <Button className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px]" onClick={handleConfirm}>
              <CheckCircle className="w-4 h-4 mr-2" />Confirm Order
            </Button>
          )}
          {canWrite && order?.id && localStatus === 'DRAFT' && (
            <Button variant="outline" className="border-red-300 text-red-600 hover:bg-red-50 h-10 px-4 rounded-[4px]" onClick={handleCancel}>
              <XCircle className="w-4 h-4 mr-2" />Cancel
            </Button>
          )}
          {canWrite && order?.id && localStatus === 'CONFIRMED' && (
            <Button className="bg-[#28a745] hover:bg-[#218838] text-white h-10 px-4 rounded-[4px]" onClick={handleCreateInvoice}>
              <Receipt className="w-4 h-4 mr-2" />Create Invoice
            </Button>
          )}
        </div>
      </div>

      {/* Main scrolling content area */}
      <div className="flex-1 overflow-auto p-6">
        <div className="max-w-[1400px] mx-auto grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
          
          {/* Left: Odoo-style central document sheet (2/3 width) */}
          <div className="lg:col-span-2 bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_4px_16px_rgba(0,0,0,0.05)] p-8 md:p-12">
            
            {/* Header block inside sheet */}
            <div className="mb-8">
              <span className="text-[12px] font-[600] text-[#898989] uppercase tracking-wider block mb-1">
                Quotation
              </span>
              <h1 className="text-[28px] font-[700] text-[#111111] leading-none mb-1">
                {orderNumber || order?.orderNumber || order?.code || 'New Quotation'}
              </h1>
              {order?.organization && (
                <div className="text-[13px] text-[#898989] mt-2">
                  Organization: <span className="text-[#242424] font-medium">{order.organization.name}</span>
                </div>
              )}
            </div>

            {/* Key-Value metadata fields in 2 columns */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-x-12 gap-y-4 pb-8 border-b border-[#e0e0e0]">
              {/* Left Column: CRM Lead / Customer info */}
              <div className="space-y-4">
                <div>
                  <label className="block text-[13px] font-[600] text-[#898989] mb-1.5 uppercase tracking-wider">
                    CRM Lead <span className="text-red-500">*</span>
                  </label>
                  <select
                    value={leadId}
                    onChange={(e) => setLeadId(e.target.value)}
                    disabled={localStatus !== 'DRAFT'}
                    className="h-9 w-full border border-[#d0d0d0] hover:border-[#a0a0a0] focus:border-[#0066cc] rounded-[4px] px-3 text-[13px] outline-none bg-white transition-colors"
                  >
                    <option value="">-- Select Lead --</option>
                    {leads.map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}
                  </select>
                  <p className="text-[11px] text-[#898989] mt-1 italic">
                    Customer is resolved automatically from the linked lead.
                  </p>
                </div>

                <div>
                  <label className="block text-[13px] font-[600] text-[#898989] mb-1.5 uppercase tracking-wider">Order Number</label>
                  <Input
                    value={orderNumber}
                    onChange={(e) => setOrderNumber(e.target.value)}
                    placeholder="Auto-generated if blank"
                    disabled={localStatus !== 'DRAFT'}
                    className="h-9 border-[#d0d0d0] hover:border-[#a0a0a0] focus:border-[#0066cc] rounded-[4px] text-[13px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                  />
                </div>
              </div>

              {/* Right Column: Dates */}
              <div className="space-y-4">
                <div>
                  <label className="block text-[13px] font-[600] text-[#898989] mb-1.5 uppercase tracking-wider">Delivery Date</label>
                  <Input
                    type="date"
                    value={deliveryDate}
                    onChange={(e) => setDeliveryDate(e.target.value)}
                    disabled={localStatus !== 'DRAFT'}
                    className="h-9 border-[#d0d0d0] hover:border-[#a0a0a0] focus:border-[#0066cc] rounded-[4px] text-[13px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                  />
                </div>

                <div>
                  <label className="block text-[13px] font-[600] text-[#898989] mb-1.5 uppercase tracking-wider">Expiration Date</label>
                  <Input
                    type="date"
                    value={expirationDate}
                    onChange={(e) => setExpirationDate(e.target.value)}
                    disabled={localStatus !== 'DRAFT'}
                    className="h-9 border-[#d0d0d0] hover:border-[#a0a0a0] focus:border-[#0066cc] rounded-[4px] text-[13px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                  />
                </div>
              </div>
            </div>

            {/* Tab header */}
            <div className="mt-8 border-b border-[#e0e0e0] flex space-x-6 text-[14px]">
              <button className="border-b-2 border-[#0066cc] pb-2 font-[600] text-[#0066cc]">
                Order Lines
              </button>
              <button className="pb-2 text-[#898989] hover:text-[#242424] cursor-not-allowed" disabled>
                Other Info
              </button>
            </div>

            {/* Order lines table */}
            <div className="mt-4 overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-[#e0e0e0] text-[13px] font-[600] text-[#242424]">
                    <th className="py-3 pr-4 w-[240px]">Product</th>
                    <th className="py-3 px-4 w-[180px]">Tax</th>
                    <th className="py-3 px-4 w-[100px] text-right">Qty</th>
                    <th className="py-3 px-4 w-[150px] text-right">Unit Price</th>
                    <th className="py-3 pl-4 w-[150px] text-right">Subtotal</th>
                    {localStatus === 'DRAFT' && <th className="py-3 pl-2 w-[40px]" />}
                  </tr>
                </thead>
                <tbody>
                  {lines.map((line, idx) => (
                    <tr key={idx} className="border-b border-[#f0f0f0] text-[13px] text-[#242424] hover:bg-[#fafafa] group">
                      <td className="py-2 pr-4">
                        <select
                          value={line.productId}
                          onChange={(e) => handleLineChange(idx, 'productId', e.target.value)}
                          disabled={localStatus !== 'DRAFT'}
                          className="w-full h-8 text-[13px] border border-[#d0d0d0] rounded px-1 outline-none bg-white focus:border-[#0066cc]"
                        >
                          <option value="">-- Select --</option>
                          {products.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                        </select>
                      </td>
                      <td className="py-2 px-4">
                        <select
                          value={line.taxId}
                          onChange={(e) => handleLineChange(idx, 'taxId', e.target.value)}
                          disabled={localStatus !== 'DRAFT'}
                          className="w-full h-8 text-[13px] border border-[#d0d0d0] rounded px-1 outline-none bg-white focus:border-[#0066cc]"
                        >
                          <option value="">None</option>
                          {taxes.map((t) => (
                            <option key={t.id} value={t.id}>
                              {t.name} ({t.amount}{t.computation === 'PERCENTAGE' ? '%' : '₫'})
                            </option>
                          ))}
                        </select>
                      </td>
                      <td className="py-2 px-4">
                        <input
                          type="number" min={0.0001}
                          value={line.quantity}
                          onChange={(e) => handleLineChange(idx, 'quantity', Number(e.target.value))}
                          disabled={localStatus !== 'DRAFT'}
                          className="w-full h-8 text-[13px] text-right border border-[#d0d0d0] rounded px-2 outline-none bg-white focus:border-[#0066cc]"
                        />
                      </td>
                      <td className="py-2 px-4">
                        <input
                          type="number" min={0}
                          value={line.unitPrice}
                          onChange={(e) => handleLineChange(idx, 'unitPrice', Number(e.target.value))}
                          disabled={localStatus !== 'DRAFT'}
                          className="w-full h-8 text-[13px] text-right font-mono border border-[#d0d0d0] rounded px-2 outline-none bg-white focus:border-[#0066cc]"
                        />
                      </td>
                      <td className="py-2 pl-4 text-right font-mono font-[600] text-[#0066cc]">
                        ₫{(line.quantity * line.unitPrice).toLocaleString()}
                      </td>
                      {localStatus === 'DRAFT' && (
                        <td className="py-2 pl-2 text-right">
                          <button onClick={() => handleRemoveLine(idx)} className="text-[#898989] hover:text-[#dc3545] opacity-0 group-hover:opacity-100 transition-opacity">
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </td>
                      )}
                    </tr>
                  ))}
                  {localStatus === 'DRAFT' && (
                    <tr>
                      <td colSpan={6} className="py-4 bg-white">
                        <Button variant="ghost" onClick={handleAddLine} className="text-[#0066cc] h-8 px-2 hover:bg-[#f0f4ff] text-[13px] font-[600]">
                          <Plus className="w-4 h-4 mr-1" />Add a product
                        </Button>
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>

            {/* Totals Section */}
            <div className="mt-6 flex justify-end">
              <div className="w-full max-w-[320px] space-y-2 text-[14px]">
                <div className="flex justify-between">
                  <span className="text-[#898989]">Untaxed Amount</span>
                  <span className="font-mono text-[#242424]">₫{netTotal.toLocaleString()}</span>
                </div>
                <div className="flex justify-between border-b border-dashed border-[#e0e0e0] pb-2">
                  <span className="text-[#898989]">Taxes</span>
                  <span className="font-mono text-[#242424]">₫{taxTotal.toLocaleString()}</span>
                </div>
                <div className="flex justify-between py-2">
                  <span className="font-[700] text-[#111111] text-[16px]">Total</span>
                  <span className="font-[700] text-[#0066cc] text-[20px] font-mono">
                    ₫{grandTotal.toLocaleString()}
                  </span>
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

              {order?.createdAt && (
                <div className="relative flex items-start z-10 gap-3">
                  {/* Timeline node: Avatar */}
                  <div className="w-8 h-8 rounded-full bg-[#0066cc] text-white font-bold flex items-center justify-center text-[12px] shrink-0 shadow-sm">
                    {order.createdBy?.firstName?.[0] || 'U'}
                  </div>
                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-center text-[13px] mb-1">
                      <span className="font-[600] text-[#242424] truncate">
                        {order.createdBy?.firstName} {order.createdBy?.lastName}
                      </span>
                      <span className="text-[11px] text-[#898989] shrink-0">Original</span>
                    </div>
                    <div className="bg-white border border-[#e0e0e0] rounded-[4px] p-3 shadow-[0px_1px_2px_rgba(0,0,0,0.02)] text-[13px]">
                      <p className="text-[#242424] font-medium mb-1">Document Created</p>
                      <p className="text-[#606060] text-[12px]">
                        Order <span className="font-mono text-[#0066cc]">{order.orderNumber || order.code}</span> was initialized.
                      </p>
                      <div className="text-[10px] text-[#898989] mt-2 font-mono">
                        {new Date(order.createdAt).toLocaleString()}
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {order?.updatedAt && order?.updatedBy && (
                <div className="relative flex items-start z-10 gap-3">
                  {/* Timeline node: Clock icon in a white circle */}
                  <div className="w-8 h-8 rounded-full bg-white border border-[#e0e0e0] text-[#898989] flex items-center justify-center shrink-0 shadow-sm">
                    <Clock className="w-4 h-4" />
                  </div>
                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-center text-[13px] mb-1">
                      <span className="font-[600] text-[#242424] truncate">
                        {order.updatedBy?.firstName} {order.updatedBy?.lastName}
                      </span>
                      <span className="text-[11px] text-[#898989] shrink-0">Recent Update</span>
                    </div>
                    <div className="bg-white border border-[#e0e0e0] rounded-[4px] p-3 shadow-[0px_1px_2px_rgba(0,0,0,0.02)] text-[13px]">
                      <p className="text-[#242424] font-medium mb-1">Document Modified</p>
                      <p className="text-[#606060] text-[12px]">
                        Changes saved by <span className="text-[#242424] font-semibold">{order.updatedBy.email}</span>.
                      </p>
                      <div className="text-[10px] text-[#898989] mt-2 font-mono">
                        {new Date(order.updatedAt).toLocaleString()}
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}




interface ReadOnlyProps {
  order: SaleOrder;
  orgId: string;
  localStatus: string;
  handleCreateInvoice: () => Promise<void>;
}

function SaleOrderReadOnlyView({ order, orgId, localStatus, handleCreateInvoice }: ReadOnlyProps) {
  const router = useRouter();

  const formatCurrency = (val: number | undefined) => {
    return `₫${Number(val ?? 0).toLocaleString()}`;
  };

  const formatDate = (dateStr: string | undefined) => {
    if (!dateStr) return '—';
    try {
      return dateStr.split('T')[0];
    } catch {
      return dateStr;
    }
  };

  const netTotal = (order.items ?? []).reduce((s, l) => s + Number(l.quantity) * Number(l.unitPrice), 0);
  const taxTotal = (order.items ?? []).reduce((s, l) => {
    const tax = l.tax;
    if (!tax) return s;
    const base = Number(l.quantity) * Number(l.unitPrice);
    return s + (tax.computation === TAX_COMPUTATION.PERCENTAGE ? (base * tax.amount) / 100 : tax.amount);
  }, 0);
  const grandTotal = order.totalAmount ?? (netTotal + taxTotal);

  return (
    <div className="h-full flex flex-col font-['Segoe_UI'] bg-[#f8f8f8]">
      {/* Odoo status bar */}
      <div className="bg-white border-b border-[#e0e0e0] h-12 px-6 flex justify-between items-center shrink-0">
        {/* Left: Action buttons */}
        <div className="flex items-center space-x-2">
          <Button 
            variant="outline" 
            className="border-[#d0d0d0] text-[#242424] h-8 px-3 text-[13px] rounded-[4px] bg-white hover:bg-[#f8f8f8]" 
            onClick={() => router.push(`/dashboard/${orgId}/sales/orders`)}
          >
            Back to List
          </Button>
          {localStatus === 'CONFIRMED' && (
            <Button 
              className="bg-[#0066cc] hover:bg-[#004499] text-white h-8 px-3 text-[13px] rounded-[4px] flex items-center" 
              onClick={handleCreateInvoice}
            >
              <Receipt className="w-4 h-4 mr-1.5" /> Create Invoice
            </Button>
          )}
        </div>
        
        {/* Right: Status steps (Odoo style) */}
        <div className="flex items-center text-[12px] font-[600] text-[#898989]">
          <div className={cn(
            "px-3 py-1 flex items-center", 
            localStatus === 'DRAFT' ? "text-[#0066cc] bg-[#f0f4ff] rounded-[2px]" : ""
          )}>
            Draft
          </div>
          <span className="mx-1 text-[#e0e0e0]"></span>
          <div className={cn(
            "px-3 py-1 flex items-center", 
            localStatus === 'CONFIRMED' ? "text-[#28a745] bg-[#eafaf1] rounded-[2px]" : ""
          )}>
            Sales Order
          </div>
          {localStatus === 'CANCELLED' && (
            <>
              <span className="mx-1 text-[#e0e0e0]"></span>
              <div className="px-3 py-1 flex items-center text-[#dc3545] bg-[#fdf2f2] rounded-[2px]">
                Cancelled
              </div>
            </>
          )}
        </div>
      </div>

      {/* Main scrolling content area */}
      <div className="flex-1 overflow-auto p-6">
        <div className="max-w-[1400px] mx-auto grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
          
          {/* Left: Odoo-style central document sheet (2/3 width) */}
          <div className="lg:col-span-2 bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_4px_16px_rgba(0,0,0,0.05)] p-8 md:p-12">
            
            {/* Header block inside sheet */}
            <div className="mb-8">
              <span className="text-[12px] font-[600] text-[#898989] uppercase tracking-wider block mb-1">
                {localStatus === 'DRAFT' ? 'Quotation' : 'Sales Order'}
              </span>
              <h1 className="text-[28px] font-[700] text-[#111111] leading-none mb-1">
                {order.orderNumber || order.code}
              </h1>
              {order.organization && (
                <div className="text-[13px] text-[#898989] mt-2">
                  Organization: <span className="text-[#242424] font-medium">{order.organization.name}</span>
                </div>
              )}
            </div>

            {/* Key-Value Form Fields in 2 columns */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-x-12 gap-y-4 pb-8 border-b border-[#e0e0e0]">
              
              {/* Left Column: Customer Details */}
              <div className="space-y-2">
                <div className="flex text-[14px]">
                  <span className="w-28 shrink-0 text-[#898989] font-[600]">Customer</span>
                  <div className="text-[#242424]">
                    <span className="font-[600]">{order.partner?.name || '—'}</span>
                    {order.partner?.partnerType && (
                      <span className="ml-2 bg-[#f0f4ff] text-[#0066cc] text-[10px] font-bold px-1.5 py-0.5 rounded uppercase tracking-wider">
                        {order.partner.partnerType}
                      </span>
                    )}
                  </div>
                </div>
                {order.partner?.taxCode && (
                  <div className="flex text-[14px]">
                    <span className="w-28 shrink-0 text-[#898989] font-[600]">Tax Code</span>
                    <span className="text-[#242424] font-mono">{order.partner.taxCode}</span>
                  </div>
                )}
                {order.partner?.email && (
                  <div className="flex text-[14px]">
                    <span className="w-28 shrink-0 text-[#898989] font-[600]">Email</span>
                    <a href={`mailto:${order.partner.email}`} className="text-[#0066cc] hover:underline">
                      {order.partner.email}
                    </a>
                  </div>
                )}
                {order.partner?.phone && (
                  <div className="flex text-[14px]">
                    <span className="w-28 shrink-0 text-[#898989] font-[600]">Phone</span>
                    <span className="text-[#242424]">{order.partner.phone}</span>
                  </div>
                )}
                {order.partner?.address && (
                  <div className="flex text-[14px]">
                    <span className="w-28 shrink-0 text-[#898989] font-[600]">Address</span>
                    <span className="text-[#606060] italic leading-relaxed">{order.partner.address}</span>
                  </div>
                )}
              </div>

              {/* Right Column: Dates & CRM details */}
              <div className="space-y-2">
                <div className="flex text-[14px]">
                  <span className="w-36 shrink-0 text-[#898989] font-[600]">Delivery Date</span>
                  <span className="text-[#242424]">{formatDate(order.deliveryDate)}</span>
                </div>
                <div className="flex text-[14px]">
                  <span className="w-36 shrink-0 text-[#898989] font-[600]">Expiration Date</span>
                  <span className="text-[#242424]">{formatDate(order.expirationDate)}</span>
                </div>

                {order.lead && (
                  <div className="border-t border-dashed border-[#e0e0e0] pt-3 mt-3 space-y-2">
                    <div className="flex text-[14px]">
                      <span className="w-36 shrink-0 text-[#898989] font-[600]">CRM Opportunity</span>
                      <div className="text-[#242424]">
                        <span className="font-[600]">{order.lead.name}</span>
                        {order.lead.stage && (
                          <span className="ml-2 bg-[#eafaf1] text-[#28a745] text-[10px] font-bold px-1.5 py-0.5 rounded uppercase tracking-wider border border-[#d2f4e1]">
                            {order.lead.stage}
                          </span>
                        )}
                      </div>
                    </div>
                    {order.lead.expectedRevenue !== undefined && (
                      <div className="flex text-[14px]">
                        <span className="w-36 shrink-0 text-[#898989] font-[600]">Expected Revenue</span>
                        <span className="text-[#242424] font-mono">{formatCurrency(order.lead.expectedRevenue)}</span>
                      </div>
                    )}
                    {order.lead.probability !== undefined && (
                      <div className="flex text-[14px]">
                        <span className="w-36 shrink-0 text-[#898989] font-[600]">Probability</span>
                        <span className="text-[#242424] font-medium">{order.lead.probability}%</span>
                      </div>
                    )}
                    {order.lead.salePerson && (
                      <div className="flex text-[14px]">
                        <span className="w-36 shrink-0 text-[#898989] font-[600]">Salesperson</span>
                        <span className="text-[#242424]">
                          {order.lead.salePerson.firstName} {order.lead.salePerson.lastName}
                          <span className="text-[#898989] text-[12px] ml-1">({order.lead.salePerson.email})</span>
                        </span>
                      </div>
                    )}
                    {order.lead.saleTeam && (
                      <div className="flex text-[14px]">
                        <span className="w-36 shrink-0 text-[#898989] font-[600]">Sales Team</span>
                        <span className="text-[#242424] font-medium">{order.lead.saleTeam.name}</span>
                      </div>
                    )}
                  </div>
                )}
              </div>

            </div>

            {/* Tabs header */}
            <div className="mt-8 border-b border-[#e0e0e0] flex space-x-6 text-[14px]">
              <button className="border-b-2 border-[#0066cc] pb-2 font-[600] text-[#0066cc]">
                Order Lines
              </button>
              <button className="pb-2 text-[#898989] hover:text-[#242424] cursor-not-allowed" disabled>
                Other Info
              </button>
            </div>

            {/* Line Items Table */}
            <div className="mt-4">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-[#e0e0e0] text-[13px] font-[600] text-[#242424]">
                    <th className="py-3 pr-4">Product</th>
                    <th className="py-3 px-4 w-[180px]">Tax</th>
                    <th className="py-3 px-4 w-[100px] text-right">Qty</th>
                    <th className="py-3 px-4 w-[150px] text-right">Unit Price</th>
                    <th className="py-3 pl-4 w-[150px] text-right">Subtotal</th>
                  </tr>
                </thead>
                <tbody>
                  {(order.items ?? []).map((line, idx) => (
                    <tr key={idx} className="border-b border-[#f0f0f0] text-[13px] text-[#242424] hover:bg-[#fafafa]">
                      <td className="py-3 pr-4">
                        <span className="font-[600]">{line.product?.name ?? '—'}</span>
                        {line.product?.code && (
                          <span className="text-[11px] text-[#898989] font-mono block mt-0.5">Code: {line.product.code}</span>
                        )}
                      </td>
                      <td className="py-3 px-4">
                        {line.tax ? (
                          <span className="bg-[#f0f4ff] text-[#0066cc] text-[11px] px-2 py-0.5 rounded font-[500]">
                            {line.tax.name}
                          </span>
                        ) : (
                          <span className="text-[#898989] italic">No Tax</span>
                        )}
                      </td>
                      <td className="py-3 px-4 text-right font-medium">{Number(line.quantity).toLocaleString()}</td>
                      <td className="py-3 px-4 text-right font-mono">{formatCurrency(Number(line.unitPrice))}</td>
                      <td className="py-3 pl-4 text-right font-mono font-[600] text-[#0066cc]">{formatCurrency(Number(line.subtotal))}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              
              {/* Totals Section */}
              <div className="mt-6 flex justify-end">
                <div className="w-full max-w-[320px] space-y-2 text-[14px]">
                  <div className="flex justify-between">
                    <span className="text-[#898989]">Untaxed Amount</span>
                    <span className="font-mono text-[#242424]">{formatCurrency(netTotal)}</span>
                  </div>
                  <div className="flex justify-between border-b border-dashed border-[#e0e0e0] pb-2">
                    <span className="text-[#898989]">Taxes</span>
                    <span className="font-mono text-[#242424]">{formatCurrency(taxTotal)}</span>
                  </div>
                  <div className="flex justify-between py-2">
                    <span className="font-[700] text-[#111111] text-[16px]">Total</span>
                    <span className="font-[700] text-[#0066cc] text-[20px] font-mono">{formatCurrency(grandTotal)}</span>
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

              {order.createdAt && (
                <div className="relative flex items-start z-10 gap-3">
                  {/* Timeline node: Avatar */}
                  <div className="w-8 h-8 rounded-full bg-[#0066cc] text-white font-bold flex items-center justify-center text-[12px] shrink-0 shadow-sm">
                    {order.createdBy?.firstName?.[0] || 'U'}
                  </div>
                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-center text-[13px] mb-1">
                      <span className="font-[600] text-[#242424] truncate">
                        {order.createdBy?.firstName} {order.createdBy?.lastName}
                      </span>
                      <span className="text-[11px] text-[#898989] shrink-0">Original</span>
                    </div>
                    <div className="bg-white border border-[#e0e0e0] rounded-[4px] p-3 shadow-[0px_1px_2px_rgba(0,0,0,0.02)] text-[13px]">
                      <p className="text-[#242424] font-medium mb-1">Document Created</p>
                      <p className="text-[#606060] text-[12px]">
                        Order <span className="font-mono text-[#0066cc]">{order.orderNumber || order.code}</span> was initialized.
                      </p>
                      <div className="text-[10px] text-[#898989] mt-2 font-mono">
                        {new Date(order.createdAt).toLocaleString()}
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {order.updatedAt && order.updatedBy && (
                <div className="relative flex items-start z-10 gap-3">
                  {/* Timeline node: Clock/Settings icon in a white circle */}
                  <div className="w-8 h-8 rounded-full bg-white border border-[#e0e0e0] text-[#898989] flex items-center justify-center shrink-0 shadow-sm">
                    <Clock className="w-4 h-4" />
                  </div>
                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <div className="flex justify-between items-center text-[13px] mb-1">
                      <span className="font-[600] text-[#242424] truncate">
                        {order.updatedBy?.firstName} {order.updatedBy?.lastName}
                      </span>
                      <span className="text-[11px] text-[#898989] shrink-0">Recent Update</span>
                    </div>
                    <div className="bg-white border border-[#e0e0e0] rounded-[4px] p-3 shadow-[0px_1px_2px_rgba(0,0,0,0.02)] text-[13px]">
                      <p className="text-[#242424] font-medium mb-1">Document Modified</p>
                      <p className="text-[#606060] text-[12px]">
                        Changes saved by <span className="text-[#242424] font-semibold">{order.updatedBy.email}</span>.
                      </p>
                      <div className="text-[10px] text-[#898989] mt-2 font-mono">
                        {new Date(order.updatedAt).toLocaleString()}
                      </div>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
