'use client';

import React, { useState, useEffect } from 'react';
import { OrderItem, Product, SaleOrder, SaleTax } from '../types';
import { Button } from '@/components/ui/button';
import { ORDER_STATUS, TAX_COMPUTATION } from '@/config/constants';
import { Input } from '@/components/ui/input';
import { Plus, Trash2, ChevronRight, Save, CheckCircle, XCircle, Receipt } from 'lucide-react';
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
      for (const line of lines) {
        const itemPayload = {
          productId: line.productId,
          taxId: line.taxId || undefined,
          quantity: line.quantity,
          unitPrice: line.unitPrice,
        };
        if (!line.productId) continue;
        if (line.id) {
          await updateOrderItem(orgId, saved.id, line.id, itemPayload);
        } else {
          const created = await createOrderItem(orgId, saved.id, itemPayload);
          line.id = created.id;
        }
      }

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

      {/* Form body */}
      <div className="flex-1 overflow-auto p-6">
        <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">

          {/* Header fields */}
          <div className="p-6 grid grid-cols-2 gap-8 border-b border-[#e0e0e0]">
            <div>
              <label className="block text-[13px] font-[600] text-[#242424] mb-1">
                CRM Lead <span className="text-red-500">*</span>
              </label>
              <select
                value={leadId}
                onChange={(e) => setLeadId(e.target.value)}
                disabled={localStatus !== 'DRAFT'}
                className="h-10 w-full border border-[#d0d0d0] rounded-[4px] px-3 text-[14px] focus:outline-none focus:border-[#0066cc]"
              >
                <option value="">-- Select Lead --</option>
                {leads.map((l) => <option key={l.id} value={l.id}>{l.name}</option>)}
              </select>
              <p className="text-[11px] text-[#898989] mt-1">
                The customer is resolved automatically from the linked lead.
              </p>
            </div>

            <div>
              <label className="block text-[13px] font-[600] text-[#242424] mb-1">Order Number</label>
              <Input
                value={orderNumber}
                onChange={(e) => setOrderNumber(e.target.value)}
                placeholder="Auto-generated if blank"
                disabled={localStatus !== 'DRAFT'}
                className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
              />
            </div>

            <div>
              <label className="block text-[13px] font-[600] text-[#242424] mb-1">Delivery Date</label>
              <Input
                type="date"
                value={deliveryDate}
                onChange={(e) => setDeliveryDate(e.target.value)}
                disabled={localStatus !== 'DRAFT'}
                className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
              />
            </div>

            <div>
              <label className="block text-[13px] font-[600] text-[#242424] mb-1">Expiration Date</label>
              <Input
                type="date"
                value={expirationDate}
                onChange={(e) => setExpirationDate(e.target.value)}
                disabled={localStatus !== 'DRAFT'}
                className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
              />
            </div>
          </div>

          {/* Order lines table */}
          <div className="w-full overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead className="bg-[#f8f8f8]">
                <tr>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-b border-[#e0e0e0] w-[220px]">Product</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-b border-[#e0e0e0] w-[160px]">Tax</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-b border-[#e0e0e0] w-[100px] text-right">Qty</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-b border-[#e0e0e0] w-[150px] text-right">Unit Price</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-b border-[#e0e0e0] w-[150px] text-right">Subtotal</th>
                  {localStatus === 'DRAFT' && <th className="border-b border-[#e0e0e0] w-[50px]" />}
                </tr>
              </thead>
              <tbody>
                {lines.map((line, idx) => (
                  <tr key={idx} className="border-b border-[#e0e0e0] hover:bg-[#f0f4ff] bg-white group">
                    <td className="px-2 py-2">
                      <select
                        value={line.productId}
                        onChange={(e) => handleLineChange(idx, 'productId', e.target.value)}
                        disabled={localStatus !== 'DRAFT'}
                        className="w-full h-8 text-[13px] border border-transparent hover:border-[#d0d0d0] focus:border-[#0066cc] rounded px-1 outline-none bg-transparent"
                      >
                        <option value="">-- Select --</option>
                        {products.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                      </select>
                    </td>
                    <td className="px-2 py-2">
                      <select
                        value={line.taxId}
                        onChange={(e) => handleLineChange(idx, 'taxId', e.target.value)}
                        disabled={localStatus !== 'DRAFT'}
                        className="w-full h-8 text-[13px] border border-transparent hover:border-[#d0d0d0] focus:border-[#0066cc] rounded px-1 outline-none bg-transparent"
                      >
                        <option value="">None</option>
                        {taxes.map((t) => (
                          <option key={t.id} value={t.id}>
                            {t.name} ({t.amount}{t.computation === 'PERCENTAGE' ? '%' : '₫'})
                          </option>
                        ))}
                      </select>
                    </td>
                    <td className="px-2 py-2">
                      <input
                        type="number" min={0.0001}
                        value={line.quantity}
                        onChange={(e) => handleLineChange(idx, 'quantity', Number(e.target.value))}
                        disabled={localStatus !== 'DRAFT'}
                        className="w-full h-8 text-[13px] text-right border border-transparent hover:border-[#d0d0d0] focus:border-[#0066cc] rounded px-2 outline-none bg-transparent"
                      />
                    </td>
                    <td className="px-2 py-2">
                      <input
                        type="number" min={0}
                        value={line.unitPrice}
                        onChange={(e) => handleLineChange(idx, 'unitPrice', Number(e.target.value))}
                        disabled={localStatus !== 'DRAFT'}
                        className="w-full h-8 text-[13px] text-right font-mono border border-transparent hover:border-[#d0d0d0] focus:border-[#0066cc] rounded px-2 outline-none bg-transparent"
                      />
                    </td>
                    <td className="px-4 py-3 text-[13px] text-[#242424] text-right font-mono font-[500]">
                      ₫{(line.quantity * line.unitPrice).toLocaleString()}
                    </td>
                    {localStatus === 'DRAFT' && (
                      <td className="px-4 py-3 text-right">
                        <button onClick={() => handleRemoveLine(idx)} className="text-[#898989] hover:text-[#dc3545] opacity-0 group-hover:opacity-100 transition-opacity">
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    )}
                  </tr>
                ))}
                {localStatus === 'DRAFT' && (
                  <tr>
                    <td colSpan={6} className="px-4 py-3 bg-white">
                      <Button variant="ghost" onClick={handleAddLine} className="text-[#0066cc] h-8 px-2 hover:bg-[#f0f4ff] text-[13px] font-[600]">
                        <Plus className="w-4 h-4 mr-1" />Add a product
                      </Button>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          {/* Totals */}
          <div className="p-6 flex justify-end bg-[#fafafa] rounded-b-[4px]">
            <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-sm p-4 w-full max-w-[320px]">
              <div className="flex justify-between py-2 border-b border-dashed border-[#e0e0e0]">
                <span className="text-[13px] text-[#898989]">Untaxed Amount</span>
                <span className="text-[13px] font-mono">₫{netTotal.toLocaleString()}</span>
              </div>
              <div className="flex justify-between py-2 border-b border-dashed border-[#e0e0e0]">
                <span className="text-[13px] text-[#898989]">Taxes</span>
                <span className="text-[13px] font-mono">₫{taxTotal.toLocaleString()}</span>
              </div>
              <div className="flex justify-between py-3">
                <span className="text-[16px] font-[700] text-[#242424]">Total</span>
                <span className="text-[18px] font-[700] text-[#0066cc] font-mono">₫{grandTotal.toLocaleString()}</span>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
