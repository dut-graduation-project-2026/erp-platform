'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/use-auth-store';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';

export default function SelectOrgPage() {
  const { organizations, setCurrentOrgId } = useAuthStore();
  const router = useRouter();

  useEffect(() => {
    if (organizations.length === 0) {
      // Redirect to contact admin or something
      return;
    }
  }, [organizations]);

  const handleSelectOrg = (orgId: string) => {
    setCurrentOrgId(orgId);
    router.push(`/${orgId}/dashboard`);
  };

  if (organizations.length === 0) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="w-full max-w-md">
          <CardHeader>
            <CardTitle>No Organizations</CardTitle>
            <CardDescription>
              You are not a member of any organization. Please contact your administrator or system mail to create a new organization.
            </CardDescription>
          </CardHeader>
        </Card>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="w-full max-w-2xl">
        <h1 className="text-2xl font-bold text-center mb-8">Select Organization</h1>
        <div className="grid gap-4">
          {organizations.map((org) => (
            <Card key={org.id} className="cursor-pointer hover:shadow-md transition-shadow">
              <CardHeader>
                <CardTitle>{org.name}</CardTitle>
                <CardDescription>{org.description}</CardDescription>
              </CardHeader>
              <CardContent>
                <Button onClick={() => handleSelectOrg(org.id)} className="w-full">
                  Select
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
}
