'use client';

import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';
import { Checkbox } from '@/components/ui/checkbox';
import { Button } from '@/components/ui/button';
import { RouteProposalResponse } from '../types';
import { 
  Brain, 
  Building, 
  CheckCircle2, 
  XCircle, 
  AlertCircle, 
  Loader2, 
  HelpCircle,
  TrendingUp,
  DollarSign
} from 'lucide-react';

interface RoutingPreviewModalProps {
  isOpen: boolean;
  onClose: () => void;
  proposals: RouteProposalResponse[];
  onConfirm: (confirmedSelections: { orderId: string; warehouseId: string }[]) => Promise<void>;
  isConfirming: boolean;
}

export default function RoutingPreviewModal({
  isOpen,
  onClose,
  proposals,
  onConfirm,
  isConfirming,
}: RoutingPreviewModalProps) {
  const [selectedOrderIds, setSelectedOrderIds] = useState<Set<string>>(new Set());

  // Automatically select all routable orders when proposals change
  useEffect(() => {
    const initialSelected = new Set<string>();
    proposals.forEach((p) => {
      if (p.routable && p.proposedWarehouseId) {
        initialSelected.add(p.orderId);
      }
    });
    setSelectedOrderIds(initialSelected);
  }, [proposals]);

  const totalProposals = proposals.length;
  const routableCount = proposals.filter((p) => p.routable).length;
  const unroutableCount = totalProposals - routableCount;
  
  const totalAmountSelected = proposals
    .filter((p) => selectedOrderIds.has(p.orderId))
    .reduce((sum, p) => sum + (p.totalAmount || 0), 0);

  const toggleSelectOrder = (orderId: string, disabled: boolean) => {
    if (disabled) return;
    setSelectedOrderIds((prev) => {
      const next = new Set(prev);
      if (next.has(orderId)) {
        next.delete(orderId);
      } else {
        next.add(orderId);
      }
      return next;
    });
  };

  const handleSelectAllRoutable = () => {
    const allRoutableSelected = proposals
      .filter((p) => p.routable)
      .every((p) => selectedOrderIds.has(p.orderId));

    if (allRoutableSelected) {
      // Deselect all
      setSelectedOrderIds(new Set());
    } else {
      // Select all routable
      const next = new Set<string>();
      proposals.forEach((p) => {
        if (p.routable && p.proposedWarehouseId) {
          next.add(p.orderId);
        }
      });
      setSelectedOrderIds(next);
    }
  };

  const handleConfirm = async () => {
    const confirmations = proposals
      .filter((p) => selectedOrderIds.has(p.orderId) && p.proposedWarehouseId)
      .map((p) => ({
        orderId: p.orderId,
        warehouseId: p.proposedWarehouseId!,
      }));

    if (confirmations.length === 0) {
      return;
    }
    
    await onConfirm(confirmations);
  };

  const allRoutableSelected = proposals.length > 0 && proposals
    .filter((p) => p.routable)
    .every((p) => selectedOrderIds.has(p.orderId));

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-[1100px] w-full p-0 overflow-hidden rounded-[8px] border border-[#e0e0e0] bg-white shadow-[0px_12px_28px_rgba(0,0,0,0.30)] flex flex-col max-h-[90vh] font-['Segoe_UI']">
        
        {/* Header with clean white background */}
        <div className="bg-white px-8 py-5 border-b border-[#e0e0e0] flex items-center justify-between shrink-0 relative">
          <div className="flex items-center gap-3">
            <div className="bg-[#f0f4ff] p-2.5 rounded-[4px] border border-[#d0e0ff]">
              <Brain className="w-6 h-6 text-[#0066cc]" />
            </div>
            <div>
              <DialogTitle className="text-[20px] font-semibold text-[#242424]">
                Smart Routing Preview & Allocation
              </DialogTitle>
              <DialogDescription className="text-[#898989] text-[13px] mt-0.5 font-normal">
                Review the AI inventory-matching results before warehouse document finalization.
              </DialogDescription>
            </div>
          </div>
        </div>

        {/* Dynamic Analytics Summary Cards */}
        <div className="bg-[#f8f8f8] border-b border-[#e0e0e0] px-8 py-5 grid grid-cols-3 gap-6 shrink-0">
          <div className="bg-white p-4 rounded-[4px] border border-[#e0e0e0] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] flex items-center gap-4 hover:shadow-[0px_2px_8px_rgba(0,0,0,0.15)] transition-all duration-200">
            <div className="bg-[#f0f4ff] p-3 rounded-[4px] text-[#0066cc]">
              <TrendingUp className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[10px] font-semibold text-[#898989] uppercase tracking-wider">Total Evaluated</p>
              <p className="text-xl font-bold text-[#242424] mt-0.5">{totalProposals} <span className="text-xs font-normal text-[#898989]">Orders</span></p>
            </div>
          </div>
          <div className="bg-white p-4 rounded-[4px] border border-[#e0e0e0] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] flex items-center gap-4 hover:shadow-[0px_2px_8px_rgba(0,0,0,0.15)] transition-all duration-200">
            <div className="bg-[#e8f5e9] p-3 rounded-[4px] text-[#28a745]">
              <CheckCircle2 className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[10px] font-semibold text-[#898989] uppercase tracking-wider">Routable (In-Stock)</p>
              <p className="text-xl font-bold text-[#28a745] mt-0.5">{routableCount} <span className="text-xs font-normal text-[#898989]">Orders</span></p>
            </div>
          </div>
          <div className="bg-white p-4 rounded-[4px] border border-[#e0e0e0] shadow-[0px_1px_3px_rgba(0,0,0,0.12)] flex items-center gap-4 hover:shadow-[0px_2px_8px_rgba(0,0,0,0.15)] transition-all duration-200">
            <div className="bg-[#fde8e8] p-3 rounded-[4px] text-[#dc3545]">
              <AlertCircle className="w-5 h-5" />
            </div>
            <div>
              <p className="text-[10px] font-semibold text-[#898989] uppercase tracking-wider">Stock Shortage</p>
              <p className="text-xl font-bold text-[#dc3545] mt-0.5">{unroutableCount} <span className="text-xs font-normal text-[#898989]">Orders</span></p>
            </div>
          </div>
        </div>

        {/* Proposals List Container */}
        <div className="flex-1 overflow-y-auto px-8 py-5">
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-base font-semibold text-[#242424] flex items-center gap-2">
              Proposed Allocations
            </h3>
            {routableCount > 0 && (
              <Button
                variant="ghost"
                onClick={handleSelectAllRoutable}
                className="text-xs h-8 px-3 text-[#0066cc] hover:text-[#004499] hover:bg-[#f0f4ff] font-semibold rounded-[4px] transition-colors"
              >
                {allRoutableSelected ? 'Deselect All' : 'Select All Routable'}
              </Button>
            )}
          </div>

          <div className="border border-[#e0e0e0] rounded-[4px] overflow-hidden shadow-[0px_1px_3px_rgba(0,0,0,0.12)] bg-white">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="bg-[#f8f8f8] border-b border-[#e0e0e0] text-[#242424] font-semibold text-[11px] uppercase tracking-wider">
                  <th className="w-[60px] px-6 py-3 text-center">Select</th>
                  <th className="w-[140px] px-6 py-3">Order Code</th>
                  <th className="min-w-[200px] px-6 py-3">Customer</th>
                  <th className="w-[125px] px-6 py-3 text-right">Order Value</th>
                  <th className="min-w-[220px] px-6 py-3">Assigned Warehouse</th>
                  <th className="w-[120px] px-6 py-3 text-center">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#e0e0e0] bg-white text-[13px] text-[#242424]">
                {proposals.map((proposal) => {
                  const isRoutable = proposal.routable;
                  const isChecked = selectedOrderIds.has(proposal.orderId);
                  
                  return (
                    <tr 
                      key={proposal.orderId}
                      onClick={() => toggleSelectOrder(proposal.orderId, !isRoutable)}
                      className={`transition-all duration-150 cursor-pointer ${
                        !isRoutable 
                          ? 'bg-[#f8f8f8] text-[#898989] cursor-not-allowed opacity-75'
                          : isChecked 
                            ? 'bg-[#f0f4ff] hover:bg-[#e0edff] border-l-2 border-l-[#0066cc]'
                            : 'hover:bg-[#f0f4ff]'
                      }`}
                    >
                      <td className="px-6 py-3 text-center" onClick={(e) => e.stopPropagation()}>
                        <Checkbox
                          checked={isChecked}
                          disabled={!isRoutable}
                          onCheckedChange={() => toggleSelectOrder(proposal.orderId, !isRoutable)}
                          className={`mx-auto ${isRoutable ? 'data-[state=checked]:bg-[#0066cc] data-[state=checked]:border-[#0066cc]' : ''}`}
                        />
                      </td>
                      <td className="px-6 py-3 font-semibold text-[#111111] whitespace-nowrap">
                        {proposal.orderNumber}
                      </td>
                      <td className="px-6 py-3 font-medium max-w-[220px] truncate whitespace-nowrap">
                        {proposal.customerName}
                      </td>
                      <td className="px-6 py-3 text-right font-semibold text-[#111111] whitespace-nowrap">
                        ${proposal.totalAmount.toLocaleString()}
                      </td>
                      <td className="px-6 py-3 whitespace-nowrap">
                        {proposal.proposedWarehouseName ? (
                          <div className="flex items-center gap-2">
                            <Building className="w-4 h-4 text-[#898989] shrink-0" />
                            <span className="font-medium text-[#242424]">{proposal.proposedWarehouseName}</span>
                          </div>
                        ) : (
                          <span className="text-[#898989] italic">Unassigned</span>
                        )}
                      </td>
                      <td className="px-6 py-3 text-center whitespace-nowrap">
                        {isRoutable ? (
                          <span className="inline-flex items-center px-2.5 py-1 rounded-[2px] text-[11px] font-semibold bg-[#e8f5e9] text-[#28a745] border border-[#c8e6c9]">
                            <CheckCircle2 className="w-3.5 h-3.5 mr-1 text-[#28a745]" /> Ready
                          </span>
                        ) : (
                          <span className="inline-flex items-center px-2.5 py-1 rounded-[2px] text-[11px] font-semibold bg-[#fde8e8] text-[#dc3545] border border-[#f8b4b4]">
                            <AlertCircle className="w-3.5 h-3.5 mr-1 text-[#dc3545]" /> No Stock
                          </span>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>

        {/* Footer actions */}
        <div className="bg-[#f8f8f8] border-t border-[#e0e0e0] px-8 py-5 flex justify-between items-center shrink-0">
          <div className="text-sm text-[#898989]">
            Selected: <span className="font-bold text-[#242424]">{selectedOrderIds.size}</span> / {routableCount} ready orders
            {selectedOrderIds.size > 0 && (
              <span className="text-[#0066cc] font-semibold ml-1">
                {` ($${totalAmountSelected.toLocaleString()} selected value)`}
              </span>
            )}
          </div>
          <div className="flex gap-2">
            <Button
              variant="outline"
              onClick={onClose}
              disabled={isConfirming}
              className="h-10 px-5 text-sm font-medium border-[#d0d0d0] text-[#242424] hover:bg-[#f8f8f8] rounded-[4px] transition-colors"
            >
              Cancel
            </Button>
            <Button
              onClick={handleConfirm}
              disabled={isConfirming || selectedOrderIds.size === 0}
              className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-6 text-sm font-semibold rounded-[4px] flex items-center gap-2 transition-colors"
            >
              {isConfirming ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Routing...
                </>
              ) : (
                <>
                  <Brain className="w-4 h-4" />
                  Confirm Allocation
                </>
              )}
            </Button>
          </div>
        </div>

      </DialogContent>
    </Dialog>
  );
}
