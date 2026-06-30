'use client';

import React, { useEffect, useState, use } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { 
  createInventoryDocument,
  getWarehouses
} from '@/features/inventory/services/inventoryService';
import { getProducts } from '@/features/sales/services/salesService';
import { Warehouse } from '@/features/inventory/types';
import { Product } from '@/features/sales/types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { ArrowLeft, Plus, Trash2, Save, Activity } from 'lucide-react';
import { toast } from 'sonner';
import { APP_ROUTES } from '@/config/constants';

export default function NewAdjustmentPage({ 
  params 
}: { 
  params: Promise<{ orgId: string }> 
}) {
  const { orgId } = use(params);
  const router = useRouter();
  const searchParams = useSearchParams();
  const defaultWarehouseId = searchParams.get('warehouseId');

  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  
  const [warehouseId, setWarehouseId] = useState<string>(defaultWarehouseId || '');
  const [scheduledDate, setScheduledDate] = useState<string>(new Date().toISOString().split('T')[0]);
  const [notes, setNotes] = useState('');
  
  const [items, setItems] = useState<{ productId: string; quantity: number }[]>([]);
  
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    setIsLoading(true);
    Promise.all([
      getWarehouses(orgId, { limit: 100 }),
      getProducts(orgId, { limit: 500 })
    ]).then(([whRes, prodRes]) => {
      const whs = whRes.data || [];
      setWarehouses(whs);
      if (!warehouseId && whs.length > 0) {
        setWarehouseId(whs[0].id);
      }
      setProducts(prodRes.data || []);
    }).catch((err) => {
      console.error(err);
      toast.error('Failed to load initial data');
    }).finally(() => {
      setIsLoading(false);
    });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [orgId]);

  const handleAddItem = () => {
    setItems([...items, { productId: '', quantity: 0 }]);
  };

  const handleRemoveItem = (index: number) => {
    const newItems = [...items];
    newItems.splice(index, 1);
    setItems(newItems);
  };

  const handleItemChange = (index: number, field: string, value: any) => {
    const newItems = [...items];
    (newItems[index] as any)[field] = value;
    setItems(newItems);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!warehouseId) {
      toast.error('Please select a warehouse');
      return;
    }
    if (items.length === 0) {
      toast.error('Please add at least one product line');
      return;
    }
    if (items.some(i => !i.productId || i.quantity === 0)) {
      toast.error('Please ensure all lines have a product and a non-zero quantity difference');
      return;
    }

    setIsSubmitting(true);
    try {
      const doc = await createInventoryDocument(orgId, warehouseId, {
        documentType: 'ADJUSTMENT',
        scheduledDate: new Date(scheduledDate).toISOString(),
        notes,
        items
      });
      toast.success('Inventory adjustment document created successfully');
      router.push(`${APP_ROUTES.INVENTORY.DOCUMENT_DETAIL(orgId, doc.id)}?whId=${warehouseId}`);
    } catch (err) {
      console.error(err);
      toast.error('Failed to create adjustment document');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return <div className="p-8 text-[#898989]">Loading form...</div>;
  }

  return (
    <div className="h-full flex flex-col font-['Segoe_UI'] bg-white">
      {/* Header */}
      <div className="px-6 py-4 border-b border-[#e0e0e0] flex items-center justify-between shrink-0">
        <div className="flex items-center space-x-3">
          <Button 
            variant="ghost" 
            size="icon" 
            onClick={() => router.back()} 
            className="h-8 w-8 text-[#898989]"
          >
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div className="flex items-center space-x-2 text-[#242424]">
            <Activity className="w-5 h-5 text-[#475569]" />
            <h2 className="text-[18px] font-[600]">New Inventory Adjustment</h2>
          </div>
        </div>
        <div className="flex space-x-2">
          <Button 
            variant="outline" 
            onClick={() => router.back()}
            className="border-[#d0d0d0] text-[#242424]"
          >
            Cancel
          </Button>
          <Button 
            onClick={handleSubmit} 
            disabled={isSubmitting}
            className="bg-[#475569] hover:bg-[#334155] text-white"
          >
            <Save className="w-4 h-4 mr-2" />
            {isSubmitting ? 'Saving...' : 'Save Draft'}
          </Button>
        </div>
      </div>

      <div className="flex-1 overflow-auto bg-[#f8f8f8] p-6">
        <div className="max-w-[1000px] mx-auto space-y-6">
          {/* General Information */}
          <div className="bg-white border border-[#e0e0e0] rounded-[6px] shadow-sm p-6">
            <h3 className="text-[14px] font-[700] text-[#242424] mb-4 border-b border-[#f0f0f0] pb-2">Audit Information</h3>
            <div className="grid grid-cols-2 gap-6">
              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Warehouse Location *</label>
                <select
                  value={warehouseId}
                  onChange={(e) => setWarehouseId(e.target.value)}
                  className="w-full h-10 border border-[#d0d0d0] rounded-[4px] px-3 text-[13px] focus:border-[#475569] focus:ring-1 outline-none"
                >
                  <option value="" disabled>Select a warehouse</option>
                  {warehouses.map(w => (
                    <option key={w.id} value={w.id}>{w.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Scheduled / Audit Date *</label>
                <Input 
                  type="date"
                  value={scheduledDate}
                  onChange={(e) => setScheduledDate(e.target.value)}
                  className="h-10 text-[13px]"
                />
              </div>
            </div>
            <div className="mt-4">
              <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Audit Notes / Reasons</label>
              <Textarea 
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="E.g. Found extra stock during end of year audit..."
                rows={3}
                className="text-[13px]"
              />
            </div>
          </div>

          {/* Product Lines */}
          <div className="bg-white border border-[#e0e0e0] rounded-[6px] shadow-sm p-6">
            <div className="flex justify-between items-center mb-4 border-b border-[#f0f0f0] pb-2">
              <h3 className="text-[14px] font-[700] text-[#242424]">Adjustment Entries</h3>
              <Button 
                onClick={handleAddItem}
                variant="outline"
                size="sm"
                className="text-[#475569] border-[#475569] hover:bg-[#f1f5f9] h-8 text-[12px]"
              >
                <Plus className="w-4 h-4 mr-1" /> Add Product
              </Button>
            </div>
            
            <table className="w-full text-left">
              <thead>
                <tr className="border-b border-[#f0f0f0] text-[12px] text-[#898989] font-[600] uppercase tracking-wider">
                  <th className="pb-2 w-[60%]">Product</th>
                  <th className="pb-2 w-[30%]">Quantity Difference (+ or -)</th>
                  <th className="pb-2 w-[10%] text-center">Action</th>
                </tr>
              </thead>
              <tbody>
                {items.length === 0 ? (
                  <tr>
                    <td colSpan={3} className="py-8 text-center text-[#898989] text-[13px]">
                      No adjustment entries added yet. Click &quot;Add Product&quot; to begin.
                    </td>
                  </tr>
                ) : (
                  items.map((item, idx) => (
                    <tr key={idx} className="border-b border-[#f5f5f5] last:border-0">
                      <td className="py-3 pr-4 align-top">
                        <select
                          value={item.productId}
                          onChange={(e) => handleItemChange(idx, 'productId', e.target.value)}
                          className="w-full h-9 border border-[#d0d0d0] rounded-[4px] px-2 text-[13px] outline-none focus:border-[#475569]"
                        >
                          <option value="" disabled>Select Product...</option>
                          {products.map(p => (
                            <option key={p.id} value={p.id}>[{p.sku}] {p.name}</option>
                          ))}
                        </select>
                      </td>
                      <td className="py-3 pr-4 align-top">
                        <Input 
                          type="number"
                          value={item.quantity}
                          onChange={(e) => handleItemChange(idx, 'quantity', Number(e.target.value))}
                          placeholder="-5 for loss, 5 for found"
                          className="h-9 text-[13px] font-mono"
                        />
                        <span className="text-[10px] text-[#898989] mt-1 block">Negative for missing stock, positive for extra stock.</span>
                      </td>
                      <td className="py-3 text-center align-top">
                        <Button
                          variant="ghost"
                          size="icon"
                          onClick={() => handleRemoveItem(idx)}
                          className="text-[#dc3545] hover:bg-[#fff0f0] h-9 w-9"
                        >
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
}
