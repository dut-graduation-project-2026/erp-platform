'use client';

import React from 'react';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Loader2 } from 'lucide-react';

interface ConfirmDialogProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  title: string;
  description: string;
  onConfirm: () => void | Promise<void>;
  confirmText?: string;
  cancelText?: string;
  variant?: 'default' | 'destructive' | 'success';
  disabled?: boolean;
}

export function ConfirmDialog({
  isOpen,
  onOpenChange,
  title,
  description,
  onConfirm,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  variant = 'default',
  disabled = false,
}: ConfirmDialogProps) {
  const getButtonClass = () => {
    switch (variant) {
      case 'destructive':
        return 'bg-[#dc3545] hover:bg-[#c82333] text-white hover:text-white border-transparent';
      case 'success':
        return 'bg-[#28a745] hover:bg-[#218838] text-white hover:text-white border-transparent';
      default:
        return 'bg-[#0066cc] hover:bg-[#004499] text-white hover:text-white border-transparent';
    }
  };

  return (
    <AlertDialog open={isOpen} onOpenChange={onOpenChange}>
      <AlertDialogContent className="font-['Segoe_UI'] bg-white max-w-[450px] rounded-lg border border-[#e0e0e0] shadow-lg p-6">
        <AlertDialogHeader className="space-y-2">
          <AlertDialogTitle className="text-[16px] font-[700] text-[#242424] text-left">
            {title}
          </AlertDialogTitle>
          <AlertDialogDescription className="text-[13px] text-[#4a4a4a] text-left leading-relaxed">
            {description}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter className="mt-6 flex flex-row justify-end space-x-2 sm:space-x-2">
          <AlertDialogCancel 
            disabled={disabled}
            className="h-9 text-[13px] font-[600] border-[#d0d0d0] text-[#242424] hover:bg-[#f8f8f8] px-4 rounded-[4px] mt-0 disabled:opacity-50"
          >
            {cancelText}
          </AlertDialogCancel>
          <AlertDialogAction
            disabled={disabled}
            onClick={(e) => {
              if (disabled) {
                e.preventDefault();
                return;
              }
              onConfirm();
            }}
            className={`${getButtonClass()} h-9 text-[13px] font-[600] px-4 rounded-[4px] transition-colors border disabled:opacity-50 flex items-center justify-center`}
          >
            {disabled && <Loader2 className="w-3.5 h-3.5 mr-1.5 animate-spin" />}
            {confirmText}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
