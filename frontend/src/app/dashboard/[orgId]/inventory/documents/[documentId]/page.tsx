'use client';

import React, { useEffect, useState, use, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { 
  getInventoryDocumentById,
  confirmInventoryDocument,
  completeInventoryDocument,
  cancelInventoryDocument,
  createReplenishmentRequest,
  getWarehouses
} from '@/features/inventory/services/inventoryService';
import { InventoryDocument } from '@/features/inventory/types';
import { Button } from '@/components/ui/button';
import { 
  ArrowLeft, 
  CheckCircle, 
  XCircle, 
  Send, 
  Calendar, 
  User, 
  AlertTriangle,
  ChevronRight,
  Plus,
  X
} from 'lucide-react';
import { cn } from '@/lib/utils';
import { usePermissions } from '@/hooks/use-permissions';
import { PERMISSIONS } from '@/config/permissions';
import { toast } from 'sonner';
import { Textarea } from '@/components/ui/textarea';

// Helper to iterate warehouses and find the document
const getInventoryDocumentByIdWithFallback = async (orgId: string, docId: string): Promise<InventoryDocument> => {
  // 1. Fetch warehouses using authenticated API client
  let activeWarehouses: any[] = [];
  try {
    const res = await getWarehouses(orgId);
    activeWarehouses = res.data || [];
  } catch (e) {
    console.error('Failed to load warehouses list', e);
  }
  
  // 2. Iterate warehouses and try to get the document
  for (const wh of activeWarehouses) {
    try {
      const found = await getInventoryDocumentById(orgId, wh.id, docId);
      if (found) return found;
    } catch {
      // ignore and continue
    }
  }
  throw new Error('Document not found in any warehouse');
};

export default function DocumentDetailsPage({ 
  params 
}: { 
  params: Promise<{ orgId: string; documentId: string }> 
}) {
  const { orgId, documentId } = use(params);
  const router = useRouter();
  const { hasPermission } = usePermissions();

  const [doc, setDoc] = useState<InventoryDocument | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isActionLoading, setIsActionLoading] = useState(false);
  
  // Replenishment Dialog state
  const [isReplenishOpen, setIsReplenishOpen] = useState(false);
  const [replenishNotes, setReplenishNotes] = useState('');

  const fallbackSearch = useCallback(async () => {
    try {
      const warehousesRes = await getInventoryDocumentByIdWithFallback(orgId, documentId);
      setDoc(warehousesRes);
    } catch (e) {
      console.error(e);
      toast.error('Failed to find document in any warehouse');
    } finally {
      setIsLoading(false);
    }
  }, [orgId, documentId]);

  const loadDocument = useCallback(() => {
    setIsLoading(true);
    const urlParams = new URLSearchParams(window.location.search);
    const queryWhId = urlParams.get('whId');

    if (queryWhId) {
      getInventoryDocumentById(orgId, queryWhId, documentId)
        .then(res => setDoc(res))
        .catch(err => {
          console.error(err);
          // if query warehouse fails, fallback to general search
          fallbackSearch();
        })
        .finally(() => setIsLoading(false));
    } else {
      fallbackSearch();
    }
  }, [orgId, documentId, fallbackSearch]);

  useEffect(() => {
    loadDocument();
  }, [loadDocument]);

  const handleConfirm = async () => {
    if (!doc) return;
    setIsActionLoading(true);
    try {
      const updated = await confirmInventoryDocument(orgId, doc.warehouseId, doc.id);
      toast.success('Document marked as CONFIRMED');
      setDoc(updated);
    } catch (e) {
      console.error(e);
      toast.error('Could not confirm document. Check inventory levels.');
    } finally {
      setIsActionLoading(false);
    }
  };

  const handleComplete = async () => {
    if (!doc) return;
    setIsActionLoading(true);
    try {
      const updated = await completeInventoryDocument(orgId, doc.warehouseId, doc.id);
      toast.success('Document marked as COMPLETED. Inventory levels and COGS updated.');
      setDoc(updated);
    } catch (e) {
      console.error(e);
      toast.error('Failed to complete document. Check stock availability.');
    } finally {
      setIsActionLoading(false);
    }
  };

  const handleCancel = async () => {
    if (!doc) return;
    if (!confirm('Are you sure you want to cancel this movement document?')) return;
    setIsActionLoading(true);
    try {
      const updated = await cancelInventoryDocument(orgId, doc.warehouseId, doc.id);
      toast.success('Document status cancelled');
      setDoc(updated);
    } catch (e) {
      console.error(e);
      toast.error('Failed to cancel document.');
    } finally {
      setIsActionLoading(false);
    }
  };

  const handleCreateReplenish = async () => {
    if (!doc) return;
    setIsActionLoading(true);
    try {
      await createReplenishmentRequest(orgId, doc.warehouseId, doc.id, { notes: replenishNotes });
      toast.success('Replenishment request submitted successfully');
      setIsReplenishOpen(false);
      // reload document to reflect changes
      loadDocument();
    } catch (e) {
      console.error(e);
      toast.error('Failed to submit replenishment request');
    } finally {
      setIsActionLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-full text-[#898989] text-[13px] bg-white">
        Loading document details...
      </div>
    );
  }

  if (!doc) {
    return (
      <div className="p-8 text-center text-[#dc3545] bg-white h-full flex flex-col justify-center items-center">
        <AlertTriangle className="w-10 h-10 mb-2" />
        <h2 className="text-[16px] font-[700]">Document Not Found</h2>
        <Button onClick={() => router.back()} className="mt-4 bg-[#0066cc]">
          Go Back
        </Button>
      </div>
    );
  }

  const isDraft = doc.documentStatus === 'DRAFT';
  const isConfirmed = doc.documentStatus === 'CONFIRMED';
  const isWaitingStock = doc.documentStatus === 'WAITING_FOR_STOCK';

  return (
    <div className="h-full flex flex-col font-['Segoe_UI'] bg-white">
      {/* Top Header / Breadcrumbs */}
      <div className="px-6 py-4 border-b border-[#e0e0e0] flex items-center justify-between bg-white shrink-0">
        <div className="flex items-center space-x-3">
          <Button 
            variant="ghost" 
            size="icon" 
            onClick={() => router.push(`/dashboard/${orgId}/inventory/documents`)} 
            className="h-8 w-8 text-[#898989] hover:text-[#242424]"
          >
            <ArrowLeft className="w-5 h-5" />
          </Button>
          <div className="flex items-center space-x-2">
            <span className="text-[#898989] text-[14px]">Stock Moves</span>
            <ChevronRight className="w-4 h-4 text-[#898989]" />
            <h2 className="text-[16px] font-[700] text-[#242424] font-mono">{doc.name}</h2>
          </div>
          <span className={cn(
            "text-[11px] font-[700] px-2.5 py-0.5 rounded-[12px] uppercase ml-3",
            doc.documentStatus === 'DRAFT' && "bg-[#e2e8f0] text-[#475569]",
            doc.documentStatus === 'CONFIRMED' && "bg-[#e8f4fd] text-[#0066cc]",
            doc.documentStatus === 'COMPLETED' && "bg-[#e2f0d9] text-[#385723]",
            doc.documentStatus === 'CANCELLED' && "bg-[#fbe5d6] text-[#c65911]",
            doc.documentStatus === 'WAITING_FOR_STOCK' && "bg-[#fff2cc] text-[#d68100]"
          )}>
            {doc.documentStatus.replace('_', ' ')}
          </span>
        </div>

        {/* State Action Buttons */}
        <div className="flex space-x-2">
          {isDraft && hasPermission(PERMISSIONS.INVENTORY_DOCUMENTS.WRITE) && (
            <>
              <Button 
                onClick={handleConfirm} 
                disabled={isActionLoading}
                className="bg-[#0066cc] hover:bg-[#004499] text-white h-9 px-4 rounded-[4px] font-[600] text-[13px]"
              >
                <CheckCircle className="w-4 h-4 mr-2" /> Confirm Details
              </Button>
              <Button 
                onClick={handleCancel} 
                disabled={isActionLoading}
                variant="ghost"
                className="text-[#dc3545] hover:bg-[#fff0f0] h-9 px-4 rounded-[4px] font-[600] text-[13px]"
              >
                <XCircle className="w-4 h-4 mr-2" /> Cancel Move
              </Button>
            </>
          )}

          {(isConfirmed || isWaitingStock) && hasPermission(PERMISSIONS.INVENTORY_DOCUMENTS.WRITE) && (
            <>
              <Button 
                onClick={handleComplete} 
                disabled={isActionLoading}
                className="bg-[#28a745] hover:bg-[#218838] text-white h-9 px-4 rounded-[4px] font-[600] text-[13px]"
              >
                <Send className="w-4 h-4 mr-2" /> Complete Transfer
              </Button>
              <Button 
                onClick={handleCancel} 
                disabled={isActionLoading}
                variant="ghost"
                className="text-[#dc3545] hover:bg-[#fff0f0] h-9 px-4 rounded-[4px] font-[600] text-[13px]"
              >
                <XCircle className="w-4 h-4 mr-2" /> Cancel Move
              </Button>
            </>
          )}
        </div>
      </div>

      {/* Main Page Layout (Split) */}
      <div className="flex-1 overflow-auto bg-[#f8f8f8] p-6">
        {/* Shortage Alert */}
        {isWaitingStock && (
          <div className="bg-[#fff2cc] border border-[#ffe599] rounded-[4px] p-4 mb-6 flex justify-between items-center text-[#d68100]">
            <div className="flex items-center space-x-3">
              <AlertTriangle className="w-5 h-5 shrink-0" />
              <div>
                <p className="text-[13px] font-[700]">Out of Stock Shortage Detected</p>
                <p className="text-[12px]">This document is currently on hold. Stock replenishment must be requested to satisfy this outbound movement.</p>
              </div>
            </div>
            <Button 
              onClick={() => setIsReplenishOpen(true)}
              className="bg-[#ff9900] hover:bg-[#e68a00] text-white text-[12px] h-8 px-3 rounded-[4px] font-[600]"
            >
              <Plus className="w-4 h-4 mr-1.5" /> Request Replenishment
            </Button>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* General Metadata Card */}
          <div className="bg-white border border-[#e0e0e0] rounded-[4px] p-5 shadow-sm space-y-4">
            <h3 className="text-[14px] font-[700] text-[#242424] border-b border-[#f5f5f5] pb-2">General Details</h3>
            
            <div className="space-y-3 text-[13px] text-[#4a4a4a]">
              <div>
                <span className="text-[#898989] block text-[11px] font-[600] uppercase">Warehouse Location</span>
                <span className="font-[600] text-[#242424]">{doc.warehouseName}</span>
              </div>
              
              {doc.sourceWarehouseName && (
                <div>
                  <span className="text-[#898989] block text-[11px] font-[600] uppercase">Source / Transfer Target</span>
                  <span className="font-[600] text-[#242424]">{doc.sourceWarehouseName}</span>
                </div>
              )}

              <div>
                <span className="text-[#898989] block text-[11px] font-[600] uppercase">Operation Type</span>
                <span className="font-[600] text-[#242424]">{doc.documentType}</span>
              </div>

              <div>
                <span className="text-[#898989] block text-[11px] font-[600] uppercase">Origin Type</span>
                <span className="font-[600] text-[#242424]">{doc.referenceType}</span>
              </div>

              {doc.referenceId && (
                <div>
                  <span className="text-[#898989] block text-[11px] font-[600] uppercase">Source ID</span>
                  <span className="font-mono text-[#0066cc] break-all">{doc.referenceId}</span>
                </div>
              )}

              <div className="flex items-center space-x-2 pt-2 border-t border-[#f5f5f5]">
                <Calendar className="w-4 h-4 text-[#898989]" />
                <span>Scheduled: <strong>{new Date(doc.scheduledDate).toLocaleString()}</strong></span>
              </div>

              {doc.dateDone && (
                <div className="flex items-center space-x-2 text-[#28a745]">
                  <CheckCircle className="w-4 h-4" />
                  <span>Completed: <strong>{new Date(doc.dateDone).toLocaleString()}</strong></span>
                </div>
              )}

              <div className="flex items-center space-x-2 pt-2 border-t border-[#f5f5f5] text-[#898989]">
                <User className="w-4 h-4" />
                <span>Created by: {doc.createdBy ? `${doc.createdBy.firstName} ${doc.createdBy.lastName}` : 'System'}</span>
              </div>
            </div>

            {doc.notes && (
              <div className="bg-[#fafafa] p-3 rounded border border-[#e0e0e0] text-[13px] text-[#4a4a4a]">
                <span className="block font-[700] mb-1">Notes:</span>
                <p className="whitespace-pre-line">{doc.notes}</p>
              </div>
            )}
          </div>

          {/* Product lines items details */}
          <div className="lg:col-span-2 bg-white border border-[#e0e0e0] rounded-[4px] p-5 shadow-sm flex flex-col justify-between">
            <div>
              <h3 className="text-[14px] font-[700] text-[#242424] border-b border-[#f5f5f5] pb-2 mb-4">Stock Move Items</h3>
              
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="border-b border-[#e0e0e0]">
                      <th className="py-2.5 text-[11px] font-bold text-[#898989] uppercase tracking-wider">Product Name</th>
                      <th className="py-2.5 text-[11px] font-bold text-[#898989] uppercase tracking-wider text-right">Quantity</th>
                      <th className="py-2.5 text-[11px] font-bold text-[#898989] uppercase tracking-wider text-right">Unit Cost</th>
                      <th className="py-2.5 text-[11px] font-bold text-[#898989] uppercase tracking-wider text-right">Total Valuation</th>
                    </tr>
                  </thead>
                  <tbody>
                    {doc.lines?.length === 0 ? (
                      <tr>
                        <td colSpan={4} className="py-8 text-center text-[#898989] text-[13px]">No product lines registered</td>
                      </tr>
                    ) : (
                      doc.lines?.map((line) => (
                        <tr key={line.id} className="border-b border-[#f5f5f5] last:border-b-0">
                          <td className="py-3 text-[13px] font-[500] text-[#242424]">
                            {line.productName}
                          </td>
                          <td className="py-3 text-[13px] text-right font-[600] text-[#242424]">
                            {line.quantity.toLocaleString()}
                          </td>
                          <td className="py-3 text-[13px] text-right text-[#4a4a4a] font-mono">
                            ${(line.unitCost || 0).toLocaleString(undefined, { minimumFractionDigits: 2 })}
                          </td>
                          <td className="py-3 text-[13px] text-right font-[600] text-[#0066cc] font-mono">
                            ${(line.valuation || 0).toLocaleString(undefined, { minimumFractionDigits: 2 })}
                          </td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Total summary summary panel */}
            <div className="border-t border-[#e0e0e0] mt-6 pt-4 flex justify-end">
              <div className="w-[240px] text-[13px] space-y-1.5 text-[#4a4a4a]">
                <div className="flex justify-between">
                  <span>Total Items:</span>
                  <span className="font-[600]">{doc.lines?.reduce((acc, l) => acc + l.quantity, 0).toLocaleString() || 0}</span>
                </div>
                <div className="flex justify-between text-[16px] font-[700] text-[#242424] border-t border-[#f5f5f5] pt-2">
                  <span>Total Valuation:</span>
                  <span className="text-[#0066cc] font-mono">
                    ${(doc.lines?.reduce((acc, l) => acc + (l.valuation || 0), 0) || 0).toLocaleString(undefined, { minimumFractionDigits: 2 })}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Replenishment Submission Modal */}
      {isReplenishOpen && (
        <div className="fixed inset-0 bg-black/55 z-50 flex items-center justify-center animate-in fade-in duration-200">
          <div className="bg-white rounded-[8px] shadow-[0px_12px_28px_rgba(0,0,0,0.30)] w-full max-w-[500px] flex flex-col">
            <div className="px-6 py-4 border-b border-[#e0e0e0] flex justify-between items-center bg-[#f8f8f8]">
              <h2 className="text-[16px] font-[700] text-[#242424]">Submit Replenishment Request</h2>
              <Button variant="ghost" size="icon" onClick={() => setIsReplenishOpen(false)} className="h-8 w-8 text-[#898989] hover:text-[#242424]">
                <X className="w-5 h-5" />
              </Button>
            </div>

            <div className="p-6 space-y-4">
              <p className="text-[13px] text-[#4a4a4a]">
                This will automatically submit a replenishment request to operations matching the quantities needed in this document (<strong className="font-mono">{doc.name}</strong>).
              </p>

              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Request Notes</label>
                <Textarea 
                  value={replenishNotes}
                  onChange={e => setReplenishNotes(e.target.value)}
                  placeholder="e.g. Inbound purchase order WH-IN-029 is on its way, need immediate reconciliation."
                  rows={3}
                  className="border-[#d0d0d0] rounded-[4px] focus-visible:ring-0"
                />
              </div>
            </div>

            <div className="px-6 py-4 bg-[#f8f8f8] border-t border-[#e0e0e0] flex justify-end space-x-2">
              <Button 
                variant="outline" 
                onClick={() => setIsReplenishOpen(false)}
                className="bg-white border-[#d0d0d0] text-[#242424] h-10 rounded-[4px]"
              >
                Discard
              </Button>
              <Button 
                onClick={handleCreateReplenish}
                disabled={isActionLoading}
                className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 rounded-[4px]"
              >
                Submit Request
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
