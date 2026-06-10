'use client';

import { useEffect, useState, use } from 'react';
import { SaleOrderForm } from '@/features/sales/components/SaleOrderForm';
import { getSaleOrderById } from '@/features/sales/services/salesService';
import { SaleOrder } from '@/features/sales/types';

export default function QuotationDetailPage({ params }: { params: Promise<{ orgId: string, id: string }> }) {
  const { orgId, id } = use(params);
  const [order, setOrder] = useState<SaleOrder | null>(null);
  const [isLoading, setIsLoading] = useState(id !== 'new');

  useEffect(() => {
    if (id === 'new') {
      return;
    }

    getSaleOrderById(orgId, id)
      .then(data => setOrder(data))
      .catch(console.error)
      .finally(() => setIsLoading(false));
  }, [orgId, id]);

  if (isLoading) {
    return <div className="p-6 text-[#898989] font-['Segoe_UI'] flex justify-center items-center h-full">Loading Quotation...</div>;
  }

  return (
    <div className="h-full">
      <SaleOrderForm order={order} orgId={orgId} />
    </div>
  );
}
