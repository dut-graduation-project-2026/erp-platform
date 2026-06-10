import { redirect } from 'next/navigation';

export default async function SalesModuleIndex({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = await params;
  redirect(`/dashboard/${orgId}/sales/quotations`);
}
