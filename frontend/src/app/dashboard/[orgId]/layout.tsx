import { Header } from '@/components/shared/Header';

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="h-screen bg-slate-50 flex flex-col overflow-hidden">
      <Header />
      <main className="flex-1 overflow-auto min-h-0">
        {children}
      </main>
    </div>
  );
}
