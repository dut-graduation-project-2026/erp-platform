'use client';

import React, { useEffect, useState, use } from 'react';
import { getSaleTeams, createSaleTeam, updateSaleTeam, deleteSaleTeam, SaleTeamResponse } from '@/features/crm/services/crmService';
import { fetchUsersApi } from '@/features/organization/services/userService';
import { Button } from '@/components/ui/button';
import { Plus, Search, Edit2, Trash2 } from 'lucide-react';
import { usePermissions } from '@/hooks/use-permissions';
import { toast } from 'sonner';
import { Input } from '@/components/ui/input';
import { PERMISSIONS } from '@/config/permissions';

export default function SaleTeamsPage({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = use(params);
  const [teams, setTeams] = useState<SaleTeamResponse[]>([]);
  const [users, setUsers] = useState<{ id: string; firstName: string; lastName: string; email: string }[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  
  // Modal state
  const [showModal, setShowModal] = useState(false);
  const [editingTeam, setEditingTeam] = useState<SaleTeamResponse | null>(null);
  const [formData, setFormData] = useState({ name: '', leaderId: '', memberIds: [] as string[] });
  const [isSaving, setIsSaving] = useState(false);

  const { hasPermission } = usePermissions();
  const canWrite = hasPermission(PERMISSIONS.SALE_TEAMS.WRITE) || hasPermission(PERMISSIONS.SALE_TEAMS.CREATE);

  const fetchTeams = async () => {
    try {
      const res = await getSaleTeams(orgId, { limit: 100 });
      setTeams(res.data || []);
    } catch (err) {
      console.error(err);
      toast.error('Failed to load sale teams');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchTeams();
    fetchUsersApi({ organizationId: orgId, limit: 100 })
      .then(res => setUsers(res.data || []))
      .catch(console.error);
  }, [orgId]);

  const handleOpenModal = (team?: SaleTeamResponse) => {
    if (team) {
      setEditingTeam(team);
      setFormData({ 
        name: team.name, 
        leaderId: team.leader?.id || '', 
        memberIds: team.members?.map(m => m.id) || [] 
      });
    } else {
      setEditingTeam(null);
      setFormData({ name: '', leaderId: '', memberIds: [] });
    }
    setShowModal(true);
  };

  const handleSave = async () => {
    if (!formData.name.trim()) return toast.error('Team name is required');
    if (!formData.leaderId) return toast.error('Team leader is required');

    setIsSaving(true);
    try {
      if (editingTeam) {
        await updateSaleTeam(orgId, editingTeam.id, formData);
        toast.success('Sale team updated successfully');
      } else {
        await createSaleTeam(orgId, formData);
        toast.success('Sale team created successfully');
      }
      setShowModal(false);
      fetchTeams();
    } catch (err: any) {
      // api client handles error toast
    } finally {
      setIsSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('Are you sure you want to delete this team?')) return;
    try {
      await deleteSaleTeam(orgId, id);
      toast.success('Team deleted successfully');
      fetchTeams();
    } catch (err) {
      // handled
    }
  };

  const filteredTeams = teams.filter(t => t.name.toLowerCase().includes(searchQuery.toLowerCase()));

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white">
      <div className="flex justify-between items-center mb-6 shrink-0">
         <div>
            <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Sales Teams</h1>
            <span className="text-[14px] text-[#898989]">Manage your sales channels and their members</span>
         </div>
         <div className="flex space-x-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
              <Input 
                placeholder="Search teams..." 
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
                <Plus className="w-4 h-4 mr-2" /> New Team
              </Button>
            )}
         </div>
      </div>

      <div className="flex-1 min-h-0 border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] overflow-hidden flex flex-col">
         <div className="flex-1 overflow-auto">
            <table className="w-full text-left border-collapse">
              <thead className="bg-[#f8f8f8] sticky top-0 z-10 shadow-[0px_1px_0px_#e0e0e0]">
                <tr>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[250px]">Team Name</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0] w-[200px]">Team Leader</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] border-r border-[#e0e0e0]">Members</th>
                  <th className="px-4 py-3 text-[13px] font-[600] text-[#242424] w-[100px] text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr><td colSpan={4} className="p-4 text-center text-[#898989]">Loading...</td></tr>
                ) : filteredTeams.length === 0 ? (
                  <tr><td colSpan={4} className="p-4 text-center text-[#898989]">No teams found</td></tr>
                ) : (
                  filteredTeams.map((team) => (
                    <tr key={team.id} className="border-b border-[#e0e0e0] hover:bg-[#f8f8f8] bg-white">
                      <td className="px-4 py-3 text-[13px] text-[#242424] font-[600] border-r border-[#e0e0e0]">{team.name}</td>
                      <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0]">
                        {team.leader ? `${team.leader.firstName} ${team.leader.lastName}` : '—'}
                      </td>
                      <td className="px-4 py-3 text-[13px] text-[#242424] border-r border-[#e0e0e0]">
                        {team.members?.length ? team.members.map(m => `${m.firstName} ${m.lastName}`).join(', ') : '—'}
                      </td>
                      <td className="px-4 py-3 text-right space-x-2">
                        {canWrite && (
                           <>
                             <button onClick={() => handleOpenModal(team)} className="text-[#0066cc] hover:text-[#004499]"><Edit2 className="w-4 h-4" /></button>
                             <button onClick={() => handleDelete(team.id)} className="text-red-500 hover:text-red-700"><Trash2 className="w-4 h-4" /></button>
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
                  {editingTeam ? 'Edit Sales Team' : 'New Sales Team'}
                </h2>
                
                <div className="space-y-4">
                  <div>
                    <label className="block text-[14px] font-[600] text-[#242424] mb-1">Team Name *</label>
                    <Input 
                      value={formData.name}
                      onChange={e => setFormData({...formData, name: e.target.value})}
                      placeholder="e.g. Europe Sales, Direct Sales..."
                      className="h-10 border-[#d0d0d0] focus-visible:ring-0 focus-visible:border-[#0066cc]"
                    />
                  </div>
                  <div>
                    <label className="block text-[14px] font-[600] text-[#242424] mb-1">Team Leader *</label>
                    <select
                      value={formData.leaderId}
                      onChange={e => setFormData({...formData, leaderId: e.target.value})}
                      className="h-10 w-full border border-[#d0d0d0] rounded-[4px] px-3 text-[14px] focus:outline-none focus:border-[#0066cc]"
                    >
                      <option value="">Select a leader...</option>
                      {users.map(u => <option key={u.id} value={u.id}>{u.firstName} {u.lastName}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="block text-[14px] font-[600] text-[#242424] mb-1">Members</label>
                    <div className="border border-[#d0d0d0] rounded-[4px] p-2 max-h-[150px] overflow-y-auto space-y-1">
                      {users.map(u => (
                        <label key={u.id} className="flex items-center space-x-2 p-1 hover:bg-[#f8f8f8] rounded cursor-pointer">
                          <input 
                            type="checkbox" 
                            checked={formData.memberIds.includes(u.id)}
                            onChange={(e) => {
                              if (e.target.checked) {
                                setFormData({...formData, memberIds: [...formData.memberIds, u.id]});
                              } else {
                                setFormData({...formData, memberIds: formData.memberIds.filter(id => id !== u.id)});
                              }
                            }}
                            className="text-[#0066cc] rounded border-[#d0d0d0] focus:ring-[#0066cc]"
                          />
                          <span className="text-[13px]">{u.firstName} {u.lastName}</span>
                        </label>
                      ))}
                    </div>
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
