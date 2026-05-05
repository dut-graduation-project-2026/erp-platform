'use client';

import { useOrganizations } from '@/features/organization/hooks/useOrganizations';
import OrgCard from '@/features/organization/components/OrgCard';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Building2, Mail, ArrowRight } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/use-auth-store';

/**
 * SelectOrgPage - Figma Design 2:5212
 * Executive Architect ERP - Organization Gate
 *
 * Features:
 * - Header with branding and user info
 * - Gradient title "Select Your Organization"
 * - 3-column grid of organization cards
 * - Cards with avatar, status badge, role info
 * - Contact admin section for new memberships
 * - Footer with company values
 */
export default function SelectOrgPage() {
  const { organizations, loading, error } = useOrganizations();
  const { setCurrentOrgId } = useAuthStore();
  const router = useRouter();

  const handleSelectOrg = async (orgId: string) => {
    setCurrentOrgId(orgId);
    router.push(`/dashboard/${orgId}`);
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-[#fcf9f8] to-[#fcf9f8]">
        <div className="text-center">
          <Building2 className="h-12 w-12 text-[#004e9f] mx-auto mb-4" />
          <p className="text-[#414753] text-[18px] font-medium">Loading organizations...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-[#fcf9f8] to-[#fcf9f8]">
        <Card className="w-full max-w-md border-[#c1c6d5] shadow-[0px_1px_3px_rgba(0,0,0,0.12)]">
          <CardContent className="p-6">
            <p className="text-[#414753] mb-4">{error}</p>
            <Button onClick={() => window.location.reload()} className="bg-[#004e9f] hover:bg-[#003d7a] text-white">
              Try Again
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#fcf9f8] to-[#fcf9f8] relative">
      {/* Header */}
      <div className="backdrop-blur-sm bg-[#fcf9f8]/80 border-b border-[#c1c6d5]/30 sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-8 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <h1 className="text-[20px] font-bold text-[#1b1c1c]">Executive Architect ERP</h1>
              <div className="h-6 w-px bg-[#c1c6d5]/30" />
              <span className="text-[14px] font-medium text-[#5f5e5e]">Organization Gate</span>
            </div>
            <div className="flex items-center gap-4">
              <div className="text-right">
                <div className="text-[12px] font-bold text-[#1b1c1c]">Alexander Sterling</div>
                <div className="text-[10px] text-[#5f5e5e]">Chief Operations Officer</div>
              </div>
              <div className="w-9 h-9 bg-[#004e9f] rounded-full flex items-center justify-center">
                <span className="text-white text-sm font-bold">AS</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Notification Banner */}
      <div className="absolute left-8 bottom-64 z-10 max-w-sm">
        <Card className="border-l-4 border-[#af4900] bg-[#af4900] text-[#ffe3d6] shadow-lg">
          <CardContent className="p-4">
            <div className="flex items-start gap-3">
              <div className="w-6 h-6 text-[#ffe3d6] mt-0.5">⚠️</div>
              <div>
                <div className="text-[12px] font-bold uppercase tracking-wide mb-1">
                  ARCHITECTURE SUGGESTION
                </div>
                <div className="text-[11px] leading-relaxed">
                  Nexus Global has pending audit reports that require your immediate attention based on your Admin role.
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Main Content */}
      <div className="flex items-center justify-center min-h-[calc(100vh-200px)] px-24 py-24 relative z-0">
        <div className="max-w-6xl w-full">
          {/* Title Section */}
          <div className="text-center mb-16">
            <h1 className="text-[48px] font-black text-[#1b1c1c] mb-4">
              Select Your{' '}
              <span className="bg-gradient-to-r from-[#004e9f] to-[#06c] bg-clip-text text-transparent">
                Organization
              </span>
            </h1>
            <p className="text-[18px] font-medium text-[#414753] max-w-2xl mx-auto leading-relaxed">
              Welcome back. Access your designated operational workspace to continue managing your enterprise architecture and supply chain logistics.
            </p>
          </div>

          {/* Organizations Grid */}
          {organizations.length > 0 ? (
            <div className="grid grid-cols-3 gap-8 mb-16">
              {organizations.map((org) => (
                <OrgCard
                  key={org.id}
                  id={org.id}
                  name={org.name}
                  description={org.description}
                  hotline={org.hotline}
                  address={org.address}
                  role={org.role}
                  onSelect={handleSelectOrg}
                />
              ))}
            </div>
          ) : (
            <div className="mb-16">
              <Card className="border-2 border-dashed border-[#c1c6d5]/30 bg-[#fcf9f8]/50 p-12 text-center">
                <div className="max-w-md mx-auto">
                  <Building2 className="h-12 w-12 text-[#414753] mx-auto mb-4" />
                  <p className="text-[16px] font-medium text-[#414753] mb-6">
                    Don't see your organization listed? New memberships may take up to 24 hours to propagate.
                  </p>
                  <Button className="bg-[#e4e2e1] hover:bg-[#d0d0d0] text-[#004e9f] border-0">
                    <Mail className="h-4 w-4 mr-2" />
                    Contact System Admin
                  </Button>
                </div>
              </Card>
            </div>
          )}
        </div>
      </div>

      {/* Footer */}
      <div className="bg-white border-t border-[#c1c6d5]/30 px-16 py-10">
        <div className="max-w-6xl mx-auto flex items-center justify-between">
          <div>
            <div className="flex items-center gap-4 mb-2">
              <span className="text-[12px] font-medium text-[#5f5e5e] uppercase tracking-wider">PRECISION</span>
              <div className="w-1 h-1 bg-[#c1c6d5] rounded-full" />
              <span className="text-[12px] font-medium text-[#5f5e5e] uppercase tracking-wider">PERFORMANCE</span>
              <div className="w-1 h-1 bg-[#c1c6d5] rounded-full" />
              <span className="text-[12px] font-medium text-[#5f5e5e] uppercase tracking-wider">SOVEREIGNTY</span>
            </div>
            <p className="text-[10px] text-[#414753]">
              © 2024 Executive Architectural Standard. All rights reserved. Tier-1 ERP Security Protocol Active.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
