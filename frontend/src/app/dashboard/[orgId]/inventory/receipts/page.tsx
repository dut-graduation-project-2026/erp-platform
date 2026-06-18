'use client';

import React, { useEffect, useState } from 'react';
import { useRouter, useSearchParams, useParams } from 'next/navigation';
import { getWarehouses, getInventoryDocuments } from '@/features/inventory/services/inventoryService';
import { Warehouse, InventoryDocument } from '@/features/inventory/types';
import { DOCUMENT_TYPE, DOCUMENT_STATUS, APP_ROUTES } from '@/config/constants';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Search, RefreshCw, Eye, Calendar, Package } from 'lucide-react';
import { cn } from '@/lib/utils';
import { toast } from 'sonner';

export default function ReceiptsPage() {
  const router = useRouter();
  const params = useParams();
  const orgId = params.orgId as string;
  const searchParams = useSearchParams();
  const initialWarehouseId = searchParams.get('warehouseId') || '';

  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState<string>(initialWarehouseId);
  const [documents, setDocuments] = useState<InventoryDocument[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  
  const [activeTab, setActiveTab] = useState<'ALL' | typeof DOCUMENT_STATUS[keyof typeof DOCUMENT_STATUS]>('ALL');

  useEffect(() => {
    getWarehouses(orgId)
      .then(res => {
        const whs = res.data || [];
        setWarehouses(whs);
        if (!selectedWarehouseId && whs.length > 0) {
          setSelectedWarehouseId(whs[0].id);
        }
      })
      .catch(err => {
        console.error(err);
        toast.error('Failed to load warehouses');
      });
  }, [orgId]);

  const fetchDocuments = () => {
    if (!selectedWarehouseId) return;

    setIsLoading(true);
    getInventoryDocuments(orgId, selectedWarehouseId, {
      search: searchQuery.trim(),
      limit: 100
    })
      .then(res => {
        // Filter only RECEIPTS
        const receipts = (res.data || []).filter(d => d.documentType === DOCUMENT_TYPE.RECEIPT);
        setDocuments(receipts);
      })
      .catch(err => {
        console.error(err);
        toast.error('Failed to load receipts');
      })
      .finally(() => setIsLoading(false));
  };

  useEffect(() => {
    fetchDocuments();
  }, [orgId, selectedWarehouseId]);

  const filteredDocs = documents.filter(doc => {
    if (activeTab !== 'ALL' && doc.documentStatus !== activeTab) {
      return false;
    }
    
    const q = searchQuery.toLowerCase();
    if (!q) return true;

    return doc.name.toLowerCase().includes(q) || 
           doc.notes?.toLowerCase().includes(q) ||
           doc.referenceId?.toLowerCase().includes(q);
  });

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white">
      {/* Header Controls */}
      <div className="flex justify-between items-center mb-5 shrink-0">
        <div className="flex items-center">
          <Package className="w-6 h-6 mr-3 text-[#0066cc]" />
          <div>
            <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Receipts</h1>
            <span className="text-[14px] text-[#898989]">Manage inbound stock receipts</span>
          </div>
        </div>
        <div className="flex space-x-3 items-center">
          <div className="flex items-center space-x-2">
            <span className="text-[13px] font-[600] text-[#4a4a4a] whitespace-nowrap">Warehouse:</span>
            <select
              value={selectedWarehouseId}
              onChange={(e) => setSelectedWarehouseId(e.target.value)}
              className="h-10 border border-[#d0d0d0] rounded-[4px] px-3 text-[13px] bg-white focus:outline-none focus:border-[#0066cc]"
            >
              {warehouses.map(wh => (
                <option key={wh.id} value={wh.id}>
                  [{wh.code}] {wh.name}
                </option>
              ))}
            </select>
          </div>

          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
            <Input 
              placeholder="Search receipts..." 
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              className="pl-9 h-10 w-[240px] border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]" 
            />
          </div>

          <Button 
            onClick={fetchDocuments}
            variant="outline" 
            className="border-[#d0d0d0] text-[#242424] h-10 px-3 bg-white rounded-[4px]"
          >
            <RefreshCw className="w-4 h-4" />
          </Button>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex space-x-1 border-b border-[#e0e0e0] mb-4 shrink-0">
        {(['ALL', DOCUMENT_STATUS.DRAFT, DOCUMENT_STATUS.CONFIRMED, DOCUMENT_STATUS.COMPLETED, DOCUMENT_STATUS.CANCELLED] as const).map(tab => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            className={cn(
              "px-4 py-2 text-[13px] font-[600] border-b-2 transition-all",
              activeTab === tab 
                ? "border-[#0066cc] text-[#0066cc]" 
                : "border-transparent text-[#64748b] hover:text-[#242424]"
            )}
          >
            {tab}
          </button>
        ))}
      </div>

      {/* Data Table */}
      <div className="flex-1 overflow-auto bg-[#f8f8f8] p-4 -mx-6 -mb-6 border-t border-[#e0e0e0]">
        <div className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-white border-b border-[#e0e0e0]">
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Reference</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Source Document</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Scheduled Date</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Notes</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-center">Status</th>
                <th className="py-3 px-4 w-[100px]"></th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-[#898989] text-[13px]">
                    <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-[#0066cc]" />
                    Fetching receipts...
                  </td>
                </tr>
              ) : filteredDocs.length === 0 ? (
                <tr>
                  <td colSpan={6} className="py-12 text-center text-[#898989] text-[13px]">
                    No receipts found.
                  </td>
                </tr>
              ) : (
                filteredDocs.map((doc) => {
                  const dateStr = new Date(doc.scheduledDate).toLocaleDateString();
                  return (
                    <tr 
                      key={doc.id} 
                      onClick={() => router.push(`${APP_ROUTES.INVENTORY.DOCUMENT_DETAIL(orgId, doc.id)}?warehouseId=${selectedWarehouseId}`)}
                      className="border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f0f4ff] transition-colors cursor-pointer group"
                    >
                      <td className="py-3.5 px-4 font-mono text-[13px] font-[700] text-[#0066cc] group-hover:underline">
                        {doc.name}
                      </td>
                      <td className="py-3.5 px-4 text-[13px] text-[#4a4a4a] font-medium">
                        {doc.referenceType !== 'MANUAL' ? (
                          <span className="bg-[#f5f5f5] px-2 py-1 border border-[#e0e0e0] rounded font-mono text-[11px]">
                            {doc.referenceType}: {doc.referenceId?.substring(0, 8)}
                          </span>
                        ) : (
                          <span className="text-[#898989] italic">Manual receipt</span>
                        )}
                      </td>
                      <td className="py-3.5 px-4 text-[13px] text-[#64748b]">
                        <div className="flex items-center">
                          <Calendar className="w-3.5 h-3.5 mr-1 text-[#898989]" />
                          {dateStr}
                        </div>
                      </td>
                      <td className="py-3.5 px-4 text-[13px] text-[#898989] max-w-[200px] truncate">
                        {doc.notes || '-'}
                      </td>
                      <td className="py-3.5 px-4 text-center">
                        <span className={cn(
                          "inline-block px-2.5 py-0.5 rounded-[4px] min-w-[110px] text-center text-[11px] font-[600] uppercase",
                          doc.documentStatus === DOCUMENT_STATUS.DRAFT && "bg-[#e2e8f0] text-[#475569]",
                          doc.documentStatus === DOCUMENT_STATUS.CONFIRMED && "bg-[#e8f4fd] text-[#0066cc]",
                          doc.documentStatus === DOCUMENT_STATUS.COMPLETED && "bg-[#e2f0d9] text-[#385723]",
                          doc.documentStatus === DOCUMENT_STATUS.CANCELLED && "bg-[#fbe5d6] text-[#c65911]",
                          doc.documentStatus === DOCUMENT_STATUS.WAITING_FOR_STOCK && "bg-[#fff2cc] text-[#d68100]"
                        )}>
                          {doc.documentStatus.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 text-right" onClick={e => e.stopPropagation()}>
                        <Button 
                          onClick={() => router.push(`${APP_ROUTES.INVENTORY.DOCUMENT_DETAIL(orgId, doc.id)}?warehouseId=${selectedWarehouseId}`)}
                          variant="ghost" 
                          className="h-8 px-2 text-[#64748b] hover:bg-[#f5f5f5] hover:text-[#242424]"
                        >
                          <Eye className="w-4 h-4 mr-1.5" /> View
                        </Button>
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
