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
import { ArrowLeft, Plus, Trash2, Save, PackageMinus } from 'lucide-react';
import { toast } from 'sonner';
import { APP_ROUTES } from '@/config/constants';

export default function NewDeliveryPage({ 
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
    setItems([...items, { productId: '', quantity: 1 }]);
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
      toast.error('Please select a source warehouse');
      return;
    }
    if (items.length === 0) {
      toast.error('Please add at least one product line');
      return;
    }
    if (items.some(i => !i.productId || i.quantity <= 0)) {
      toast.error('Please ensure all lines have a product and valid positive quantity');
      return;
    }

    setIsSubmitting(true);
    try {
      const doc = await createInventoryDocument(orgId, warehouseId, {
        documentType: 'ISSUE',
        scheduledDate: new Date(scheduledDate).toISOString(),
        notes,
        items
      });
      toast.success('Delivery document created successfully');
      router.push(`${APP_ROUTES.INVENTORY.DOCUMENT_DETAIL(orgId, doc.id)}?whId=${warehouseId}`);
    } catch (err) {
      console.error(err);
      toast.error('Failed to create delivery document');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return <div className="p-8 text-[#898989]">Loading form...</div>;
  }

  return (
    <div className="h-full flex flex-col items-center justify-center font-['Segoe_UI'] bg-[#f8f8f8] p-8">
      <div className="bg-white p-8 rounded-[8px] shadow-sm border border-[#e0e0e0] max-w-[500px] text-center">
        <PackageMinus className="w-12 h-12 text-[#c65911] mx-auto mb-4" />
        <h2 className="text-[20px] font-[700] text-[#242424] mb-2">Manual Creation Disabled</h2>
        <p className="text-[14px] text-[#555] mb-6">
          Manual creation of Delivery Orders has been disabled. To maintain data integrity, all Delivery Orders must be automatically generated from confirmed Sales Orders via the Pending Fulfillment queue.
        </p>
        <Button 
          onClick={() => router.back()} 
          className="bg-[#0066cc] hover:bg-[#004499] text-white"
        >
          <ArrowLeft className="w-4 h-4 mr-2" /> Go Back
        </Button>
      </div>
    </div>
  );
}
