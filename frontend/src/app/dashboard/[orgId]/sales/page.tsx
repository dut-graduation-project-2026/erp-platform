import { redirect } from 'next/navigation';
import { APP_ROUTES } from '@/config/constants';

export default async function SalesModuleIndex({ params }: { params: Promise<{ orgId: string }> }) {
  const { orgId } = await params;
  redirect(APP_ROUTES.SALES.QUOTATIONS(orgId));
}
