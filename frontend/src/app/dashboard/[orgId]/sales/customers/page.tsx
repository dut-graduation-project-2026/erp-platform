'use client';

import React, { useEffect, useState, use } from 'react';
import { getPartners } from '@/features/sales/services/salesService';
import { SalePartner } from '@/features/sales/types';
import { Button } from '@/components/ui/button';
import { Plus, Search, Building2, Phone, Mail } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Input } from '@/components/ui/input';

export default function CustomersListPage({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = use(params);
  const [customers, setCustomers] = useState<SalePartner[]>([]);
  const [filteredCustomers, setFilteredCustomers] = useState<SalePartner[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    getPartners(orgId)
      .then(res => {
        setCustomers(res.data || []);
        setFilteredCustomers(res.data || []);
      })
      .catch(console.error)
      .finally(() => setIsLoading(false));
  }, [orgId]);

  useEffect(() => {
    if (searchQuery.trim() === '') {
      setFilteredCustomers(customers);
    } else {
      const q = searchQuery.toLowerCase();
      setFilteredCustomers(customers.filter(c => 
        (c.name && c.name.toLowerCase().includes(q)) || 
        (c.email && c.email.toLowerCase().includes(q))
      ));
    }
  }, [searchQuery, customers]);

  return (
    <div className="p-6 h-full flex flex-col font-['Segoe_UI'] bg-white">
      <div className="flex justify-between items-center mb-6 shrink-0">
         <div>
            <h1 className="text-[24px] font-[600] text-[#242424] mb-1">Customers</h1>
            <span className="text-[14px] text-[#898989]">Manage your customers and partners</span>
         </div>
         <div className="flex space-x-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-[#898989]" />
              <Input 
                placeholder="Search customers..." 
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                className="pl-9 h-10 w-[250px] border-[#d0d0d0] rounded-[4px] focus-visible:ring-0 focus-visible:border-[#0066cc]" 
              />
            </div>
            <Button 
              className="bg-[#0066cc] hover:bg-[#004499] text-white h-10 px-4 rounded-[4px] font-[600]"
              onClick={() => alert('Customer creation modal would open here.')}
            >
              <Plus className="w-4 h-4 mr-2" /> New Customer
            </Button>
         </div>
      </div>

      <div className="flex-1 overflow-auto bg-[#f8f8f8] p-6 -mx-6 -mb-6 border-t border-[#e0e0e0]">
        {isLoading ? (
          <div className="flex justify-center items-center h-full text-[#898989]">Loading Customers...</div>
        ) : filteredCustomers.length === 0 ? (
          <div className="flex justify-center items-center h-full text-[#898989]">No customers found</div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {filteredCustomers.map((customer) => (
              <div 
                key={customer.id} 
                className="bg-white border border-[#e0e0e0] rounded-[4px] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] hover:shadow-[0px_4px_12px_rgba(0,0,0,0.15)] hover:border-[#0066cc] transition-all cursor-pointer p-4 flex flex-col"
              >
                 <div className="flex items-center space-x-3 mb-4">
                    <div className="w-12 h-12 bg-[#f0f4ff] rounded-full flex items-center justify-center text-[#0066cc] shrink-0">
                       <Building2 className="w-6 h-6" />
                    </div>
                    <div>
                       <h3 className="text-[15px] font-[600] text-[#242424] leading-tight">{customer.name}</h3>
                       <span className="text-[12px] text-[#898989] uppercase tracking-wide font-[600]">{customer.type || 'Customer'}</span>
                    </div>
                 </div>
                 
                 <div className="mt-auto space-y-2 text-[13px] text-[#242424]">
                    {customer.email && (
                      <div className="flex items-center">
                        <Mail className="w-3.5 h-3.5 mr-2 text-[#898989]" />
                        <span className="truncate">{customer.email}</span>
                      </div>
                    )}
                    {customer.phone && (
                      <div className="flex items-center">
                        <Phone className="w-3.5 h-3.5 mr-2 text-[#898989]" />
                        <span>{customer.phone}</span>
                      </div>
                    )}
                 </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
