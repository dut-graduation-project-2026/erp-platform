import React, { useState, useEffect } from 'react';
import { CrmLead, CreateCrmLeadRequest } from '../types';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { AddressInput } from '@/components/ui/address-input';
import { Textarea } from '@/components/ui/textarea';
import { Calendar, Clock, MessageSquare, Mail, Phone, Building, X } from 'lucide-react';
import { cn } from '@/lib/utils';
import { updateLead, createLead } from '../services/crmService';
import { useRouter } from 'next/navigation';
import { usePermissions } from '@/hooks/use-permissions';
import { getSaleTeams, getSaleTeamById } from '../services/crmService';
import { getPartners, createPartner } from '@/features/sales/services/salesService';
import { fetchUsersApi } from '@/features/organization/services/userService';
import { toast } from 'sonner';
import { PERMISSIONS } from '@/config/permissions';
import { APP_ROUTES } from '@/config/constants';
interface Props {
  lead: CrmLead | null;
  orgId: string;
  isNew?: boolean;
}

export function CrmLeadForm({ lead, orgId, isNew = false }: Props) {
  const router = useRouter();
  const { hasPermission } = usePermissions();

  const [formData, setFormData] = useState<Partial<CreateCrmLeadRequest>>({
    name: lead?.name || '',
    taxCode: (lead as any)?.taxCode || '',
    email: (lead as any)?.email || '',
    phone: (lead as any)?.phone || '',
    address: (lead as any)?.address || '',
    notes: (lead as any)?.notes || '',
    expectedRevenue: lead?.expectedRevenue || 0,
    probability: lead?.probability || 10,
    saleTeamId: lead?.salesTeam?.id || (lead as any)?.saleTeam?.id || '',
    salePersonId: lead?.salesperson?.id || (lead as any)?.salePerson?.id || '',
    partnerId: lead?.partner?.id || '',
  });

  const [saleTeams, setSaleTeams] = useState<any[]>([]);
  const [partners, setPartners] = useState<any[]>([]);
  const [users, setUsers] = useState<any[]>([]);
  const [isSaving, setIsSaving] = useState(false);
  const [isNewCustomerModalOpen, setIsNewCustomerModalOpen] = useState(false);
  const [newCustomer, setNewCustomer] = useState<any>({ name: '', type: 'INDIVIDUAL', code: '', email: '', phone: '', address: '', taxCode: '', contacts: [] });
  const [isSavingCustomer, setIsSavingCustomer] = useState(false);
  const [selectedTeamMembers, setSelectedTeamMembers] = useState<any[]>([]);

  useEffect(() => {
    // Load Sale Teams
    getSaleTeams(orgId, { limit: 100 })
      .then(res => setSaleTeams(res.data || []))
      .catch(err => console.error("Failed to load sale teams", err));

    // Load Partners
    getPartners(orgId, { limit: 100 })
      .then(res => setPartners(res.data || []))
      .catch(err => console.error("Failed to load partners", err));

    // Load Users (Salespersons)
    fetchUsersApi({ organizationId: orgId, limit: 100 })
      .then(res => setUsers(res.data || []))
      .catch(err => console.error("Failed to load users", err));

    // Load initial team members if a team is already selected
    if (formData.saleTeamId) {
      getSaleTeamById(orgId, formData.saleTeamId)
        .then(res => setSelectedTeamMembers(res.members || []))
        .catch(err => console.error("Failed to load initial team members", err));
    }
  }, [orgId]);

  // Chatter state
  const [notes, setNotes] = useState<{ id: string; author: string; text: string; time: string; color: string }[]>([
    { id: '1', author: 'Mitchell Admin', text: 'Had a great discovery call. They are highly interested in the Enterprise Licensing package for 500 users.', time: '2 hours ago', color: '#0066cc' },
    { id: '2', author: 'System', text: 'Stage changed from "New" to "Qualified"', time: 'Yesterday', color: '#e0e0e0' },
    { id: '3', author: 'Marc Demo', text: 'Email sent: "Introduction to TechCorp Enterprise Solutions"', time: 'Oct 12, 2026', color: '#e0e0e0' }
  ]);
  const [newNote, setNewNote] = useState('');
  const [isLogging, setIsLogging] = useState(false);

  const handleCreateCustomer = async () => {
    if (!newCustomer.name?.trim()) return alert('Customer name is required.');
    if (!newCustomer.code?.trim()) return alert('Customer code is required.');
    setIsSavingCustomer(true);
    try {
      const res = await createPartner(orgId, newCustomer);
      const updatedPartnersRes = await getPartners(orgId, { limit: 100 });
      const updatedList = updatedPartnersRes.data || [];
      setPartners(updatedList);
      setFormData(prev => ({ ...prev, partnerId: res.id }));
      setIsNewCustomerModalOpen(false);
      toast.success('Customer created and linked successfully!');
    } catch (err) {
      console.error(err);
      toast.error('Failed to create customer.');
    } finally {
      setIsSavingCustomer(false);
    }
  };

  const handleSave = async () => {
    if (!formData.name) return alert("Opportunity name is required.");
    if (!formData.saleTeamId) return alert("Sale Team is required.");
    setIsSaving(true);
    try {
      if (isNew) {
        const res = await createLead(orgId, formData as CreateCrmLeadRequest);
        alert("Lead created successfully!");
        router.push(APP_ROUTES.CRM.LEAD_DETAIL(orgId, res.id));
      } else if (lead?.id) {
        await updateLead(orgId, lead.id, formData as CreateCrmLeadRequest);
        alert("Lead updated successfully!");
      }
    } catch (e) {
      console.error(e);
      alert("Error saving lead.");
    } finally {
      setIsSaving(false);
    }
  };

  const handlePostNote = () => {
    if (!newNote.trim()) return;

    const note = {
      id: Date.now().toString(),
      author: 'You (Current User)',
      text: newNote,
      time: 'Just now',
      color: '#28a745'
    };

    setNotes([note, ...notes]);
    setNewNote('');
    setIsLogging(false);
  };

  return (
    <div className="h-full flex flex-col font-['Segoe_UI'] bg-white">
      <div className="flex justify-between items-center px-6 py-4 border-b border-[#e0e0e0] bg-[#f8f8f8]">
        <div>
          <h1 className="text-[24px] font-[700] text-[#242424] leading-tight">
            {lead ? lead.name : 'New Opportunity'}
          </h1>
          {lead?.partner?.name && (
            <div className="text-[14px] text-[#0066cc] flex items-center mt-1">
              <Building className="w-4 h-4 mr-1" /> {lead.partner.name}
            </div>
          )}
        </div>
        <div className="flex space-x-2">
          {!isNew && (
            <Button
              variant="outline"
              className="border-[#0066cc] text-[#0066cc] hover:bg-[#f0f4ff] h-10 px-4 rounded-[4px] font-[600]"
              onClick={() => {
                if (!formData.partnerId) {
                  toast.error('You must link a Customer/Partner to this lead and save before converting to an order.');
                  return;
                }
                router.push(`${APP_ROUTES.SALES.QUOTATION_NEW(orgId)}?leadId=${lead?.id}`);
              }}
            >
              Convert to Order
            </Button>
          )}
          <Button variant="outline" className="border-[#d0d0d0] text-[#242424] hover:bg-[#f8f8f8] h-10 px-4 rounded-[4px] font-[600]" onClick={() => router.push(APP_ROUTES.CRM.DASHBOARD(orgId))}>
            Cancel
          </Button>
          {(isNew ? hasPermission(PERMISSIONS.LEADS.CREATE) : hasPermission(PERMISSIONS.LEADS.WRITE)) && (
            <Button
              onClick={handleSave}
              disabled={isSaving}
              className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px] font-[600]"
            >
              {isSaving ? 'Saving...' : 'Save'}
            </Button>
          )}
        </div>
      </div>

      <div className="flex flex-1 overflow-hidden">
        <div className="w-3/5 overflow-y-auto p-6 border-r border-[#e0e0e0]">
          <div className="max-w-2xl space-y-6">

            <div>
              <label className="block text-[14px] font-[600] text-[#242424] mb-1">Opportunity Name *</label>
              <Input
                value={formData.name || ''}
                onChange={e => setFormData({ ...formData, name: e.target.value })}
                className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] focus-visible:border-2"
              />
            </div>

            <div className="grid grid-cols-2 gap-6">
              <div>
                <label className="block text-[14px] font-[600] text-[#242424] mb-1">Sale Team *</label>
                <select
                  value={formData.saleTeamId || ''}
                  onChange={async (e) => {
                    const newTeamId = e.target.value;
                    if (!newTeamId) {
                      setSelectedTeamMembers([]);
                      setFormData({ ...formData, saleTeamId: '', salePersonId: '' });
                      return;
                    }
                    try {
                      const teamInfo = await getSaleTeamById(orgId, newTeamId);
                      const members = teamInfo.members || [];
                      setSelectedTeamMembers(members);
                      const isMember = members.some((m: any) => m.id === formData.salePersonId);
                      setFormData({
                        ...formData,
                        saleTeamId: newTeamId,
                        salePersonId: isMember ? formData.salePersonId : ''
                      });
                    } catch (err) {
                      console.error("Failed to fetch team members", err);
                    }
                  }}
                  className="h-10 w-full border border-[#d0d0d0] rounded-[4px] px-3 bg-white focus-visible:ring-0 focus-visible:border-[#0066cc] focus-visible:border-2 text-[14px]"
                >
                  <option value="">Select Sale Team...</option>
                  {saleTeams.map(team => (
                    <option key={team.id} value={team.id}>{team.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-[14px] font-[600] text-[#242424] mb-1">Salesperson</label>
                <select
                  value={formData.salePersonId || ''}
                  onChange={e => setFormData({ ...formData, salePersonId: e.target.value || undefined })}
                  className="h-10 w-full border border-[#d0d0d0] rounded-[4px] px-3 bg-white focus-visible:ring-0 focus-visible:border-[#0066cc] focus-visible:border-2 text-[14px]"
                >
                  <option value="">Select Salesperson...</option>
                  {selectedTeamMembers.map((u: any) => (
                    <option key={u.id} value={u.id}>{u.firstName} {u.lastName} ({u.email})</option>
                  ))}
                </select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-6">
              <div>
                <label className="block text-[14px] font-[600] text-[#242424] mb-1">Expected Revenue ($) *</label>
                <Input
                  type="number"
                  value={formData.expectedRevenue || 0}
                  onChange={e => setFormData({ ...formData, expectedRevenue: parseFloat(e.target.value) || 0 })}
                  className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] focus-visible:border-2 font-mono"
                />
              </div>
              <div>
                <label className="block text-[14px] font-[600] text-[#242424] mb-1">Probability (%) *</label>
                <Input
                  type="number"
                  value={formData.probability || 0}
                  onChange={e => setFormData({ ...formData, probability: parseFloat(e.target.value) || 0 })}
                  className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] focus-visible:border-2 font-mono"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-6">
              <div>
                <div className="flex justify-between items-center mb-1">
                  <label className="block text-[14px] font-[600] text-[#242424]">Customer / Partner</label>
                  <button
                    type="button"
                    onClick={() => {
                      setNewCustomer({ name: '', type: 'INDIVIDUAL', code: `CUST-${Date.now().toString().slice(-6)}`, email: '', phone: '', address: '', taxCode: '', contacts: [] });
                      setIsNewCustomerModalOpen(true);
                    }}
                    className="text-[12px] text-[#0066cc] hover:underline font-[600]"
                  >
                    + Create new customer
                  </button>
                </div>
                <select
                  value={formData.partnerId || ''}
                  onChange={e => setFormData({ ...formData, partnerId: e.target.value || undefined })}
                  className="h-10 w-full border border-[#d0d0d0] rounded-[4px] px-3 bg-white focus-visible:ring-0 focus-visible:border-[#0066cc] focus-visible:border-2 text-[14px]"
                >
                  <option value="">Select Partner...</option>
                  {partners.map(p => (
                    <option key={p.id} value={p.id}>{p.name}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-[14px] font-[600] text-[#242424] mb-1">Tax Code</label>
                <Input
                  value={formData.taxCode || ''}
                  onChange={e => setFormData({ ...formData, taxCode: e.target.value })}
                  className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] focus-visible:border-2"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-6">
              <div>
                <label className="block text-[14px] font-[600] text-[#242424] mb-1">Email</label>
                <Input
                  type="email"
                  value={formData.email || ''}
                  onChange={e => setFormData({ ...formData, email: e.target.value })}
                  className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] focus-visible:border-2"
                />
              </div>
              <div>
                <label className="block text-[14px] font-[600] text-[#242424] mb-1">Phone</label>
                <Input
                  value={formData.phone || ''}
                  onChange={e => setFormData({ ...formData, phone: e.target.value })}
                  className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] focus-visible:border-2"
                />
              </div>
            </div>

            <div>
              <label className="block text-[14px] font-[600] text-[#242424] mb-1">Address</label>
              <AddressInput
                value={formData.address || ''}
                onChange={val => setFormData({ ...formData, address: val })}
                className="h-10 border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] focus-visible:border-2"
              />
            </div>

            <div>
              <label className="block text-[14px] font-[600] text-[#242424] mb-1">Internal Notes</label>
              <Textarea
                placeholder="Add your notes here..."
                value={formData.notes || ''}
                onChange={e => setFormData({ ...formData, notes: e.target.value })}
                className="min-h-[120px] border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc] focus-visible:border-2 resize-y"
              />
              <span className="text-[12px] text-[#898989] mt-1 block">These notes are only visible to internal employees.</span>
            </div>
          </div>
        </div>

        <div className="w-2/5 flex flex-col bg-[#f8f8f8]">
          <div className="p-4 border-b border-[#e0e0e0] flex items-center space-x-2 bg-white shrink-0">
            <Button
              variant="ghost"
              className={cn("h-8 px-3 text-[13px] font-[600]", isLogging ? "bg-[#0066cc] text-white hover:bg-[#004499] hover:text-white" : "text-[#0066cc] hover:bg-[#f0f4ff]")}
              onClick={() => setIsLogging(!isLogging)}
            >
              <MessageSquare className="w-4 h-4 mr-2" /> Log Note
            </Button>
            <Button variant="ghost" className="text-[#898989] hover:text-[#242424] h-8 px-3 text-[13px]">
              <Mail className="w-4 h-4 mr-2" /> Send Email
            </Button>
            <Button variant="ghost" className="text-[#898989] hover:text-[#242424] h-8 px-3 text-[13px]">
              <Phone className="w-4 h-4 mr-2" /> Call
            </Button>
          </div>

          {isLogging && (
            <div className="p-4 bg-white border-b border-[#e0e0e0]">
              <Textarea
                autoFocus
                placeholder="Log a note..."
                value={newNote}
                onChange={e => setNewNote(e.target.value)}
                className="min-h-[80px] text-[13px] mb-2"
              />
              <div className="flex justify-end space-x-2">
                <Button variant="outline" size="sm" onClick={() => setIsLogging(false)}>Cancel</Button>
                <Button className="bg-[#0066cc] text-white" size="sm" onClick={handlePostNote}>Log</Button>
              </div>
            </div>
          )}

          <div className="flex-1 overflow-y-auto p-6 space-y-6">
            {notes.map(note => (
              <div key={note.id} className="relative pl-6 border-l-2 border-[#e0e0e0]">
                <div
                  className="absolute w-3 h-3 rounded-full -left-[7px] top-1 border-2 border-[#f8f8f8]"
                  style={{ backgroundColor: note.color }}
                ></div>
                <div className="flex justify-between items-baseline mb-1">
                  <span className="font-[600] text-[13px] text-[#242424]">{note.author}</span>
                  <span className="text-[12px] text-[#898989]">{note.time}</span>
                </div>
                {note.author === 'System' ? (
                  <p className="text-[13px] text-[#898989] italic">
                    {note.text}
                  </p>
                ) : (
                  <p className="text-[13px] text-[#242424] bg-white p-3 border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] whitespace-pre-wrap">
                    {note.text}
                  </p>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Inline Customer Creation Modal */}
      {isNewCustomerModalOpen && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center">
          <div className="bg-white rounded-[8px] shadow-[0px_12px_28px_rgba(0,0,0,0.30)] w-full max-w-[500px] overflow-hidden">
            <div className="px-6 py-4 border-b border-[#e0e0e0] flex justify-between items-center bg-[#f8f8f8]">
              <h2 className="text-[18px] font-[700] text-[#242424]">New Customer / Partner</h2>
              <Button variant="ghost" size="icon" onClick={() => setIsNewCustomerModalOpen(false)} className="h-8 w-8 text-[#898989] hover:text-[#242424]">
                <X className="w-5 h-5" />
              </Button>
            </div>

            <div className="p-6 space-y-4 max-h-[70vh] overflow-y-auto">
              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1">Name *</label>
                <Input
                  value={newCustomer.name}
                  onChange={e => setNewCustomer({ ...newCustomer, name: e.target.value })}
                  placeholder="e.g. John Doe"
                  className="h-9"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-[13px] font-[600] text-[#242424] mb-1">Code *</label>
                  <Input
                    value={newCustomer.code}
                    onChange={e => setNewCustomer({ ...newCustomer, code: e.target.value })}
                    className="h-9 uppercase font-mono"
                  />
                </div>
                <div>
                  <label className="block text-[13px] font-[600] text-[#242424] mb-1">Type *</label>
                  <select
                    value={newCustomer.type}
                    onChange={e => setNewCustomer({ ...newCustomer, type: e.target.value })}
                    className="h-9 w-full border border-[#d0d0d0] rounded-[4px] px-3 bg-white text-[13px]"
                  >
                    <option value="INDIVIDUAL">Individual</option>
                    <option value="COMPANY">Company</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-[13px] font-[600] text-[#242424] mb-1">Email</label>
                  <Input
                    type="email"
                    value={newCustomer.email}
                    onChange={e => setNewCustomer({ ...newCustomer, email: e.target.value })}
                    placeholder="e.g. contact@example.com"
                    className="h-9"
                  />
                </div>
                <div>
                  <label className="block text-[13px] font-[600] text-[#242424] mb-1">Phone</label>
                  <Input
                    value={newCustomer.phone}
                    onChange={e => setNewCustomer({ ...newCustomer, phone: e.target.value })}
                    placeholder="e.g. +123456789"
                    className="h-9"
                  />
                </div>
              </div>

              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1">Address</label>
                <AddressInput
                  value={newCustomer.address}
                  onChange={val => setNewCustomer({ ...newCustomer, address: val })}
                  placeholder="e.g. 123 Main St"
                  className="h-9"
                />
              </div>

              <div>
                <label className="block text-[13px] font-[600] text-[#242424] mb-1">Tax Code</label>
                <Input
                  value={newCustomer.taxCode}
                  onChange={e => setNewCustomer({ ...newCustomer, taxCode: e.target.value })}
                  className="h-9"
                />
              </div>
            </div>

            <div className="px-6 py-4 bg-[#f8f8f8] border-t border-[#e0e0e0] flex justify-end space-x-2">
              <Button
                variant="outline"
                onClick={() => setIsNewCustomerModalOpen(false)}
                className="h-9 px-4 rounded-[4px]"
              >
                Cancel
              </Button>
              <Button
                className="bg-[#0066cc] hover:bg-[#004499] text-white h-9 px-4 rounded-[4px]"
                disabled={isSavingCustomer}
                onClick={handleCreateCustomer}
              >
                {isSavingCustomer ? 'Saving...' : 'Save'}
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
