import { redirect } from 'next/navigation';

export default async function InventoryModuleIndex({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = await params;
  redirect(`/dashboard/${orgId}/inventory/documents`);
}
