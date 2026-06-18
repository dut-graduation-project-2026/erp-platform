'use client';

import React, { useEffect, useState } from 'react';
import { useRouter, useParams, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { 
  getWarehouses, 
  getInventoryDocuments, 
  createInventoryDocument 
} from '@/features/inventory/services/inventoryService';
import { getProducts } from '@/features/sales/services/salesService';
import { 
  Warehouse, 
  InventoryDocument, 
  DocumentType, 
  CreateInventoryDocumentRequest,
  InventoryDocumentItemRequest
} from '@/features/inventory/types';
import { Product } from '@/features/sales/types';
import { DOCUMENT_TYPE, DOCUMENT_STATUS, REFERENCE_TYPE, APP_ROUTES } from '@/config/constants';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Plus, Search, Filter, RefreshCw, X, Save, Eye, Calendar, FileText } from 'lucide-react';
import { cn } from '@/lib/utils';
import { usePermissions } from '@/hooks/use-permissions';
import { PERMISSIONS } from '@/config/permissions';
import { toast } from 'sonner';

export default function DocumentsListPage() {
  const params = useParams();
  const orgId = params.orgId as string;
  const router = useRouter();
  const { hasPermission } = usePermissions();

  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [selectedWarehouseId, setSelectedWarehouseId] = useState<string>('');
  const [documents, setDocuments] = useState<InventoryDocument[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  
  const searchParams = useSearchParams();
  const initialType = searchParams.get('type') || 'ALL';
  const [activeType, setActiveType] = useState<string>(initialType);
  
  // Status Filters
  const [activeTab, setActiveTab] = useState<'ALL' | typeof DOCUMENT_STATUS[keyof typeof DOCUMENT_STATUS]>('ALL');

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [productsList, setProductsList] = useState<Product[]>([]);
  const [docType, setDocType] = useState<DocumentType>(DOCUMENT_TYPE.RECEIPT);
  const [srcWhId, setSrcWhId] = useState<string>('');
  const [scheduledDate, setScheduledDate] = useState<string>(
    new Date().toISOString().substring(0, 16) // Format for datetime-local input
  );
  const [notes, setNotes] = useState('');
  const [items, setItems] = useState<InventoryDocumentItemRequest[]>([]);
  const [isSaving, setIsSaving] = useState(false);

  // Load warehouses first
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

  // Load products when modal is opened
  useEffect(() => {
    if (isModalOpen) {
      getProducts(orgId, { limit: 100 })
        .then(res => {
          setProductsList(res.data || []);
        })
        .catch(err => {
          console.error(err);
          toast.error('Failed to load products list');
        });
    }
  }, [isModalOpen, orgId]);

  const fetchDocuments = () => {
    if (!selectedWarehouseId) return;

    setIsLoading(true);
    getInventoryDocuments(orgId, selectedWarehouseId, {
      search: searchQuery.trim(),
      limit: 50
    })
      .then(res => {
        setDocuments(res.data || []);
      })
      .catch(err => {
        console.error(err);
        toast.error('Failed to load stock movements');
      })
      .finally(() => setIsLoading(false));
  };

  useEffect(() => {
    fetchDocuments();
  }, [orgId, selectedWarehouseId]);

  const handleWarehouseChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedWarehouseId(e.target.value);
  };

  const handleOpenCreateModal = () => {
    setDocType(DOCUMENT_TYPE.RECEIPT);
    setSrcWhId('');
    setScheduledDate(new Date().toISOString().substring(0, 16));
    setNotes('');
    setItems([{ productId: '', quantity: 1 }]);
    setIsModalOpen(true);
  };

  const handleAddItemRow = () => {
    setItems([...items, { productId: '', quantity: 1 }]);
  };

  const handleRemoveItemRow = (idx: number) => {
    setItems(items.filter((_, i) => i !== idx));
  };

  const handleItemChange = (idx: number, field: keyof InventoryDocumentItemRequest, value: string | number) => {
    const updated = [...items];
    updated[idx] = {
      ...updated[idx],
      [field]: value
    };
    setItems(updated);
  };

  const handleSaveDocument = async () => {
    // Validate
    if (items.length === 0) return toast.error('At least one item is required.');
    
    for (const item of items) {
      if (!item.productId) return toast.error('All items must have a product selected.');
      if (item.quantity <= 0) return toast.error('Quantities must be greater than 0.');
    }

    const payload: CreateInventoryDocumentRequest = {
      documentType: docType,
      transferSourceWarehouseId: srcWhId || undefined,
      scheduledDate: new Date(scheduledDate).toISOString(),
      notes,
      items
    };

    setIsSaving(true);
    try {
      const created = await createInventoryDocument(orgId, selectedWarehouseId, payload);
      toast.success('Draft document created successfully');
      setIsModalOpen(false);
      fetchDocuments();
      router.push(APP_ROUTES.INVENTORY.DOCUMENT_DETAIL(orgId, created.id));
    } catch (e) {
      console.error(e);
      toast.error('Failed to create stock move document');
    } finally {
      setIsSaving(false);
    }
  };

  const filteredDocs = documents.filter(doc => {
    // Tab filtering
    if (activeTab !== 'ALL' && doc.documentStatus !== activeTab) {
      return false;
    }

    // Type filtering
    if (activeType !== 'ALL') {
      if (activeType === 'TRANSFER' || activeType === 'TRANSFER_OUT') {
        if (doc.documentType !== DOCUMENT_TYPE.TRANSFER_IN && doc.documentType !== DOCUMENT_TYPE.TRANSFER_OUT) {
          return false;
        }
      } else if (doc.documentType !== activeType) {
        return false;
      }
    }
    
    // Search filtering
    const q = searchQuery.toLowerCase();
    if (!q) return true;

    return doc.name.toLowerCase().includes(q) || 
           doc.notes?.toLowerCase().includes(q) || 
           doc.documentType.toLowerCase().includes(q) ||
           doc.orderNumber?.toLowerCase().includes(q);
  });

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white">
      {/* Header Controls */}
      <div className="flex justify-between items-center mb-5 shrink-0">
        <div>
          <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Stock Move Documents</h1>
          <span className="text-[14px] text-[#898989]">Manage receipts, shipments, transfers, and warehouse counts</span>
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

          {/* Operation Type Filter */}
          <div className="flex items-center space-x-2">
            <Filter className="w-4 h-4 text-[#898989]" />
            <select
              value={activeType}
              onChange={(e) => setActiveType(e.target.value)}
              className="h-10 border border-[#d0d0d0] rounded-[4px] px-3 text-[13px] bg-white focus:outline-none focus:border-[#0066cc] w-[140px]"
            >
              <option value="ALL">All Operations</option>
              <option value={DOCUMENT_TYPE.RECEIPT}>Receipts (IN)</option>
              <option value={DOCUMENT_TYPE.ISSUE}>Deliveries (OUT)</option>
              <option value="TRANSFER">Transfers (Internal)</option>
              <option value={DOCUMENT_TYPE.ADJUSTMENT}>Adjustments</option>
            </select>
          </div>

          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
            <Input 
              placeholder="Search documents..." 
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
              onClick={handleOpenCreateModal}
              className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px] font-[600] text-[13px]"
            >
              <Plus className="w-4 h-4 mr-2" /> New Stock Move
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
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Operation Type</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Source Document</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Order No.</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Scheduled Date</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider">Notes</th>
                <th className="py-3 px-4 text-[12px] font-bold text-[#242424] uppercase tracking-wider text-center">Status</th>
                <th className="py-3 px-4 w-[100px]"></th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-[#898989] text-[13px]">
                    <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-[#0066cc]" />
                    Fetching documents...
                  </td>
                </tr>
              ) : filteredDocs.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-[#898989] text-[13px]">
                    No stock movements found.
                  </td>
                </tr>
              ) : (
                filteredDocs.map((doc) => {
                  const dateStr = new Date(doc.scheduledDate).toLocaleDateString();

                  return (
                    <tr 
                      key={doc.id} 
                      onClick={() => router.push(APP_ROUTES.INVENTORY.DOCUMENT_DETAIL(orgId, doc.id))}
                      className="border-b border-[#e0e0e0] last:border-b-0 hover:bg-[#f0f4ff] transition-colors cursor-pointer group"
                    >
                      <td className="py-3.5 px-4 font-mono text-[13px] font-[700] text-[#0066cc] group-hover:underline">
                        {doc.name}
                      </td>
                      <td className="py-3.5 px-4 text-[13px]">
                        <span className={cn(
                          "inline-flex items-center px-2 py-0.5 rounded-[4px] text-[11px] font-[600] uppercase",
                          doc.documentType === DOCUMENT_TYPE.RECEIPT && "bg-[#e2f0d9] text-[#385723]",
                          doc.documentType === DOCUMENT_TYPE.ISSUE && "bg-[#fbe5d6] text-[#c65911]",
                          doc.documentType === DOCUMENT_TYPE.TRANSFER_IN && "bg-[#e8f4fd] text-[#1b75bb]",
                          doc.documentType === DOCUMENT_TYPE.TRANSFER_OUT && "bg-[#e8f4fd] text-[#1b75bb]",
                          doc.documentType === DOCUMENT_TYPE.ADJUSTMENT && "bg-[#e2e8f0] text-[#475569]"
                        )}>
                          {doc.documentType.replace('_', ' ')}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 text-[13px] text-[#4a4a4a] font-medium">
                        {doc.referenceType !== REFERENCE_TYPE.MANUAL ? (
                          <span className="bg-[#f5f5f5] px-2 py-1 border border-[#e0e0e0] rounded font-mono text-[11px]">
                            {doc.referenceType}: {doc.referenceId?.substring(0, 8)}
                          </span>
                        ) : (
                          <span className="text-[#898989] italic">Manual adjustment</span>
                        )}
                      </td>
                      <td className="py-3.5 px-4 text-[13px] text-[#4a4a4a] font-medium" onClick={(e) => e.stopPropagation()}>
                        {doc.orderNumber && doc.referenceId ? (
                          <Link 
                            href={APP_ROUTES.SALES.ORDER_DETAIL(orgId, doc.referenceId)}
                            className="inline-flex items-center justify-center min-w-[110px] text-center px-2 py-0.5 rounded-[4px] text-[11px] font-[600] uppercase bg-[#e8f4fd] text-[#1b75bb] border border-[#d0e8fc] hover:bg-[#d0e8fc] hover:text-[#004499] transition-colors"
                          >
                            {doc.orderNumber}
                          </Link>
                        ) : doc.orderNumber ? (
                          <span className="inline-flex items-center justify-center min-w-[110px] text-center px-2 py-0.5 rounded-[4px] text-[11px] font-[600] uppercase bg-gray-100 text-gray-750 border border-gray-200">
                            {doc.orderNumber}
                          </span>
                        ) : (
                          <span className="text-[#898989] italic">-</span>
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
                          onClick={() => router.push(APP_ROUTES.INVENTORY.DOCUMENT_DETAIL(orgId, doc.id))}
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

      {/* Manual Stock Move Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black/55 z-50 flex items-center justify-center animate-in fade-in duration-200">
          <div className="bg-white rounded-[8px] shadow-[0px_12px_28px_rgba(0,0,0,0.30)] w-full max-w-[720px] flex flex-col max-h-[90vh]">
            <div className="px-6 py-4 border-b border-[#e0e0e0] flex justify-between items-center bg-[#f8f8f8] shrink-0">
              <h2 className="text-[18px] font-[700] text-[#242424]">Create Stock Move Document</h2>
              <Button variant="ghost" size="icon" onClick={() => setIsModalOpen(false)} className="h-8 w-8 text-[#898989] hover:text-[#242424]">
                <X className="w-5 h-5" />
              </Button>
            </div>

            <div className="p-6 overflow-y-auto space-y-5">
              <div className="grid grid-cols-2 gap-5">
                <div>
                  <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Operation Type</label>
                  <select
                    value={docType}
                    onChange={e => setDocType(e.target.value as DocumentType)}
                    className="w-full h-10 border border-[#d0d0d0] rounded-[4px] px-3 text-[13px] bg-white focus:outline-none focus:border-[#0066cc]"
                  >
                    <option value={DOCUMENT_TYPE.RECEIPT}>INBOUND: Stock Receipt</option>
                    <option value={DOCUMENT_TYPE.ISSUE}>OUTBOUND: Stock Issue</option>
                    <option value={DOCUMENT_TYPE.TRANSFER_OUT}>INTERNAL: Stock Transfer</option>
                    <option value={DOCUMENT_TYPE.ADJUSTMENT}>AUDIT: Inventory Adjustment</option>
                  </select>
                </div>
                <div>
                  <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Scheduled Date</label>
                  <input
                    type="datetime-local"
                    value={scheduledDate}
                    onChange={e => setScheduledDate(e.target.value)}
                    className="w-full h-10 border border-[#d0d0d0] rounded-[4px] px-3 text-[13px] focus:outline-none focus:border-[#0066cc]"
                  />
                </div>
              </div>

              {/* Source warehouse selector if Internal Transfer */}
              {docType === DOCUMENT_TYPE.TRANSFER_OUT && (
                <div>
                  <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Destination Warehouse Location</label>
                  <select
                    value={srcWhId}
                    onChange={e => setSrcWhId(e.target.value)}
                    className="w-full h-10 border border-[#d0d0d0] rounded-[4px] px-3 text-[13px] bg-white focus:outline-none focus:border-[#0066cc]"
                  >
                    <option value="">-- Select Destination --</option>
                    {warehouses
                      .filter(wh => wh.id !== selectedWarehouseId)
                      .map(wh => (
                        <option key={wh.id} value={wh.id}>
                          [{wh.code}] {wh.name}
                        </option>
                      ))}
                  </select>
                </div>
              )}

              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Notes</label>
                <Textarea 
                  value={notes}
                  onChange={e => setNotes(e.target.value)}
                  placeholder="Describe the reason for this stock movement..."
                  rows={2}
                  className="border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                />
              </div>

              {/* Document Items Table */}
              <div className="border-t border-[#e0e0e0] pt-4">
                <div className="flex justify-between items-center mb-3">
                  <h3 className="text-[14px] font-[700] text-[#242424]">Products to Move</h3>
                  <Button 
                    type="button" 
                    variant="outline" 
                    onClick={handleAddItemRow}
                    className="h-8 text-[12px] border-[#d0d0d0]"
                  >
                    <Plus className="w-3.5 h-3.5 mr-1" /> Add Product
                  </Button>
                </div>

                <div className="space-y-3">
                  {items.map((item, idx) => (
                    <div key={idx} className="flex items-center space-x-3 bg-[#f8f8f8] p-3 rounded border border-[#e0e0e0]">
                      <div className="flex-1">
                        <select
                          value={item.productId}
                          onChange={e => handleItemChange(idx, 'productId', e.target.value)}
                          className="w-full h-9 border border-[#d0d0d0] rounded-[4px] px-2.5 text-[12px] bg-white focus:outline-none focus:border-[#0066cc]"
                        >
                          <option value="">-- Select SKU Product --</option>
                          {productsList.map(p => (
                            <option key={p.id} value={p.id}>
                              [{p.sku || p.code}] {p.name}
                            </option>
                          ))}
                        </select>
                      </div>
                      
                      <div className="w-[140px]">
                        <Input 
                          type="number" 
                          min={1}
                          value={item.quantity}
                          onChange={e => handleItemChange(idx, 'quantity', parseFloat(e.target.value) || 0)}
                          placeholder="Quantity"
                          className="h-9 text-[13px] border-[#d0d0d0] rounded-[4px] focus-visible:ring-0"
                        />
                      </div>

                      <Button 
                        type="button" 
                        variant="ghost" 
                        size="icon"
                        onClick={() => handleRemoveItemRow(idx)}
                        disabled={items.length === 1}
                        className="h-9 w-9 text-[#dc3545] hover:bg-[#fff0f0] rounded-[4px]"
                      >
                        <X className="w-4 h-4" />
                      </Button>
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="px-6 py-4 bg-[#f8f8f8] border-t border-[#e0e0e0] flex justify-end space-x-2 shrink-0">
              <Button 
                variant="outline" 
                onClick={() => setIsModalOpen(false)}
                className="bg-white border-[#d0d0d0] text-[#242424] h-10 px-4 rounded-[4px] font-[600]"
              >
                Discard
              </Button>
              <Button 
                onClick={handleSaveDocument}
                disabled={isSaving}
                className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px] font-[600]"
              >
                <Save className="w-4 h-4 mr-2" />
                {isSaving ? 'Creating...' : 'Create Draft'}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
