'use client';

import React, { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { Building, Package, ArrowRightLeft, Clock, Search, ExternalLink, Settings, LayoutGrid } from 'lucide-react';
import { getWarehouses, getWarehouseMetrics } from '@/features/inventory/services/inventoryService';
import { Warehouse } from '@/features/inventory/types';
import { usePermissions } from '@/hooks/use-permissions';
import { PERMISSIONS } from '@/config/permissions';
import { DOCUMENT_TYPE, DOCUMENT_STATUS, ORDER_STATUS, APP_ROUTES } from '@/config/constants';
import Link from 'next/link';

export default function InventoryDashboard() {
  const router = useRouter();
  const params = useParams();
  const orgId = params.orgId as string;
  const { hasPermission } = usePermissions();

  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState<string>('');
  
  // Dashboard Metrics
  const [metrics, setMetrics] = useState({
    receipts: { toProcess: 0, late: 0, backorders: 0 },
    deliveries: { toProcess: 0, late: 0, backorders: 0 },
    transfers: { toProcess: 0, late: 0, backorders: 0 },
    pendingOrders: 0
  });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // 1. Fetch Warehouses
    getWarehouses(orgId, { limit: 50 }).then((res) => {
      const whs = res.data || [];
      setWarehouses(whs);
      if (whs.length > 0) {
        setSelectedWarehouseId(whs[0].id);
      } else {
        setIsLoading(false);
      }
    }).catch(() => setIsLoading(false));
  }, [orgId]);

  useEffect(() => {
    if (!selectedWarehouseId) return;
    setIsLoading(true);

    getWarehouseMetrics(orgId, selectedWarehouseId).then((res) => {
      setMetrics({
        receipts: res.receipts,
        deliveries: res.deliveries,
        transfers: res.internalTransfers,
        pendingOrders: res.pendingFulfillmentCount
      });
    }).catch((err) => {
      console.error("Failed to load warehouse metrics", err);
    }).finally(() => {
      setIsLoading(false);
    });

  }, [orgId, selectedWarehouseId]);

  return (
    <div className="h-full flex flex-col font-['Segoe_UI'] bg-[#f0f2f5]">
      {/* Header */}
      <div className="bg-white px-6 py-4 shadow-[0px_1px_2px_rgba(0,0,0,0.05)] flex justify-between items-center shrink-0 z-10">
        <h1 className="text-[20px] font-[600] text-[#242424] flex items-center">
          <LayoutGrid className="w-5 h-5 mr-3 text-[#0066cc]" />
          Inventory Overview
        </h1>
        <div className="flex items-center space-x-3">
          <label className="text-[13px] font-[600] text-[#555]">Warehouse</label>
          <select
            value={selectedWarehouseId}
            onChange={(e) => setSelectedWarehouseId(e.target.value)}
            className="h-9 border border-[#d0d0d0] rounded-[4px] px-3 text-[13px] outline-none focus:border-[#0066cc] focus:ring-1 focus:ring-[#0066cc] min-w-[200px] shadow-sm bg-[#f9fafb]"
          >
            {warehouses.map(w => (
              <option key={w.id} value={w.id}>{w.name}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 overflow-auto p-6">
        <div className="max-w-[1400px] mx-auto">
          
          {isLoading ? (
            <div className="flex flex-col items-center justify-center h-[400px] text-[#898989]">
              <div className="w-8 h-8 border-4 border-[#e0eafb] border-t-[#0066cc] rounded-full animate-spin mb-4"></div>
              <span className="text-[14px] font-[500]">Loading warehouse operations...</span>
            </div>
          ) : !selectedWarehouseId ? (
            <div className="bg-white p-12 rounded-[8px] shadow-sm text-center border border-[#e0e0e0]">
              <Package className="w-12 h-12 text-[#d0d0d0] mx-auto mb-4" />
              <h3 className="text-[18px] font-[600] text-[#242424] mb-2">No Warehouses Configured</h3>
              <p className="text-[14px] text-[#555]">Please create a warehouse to view the inventory dashboard.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              
              {/* Receipts Card */}
              <div className="bg-white rounded-[6px] shadow-[0px_2px_4px_rgba(0,0,0,0.04)] border border-[#e0e0e0] overflow-hidden flex flex-col group transition-shadow hover:shadow-[0px_4px_12px_rgba(0,0,0,0.08)]">
                <div className="bg-[#f8f9fa] border-b border-[#e0e0e0] p-4 flex justify-between items-start">
                  <div>
                    <h2 
                      className="text-[16px] font-[600] text-[#0066cc] hover:underline cursor-pointer flex items-center"
                      onClick={() => router.push(`${APP_ROUTES.INVENTORY.RECEIPTS(orgId)}?warehouseId=${selectedWarehouseId}`)}
                    >
                      Receipts
                    </h2>
                    <span className="text-[12px] text-[#898989]">{warehouses.find(w => w.id === selectedWarehouseId)?.name}</span>
                  </div>
                
                </div>
                <div className="p-5 flex-1 grid grid-cols-2 gap-4">
                  <div className="flex flex-col items-center justify-center p-3 bg-[#f0f4ff] rounded-[6px] cursor-pointer hover:bg-[#e0eafb] transition-colors" onClick={() => router.push(`${APP_ROUTES.INVENTORY.RECEIPTS(orgId)}?warehouseId=${selectedWarehouseId}`)}>
                    <span className="text-[28px] font-mono font-[700] text-[#0066cc] leading-none mb-1">{metrics.receipts.toProcess}</span>
                    <span className="text-[12px] font-[600] text-[#0066cc] uppercase tracking-wide">To Process</span>
                  </div>
                  <div className="flex flex-col space-y-2 justify-center">
                    {metrics.receipts.late > 0 && (
                      <div className="flex justify-between items-center text-[13px] text-red-600 hover:underline cursor-pointer font-[600]">
                        <span>Late</span><span>{metrics.receipts.late}</span>
                      </div>
                    )}
                    {metrics.receipts.backorders > 0 && (
                      <div className="flex justify-between items-center text-[13px] text-[#ff9800] hover:underline cursor-pointer font-[600]">
                        <span>Backorders</span><span>{metrics.receipts.backorders}</span>
                      </div>
                    )}
                  </div>
                </div>
                <div className="px-5 py-3 bg-[#fbfbfb] border-t border-[#f0f0f0]">
                  <button 
                    onClick={() => router.push(`${APP_ROUTES.INVENTORY.RECEIPTS_NEW(orgId)}?warehouseId=${selectedWarehouseId}`)} 
                    className="text-[13px] font-[600] text-[#0066cc] hover:underline"
                  >
                    New Receipt
                  </button>
                </div>
              </div>

              {/* Delivery Orders Card */}
              <div className="bg-white rounded-[6px] shadow-[0px_2px_4px_rgba(0,0,0,0.04)] border border-[#e0e0e0] overflow-hidden flex flex-col group transition-shadow hover:shadow-[0px_4px_12px_rgba(0,0,0,0.08)]">
                <div className="bg-[#f8f9fa] border-b border-[#e0e0e0] p-4 flex justify-between items-start">
                  <div>
                    <h2 
                      className="text-[16px] font-[600] text-[#0066cc] hover:underline cursor-pointer flex items-center"
                      onClick={() => router.push(`${APP_ROUTES.INVENTORY.DELIVERIES(orgId)}?warehouseId=${selectedWarehouseId}`)}
                    >
                      Delivery Orders
                    </h2>
                    <span className="text-[12px] text-[#898989]">{warehouses.find(w => w.id === selectedWarehouseId)?.name}</span>
                  </div>
                
                </div>
                <div className="p-5 flex-1 grid grid-cols-2 gap-4">
                  <div className="flex flex-col items-center justify-center p-3 bg-[#f0f4ff] rounded-[6px] cursor-pointer hover:bg-[#e0eafb] transition-colors" onClick={() => router.push(`${APP_ROUTES.INVENTORY.DELIVERIES(orgId)}?warehouseId=${selectedWarehouseId}`)}>
                    <span className="text-[28px] font-mono font-[700] text-[#0066cc] leading-none mb-1">{metrics.deliveries.toProcess}</span>
                    <span className="text-[12px] font-[600] text-[#0066cc] uppercase tracking-wide">To Process</span>
                  </div>
                  <div className="flex flex-col space-y-2 justify-center">
                    {metrics.deliveries.late > 0 && (
                      <div className="flex justify-between items-center text-[13px] text-red-600 hover:underline cursor-pointer font-[600]">
                        <span>Late</span><span>{metrics.deliveries.late}</span>
                      </div>
                    )}
                    {metrics.deliveries.backorders > 0 && (
                      <div className="flex justify-between items-center text-[13px] text-[#ff9800] hover:underline cursor-pointer font-[600]">
                        <span>Waiting</span><span>{metrics.deliveries.backorders}</span>
                      </div>
                    )}
                  </div>
                </div>
                <div className="px-5 py-3 bg-[#fbfbfb] border-t border-[#f0f0f0]">
                  <button 
                    onClick={() => router.push(`${APP_ROUTES.INVENTORY.DELIVERIES_NEW(orgId)}?warehouseId=${selectedWarehouseId}`)} 
                    className="text-[13px] font-[600] text-[#0066cc] hover:underline"
                  >
                    New Delivery
                  </button>
                </div>
              </div>

              {/* Internal Transfers Card */}
              <div className="bg-white rounded-[6px] shadow-[0px_2px_4px_rgba(0,0,0,0.04)] border border-[#e0e0e0] overflow-hidden flex flex-col group transition-shadow hover:shadow-[0px_4px_12px_rgba(0,0,0,0.08)]">
                <div className="bg-[#f8f9fa] border-b border-[#e0e0e0] p-4 flex justify-between items-start">
                  <div>
                    <h2 
                      className="text-[16px] font-[600] text-[#0066cc] hover:underline cursor-pointer flex items-center"
                      onClick={() => router.push(`${APP_ROUTES.INVENTORY.DOCUMENTS(orgId)}?type=TRANSFER&warehouseId=${selectedWarehouseId}`)}
                    >
                      Internal Transfers
                    </h2>
                    <span className="text-[12px] text-[#898989]">{warehouses.find(w => w.id === selectedWarehouseId)?.name}</span>
                  </div>
                 
                </div>
                <div className="p-5 flex-1 grid grid-cols-2 gap-4">
                  <div className="flex flex-col items-center justify-center p-3 bg-[#f0f4ff] rounded-[6px] cursor-pointer hover:bg-[#e0eafb] transition-colors" onClick={() => router.push(`${APP_ROUTES.INVENTORY.DOCUMENTS(orgId)}?type=TRANSFER&warehouseId=${selectedWarehouseId}`)}>
                    <span className="text-[28px] font-mono font-[700] text-[#0066cc] leading-none mb-1">{metrics.transfers.toProcess}</span>
                    <span className="text-[12px] font-[600] text-[#0066cc] uppercase tracking-wide">To Process</span>
                  </div>
                  <div className="flex flex-col space-y-2 justify-center">
                    {metrics.transfers.late > 0 && (
                      <div className="flex justify-between items-center text-[13px] text-red-600 hover:underline cursor-pointer font-[600]">
                        <span>Late</span><span>{metrics.transfers.late}</span>
                      </div>
                    )}
                    {metrics.transfers.backorders > 0 && (
                      <div className="flex justify-between items-center text-[13px] text-[#ff9800] hover:underline cursor-pointer font-[600]">
                        <span>Waiting</span><span>{metrics.transfers.backorders}</span>
                      </div>
                    )}
                  </div>
                </div>
                <div className="px-5 py-3 bg-[#fbfbfb] border-t border-[#f0f0f0]">
                  <button 
                    onClick={() => router.push(`${APP_ROUTES.INVENTORY.TRANSFERS_NEW(orgId)}?warehouseId=${selectedWarehouseId}`)} 
                    className="text-[13px] font-[600] text-[#0066cc] hover:underline"
                  >
                    New Transfer
                  </button>
                </div>
              </div>

              {/* Pending Fulfillment Orders (Sales to Inventory Link) */}
              <div className="bg-white rounded-[6px] shadow-[0px_2px_4px_rgba(0,0,0,0.04)] border border-[#e0e0e0] overflow-hidden flex flex-col group transition-shadow hover:shadow-[0px_4px_12px_rgba(0,0,0,0.08)]">
                <div className="bg-[#fffdf2] border-b border-[#fcefc7] p-4 flex justify-between items-start">
                  <div>
                    <h2 
                      className="text-[16px] font-[600] text-[#b7791f] hover:underline cursor-pointer flex items-center"
                      onClick={() => router.push(`${APP_ROUTES.INVENTORY.PENDING_ORDERS(orgId)}?warehouseId=${selectedWarehouseId}`)}
                    >
                      Pending Fulfillment
                    </h2>
                    <span className="text-[12px] text-[#b7791f] opacity-80">Sales Orders awaiting processing</span>
                  </div>
                  <button className="text-[#b7791f] opacity-60 hover:opacity-100 p-1"><Clock className="w-4 h-4" /></button>
                </div>
                <div className="p-5 flex-1 grid grid-cols-2 gap-4">
                  <div className="flex flex-col items-center justify-center p-3 bg-[#fffaf0] rounded-[6px] cursor-pointer hover:bg-[#fef3c7] transition-colors border border-[#fef3c7]" onClick={() => router.push(`${APP_ROUTES.INVENTORY.PENDING_ORDERS(orgId)}?warehouseId=${selectedWarehouseId}`)}>
                    <span className="text-[28px] font-mono font-[700] text-[#b7791f] leading-none mb-1">{metrics.pendingOrders}</span>
                    <span className="text-[12px] font-[600] text-[#b7791f] uppercase tracking-wide text-center">Orders to Claim</span>
                  </div>
                  <div className="flex flex-col justify-center text-[12px] text-[#555] px-2">
                    These sales orders have been confirmed and are waiting for a warehouse manager to claim and generate delivery orders.
                  </div>
                </div>
                <div className="px-5 py-3 bg-[#fbfbfb] border-t border-[#f0f0f0]">
                  <button 
                    onClick={() => router.push(`${APP_ROUTES.INVENTORY.PENDING_ORDERS(orgId)}?warehouseId=${selectedWarehouseId}`)} 
                    className="text-[13px] font-[600] text-[#0066cc] hover:underline"
                  >
                    View Sales Orders
                  </button>
                </div>
              </div>

            </div>
          )}

        </div>
      </div>
    </div>
  );
}
