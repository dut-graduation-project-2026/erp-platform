'use client';

import { useParams } from 'next/navigation';
import { useAuthStore } from '@/store/use-auth-store';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';

export default function DashboardPage() {
  const params = useParams();
  const orgId = params.orgId as string;
  const { organizations, currentOrgId } = useAuthStore();

  const currentOrg = organizations.find(org => org.id === orgId);

  if (!currentOrg || currentOrgId !== orgId) {
    return <div>Access denied</div>;
  }

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-6">Dashboard - {currentOrg.name}</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Sales</CardTitle>
          </CardHeader>
          <CardContent>
            <p>Overview of sales data</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Inventory</CardTitle>
          </CardHeader>
          <CardContent>
            <p>Inventory management</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>Blockchain</CardTitle>
          </CardHeader>
          <CardContent>
            <p>Blockchain transactions</p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
