'use client';

import React, { useEffect, useState, use } from 'react';
import { useRouter } from 'next/navigation';
import { getWarehouses, getReplenishmentRequests } from '@/features/inventory/services/inventoryService';
import { Warehouse, ReplenishmentRequest } from '@/features/inventory/types';
import { Button } from '@/components/ui/button';
import { RefreshCw, Boxes, Calendar, FileText, User } from 'lucide-react';
import { cn } from '@/lib/utils';
import { toast } from 'sonner';

export default function ReplenishmentsListPage({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = use(params);
  const router = useRouter();

  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState<string>('');
  const [requests, setRequests] = useState<ReplenishmentRequest[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  
  // Pagination
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const limit = 15;

  // Fetch warehouses
  useEffect(() => {
    getWarehouses(orgId)
      .then(res => {
        setWarehouses(res.data || []);
        if (res.data && res.data.length > 0) {
          setSelectedWarehouseId(res.data[0].id);
        }
      })
      .catch(err => {
        console.error(err);
        toast.error('Failed to load warehouses');
      });
  }, [orgId]);

  const fetchReplenishments = () => {
    if (!selectedWarehouseId) return;

    setIsLoading(true);
    getReplenishmentRequests(orgId, selectedWarehouseId, { page, limit })
      .then(res => {
        setRequests(res.data || []);
        setTotalPages(res.totalPages || Math.ceil((res.total || 1) / limit) || 1);
      })
      .catch(err => {
        console.error(err);
        toast.error('Failed to fetch replenishment tickets');
      })
      .finally(() => setIsLoading(false));
  };

  useEffect(() => {
    fetchReplenishments();
  }, [orgId, selectedWarehouseId, page]);

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white">
      {/* Header controls */}
      <div className="flex justify-between items-center mb-6 shrink-0">
        <div>
          <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Stock Replenishment Requests</h1>
          <span className="text-[14px] text-[#898989]">View active requests submitted when stock levels were insufficient</span>
        </div>
        <div className="flex space-x-3 items-center">
          <div className="flex items-center space-x-2">
            <span className="text-[13px] font-[600] text-[#4a4a4a] whitespace-nowrap">Warehouse:</span>
            <select
              value={selectedWarehouseId}
              onChange={e => {
                setSelectedWarehouseId(e.target.value);
                setPage(1);
              }}
              className="h-10 border border-[#d0d0d0] rounded-[4px] px-3 text-[13px] bg-white focus:outline-none focus:border-[#0066cc]"
            >
              {warehouses.map(wh => (
                <option key={wh.id} value={wh.id}>
                  [{wh.code}] {wh.name}
                </option>
              ))}
            </select>
          </div>

          <Button 
            onClick={fetchReplenishments}
            variant="outline" 
            className="border-[#d0d0d0] text-[#242424] h-10 px-3 bg-white rounded-[4px]"
          >
            <RefreshCw className="w-4 h-4" />
          </Button>
        </div>
      </div>

      {/* Main Table */}
      <div className="flex-1 overflow-auto bg-[#f8f8f8] p-4 -mx-6 -mb-6 border-t border-[#e0e0e0] flex flex-col justify-between">
        <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-white border-b border-[#e0e0e0]">
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Ticket ID</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Warehouse</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Linked Stock Move</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Requested By</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Reason / Notes</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Request Date</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-[#898989] text-[13px]">
                    <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-[#0066cc]" />
                    Loading requests...
                  </td>
                </tr>
              ) : requests.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-[#898989] text-[13px]">
                    <div className="bg-[#f8f8f8] w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-3">
                      <Boxes className="w-6 h-6 text-[#d0d0d0]" />
                    </div>
                    No active replenishment requests.
                  </td>
                </tr>
              ) : (
                requests.map((req) => {
                  const dateStr = new Date(req.createdAt).toLocaleString();

                  return (
                    <tr 
                      key={req.id} 
                      onClick={() => router.push(`/dashboard/${orgId}/inventory/documents/${req.inventoryDocumentId}?whId=${req.warehouseId}`)}
                      className="border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f0f4ff] transition-colors cursor-pointer group"
                    >
                      <td className="py-3.5 px-4 font-mono text-[12px] text-[#898989]">
                        #{req.id.substring(0, 8)}
                      </td>
                      <td className="py-3.5 px-4 text-[13px] font-[500] text-[#242424]">
                        {req.warehouseName}
                      </td>
                      <td className="py-3.5 px-4 text-[13px] font-mono font-[700] text-[#0066cc] group-hover:underline">
                        <div className="flex items-center">
                          <FileText className="w-4 h-4 mr-1.5 text-[#898989]" />
                          {req.inventoryDocumentName}
                        </div>
                      </td>
                      <td className="py-3.5 px-4 text-[13px] text-[#4a4a4a]">
                        <div className="flex items-center">
                          <User className="w-3.5 h-3.5 mr-1 text-[#898989]" />
                          {req.requestedBy ? `${req.requestedBy.firstName} ${req.requestedBy.lastName}` : 'System'}
                        </div>
                      </td>
                      <td className="py-3.5 px-4 text-[13px] text-[#898989] max-w-[250px] truncate">
                        {req.notes || 'No description provided.'}
                      </td>
                      <td className="py-3.5 px-4 text-[12px] text-[#64748b]">
                        <div className="flex items-center">
                          <Calendar className="w-3.5 h-3.5 mr-1 text-[#898989]" />
                          {dateStr}
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
