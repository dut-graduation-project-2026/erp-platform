'use client';

import { useParams } from 'next/navigation';
import Link from 'next/link';
import { useAuthStore } from '@/store/use-auth-store';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Settings, Users, Building2, Shield } from 'lucide-react';
import { PermissionGuard } from '@/components/rbac/PermissionGuard';

export default function DashboardPage() {
  const params = useParams();
  const orgId = params.orgId as string;
  const { organizations, currentOrgId } = useAuthStore();

  const currentOrg = organizations.find(org => org.id === orgId);

  if (!currentOrg || currentOrgId !== orgId) {
    return <div>Access denied</div>;
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Navigation Bar */}
      <nav className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <h1 className="text-xl font-semibold text-gray-900">
                {currentOrg.name} Dashboard
              </h1>
            </div>
            <div className="flex items-center space-x-4">
              <PermissionGuard permission="administration:read">
                <Link href="/administration">
                  <Button variant="outline" className="flex items-center gap-2">
                    <Settings className="w-4 h-4" />
                    Administration
                  </Button>
                </Link>
              </PermissionGuard>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <div className="p-6">
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
    </div>
  );
}
