'use client';

import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Building2, ArrowRight } from 'lucide-react';

interface OrgCardProps {
  id: string;
  name: string;
  description?: string;
  role: string;
  onSelect: (orgId: string) => void;
}

export default function OrgCard({ id, name, description, role, onSelect }: OrgCardProps) {
  return (
    <Card 
      onClick={() => onSelect(id)}
      className="bg-[#f6f3f2] border-2 border-transparent shadow-none hover:shadow-lg hover:border-[#004e9f] transition-all duration-200 cursor-pointer h-full flex flex-col group"
    >
      <CardContent className="p-8 flex flex-col h-full">
        <div className="flex flex-col h-full">
          {/* Header with avatar and status */}
          <div className="flex items-start justify-between mb-6">
            <div className="w-14 h-14 bg-white rounded-full flex items-center justify-center shadow-sm">
              <Building2 className="h-7 w-7 text-[#004e9f]" />
            </div>
            <Badge className="bg-[#004e9f] text-white text-[10px] font-bold uppercase tracking-wider px-3 py-1">
              ACTIVE
            </Badge>
          </div>

          {/* Content */}
          <div className="flex-1 mb-6">
            <h3 className="text-[24px] font-bold text-[#1b1c1c] mb-1 leading-tight group-hover:text-[#004e9f] transition-colors">
              {name}
            </h3>
            {description && (
              <p className="text-[14px] font-medium text-[#5f5e5e] leading-relaxed">
                {description}
              </p>
            )}
          </div>

          <div className="mt-auto">
            {/* Role section */}
            <div className="border-t border-[#c1c6d5]/10 pt-6">
              <div className="flex items-center justify-between">
                <div>
                  <div className="text-[10px] font-bold text-[#414753] uppercase tracking-[-0.5px] mb-1">
                    YOUR ROLE
                  </div>
                  <div className="text-[14px] font-semibold text-[#004e9f]">
                    {role}
                  </div>
                </div>
                <ArrowRight className="h-5 w-5 text-[#004e9f] opacity-50 group-hover:opacity-100 group-hover:translate-x-1 transition-all" />
              </div>
            </div>

            {/* Select button */}
            <Button
              onClick={(e) => { e.stopPropagation(); onSelect(id); }}
              className="w-full mt-6 bg-[#004e9f] hover:bg-[#003d7a] text-white font-semibold h-12 rounded-lg cursor-pointer transition-colors"
            >
              Select Organization
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
