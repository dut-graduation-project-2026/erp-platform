'use client';

import { useParams } from 'next/navigation';
import Link from 'next/link';
import { useAuthStore } from '@/store/use-auth-store';
import { APP_MODULES } from '@/config/modules';
import { PermissionGuard } from '@/components/rbac/PermissionGuard';
import { useErpModules } from '@/features/organization/hooks/useErpModules';
import { Loader2 } from 'lucide-react';

import { usePermissions } from '@/hooks/use-permissions';

export default function AppLauncherPage() {
  const params = useParams();
  const orgId = params.orgId as string;
  const { organizations, currentOrgId } = useAuthStore();
  const { modules: backendModules, loading, error } = useErpModules(orgId);
  const { hasModuleAccess } = usePermissions();

  const currentOrg = organizations.find(org => org.id === orgId);

  if (!currentOrg || currentOrgId !== orgId) {
    return (
      <div className="flex h-[calc(100vh-4rem)] items-center justify-center bg-white">
        <p className="text-[#898989]">Access denied to this organization.</p>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="flex h-[calc(100vh-4rem)] items-center justify-center bg-white">
        <Loader2 className="h-8 w-8 animate-spin text-[#0066cc]" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-[calc(100vh-4rem)] items-center justify-center bg-white">
        <p className="text-[#dc3545]">{error}</p>
      </div>
    );
  }

  // Filter APP_MODULES config based on what backend returned and user's permissions
  const accessibleModules = APP_MODULES.filter(configModule => {
    // 1. Check if the module is seeded/enabled in the backend
    const isEnabled = backendModules.some(backendModule => {
      const backendCode = backendModule.code.toLowerCase().replace(/_/g, '-');
      const configCode = configModule.id.toLowerCase().replace(/_/g, '-');
      return backendCode === configCode;
    });
    if (!isEnabled) return false;

    // 2. Check if the user has permission to access this module
    return hasModuleAccess(configModule.id, configModule.permission);
  });

  console.log("Backend Modules:", backendModules);
  console.log("Accessible Config Modules:", accessibleModules);

  return (
    <div className="min-h-[calc(100vh-4rem)] bg-white p-8">
      {/* App Grid */}
      <div className="flex flex-wrap gap-[32px] justify-center max-w-5xl mx-auto mt-12">
        {accessibleModules.map((module) => {
          const Icon = module.icon;
          
          return (
            <PermissionGuard key={module.id} permission={module.permission}>
              <Link 
                href={`/dashboard/${orgId}${module.route}`}
                className="group flex flex-col items-center w-[120px] hover:translate-y-[-2px] transition-transform duration-200"
              >
                {/* App Icon Box */}
                <div 
                  className={`w-[80px] h-[80px] rounded-[16px] flex items-center justify-center text-white shadow-[0px_2px_8px_rgba(0,0,0,0.15)] group-hover:shadow-[0px_8px_20px_rgba(0,0,0,0.25)] transition-shadow duration-200 ${module.bgColor}`}
                >
                  <Icon className="w-[36px] h-[36px]" strokeWidth={1.5} />
                </div>
                
                {/* App Name */}
                <span className="mt-[16px] text-[14px] font-[500] text-[#242424] text-center leading-[1.4] group-hover:text-[#0066cc] transition-colors">
                  {module.name}
                </span>
              </Link>
            </PermissionGuard>
          );
        })}
      </div>
    </div>
  );
}
