import React, { useState, useEffect } from 'react';
import { SaleOrder, SaleOrderLine, Product, SalePartner } from '../types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Plus, Trash2, ChevronRight, Save, CheckCircle } from 'lucide-react';
import { cn } from '@/lib/utils';
import Link from 'next/link';
import { getProducts, getPartners, createSaleOrder } from '../services/salesService';
import { useRouter } from 'next/navigation';
import { usePermissions } from '@/hooks/use-permissions';

interface Props {
  order: SaleOrder | null;
  orgId: string;
}

export function SaleOrderForm({ order, orgId }: Props) {
  const router = useRouter();
  
  const [products, setProducts] = useState<Product[]>([]);
  const [partners, setPartners] = useState<SalePartner[]>([]);
  const [isLoadingMetadata, setIsLoadingMetadata] = useState(true);
  const { hasPermission } = usePermissions();
  
  const [isSaving, setIsSaving] = useState(false);
  const [status, setStatus] = useState(order?.status || 'DRAFT');
  const [partnerId, setPartnerId] = useState(order?.partner?.id || '');
  const [orderDate, setOrderDate] = useState(order?.orderDate?.split('T')[0] || new Date().toISOString().split('T')[0]);
  const [terms, setTerms] = useState(order?.termsAndConditions || '');
  const [lines, setLines] = useState<SaleOrderLine[]>(order?.lines || []);

  useEffect(() => {
    Promise.all([getProducts(orgId, { limit: 100 }), getPartners(orgId, { limit: 100 })])
      .then(([prodRes, partRes]) => {
        setProducts(prodRes.data || []);
        setPartners(partRes.data || []);
      })
      .catch(console.error)
      .finally(() => setIsLoadingMetadata(false));
  }, [orgId]);

  const calculateSubtotal = (quantity: number, unitPrice: number) => quantity * unitPrice;
  const netTotal = lines.reduce((acc, line) => acc + calculateSubtotal(line.quantity, line.unitPrice), 0);
  const taxTotal = lines.reduce((acc, line) => acc + (calculateSubtotal(line.quantity, line.unitPrice) * (line.taxPercentage / 100)), 0);
  const grandTotal = netTotal + taxTotal;

  const handleAddLine = () => {
    setLines([...lines, { 
      productId: '', 
      description: '', 
      quantity: 1, 
      unitPrice: 0, 
      taxPercentage: 10, 
      subtotal: 0 
    }]);
  };

  const handleLineChange = (index: number, field: keyof SaleOrderLine, value: any) => {
    const newLines = [...lines];
    if (field === 'productId') {
      const prod = products.find(p => p.id === value);
      if (prod) {
        newLines[index] = {
          ...newLines[index],
          productId: prod.id,
          description: prod.name,
          unitPrice: prod.price,
          subtotal: calculateSubtotal(newLines[index].quantity, prod.price)
        };
      } else {
        newLines[index].productId = value;
      }
    } else {
      newLines[index] = { ...newLines[index], [field]: value };
      if (field === 'quantity' || field === 'unitPrice') {
        newLines[index].subtotal = calculateSubtotal(Number(newLines[index].quantity), Number(newLines[index].unitPrice));
      }
    }
    setLines(newLines);
  };

  const handleRemoveLine = (index: number) => {
    setLines(lines.filter((_, i) => i !== index));
  };

  const handleSave = async () => {
    if (!partnerId) return alert("Please select a customer.");
    if (lines.length === 0) return alert("Please add at least one product.");
    
    setIsSaving(true);
    try {
      const payload = {
        partnerId,
        orderDate: new Date(orderDate).toISOString(),
        termsAndConditions: terms,
        lines: lines.map(l => ({
          productId: l.productId,
          quantity: Number(l.quantity),
          unitPrice: Number(l.unitPrice),
          discountPercent: 0
        }))
      };
      
      const isMockedUUIDs = partnerId.length !== 36 || lines.some(l => l.productId.length !== 36);
      
      if (order?.id) {
        if (!isMockedUUIDs && order.id.length === 36) {
           // MOCK: await updateSaleOrder(orgId, order.id, payload);
           // Not actually implemented in SalesService yet
           await new Promise(resolve => setTimeout(resolve, 500));
        } else {
           await new Promise(resolve => setTimeout(resolve, 500)); 
        }
        alert("Saved successfully! (Mocked since API is pending)");
      } else {
        if (!isMockedUUIDs) {
           const res = await createSaleOrder(orgId, payload);
           router.push(`/dashboard/${orgId}/sales/quotations/${res.id}`);
        } else {
           await new Promise(resolve => setTimeout(resolve, 500));
           alert("Quotation saved locally! (Mocked due to missing UUIDs)");
           router.push(`/dashboard/${orgId}/sales/quotations`);
        }
      }
    } catch (err) {
      console.error(err);
      alert("Error saving order. Make sure backend API is implemented.");
    } finally {
      setIsSaving(false);
    }
  };

  const handleConfirm = async () => {
    if (!order?.id) return alert("Please save the quotation first.");
    try {
      // MOCK: await confirmSaleOrder(orgId, order.id);
      await new Promise(resolve => setTimeout(resolve, 500)); // Fake network delay
      setStatus('CONFIRMED');
      alert("Order Confirmed! (Mocked since API is pending)");
    } catch (err) {
      console.error(err);
      alert("Failed to confirm. Make sure backend API is implemented.");
    }
  };

  return (
    <div className="h-full flex flex-col font-['Segoe_UI'] bg-[#f8f8f8]">
      <div className="bg-white border-b border-[#e0e0e0] px-6 h-12 flex items-center shrink-0 justify-between">
        <div className="flex items-center text-[14px]">
          <Link href={`/dashboard/${orgId}/sales/quotations`} className="text-[#898989] hover:text-[#242424]">Quotations</Link>
          <ChevronRight className="w-4 h-4 text-[#898989] mx-2" />
          <span className="text-[#0066cc] font-[600]">{order?.code || 'New Quotation'}</span>
        </div>
        <div className="flex space-x-2">
           <span className={cn("px-3 py-1 rounded-[4px] text-[12px] font-[600] uppercase", 
              status === 'CONFIRMED' ? "bg-[#28a745]/10 text-[#28a745]" : "bg-[#898989]/10 text-[#898989]"
           )}>
             {status}
           </span>
        </div>
      </div>

      <div className="bg-white px-6 py-4 border-b border-[#e0e0e0] flex justify-between items-center shrink-0">
         <div>
            <h1 className="text-[24px] font-[700] text-[#242424] mb-1">{order?.code || 'New Quotation'}</h1>
         </div>
         <div className="flex space-x-2">
            {(order?.id ? hasPermission('sales:write') : hasPermission('sales:create')) && (
              <Button variant="outline" className="border-[#d0d0d0] text-[#242424] hover:bg-[#f8f8f8] h-10 px-4 rounded-[4px] font-[600]" onClick={handleSave} disabled={isSaving}>
                <Save className="w-4 h-4 mr-2" /> {isSaving ? 'Saving...' : 'Save'}
              </Button>
            )}
            {status !== 'CONFIRMED' && hasPermission('sales:write') && (
              <Button className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px] font-[600]" onClick={handleConfirm}>
                <CheckCircle className="w-4 h-4 mr-2" /> Confirm Order
              </Button>
            )}
         </div>
      </div>

      <div className="flex-1 overflow-auto p-6">
         <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
            <div className="p-6 grid grid-cols-2 gap-8 border-b border-[#e0e0e0]">
               <div>
                 <label className="block text-[14px] font-[600] text-[#242424] mb-1">Customer</label>
                 <select 
                   value={partnerId} 
                   onChange={(e) => setPartnerId(e.target.value)}
                   className="h-10 w-full max-w-[300px] border border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] px-3 text-[14px]"
                 >
                   <option value="">-- Select Customer --</option>
                   {partners.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                 </select>
               </div>
               <div>
                 <label className="block text-[14px] font-[600] text-[#242424] mb-1">Order Date</label>
                 <Input 
                   type="date" 
                   value={orderDate}
                   onChange={(e) => setOrderDate(e.target.value)}
                   className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] max-w-[200px]" 
                 />
               </div>
            </div>

            <div className="w-full overflow-x-auto">
               <table className="w-full text-left border-collapse">
                  <thead className="bg-[#f8f8f8]">
                    <tr>
                      <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-b border-[#e0e0e0] w-[200px]">Product</th>
                      <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-b border-[#e0e0e0]">Description</th>
                      <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-b border-[#e0e0e0] w-[100px] text-right">Quantity</th>
                      <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-b border-[#e0e0e0] w-[150px] text-right">Unit Price</th>
                      <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-b border-[#e0e0e0] w-[100px] text-right">Tax %</th>
                      <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-b border-[#e0e0e0] w-[150px] text-right">Subtotal</th>
                      <th className="px-4 py-3 border-b border-[#e0e0e0] w-[50px]"></th>
                    </tr>
                  </thead>
                  <tbody>
                    {lines.map((line, idx) => (
                      <tr key={idx} className="border-b border-[#e0e0e0] hover:bg-[#f0f4ff] bg-white group">
                        <td className="px-2 py-2">
                          <select 
                            value={line.productId}
                            onChange={(e) => handleLineChange(idx, 'productId', e.target.value)}
                            className="w-full h-8 text-[13px] border border-transparent hover:border-[#d0d0d0] focus:border-[#0066cc] rounded px-1 outline-none bg-transparent"
                          >
                            <option value="">-- Select --</option>
                            {products.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
                          </select>
                        </td>
                        <td className="px-2 py-2">
                          <input 
                            type="text" 
                            value={line.description}
                            onChange={(e) => handleLineChange(idx, 'description', e.target.value)}
                            className="w-full h-8 text-[13px] border border-transparent hover:border-[#d0d0d0] focus:border-[#0066cc] rounded px-2 outline-none bg-transparent"
                          />
                        </td>
                        <td className="px-2 py-2">
                          <input 
                            type="number" 
                            value={line.quantity}
                            onChange={(e) => handleLineChange(idx, 'quantity', e.target.value)}
                            className="w-full h-8 text-[13px] text-right border border-transparent hover:border-[#d0d0d0] focus:border-[#0066cc] rounded px-2 outline-none bg-transparent"
                          />
                        </td>
                        <td className="px-2 py-2">
                          <input 
                            type="number" 
                            value={line.unitPrice}
                            onChange={(e) => handleLineChange(idx, 'unitPrice', e.target.value)}
                            className="w-full h-8 text-[13px] text-right font-mono border border-transparent hover:border-[#d0d0d0] focus:border-[#0066cc] rounded px-2 outline-none bg-transparent"
                          />
                        </td>
                        <td className="px-2 py-2">
                          <input 
                            type="number" 
                            value={line.taxPercentage}
                            onChange={(e) => handleLineChange(idx, 'taxPercentage', e.target.value)}
                            className="w-full h-8 text-[13px] text-right border border-transparent hover:border-[#d0d0d0] focus:border-[#0066cc] rounded px-2 outline-none bg-transparent"
                          />
                        </td>
                        <td className="px-4 py-3 text-[13px] text-[#242424] text-right font-mono font-[500]">
                          ₫{line.subtotal.toLocaleString()}
                        </td>
                        <td className="px-4 py-3 text-right">
                          <button onClick={() => handleRemoveLine(idx)} className="text-[#898989] hover:text-[#dc3545] opacity-0 group-hover:opacity-100 transition-opacity">
                            <Trash2 className="w-4 h-4" />
                          </button>
                        </td>
                      </tr>
                    ))}
                    <tr>
                      <td colSpan={7} className="px-4 py-3 bg-white">
                        <Button variant="ghost" onClick={handleAddLine} className="text-[#0066cc] h-8 px-2 hover:bg-[#f0f4ff] text-[13px] font-[600]">
                          <Plus className="w-4 h-4 mr-1" /> Add a product
                        </Button>
                      </td>
                    </tr>
                  </tbody>
               </table>
            </div>

            <div className="p-6 grid grid-cols-2 gap-8 items-start bg-[#fafafa] rounded-b-[4px]">
               <div>
                  <Textarea 
                    placeholder="Terms and conditions..."
                    value={terms}
                    onChange={(e) => setTerms(e.target.value)}
                    className="min-h-[120px] bg-white border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] text-[13px]"
                  />
               </div>
               
               <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] p-4 ml-auto w-full max-w-[320px]">
                  <div className="flex justify-between items-center py-2 border-b border-dashed border-[#e0e0e0]">
                    <span className="text-[13px] text-[#898989]">Untaxed Amount</span>
                    <span className="text-[13px] text-[#242424] font-mono">₫{netTotal.toLocaleString()}</span>
                  </div>
                  <div className="flex justify-between items-center py-2 border-b border-dashed border-[#e0e0e0]">
                    <span className="text-[13px] text-[#898989]">Taxes</span>
                    <span className="text-[13px] text-[#242424] font-mono">₫{taxTotal.toLocaleString()}</span>
                  </div>
                  <div className="flex justify-between items-center py-3 mt-1">
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
