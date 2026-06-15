import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';

interface RegisterPaymentModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (amount: number) => Promise<void>;
  remainingBalance: number;
}

export function RegisterPaymentModal({
  isOpen,
  onClose,
  onConfirm,
  remainingBalance,
}: RegisterPaymentModalProps) {
  const [amount, setAmount] = useState<string>(remainingBalance.toString());
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Reset state when modal opens
  React.useEffect(() => {
    if (isOpen) {
      setAmount(remainingBalance.toString());
      setError(null);
    }
  }, [isOpen, remainingBalance]);

  const handleSubmit = async () => {
    const paymentAmount = parseFloat(amount);
    
    if (isNaN(paymentAmount) || paymentAmount <= 0) {
      setError('Amount must be greater than 0');
      return;
    }
    
    if (paymentAmount > remainingBalance) {
      setError(`Amount cannot exceed the remaining balance (₫${remainingBalance.toLocaleString()})`);
      return;
    }

    setIsSubmitting(true);
    setError(null);
    
    try {
      await onConfirm(paymentAmount);
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to register payment');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-[425px] font-['Segoe_UI']">
        <DialogHeader>
          <DialogTitle className="text-[18px] font-[600] text-[#242424]">Register Payment</DialogTitle>
        </DialogHeader>
        
        <div className="py-4 space-y-4">
          <div className="bg-[#f8f8f8] p-3 rounded-[4px] border border-[#e0e0e0]">
            <div className="flex justify-between text-[13px] mb-1">
              <span className="text-[#898989]">Remaining Balance:</span>
              <span className="font-semibold text-[#dc3545]">₫{remainingBalance.toLocaleString()}</span>
            </div>
          </div>
          
          <div className="space-y-2">
            <div className="flex justify-between items-end">
              <label className="text-[13px] font-[600] text-[#242424]">Payment Amount (₫)</label>
              <button
                type="button"
                onClick={() => {
                  setAmount(remainingBalance.toString());
                  setError(null);
                }}
                className="text-[12px] text-[#0066cc] hover:text-[#004499] hover:underline font-[500]"
              >
                Set to Full Amount
              </button>
            </div>
            <Input
              type="number"
              value={amount}
              onChange={(e) => {
                setAmount(e.target.value);
                setError(null);
              }}
              min={0}
              max={remainingBalance}
              step="0.01"
              placeholder="Enter amount..."
              className="h-10 border-[#d0d0d0] hover:border-[#a0a0a0] focus:border-[#0066cc] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
            />
            {error && <p className="text-[12px] text-[#dc3545]">{error}</p>}
          </div>
        </div>

        <DialogFooter className="gap-2 sm:gap-0">
          <Button
            variant="outline"
            onClick={onClose}
            disabled={isSubmitting}
            className="border-[#d0d0d0] hover:bg-[#f0f0f0] text-[#242424] h-10 px-4 rounded-[4px]"
          >
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={isSubmitting || !amount}
            className="bg-[#28a745] hover:bg-[#218838] text-white h-10 px-4 rounded-[4px]"
          >
            {isSubmitting ? 'Processing...' : 'Confirm Payment'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
