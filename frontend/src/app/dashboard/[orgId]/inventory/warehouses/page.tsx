'use client';

import React, { useEffect, useState, use } from 'react';
import { 
  getWarehouses, 
  createWarehouse, 
  updateWarehouse, 
  deleteWarehouse 
} from '@/features/inventory/services/inventoryService';
import { Warehouse, CreateWarehouseRequest } from '@/features/inventory/types';
import { useOrganizationMembers } from '@/features/organization/hooks/useOrganizationMembers';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { AddressInput } from '@/components/ui/address-input';
import { Textarea } from '@/components/ui/textarea';
import { Plus, Search, Filter, X, Save, Edit, Trash2, User, MapPin, Clipboard } from 'lucide-react';
import { cn } from '@/lib/utils';
import { usePermissions } from '@/hooks/use-permissions';
import { PERMISSIONS } from '@/config/permissions';
import { toast } from 'sonner';

export default function WarehousesListPage({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = use(params);
  const [warehouses, setWarehouses] = useState<Warehouse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [staffSearchQuery, setStaffSearchQuery] = useState('');
  const { hasPermission } = usePermissions();

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedWarehouse, setSelectedWarehouse] = useState<Partial<Warehouse> | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  // Fetch all organization members for dropdown selections (Manager / Staff)
  const { members: orgMembers } = useOrganizationMembers({
    organizationId: orgId,
    limit: 100, // retrieve a broad list of members
  });

  const loadWarehouses = () => {
    setIsLoading(true);
    getWarehouses(orgId)
      .then(res => {
        setWarehouses(res.data || []);
      })
      .catch(err => {
        console.error(err);
        toast.error('Failed to load warehouses');
      })
      .finally(() => setIsLoading(false));
  };

  useEffect(() => {
    loadWarehouses();
  }, [orgId]);

  const handleOpenModal = (wh?: Warehouse) => {
    setStaffSearchQuery('');
    if (wh) {
      setSelectedWarehouse(wh);
    } else {
      setSelectedWarehouse({
        code: '',
        name: '',
        address: '',
        description: '',
        isActive: true,
        manager: undefined,
        staff: [],
      });
    }
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedWarehouse(null);
    setStaffSearchQuery('');
  };

  const handleSaveWarehouse = async () => {
    if (!selectedWarehouse?.name?.trim()) return toast.error('Warehouse name is required.');
    if (!selectedWarehouse?.code?.trim()) return toast.error('Warehouse code is required.');
    if (!selectedWarehouse?.manager?.id) return toast.error('Warehouse manager is required.');

    const managerId = selectedWarehouse.manager.id;
    const staffIds = selectedWarehouse.staff?.map(s => s.id) || [];

    const payload: CreateWarehouseRequest = {
      name: selectedWarehouse.name.trim(),
      code: selectedWarehouse.code.trim().toUpperCase(),
      address: selectedWarehouse.address?.trim() || '',
      description: selectedWarehouse.description?.trim() || '',
      managerId,
      staffIds,
    };

    setIsSaving(true);
    try {
      if (selectedWarehouse.id) {
        // Update
        await updateWarehouse(orgId, selectedWarehouse.id, payload);
        toast.success('Warehouse updated successfully');
      } else {
        // Create
        await createWarehouse(orgId, payload);
        toast.success('Warehouse created successfully');
      }
      loadWarehouses();
      handleCloseModal();
    } catch (e) {
      console.error(e);
      toast.error('Error saving warehouse. Check logs.');
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteWarehouse = async (id: string, name: string) => {
    if (!confirm(`Are you sure you want to delete warehouse "${name}"?`)) return;

    try {
      await deleteWarehouse(orgId, id);
      toast.success('Warehouse deleted');
      loadWarehouses();
    } catch (e) {
      console.error(e);
      toast.error('Could not delete warehouse. It might contain active inventory.');
    }
  };

  const filteredWarehouses = warehouses.filter(wh => {
    const q = searchQuery.toLowerCase();
    return wh.name.toLowerCase().includes(q) || 
           wh.code.toLowerCase().includes(q) || 
           wh.address.toLowerCase().includes(q);
  });

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white relative">
      {/* Header */}
      <div className="flex justify-between items-center mb-6 shrink-0">
        <div>
          <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Warehouse Locations</h1>
          <span className="text-[14px] text-[#898989]">Manage your storage facilities, operations staff, and addresses</span>
        </div>
        <div className="flex space-x-3">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
            <Input 
              placeholder="Search warehouses..." 
              value={searchQuery}
              onChange={e => setSearchQuery(e.target.value)}
              className="pl-9 h-10 w-[250px] border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]" 
            />
          </div>
          <Button variant="outline" className="border-[#d0d0d0] text-[#242424] h-10 px-3 bg-white rounded-[4px] font-[500] text-[13px]">
            <Filter className="w-4 h-4" />
          </Button>
          {hasPermission(PERMISSIONS.WAREHOUSES.CREATE) && (
            <Button 
              onClick={() => handleOpenModal()}
              className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px] font-[600] text-[13px]"
            >
              <Plus className="w-4 h-4 mr-2" /> New Warehouse
            </Button>
          )}
        </div>
      </div>

      {/* Grid List */}
      <div className="flex-1 overflow-auto bg-[#f8f8f8] p-6 -mx-6 -mb-6 border-t border-[#e0e0e0]">
        {isLoading ? (
          <div className="flex justify-center items-center h-full text-[#898989] text-[13px]">Loading warehouses...</div>
        ) : filteredWarehouses.length === 0 ? (
          <div className="flex justify-center items-center h-full text-[#898989] text-[13px]">No warehouses found</div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredWarehouses.map((wh) => (
              <div 
                key={wh.id} 
                className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] p-5 hover:shadow-[0px_4px_12px_rgba(0,0,0,0.12)] transition-all flex flex-col justify-between"
              >
                <div>
                  <div className="flex justify-between items-start mb-3">
                    <div>
                      <span className="bg-[#f0f4ff] text-[#0066cc] font-mono font-[700] text-[11px] px-2 py-0.5 rounded-[2px] uppercase">
                        {wh.code}
                      </span>
                      <h3 className="text-[16px] font-[600] text-[#242424] mt-1.5">{wh.name}</h3>
                    </div>
                    <span className={cn(
                      "text-[12px] font-[600] px-2 py-0.5 rounded-[4px] min-w-[70px] text-center",
                      wh.isActive ? "bg-[#e2f0d9] text-[#385723]" : "bg-[#fbe5d6] text-[#c65911]"
                    )}>
                      {wh.isActive ? 'Active' : 'Inactive'}
                    </span>
                  </div>

                  <p className="text-[13px] text-[#898989] line-clamp-2 mb-4 h-9">
                    {wh.description || 'No description provided.'}
                  </p>

                  <div className="space-y-2 text-[13px] text-[#4a4a4a] border-t border-[#f5f5f5] pt-3">
                    <div className="flex items-center space-x-2">
                      <MapPin className="w-4 h-4 text-[#898989] shrink-0" />
                      <span className="truncate">{wh.address || 'No address registered'}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <User className="w-4 h-4 text-[#898989] shrink-0" />
                      <span>Manager: <strong className="font-[600]">{wh.manager ? `${wh.manager.firstName} ${wh.manager.lastName}` : 'Unassigned'}</strong></span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Clipboard className="w-4 h-4 text-[#898989] shrink-0" />
                      <span>Operations Staff: <strong className="font-[600]">{wh.staff?.length || 0} assigned</strong></span>
                    </div>
                  </div>
                </div>

                <div className="flex justify-end space-x-2 border-t border-[#f5f5f5] mt-4 pt-3 shrink-0">
                  {hasPermission(PERMISSIONS.WAREHOUSES.WRITE) && (
                    <Button 
                      variant="ghost" 
                      onClick={() => handleOpenModal(wh)}
                      className="h-8 px-2 text-[#4a4a4a] hover:bg-[#f5f5f5]"
                    >
                      <Edit className="w-4 h-4 mr-1.5" /> Edit
                    </Button>
                  )}
                  {hasPermission(PERMISSIONS.WAREHOUSES.DELETE) && (
                    <Button 
                      variant="ghost" 
                      onClick={() => handleDeleteWarehouse(wh.id, wh.name)}
                      className="h-8 px-2 text-[#dc3545] hover:bg-[#fff0f0] hover:text-[#dc3545]"
                    >
                      <Trash2 className="w-4 h-4 mr-1.5" /> Delete
                    </Button>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Warehouse Modal */}
      {isModalOpen && selectedWarehouse && (
        <div className="fixed inset-0 bg-black/55 z-50 flex items-center justify-center animate-in fade-in duration-200">
          <div className="bg-white rounded-[8px] shadow-[0px_12px_28px_rgba(0,0,0,0.30)] w-full max-w-[640px] flex flex-col max-h-[90vh]">
            <div className="px-6 py-4 border-b border-[#e0e0e0] flex justify-between items-center bg-[#f8f8f8] shrink-0">
              <h2 className="text-[18px] font-[700] text-[#242424]">
                {selectedWarehouse.id ? 'Edit Warehouse Location' : 'New Warehouse Location'}
              </h2>
              <Button variant="ghost" size="icon" onClick={handleCloseModal} className="h-8 w-8 text-[#898989] hover:text-[#242424]">
                <X className="w-5 h-5" />
              </Button>
            </div>
            
            <div className="p-6 overflow-y-auto space-y-5">
              <div className="grid grid-cols-2 gap-5">
                <div>
                  <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Warehouse Name <span className="text-[#dc3545]">*</span></label>
                  <Input 
                    value={selectedWarehouse.name || ''}
                    onChange={e => setSelectedWarehouse({...selectedWarehouse, name: e.target.value})}
                    placeholder="e.g. Southern Distribution Hub"
                    className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                  />
                </div>
                <div>
                  <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Warehouse Code <span className="text-[#dc3545]">*</span></label>
                  <Input 
                    value={selectedWarehouse.code || ''}
                    disabled={!!selectedWarehouse.id}
                    onChange={e => setSelectedWarehouse({...selectedWarehouse, code: e.target.value})}
                    placeholder="e.g. WH-SUD"
                    className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] uppercase font-mono"
                  />
                </div>
              </div>
              
              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Address</label>
                <AddressInput 
                  value={selectedWarehouse.address || ''}
                  onChange={val => setSelectedWarehouse({...selectedWarehouse, address: val})}
                  placeholder="e.g. 123 Logistics Parkway, Binh Duong"
                  className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                />
              </div>

              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Description</label>
                <Textarea 
                  value={selectedWarehouse.description || ''}
                  onChange={e => setSelectedWarehouse({...selectedWarehouse, description: e.target.value})}
                  placeholder="Describe warehouse usage or access codes..."
                  className="border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                />
              </div>

              {/* Manager Dropdown */}
              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Warehouse Manager <span className="text-[#dc3545]">*</span></label>
                <select
                  value={selectedWarehouse.manager?.id || ''}
                  onChange={e => {
                    const matchedUser = orgMembers.find(m => m.id === e.target.value);
                    if (matchedUser) {
                      const managerObj = {
                        id: matchedUser.id,
                        firstName: matchedUser.firstName,
                        lastName: matchedUser.lastName,
                        email: matchedUser.email,
                      };
                      const currentStaff = selectedWarehouse.staff || [];
                      const hasStaff = currentStaff.some(s => s.id === matchedUser.id);
                      setSelectedWarehouse({
                        ...selectedWarehouse,
                        manager: managerObj,
                        staff: hasStaff ? currentStaff : [...currentStaff, managerObj]
                      });
                    } else {
                      setSelectedWarehouse({
                        ...selectedWarehouse,
                        manager: undefined
                      });
                    }
                  }}
                  className="w-full h-10 border border-[#d0d0d0] rounded-[4px] px-3 text-[13px] focus:outline-none focus:border-[#0066cc]"
                >
                  <option value="">-- Unassigned --</option>
                  {orgMembers.map(member => (
                    <option key={member.id} value={member.id}>
                      {member.firstName} {member.lastName} ({member.email})
                    </option>
                  ))}
                </select>
              </div>

              {/* Staff Multi-Select (Simple checkboxes) */}
              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1.5">Warehouse Operations Staff</label>
                <div className="mb-2 relative">
                  <Search className="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-[#898989]" />
                  <Input
                    placeholder="Search staff members..."
                    value={staffSearchQuery}
                    onChange={e => setStaffSearchQuery(e.target.value)}
                    className="h-8 pl-8 text-[12px] border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                  />
                </div>
                <div className="border border-[#d0d0d0] rounded-[4px] p-3 max-h-[160px] overflow-y-auto space-y-2 bg-[#fafafa]">
                  {orgMembers.length === 0 ? (
                    <span className="text-[12px] text-[#898989]">No organization members found</span>
                  ) : (
                    (() => {
                      const filteredMembers = orgMembers.filter(m => {
                        const q = staffSearchQuery.toLowerCase();
                        return m.firstName.toLowerCase().includes(q) || 
                               m.lastName.toLowerCase().includes(q) || 
                               m.email.toLowerCase().includes(q);
                      });
                      if (filteredMembers.length === 0) {
                        return <span className="text-[12px] text-[#898989]">No members match search</span>;
                      }
                      return filteredMembers.map(member => {
                        const isChecked = selectedWarehouse.staff?.some(s => s.id === member.id) || false;
                        return (
                          <label key={member.id} className="flex items-center space-x-2 text-[13px] text-[#4a4a4a] cursor-pointer hover:text-[#242424]">
                            <input 
                              type="checkbox"
                              checked={isChecked}
                              onChange={e => {
                                const currentStaff = selectedWarehouse.staff || [];
                                if (e.target.checked) {
                                  const newStaffMember = {
                                    id: member.id,
                                    firstName: member.firstName,
                                    lastName: member.lastName,
                                    email: member.email,
                                  };
                                  setSelectedWarehouse({
                                    ...selectedWarehouse,
                                    staff: [...currentStaff, newStaffMember]
                                  });
                                } else {
                                  const newStaff = currentStaff.filter(s => s.id !== member.id);
                                  const isManager = selectedWarehouse.manager?.id === member.id;
                                  setSelectedWarehouse({
                                    ...selectedWarehouse,
                                    staff: newStaff,
                                    manager: isManager ? undefined : selectedWarehouse.manager
                                  });
                                }
                              }}
                              className="w-4 h-4 text-[#0066cc] focus:ring-[#0066cc] border-[#d0d0d0] rounded"
                            />
                            <span>{member.firstName} {member.lastName} <span className="text-[#898989] text-[11px]">({member.email})</span></span>
                          </label>
                        );
                      });
                    })()
                  )}
                </div>
              </div>
            </div>

            <div className="px-6 py-4 bg-[#f8f8f8] border-t border-[#e0e0e0] flex justify-end space-x-2 shrink-0">
              <Button 
                variant="outline" 
                onClick={handleCloseModal}
                className="bg-white border-[#d0d0d0] text-[#242424] h-10 px-4 rounded-[4px] font-[600]"
              >
                Discard
              </Button>
              <Button 
                onClick={handleSaveWarehouse}
                disabled={isSaving}
                className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px] font-[600]"
              >
                <Save className="w-4 h-4 mr-2" />
                {isSaving ? 'Saving...' : 'Save Warehouse'}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
