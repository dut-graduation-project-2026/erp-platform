'use client';

import React, { useEffect, useState, use } from 'react';
import { getSaleInvoiceById, getOrderItems } from '@/features/sales/services/salesService';
import { SaleInvoice, OrderItem } from '@/features/sales/types';
import { Button } from '@/components/ui/button';
import { ArrowLeft, Printer } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { format } from 'date-fns';
import { toast } from 'sonner';
import Image from 'next/image';
import dutIcon from '@/app/icon_dut.ico';

export default function InvoicePrintPage({ params }: { params: Promise<{ orgId: string, id: string }> }) {
  const router = useRouter();
  const { orgId, id } = use(params);
  const [invoice, setInvoice] = useState<SaleInvoice | null>(null);
  const [orderItems, setOrderItems] = useState<OrderItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const fetchInvoiceAndItems = async () => {
      try {
        const inv = await getSaleInvoiceById(orgId, id);
        setInvoice(inv);
        if (inv.saleOrder?.id) {
          const items = await getOrderItems(orgId, inv.saleOrder.id);
          setOrderItems(items);
        }
      } catch (err: any) {
        console.error(err);
        toast.error('Failed to load invoice for printing.');
      } finally {
        setIsLoading(false);
        // Automatically trigger print after a short delay to allow rendering
        setTimeout(() => {
          window.print();
        }, 500);
      }
    };
    fetchInvoiceAndItems();
  }, [orgId, id]);

  if (isLoading) {
    return <div className="p-6 text-center text-[#898989] font-['Segoe_UI']">Loading invoice for print...</div>;
  }

  if (!invoice) {
    return <div className="p-6 text-center text-red-500 font-['Segoe_UI']">Invoice not found.</div>;
  }

  // Calculate totals
  const untaxedTotal = orderItems.reduce((sum, item) => sum + (item.quantity * item.unitPrice), 0);
  const taxTotal = orderItems.reduce((sum, item) => sum + (item.subtotal - (item.quantity * item.unitPrice)), 0);
  // Wait, item.subtotal in our system might be amount including tax or excluding tax depending on backend.
  // Let's calculate tax properly:
  let calcUntaxed = 0;
  let calcTax = 0;
  
  orderItems.forEach(item => {
    const lineUntaxed = item.quantity * item.unitPrice;
    const rate = item.tax?.amount || item.taxPercentage || 0;
    const lineTax = (lineUntaxed * rate) / 100;
    calcUntaxed += lineUntaxed;
    calcTax += lineTax;
  });

  const grandTotal = calcUntaxed + calcTax;

  // Group taxes for summary
  const taxGroups: Record<number, { untaxed: number, taxAmount: number }> = {};
  orderItems.forEach(item => {
    const rate = item.tax?.amount || item.taxPercentage || 0;
    const lineUntaxed = item.quantity * item.unitPrice;
    const lineTax = (lineUntaxed * rate) / 100;
    
    if (!taxGroups[rate]) {
      taxGroups[rate] = { untaxed: 0, taxAmount: 0 };
    }
    taxGroups[rate].untaxed += lineUntaxed;
    taxGroups[rate].taxAmount += lineTax;
  });

  return (
    <div className="min-h-screen bg-[#f8f8f8] p-8 font-['Segoe_UI'] flex flex-col items-center">
      <style dangerouslySetInnerHTML={{__html: `
        @media print {
          body * {
            visibility: hidden;
          }
          .print-wrapper, .print-wrapper * {
            visibility: visible;
          }
          .print-wrapper {
            position: absolute;
            left: 0;
            top: 0;
            width: 100%;
            margin: 0;
            padding: 0;
            background: white !important;
            box-shadow: none !important;
          }
          @page {
            size: A4 portrait;
            margin: 15mm;
          }
        }
      `}} />

      {/* Screen controls */}
      <div className="w-full max-w-[800px] flex justify-between items-center mb-6 print:hidden">
        <Button variant="outline" onClick={() => router.back()} className="border-[#d0d0d0] text-[#4a4a4a]">
          <ArrowLeft className="w-4 h-4 mr-2" /> Back to Invoice
        </Button>
        <Button onClick={() => window.print()} className="bg-[#0066cc] hover:bg-[#004499] text-white">
          <Printer className="w-4 h-4 mr-2" /> Print PDF
        </Button>
      </div>

      {/* Printable Area */}
      <div className="print-wrapper bg-white shadow-sm border border-[#e0e0e0] w-full max-w-[800px] p-[40px]">
        {/* Header Section */}
        <div className="flex justify-between items-start border-b-2 border-[#0066cc] pb-6 mb-6">
          <div className="flex items-center space-x-4">
            <div className="w-[80px] h-[80px] bg-white border border-[#d0d0d0] flex items-center justify-center relative overflow-hidden">
              <Image src={dutIcon} alt="DUT Logo" width={80} height={80} className="object-contain" priority />
            </div>
            <div>
              <h1 className="text-[20px] font-bold text-[#0066cc] uppercase tracking-wide">
                {invoice.organization?.name || "Company Name"}
              </h1>
              <p className="text-[12px] text-[#4a4a4a] mt-1">Tax Code: 0101234567</p>
              <p className="text-[12px] text-[#4a4a4a]">Address: 123 Business Blvd, Tech District, City</p>
              <p className="text-[12px] text-[#4a4a4a]">Phone: +1 234 567 8900</p>
            </div>
          </div>
          <div className="text-right">
            <h2 className="text-[24px] font-bold text-[#242424] uppercase tracking-wider">VAT Invoice</h2>
            <p className="text-[12px] text-[#898989] mt-2 italic">
              Date: {invoice.createdAt ? format(new Date(invoice.createdAt), 'dd MMMM yyyy') : 'N/A'}
            </p>
            <p className="text-[14px] font-bold text-[#242424] mt-1">
              No: <span className="text-[#dc3545]">{invoice.invoiceNumber}</span>
            </p>
          </div>
        </div>

        {/* Customer Information */}
        <div className="mb-8 bg-[#f8f8f8] p-4 border border-[#e0e0e0]">
          <h3 className="text-[14px] font-bold text-[#0066cc] mb-3 uppercase border-b border-[#d0d0d0] pb-2">Customer Details</h3>
          <div className="grid grid-cols-4 gap-y-2 text-[13px]">
            <div className="font-semibold text-[#4a4a4a] col-span-1">Customer Name:</div>
            <div className="col-span-3 text-[#242424] uppercase font-bold">{invoice.partner?.name || 'N/A'}</div>
            
            <div className="font-semibold text-[#4a4a4a] col-span-1">Tax Code:</div>
            <div className="col-span-3 text-[#242424]">{invoice.partner?.taxCode || 'N/A'}</div>
            
            <div className="font-semibold text-[#4a4a4a] col-span-1">Address:</div>
            <div className="col-span-3 text-[#242424]">{invoice.partner?.address || 'N/A'}</div>
            
            <div className="font-semibold text-[#4a4a4a] col-span-1">Phone/Email:</div>
            <div className="col-span-3 text-[#242424]">{invoice.partner?.phone} / {invoice.partner?.email}</div>
          </div>
        </div>

        {/* Items Table */}
        <table className="w-full text-left border-collapse mb-8 text-[12px]">
          <thead>
            <tr className="bg-[#f0f4ff] border border-[#d0d0d0] text-[#0066cc] font-bold">
              <th className="p-3 border border-[#d0d0d0] text-center w-[5%]">No.</th>
              <th className="p-3 border border-[#d0d0d0] w-[35%]">Description</th>
              <th className="p-3 border border-[#d0d0d0] text-center w-[10%]">Qty</th>
              <th className="p-3 border border-[#d0d0d0] text-right w-[15%]">Unit Price</th>
              <th className="p-3 border border-[#d0d0d0] text-right w-[15%]">Amount</th>
              <th className="p-3 border border-[#d0d0d0] text-center w-[10%]">VAT %</th>
              <th className="p-3 border border-[#d0d0d0] text-right w-[10%]">VAT Amt</th>
            </tr>
          </thead>
          <tbody>
            {orderItems.length > 0 ? orderItems.map((item, index) => {
              const lineUntaxed = item.quantity * item.unitPrice;
              const rate = item.tax?.amount || item.taxPercentage || 0;
              const lineTax = (lineUntaxed * rate) / 100;
              return (
                <tr key={index} className="border border-[#d0d0d0]">
                  <td className="p-3 border border-[#d0d0d0] text-center">{index + 1}</td>
                  <td className="p-3 border border-[#d0d0d0] font-medium text-[#242424]">{item.product?.name || item.description || 'Unknown Item'}</td>
                  <td className="p-3 border border-[#d0d0d0] text-center">{item.quantity}</td>
                  <td className="p-3 border border-[#d0d0d0] text-right">${item.unitPrice.toLocaleString()}</td>
                  <td className="p-3 border border-[#d0d0d0] text-right">${lineUntaxed.toLocaleString()}</td>
                  <td className="p-3 border border-[#d0d0d0] text-center">{rate}%</td>
                  <td className="p-3 border border-[#d0d0d0] text-right">${lineTax.toLocaleString()}</td>
                </tr>
              );
            }) : (
              <tr>
                <td colSpan={7} className="p-4 border border-[#d0d0d0] text-center text-[#898989]">No items found.</td>
              </tr>
            )}
          </tbody>
        </table>

        {/* Tax Summary & Totals */}
        <div className="flex justify-between items-start mb-10">
          <div className="w-[45%]">
            <table className="w-full text-left border-collapse text-[12px]">
              <thead>
                <tr className="bg-[#f0f4ff] border border-[#d0d0d0] text-[#0066cc] font-bold">
                  <th className="p-2 border border-[#d0d0d0]">VAT Rate</th>
                  <th className="p-2 border border-[#d0d0d0] text-right">Amount Excl. VAT</th>
                  <th className="p-2 border border-[#d0d0d0] text-right">VAT Amount</th>
                </tr>
              </thead>
              <tbody>
                {Object.keys(taxGroups).map(rate => (
                  <tr key={rate} className="border border-[#d0d0d0]">
                    <td className="p-2 border border-[#d0d0d0] text-center">{rate}%</td>
                    <td className="p-2 border border-[#d0d0d0] text-right">${taxGroups[Number(rate)].untaxed.toLocaleString()}</td>
                    <td className="p-2 border border-[#d0d0d0] text-right">${taxGroups[Number(rate)].taxAmount.toLocaleString()}</td>
                  </tr>
                ))}
                {Object.keys(taxGroups).length === 0 && (
                  <tr>
                    <td colSpan={3} className="p-2 border border-[#d0d0d0] text-center text-[#898989]">No VAT applied</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
          
          <div className="w-[45%]">
            <div className="grid grid-cols-2 gap-y-3 text-[13px] border border-[#d0d0d0] p-4">
              <div className="font-semibold text-[#4a4a4a]">Total Amount (Excl. VAT):</div>
              <div className="text-right text-[#242424]">${calcUntaxed.toLocaleString()}</div>
              
              <div className="font-semibold text-[#4a4a4a]">Total VAT Amount:</div>
              <div className="text-right text-[#242424]">${calcTax.toLocaleString()}</div>
              
              <div className="font-bold text-[#0066cc] border-t border-[#d0d0d0] pt-3 mt-1">Total Payment:</div>
              <div className="text-right font-bold text-[#dc3545] text-[16px] border-t border-[#d0d0d0] pt-3 mt-1">
                ${grandTotal.toLocaleString()}
              </div>
            </div>
          </div>
        </div>

        {/* Signatures */}
        <div className="grid grid-cols-2 text-center mt-12 mb-8 text-[13px]">
          <div>
            <p className="font-bold text-[#242424]">Buyer</p>
            <p className="italic text-[#898989] text-[11px] mt-1">(Signature & Full name)</p>
            <div className="h-[80px]"></div>
          </div>
          <div>
            <p className="font-bold text-[#242424]">Seller</p>
            <p className="italic text-[#898989] text-[11px] mt-1">(Signature, Stamp & Full name)</p>
            <div className="h-[80px] flex items-center justify-center">
              {/* Fake signature stamp */}
              <div className="border-[3px] border-[#dc3545] text-[#dc3545] p-2 rotate-[-10deg] opacity-70 mt-6 inline-block">
                <p className="font-bold text-[14px]">APPROVED</p>
                <p className="text-[8px]">{format(new Date(), 'dd/MM/yyyy')}</p>
              </div>
            </div>
            <p className="font-bold text-[#242424] mt-2">SYSTEM ADMINISTRATOR</p>
          </div>
        </div>

      </div>
    </div>
  );
}
