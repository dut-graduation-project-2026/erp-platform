'use client';

import React, { useEffect, useState, use } from 'react';
import { getTaxes, createTax, updateTax, deleteTax } from '@/features/sales/services/salesService';
import { SaleTax } from '@/features/sales/types';
import { TaxComputation } from '@/config/constants';
import { Button } from '@/components/ui/button';
import { Plus, Search, Edit2, Trash2 } from 'lucide-react';
import { usePermissions } from '@/hooks/use-permissions';
import { toast } from 'sonner';
import { Input } from '@/components/ui/input';
import { PERMISSIONS } from '@/config/permissions';

export default function TaxesPage({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = use(params);
  const [taxes, setTaxes] = useState<SaleTax[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  
  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [editingTax, setEditingTax] = useState<SaleTax | null>(null);
  const [formData, setFormData] = useState<{ name: string; computation: TaxComputation; amount: number; description?: string }>({ 
    name: '', computation: 'PERCENTAGE', amount: 0, description: '' 
  });
  const [isSaving, setIsSaving] = useState(false);

  const { hasPermission } = usePermissions();
  const canWrite = hasPermission(PERMISSIONS.TAXES.WRITE) || hasPermission(PERMISSIONS.TAXES.CREATE);

  const fetchTaxes = async () => {
    try {
      const res = await getTaxes(orgId, { limit: 100 });
      setTaxes(res.data || []);
    } catch (err) {
      console.error(err);
      toast.error('Failed to load taxes');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTaxes();
  }, [orgId]);

  const handleOpenModal = (tax?: SaleTax) => {
    if (tax) {
      setEditingTax(tax);
      setFormData({ 
        name: tax.name, 
        computation: tax.computation, 
        amount: tax.amount, 
        description: tax.description || '' 
      });
    } else {
      setEditingTax(null);
      setFormData({ name: '', computation: 'PERCENTAGE', amount: 0, description: '' });
    }
    setShowModal(true);
  };

  const handleSave = async () => {
    if (!formData.name.trim()) return toast.error('Tax name is required');
    if (formData.amount < 0) return toast.error('Amount cannot be negative');

    setIsSaving(true);
    try {
      if (editingTax) {
        await updateTax(orgId, editingTax.id, formData);
        toast.success('Tax updated successfully');
      } else {
        await createTax(orgId, formData);
        toast.success('Tax created successfully');
      }
      setShowModal(false);
      fetchTaxes();
    } catch (err: any) {
      // api client handles error toast
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this tax?')) return;
    try {
      await deleteTax(orgId, id);
      toast.success('Tax deleted successfully');
      fetchTaxes();
    } catch (err) {
      // handled
    }
  };

  const filteredTaxes = taxes.filter(t => t.name.toLowerCase().includes(searchQuery.toLowerCase()));

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white">
      <div className="flex justify-between items-center mb-6 shrink-0">
         <div>
            <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Taxes</h1>
            <span className="text-[14px] text-[#898989]">Manage your tax rates and computations</span>
         </div>
         <div className="flex space-x-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
              <Input 
                placeholder="Search taxes..." 
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                className="pl-9 h-10 w-[250px] border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]" 
              />
            </div>
            {canWrite && (
              <Button 
                onClick={() => handleOpenModal()}
                className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px] font-[600]"
              >
                <Plus className="w-4 h-4 mr-2" /> New Tax
              </Button>
            )}
         </div>
      </div>

      <div className="flex-1 min-h-0 border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden flex flex-col">
         <div className="flex-1 overflow-auto">
            <table className="w-full text-left border-collapse">
              <thead className="bg-[#f8f8f8] sticky top-0 z-10 shadow-[0px_1px_0px_#e0e0e0]">
                <tr>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[250px]">Tax Name</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[150px]">Computation</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[150px] text-right">Amount</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0]">Description</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] w-[100px] text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr><td colSpan={5} className="p-4 text-center text-[#898989]">Loading...</td></tr>
                ) : filteredTaxes.length === 0 ? (
                  <tr><td colSpan={5} className="p-4 text-center text-[#898989]">No taxes found</td></tr>
                ) : (
                  filteredTaxes.map((tax) => (
                    <tr key={tax.id} className="border-b border-[#e0e0e0] hover:bg-[#f8f8f8] bg-white">
                      <td className="px-4 py-3 text-[13px] text-[#242424] font-[600] border-r border-[#e0e0e0]">{tax.name}</td>
                      <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0]">{tax.computation}</td>
                      <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0] font-mono text-right">
                        {tax.computation === 'PERCENTAGE' ? `${tax.amount}%` : `₫${tax.amount.toLocaleString()}`}
                      </td>
                      <td className="px-4 py-3 text-[13px] text-[#898989] border-r border-[#e0e0e0]">{tax.description || '—'}</td>
                      <td className="px-4 py-3 text-right space-x-2">
                        {canWrite && (
                           <>
                             <button onClick={() => handleOpenModal(tax)} className="text-[#0066cc] hover:text-[#004499]"><Edit2 className="w-4 h-4" /></button>
                             <button onClick={() => handleDelete(tax.id)} className="text-red-500 hover:text-red-700"><Trash2 className="w-4 h-4" /></button>
                           </>
                        )}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
         </div>
      </div>

      {showModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center">
          <div className="bg-white rounded-[8px] shadow-[0px_12px_28px_rgba(0,0,0,0.30)] w-full max-w-[500px] overflow-hidden">
             <div className="p-6">
                <h2 className="text-[24px] font-[700] text-[#242424] mb-6">
                  {editingTax ? 'Edit Tax' : 'New Tax'}
                </h2>
                
                <div className="space-y-4">
                  <div>
                    <label className="block text-[14px] font-[600] text-[#242424] mb-1">Tax Name *</label>
                    <Input 
                      value={formData.name}
                      onChange={e => setFormData({...formData, name: e.target.value})}
                      placeholder="e.g. VAT 10%"
                      className="h-10 border-[#d0d0d0] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                    />
                  </div>
                  <div>
                    <label className="block text-[14px] font-[600] text-[#242424] mb-1">Computation *</label>
                    <select
                      value={formData.computation}
                      onChange={e => setFormData({...formData, computation: e.target.value as TaxComputation})}
                      className="h-10 w-full border border-[#d0d0d0] rounded-[4px] px-3 text-[14px] focus:outline-none focus:border-[#0066cc]"
                    >
                      <option value="PERCENTAGE">Percentage of price (%)</option>
                      <option value="FIXED_AMOUNT">Fixed amount (₫)</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-[14px] font-[600] text-[#242424] mb-1">Amount *</label>
                    <Input 
                      type="number"
                      min={0}
                      value={formData.amount}
                      onChange={e => setFormData({...formData, amount: Number(e.target.value)})}
                      className="h-10 border-[#d0d0d0] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                    />
                  </div>
                  <div>
                    <label className="block text-[14px] font-[600] text-[#242424] mb-1">Description</label>
                    <Input 
                      value={formData.description || ''}
                      onChange={e => setFormData({...formData, description: e.target.value})}
                      placeholder="Optional details"
                      className="h-10 border-[#d0d0d0] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                    />
                  </div>
                </div>
             </div>
             <div className="px-6 py-4 bg-[#f8f8f8] border-t border-[#e0e0e0] flex justify-end space-x-2">
                <Button 
                  variant="outline" 
                  onClick={() => setShowModal(false)}
                  className="bg-white border-[#d0d0d0] text-[#242424] h-10 px-4 rounded-[4px] font-[600]"
                >
                  Cancel
                </Button>
                <Button 
                  onClick={handleSave}
                  disabled={isSaving}
                  className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px] font-[600]"
                >
                  {isSaving ? 'Saving...' : 'Save'}
                </Button>
             </div>
          </div>
        </div>
      )}
    </div>
  );
}
