'use client';

import React, { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { 
  getWarehouses, 
  getInventoryDocuments, 
} from '@/features/inventory/services/inventoryService';
import { 
  Warehouse, 
  InventoryDocument, 
} from '@/features/inventory/types';
import { DOCUMENT_TYPE, DOCUMENT_STATUS } from '@/config/constants';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Plus, Search, RefreshCw, Eye, Calendar, FileText } from 'lucide-react';
import { cn } from '@/lib/utils';
import { usePermissions } from '@/hooks/use-permissions';
import { PERMISSIONS } from '@/config/permissions';
import { toast } from 'sonner';

export default function AdjustmentsListPage() {
  const params = useParams();
  const orgId = params.orgId as string;
  const router = useRouter();
  const { hasPermission } = usePermissions();

  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState<string>('');
  const [documents, setDocuments] = useState<InventoryDocument[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [activeTab, setActiveTab] = useState<'ALL' | typeof DOCUMENT_STATUS[keyof typeof DOCUMENT_STATUS]>('ALL');

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

  const fetchDocuments = () => {
    if (!selectedWarehouseId) return;

    setIsLoading(true);
    getInventoryDocuments(orgId, selectedWarehouseId, {
      search: searchQuery.trim(),
      limit: 100
    })
      .then(res => {
        // Filter only ADJUSTMENT
        const adjustments = (res.data || []).filter(doc => doc.documentType === DOCUMENT_TYPE.ADJUSTMENT);
        setDocuments(adjustments);
      })
      .catch(err => {
        console.error(err);
        toast.error('Failed to load adjustments');
      })
      .finally(() => setIsLoading(false));
  };

  useEffect(() => {
    fetchDocuments();
  }, [orgId, selectedWarehouseId]);

  const handleWarehouseChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedWarehouseId(e.target.value);
  };

  const filteredDocs = documents.filter(doc => {
    if (activeTab !== 'ALL' && doc.documentStatus !== activeTab) {
      return false;
    }
    const q = searchQuery.toLowerCase();
    if (!q) return true;
    return doc.name.toLowerCase().includes(q) || 
           doc.notes?.toLowerCase().includes(q);
  });

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white">
      {/* Header Controls */}
      <div className="flex justify-between items-center mb-5 shrink-0">
        <div>
          <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Inventory Adjustments</h1>
          <span className="text-[14px] text-[#898989]">Manage warehouse stock counting and audit adjustments</span>
        </div>
        <div className="flex space-x-3 items-center">
          {/* Warehouse Selector */}
          <div className="flex items-center space-x-2">
            <span className="text-[13px] font-[600] text-[#4a4a4a] whitespace-nowrap">Warehouse:</span>
            <select
              value={selectedWarehouseId}
              onChange={handleWarehouseChange}
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
              placeholder="Search adjustments..." 
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

          {hasPermission(PERMISSIONS.INVENTORY_DOCUMENTS.CREATE) && (
            <Button 
              onClick={() => router.push(`/dashboard/${orgId}/inventory/adjustments/new?warehouseId=${selectedWarehouseId}`)}
              className="bg-[#475569] hover:bg-[#334155] text-white h-10 px-4 rounded-[4px] font-[600] text-[13px]"
            >
              <Plus className="w-4 h-4 mr-2" /> New Adjustment
            </Button>
          )}
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
                ? "border-[#475569] text-[#475569]" 
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
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Scheduled Date</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Notes</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-center">Status</th>
                <th className="py-3 px-4 w-[100px]"></th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan={5} className="py-12 text-center text-[#898989] text-[13px]">
                    <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-[#475569]" />
                    Fetching adjustments...
                  </td>
                </tr>
              ) : filteredDocs.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-12 text-center text-[#898989] text-[13px]">
                    No adjustments found.
                  </td>
                </tr>
              ) : (
                filteredDocs.map((doc) => {
                  const dateStr = new Date(doc.scheduledDate).toLocaleDateString();

                  return (
                    <tr 
                      key={doc.id} 
                      onClick={() => router.push(`/dashboard/${orgId}/inventory/documents/${doc.id}`)}
                      className="border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f8fafc] transition-colors cursor-pointer group"
                    >
                      <td className="py-3.5 px-4 font-mono text-[13px] font-[700] text-[#475569] group-hover:underline">
                        {doc.name}
                      </td>
                      <td className="py-3.5 px-4 text-[13px] text-[#64748b]">
                        <div className="flex items-center">
                          <Calendar className="w-3.5 h-3.5 mr-1 text-[#898989]" />
                          {dateStr}
                        </div>
                      </td>
                      <td className="py-3.5 px-4 text-[13px] text-[#898989] max-w-[300px] truncate">
                        {doc.notes || '-'}
                      </td>
                      <td className="py-3.5 px-4 text-center">
                        <span className={cn(
                          "inline-block px-2.5 py-0.5 rounded-[4px] min-w-[110px] text-center text-[11px] font-[600] uppercase",
                          doc.documentStatus === DOCUMENT_STATUS.DRAFT && "bg-[#e2e8f0] text-[#475569]",
                          doc.documentStatus === DOCUMENT_STATUS.CONFIRMED && "bg-[#e8f4fd] text-[#0066cc]",
                          doc.documentStatus === DOCUMENT_STATUS.COMPLETED && "bg-[#e2f0d9] text-[#385723]",
                          doc.documentStatus === DOCUMENT_STATUS.CANCELLED && "bg-[#fbe5d6] text-[#c65911]"
                        )}>
                          {doc.documentStatus.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 text-right" onClick={e => e.stopPropagation()}>
                        <Button 
                          onClick={() => router.push(`/dashboard/${orgId}/inventory/documents/${doc.id}`)}
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
