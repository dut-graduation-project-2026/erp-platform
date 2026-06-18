"use client";

import { useEffect, use } from "react";
import { useRouter } from "next/navigation";

import { APP_ROUTES } from "@/config/constants";

export default function RolesRedirectPage({ params }: { params: Promise<{ orgId: string }> }) {
  const router = useRouter();
  const unwrappedParams = use(params);
  
  useEffect(() => {
    router.replace(APP_ROUTES.ADMINISTRATION.ROLES(unwrappedParams.orgId));
  }, [unwrappedParams.orgId, router]);

  return (
    <div className="flex items-center justify-center h-full p-8 text-[#898989]">
      Redirecting to Role Management...
    </div>
  );
}
