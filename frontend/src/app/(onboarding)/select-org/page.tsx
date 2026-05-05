'use client';

import { useOrganizations } from '@/features/organization/hooks/useOrganizations';
import OrgCard from '@/features/organization/components/OrgCard';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Mail, Building2 } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/use-auth-store';

/**
 * SelectOrgPage
 * 🔵 BƯỚC 3: LỰA CHỌN TỔ CHỨC (PAGE)
 *
 * Quy trình:
 * - Load danh sách orgs từ API
 * - Hiển thị grid org cards
 * - Nếu không có org, show "Contact Admin"
 * - Select org → set currentOrgId, redirect dashboard
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
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <Building2 className="h-12 w-12 text-blue-600 mx-auto mb-4" />
          <p className="text-gray-600">Loading organizations...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle className="text-red-600">Error</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-4">{error}</p>
            <Button onClick={() => window.location.reload()}>
              Try Again
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (organizations.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Building2 className="h-5 w-5" />
              No Organizations Available
            </CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-gray-600 mb-4">
              You don't have access to any organizations yet.
              Please contact your administrator to get access.
            </p>
            <Button className="w-full">
              <Mail className="h-4 w-4 mr-2" />
              Contact Admin
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4">
      <div className="max-w-4xl mx-auto">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Select Organization
          </h1>
          <p className="text-gray-600">
            Choose an organization to continue to your dashboard
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
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
      </div>
    </div>
  );
}
