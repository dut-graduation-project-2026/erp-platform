'use client';

import React, { useEffect, useState } from 'react';
import { useRouter, useParams, useSearchParams } from 'next/navigation';
import { getOrders } from '@/features/sales/services/salesService';
import { 
  claimOrderStockMove, 
  previewSmartRoute, 
  confirmSmartRoute 
} from '@/features/inventory/services/inventoryService';
import { SaleOrder } from '@/features/sales/types';
import { ORDER_STATUS } from '@/config/constants';
import { Clock, ArrowRightLeft, Search, Building, Brain, Loader2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { toast } from 'sonner';
import RoutingPreviewModal from '@/features/inventory/components/RoutingPreviewModal';
import { RouteProposalResponse } from '@/features/inventory/types';

export default function PendingOrdersPage() {
  const router = useRouter();
  const params = useParams();
  const orgId = params.orgId as string;
  const searchParams = useSearchParams();
  const warehouseId = searchParams.get('warehouseId') || '';

  const [orders, setOrders] = useState<SaleOrder[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [isClaiming, setIsClaiming] = useState<string | null>(null);

  // Smart routing states
  const [proposals, setProposals] = useState<RouteProposalResponse[]>([]);
  const [isPreviewOpen, setIsPreviewOpen] = useState(false);
  const [isConfirmingRoute, setIsConfirmingRoute] = useState(false);
  const [isPreviewLoading, setIsPreviewLoading] = useState(false);

  const handlePreviewSmartRoute = async () => {
    setIsPreviewLoading(true);
    try {
      const result = await previewSmartRoute(orgId);
      if (result.length === 0) {
        toast.info('No pending confirmed orders to evaluate for routing.');
        return;
      }
      setProposals(result);
      setIsPreviewOpen(true);
    } catch (error: any) {
      console.error(error);
      toast.error(error.response?.data?.message || 'Failed to retrieve routing preview.');
    } finally {
      setIsPreviewLoading(false);
    }
  };

  const handleConfirmRoute = async (confirmations: { orderId: string; warehouseId: string }[]) => {
    setIsConfirmingRoute(true);
    try {
      await confirmSmartRoute(orgId, { routeConfirmations: confirmations });
      toast.success(`Successfully allocated ${confirmations.length} orders to warehouses.`);
      setIsPreviewOpen(false);
      await fetchPendingOrders();
    } catch (error: any) {
      console.error(error);
      toast.error(error.response?.data?.message || 'Failed to confirm order routing.');
    } finally {
      setIsConfirmingRoute(false);
    }
  };

  const fetchPendingOrders = async () => {
    setIsLoading(true);
    try {
      const res = await getOrders(orgId, { limit: 100 });
      // Filter for CONFIRMED orders only
      const pending = (res.data || []).filter(o => o.status === ORDER_STATUS.CONFIRMED);
      setOrders(pending);
    } catch (error) {
      console.error(error);
      toast.error('Failed to load pending orders');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchPendingOrders();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [orgId]);

  const handleClaimOrder = async (orderId: string) => {
    if (!warehouseId) {
      toast.error('Please select a warehouse from the dashboard first.');
      return;
    }
    
    setIsClaiming(orderId);
    try {
      const doc = await claimOrderStockMove(orgId, warehouseId, orderId);
      toast.success('Order claimed successfully! Issue document created.');
      // Remove from list
      setOrders(prev => prev.filter(o => o.id !== orderId));
      // Optionally redirect to the created document
      router.push(`/dashboard/${orgId}/inventory/documents/${doc.id}?warehouseId=${warehouseId}`);
    } catch (error: any) {
      console.error(error);
      toast.error(error.response?.data?.message || 'Failed to claim order.');
    } finally {
      setIsClaiming(null);
    }
  };

  const filteredOrders = orders.filter(o => 
    o.orderNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    o.partner?.name?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="h-full flex flex-col font-['Segoe_UI'] bg-[#f8f8f8]">
      {/* Header */}
      <div className="bg-white px-6 py-4 border-b border-[#e0e0e0] flex justify-between items-center shrink-0">
        <div className="flex items-center">
          <Clock className="w-5 h-5 mr-2 text-[#ffc107]" />
          <h1 className="text-[20px] font-[600] text-[#242424]">Orders to Process (Pending Fulfillment)</h1>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 overflow-auto p-6">
        <div className="max-w-[1200px] mx-auto bg-white rounded-[4px] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] border border-[#e0e0e0]">
          
          {/* Toolbar */}
          <div className="p-4 border-b border-[#e0e0e0] flex justify-between items-center">
            <div className="flex items-center gap-4">
              <div className="relative w-[300px]">
                <Search className="w-4 h-4 absolute left-3 top-2.5 text-[#898989]" />
                <Input
                  placeholder="Search by Order Number or Customer..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-9 h-9 text-[13px] rounded-[4px] border-[#d0d0d0]"
                />
              </div>
              <Button
                onClick={handlePreviewSmartRoute}
                disabled={isLoading || isPreviewLoading || orders.length === 0}
                className="bg-gradient-to-r from-[#10b981] to-[#059669] hover:from-[#059669] hover:to-[#047857] text-white h-9 px-4 text-[13px] rounded-[4px] font-[600] flex items-center gap-1.5 shadow-sm transition-all"
              >
                {isPreviewLoading ? (
                  <>
                    <Loader2 className="w-3.5 h-3.5 animate-spin" />
                    Calculating...
                  </>
                ) : (
                  <>
                    <Brain className="w-3.5 h-3.5" />
                    Smart Auto-Route All
                  </>
                )}
              </Button>
            </div>
            {!warehouseId && (
              <div className="text-[13px] text-[#dc3545] font-[500] flex items-center bg-[#fdf2f2] px-3 py-1 rounded-[4px]">
                <Building className="w-4 h-4 mr-1.5" /> No warehouse context. Go back to Dashboard to select a warehouse.
              </div>
            )}
          </div>

          {/* Table */}
          <div className="overflow-x-auto">
            <table className="w-full text-[13px] text-left">
              <thead className="bg-[#f5f5f5] text-[#898989] font-[600] uppercase text-[11px] border-b border-[#e0e0e0]">
                <tr>
                  <th className="px-4 py-3">Order Number</th>
                  <th className="px-4 py-3">Customer</th>
                  <th className="px-4 py-3">Order Date</th>
                  <th className="px-4 py-3 text-right">Amount</th>
                  <th className="px-4 py-3 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#e0e0e0]">
                {isLoading ? (
                  <tr>
                    <td colSpan={5} className="px-4 py-8 text-center text-[#898989]">
                      Loading pending orders...
                    </td>
                  </tr>
                ) : filteredOrders.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-4 py-8 text-center text-[#898989]">
                      No pending orders found.
                    </td>
                  </tr>
                ) : (
                  filteredOrders.map((order) => (
                    <tr key={order.id} className="hover:bg-[#fafafa] transition-colors">
                      <td className="px-4 py-3 font-[600] text-[#0066cc]">
                        {order.orderNumber}
                      </td>
                      <td className="px-4 py-3 text-[#242424]">
                        {order.partner?.name || '-'}
                      </td>
                      <td className="px-4 py-3 text-[#4a4a4a]">
                        {new Date(order.createdAt || '').toLocaleDateString()}
                      </td>
                      <td className="px-4 py-3 text-right font-[500] text-[#242424]">
                        ${(order.totalAmount || 0).toLocaleString()}
                      </td>
                      <td className="px-4 py-3 text-right">
                        <Button
                          onClick={() => handleClaimOrder(order.id)}
                          disabled={!warehouseId || isClaiming === order.id}
                          className="bg-[#0066cc] hover:bg-[#004499] text-white h-8 px-3 text-[12px] rounded-[4px] inline-flex items-center"
                        >
                          {isClaiming === order.id ? (
                            <span className="flex items-center"><ArrowRightLeft className="w-3.5 h-3.5 mr-1.5 animate-spin" /> Claiming...</span>
                          ) : (
                            <span className="flex items-center"><ArrowRightLeft className="w-3.5 h-3.5 mr-1.5" /> Claim to Warehouse</span>
                          )}
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
      <RoutingPreviewModal
        isOpen={isPreviewOpen}
        onClose={() => setIsPreviewOpen(false)}
        proposals={proposals}
        onConfirm={handleConfirmRoute}
        isConfirming={isConfirmingRoute}
      />
    </div>
  );
}
