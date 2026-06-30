'use client';

import React, { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { Building, Package, ArrowRightLeft, Clock, Search, ExternalLink, Settings, LayoutGrid, X, Loader2 } from 'lucide-react';
import { getWarehouses, getWarehouseMetrics, getInventoryDocuments } from '@/features/inventory/services/inventoryService';
import { Warehouse, InventoryDocument } from '@/features/inventory/types';
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

  // Drill-down Modal State
  const [drillDownModal, setDrillDownModal] = useState<{
    isOpen: boolean;
    title: string;
    type: 'RECEIPT' | 'ISSUE' | 'TRANSFER';
    filter: 'LATE' | 'WAITING' | 'BACKORDER';
  }>({
    isOpen: false,
    title: '',
    type: 'RECEIPT',
    filter: 'LATE'
  });
  const [drillDownDocs, setDrillDownDocs] = useState<InventoryDocument[]>([]);
  const [isDrillDownLoading, setIsDrillDownLoading] = useState(false);

  const openDrillDown = async (title: string, type: 'RECEIPT' | 'ISSUE' | 'TRANSFER', filter: 'LATE' | 'WAITING' | 'BACKORDER') => {
    setDrillDownModal({ isOpen: true, title, type, filter });
    
    // Update URL query parameters
    const searchParams = new URLSearchParams(window.location.search);
    searchParams.set('modalType', type);
    searchParams.set('modalFilter', filter);
    router.replace(`${window.location.pathname}?${searchParams.toString()}`);

    setIsDrillDownLoading(true);
    try {
      const res = await getInventoryDocuments(orgId, selectedWarehouseId, { type, limit: 300 });
      const activeDocs = (res.data || []).filter(d => d.documentStatus !== 'COMPLETED' && d.documentStatus !== 'CANCELLED');
      
      let filtered: InventoryDocument[] = [];
      if (filter === 'WAITING' || filter === 'BACKORDER') {
        filtered = activeDocs.filter(d => d.documentStatus === 'WAITING_FOR_STOCK');
      } else if (filter === 'LATE') {
        const now = new Date();
        filtered = activeDocs.filter(d => {
          if (!d.scheduledDate) return false;
          return new Date(d.scheduledDate) < now;
        });
      }
      setDrillDownDocs(filtered);
    } catch (e) {
      console.error("Failed to load drill down docs", e);
    } finally {
      setIsDrillDownLoading(false);
    }
  };

  const closeDrillDown = () => {
    setDrillDownModal(prev => ({ ...prev, isOpen: false }));
    
    // Clear URL query parameters
    const searchParams = new URLSearchParams(window.location.search);
    searchParams.delete('modalType');
    searchParams.delete('modalFilter');
    const newSearch = searchParams.toString();
    router.replace(`${window.location.pathname}${newSearch ? `?${newSearch}` : ''}`);
  };

  useEffect(() => {
    if (!selectedWarehouseId) return;
    
    const searchParams = new URLSearchParams(window.location.search);
    const mType = searchParams.get('modalType');
    const mFilter = searchParams.get('modalFilter');
    
    if (mType && mFilter) {
      const type = mType as 'RECEIPT' | 'ISSUE' | 'TRANSFER';
      const filter = mFilter as 'LATE' | 'WAITING' | 'BACKORDER';
      
      let title = '';
      if (type === 'RECEIPT') {
        title = filter === 'LATE' ? 'Late Receipts' : 'Backordered Receipts';
      } else if (type === 'ISSUE') {
        title = filter === 'LATE' ? 'Late Deliveries' : 'Waiting Deliveries';
      } else if (type === 'TRANSFER') {
        title = filter === 'LATE' ? 'Late Transfers' : 'Waiting Transfers';
      }
      
      if (title) {
        openDrillDown(title, type, filter);
      }
    }
  }, [selectedWarehouseId]);


  useEffect(() => {
    // 1. Fetch Warehouses
    getWarehouses(orgId, { limit: 50 }).then((res) => {
      const whs = res.data || [];
      setWarehouses(whs);
      if (whs.length > 0) {
        const savedWhId = localStorage.getItem(`erp_last_warehouse_id_${orgId}`);
        if (savedWhId && whs.some(w => w.id === savedWhId)) {
          setSelectedWarehouseId(savedWhId);
        } else {
          setSelectedWarehouseId(whs[0].id);
          localStorage.setItem(`erp_last_warehouse_id_${orgId}`, whs[0].id);
        }
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
            onChange={(e) => {
              const val = e.target.value;
              setSelectedWarehouseId(val);
              localStorage.setItem(`erp_last_warehouse_id_${orgId}`, val);
            }}
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
                      <div 
                        className="flex justify-between items-center text-[13px] text-red-600 hover:underline cursor-pointer font-[600]"
                        onClick={() => openDrillDown('Late Receipts', 'RECEIPT', 'LATE')}
                      >
                        <span>Late</span><span>{metrics.receipts.late}</span>
                      </div>
                    )}
                    {metrics.receipts.backorders > 0 && (
                      <div 
                        className="flex justify-between items-center text-[13px] text-[#ff9800] hover:underline cursor-pointer font-[600]"
                        onClick={() => openDrillDown('Backordered Receipts', 'RECEIPT', 'BACKORDER')}
                      >
                        <span>Backorders</span><span>{metrics.receipts.backorders}</span>
                      </div>
                    )}
                  </div>
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
                      <div 
                        className="flex justify-between items-center text-[13px] text-red-600 hover:underline cursor-pointer font-[600]"
                        onClick={() => openDrillDown('Late Deliveries', 'ISSUE', 'LATE')}
                      >
                        <span>Late</span><span>{metrics.deliveries.late}</span>
                      </div>
                    )}
                    {metrics.deliveries.backorders > 0 && (
                      <div 
                        className="flex justify-between items-center text-[13px] text-[#ff9800] hover:underline cursor-pointer font-[600]"
                        onClick={() => openDrillDown('Waiting Deliveries', 'ISSUE', 'WAITING')}
                      >
                        <span>Waiting</span><span>{metrics.deliveries.backorders}</span>
                      </div>
                    )}
                  </div>
                </div>
                <div className="px-5 py-3 bg-[#fbfbfb] border-t border-[#f0f0f0]">
                  <span className="text-[13px] font-[500] text-[#898989] italic">
                    Auto-generated from Sales Orders
                  </span>
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
                      <div 
                        className="flex justify-between items-center text-[13px] text-red-600 hover:underline cursor-pointer font-[600]"
                        onClick={() => openDrillDown('Late Transfers', 'TRANSFER', 'LATE')}
                      >
                        <span>Late</span><span>{metrics.transfers.late}</span>
                      </div>
                    )}
                    {metrics.transfers.backorders > 0 && (
                      <div 
                        className="flex justify-between items-center text-[13px] text-[#ff9800] hover:underline cursor-pointer font-[600]"
                        onClick={() => openDrillDown('Waiting Transfers', 'TRANSFER', 'WAITING')}
                      >
                        <span>Waiting</span><span>{metrics.transfers.backorders}</span>
                      </div>
                    )}
                  </div>
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

      {/* Drill Down Modal */}
      {drillDownModal.isOpen && (
        <div className="fixed inset-0 bg-black/55 z-50 flex items-center justify-center animate-in fade-in duration-200">
          <div className="bg-white rounded-[8px] shadow-[0px_12px_28px_rgba(0,0,0,0.30)] w-full max-w-[800px] flex flex-col max-h-[90vh]">
            <div className="px-6 py-4 border-b border-[#e0e0e0] flex justify-between items-center bg-[#f8f8f8] shrink-0">
              <h2 className="text-[18px] font-[700] text-[#242424] flex items-center">
                {drillDownModal.filter === 'LATE' ? (
                  <Clock className="w-5 h-5 mr-2 text-red-600" />
                ) : (
                  <Package className="w-5 h-5 mr-2 text-[#ff9800]" />
                )}
                {drillDownModal.title}
              </h2>
              <button 
                onClick={closeDrillDown} 
                className="h-8 w-8 text-[#898989] hover:text-[#242424] flex items-center justify-center rounded-full hover:bg-[#f0f0f0]"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
            
            <div className="p-0 overflow-y-auto flex-1">
              {isDrillDownLoading ? (
                <div className="py-12 flex justify-center items-center text-[#898989]">
                  <Loader2 className="w-6 h-6 animate-spin mr-2" />
                  Loading details...
                </div>
              ) : (
                <table className="w-full text-left text-[13px]">
                  <thead className="bg-[#f5f5f5] text-[#898989] font-[600] uppercase text-[11px] border-b border-[#e0e0e0] sticky top-0">
                    <tr>
                      <th className="px-6 py-3">Document No.</th>
                      <th className="px-6 py-3">Status</th>
                      <th className="px-6 py-3">Scheduled Date</th>
                      <th className="px-6 py-3">Contact</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-[#e0e0e0]">
                    {drillDownDocs.length === 0 ? (
                      <tr>
                        <td colSpan={4} className="px-6 py-8 text-center text-[#898989]">
                          No documents found.
                        </td>
                      </tr>
                    ) : (
                      drillDownDocs.map((doc) => (
                        <tr key={doc.id} className="hover:bg-[#fafafa] transition-colors">
                          <td className="px-6 py-3 font-[600] text-[#0066cc]">
                            <a href={`${APP_ROUTES.INVENTORY.DOCUMENT_DETAIL(orgId, doc.id)}?warehouseId=${selectedWarehouseId}`} className="hover:underline">
                              {doc.name}
                            </a>
                          </td>
                          <td className="px-6 py-3">
                            <span className={`inline-flex px-2 py-0.5 rounded-[4px] text-[11px] font-[600] ${
                              doc.documentStatus === 'WAITING_FOR_STOCK' ? 'bg-[#fff4e5] text-[#ed6c02]' :
                              doc.documentStatus === 'DRAFT' ? 'bg-[#f0f0f0] text-[#555]' :
                              'bg-[#e3f2fd] text-[#1976d2]'
                            }`}>
                              {doc.documentStatus.replace(/_/g, ' ')}
                            </span>
                          </td>
                          <td className="px-6 py-3 text-[#4a4a4a]">
                            {doc.scheduledDate ? new Date(doc.scheduledDate).toLocaleDateString() : '-'}
                          </td>
                          <td className="px-6 py-3 text-[#242424]">
                            {doc.partnerName || '-'}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              )}
            </div>
            
            <div className="px-6 py-4 bg-[#f8f8f8] border-t border-[#e0e0e0] flex justify-end shrink-0">
              <button 
                onClick={closeDrillDown}
                className="bg-white border border-[#d0d0d0] text-[#242424] hover:bg-[#f0f0f0] px-4 py-2 rounded-[4px] text-[13px] font-[600]"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
