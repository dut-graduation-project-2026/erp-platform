"use client";

import { useAuthStore } from "@/store/use-auth-store";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

export type SelectOrgFormValues = {
  orgId: string;
};

type SelectOrgFormProps = {
  onSubmit: (values: SelectOrgFormValues) => Promise<void>;
  isSubmitting?: boolean;
};

/**
 * SelectOrgForm Component
 * 🔵 BƯỚC 3: LỰA CHỌN TỔ CHỨC (UI)
 * 
 * Hiển thị danh sách các Organization mà user có thể access
 * User click vào 1 org sẽ trigger handleSelectOrg từ hook
 */
const SelectOrgForm = ({ onSubmit, isSubmitting = false }: SelectOrgFormProps) => {
  const { organizations } = useAuthStore();

  const handleSelectOrg = async (orgId: string) => {
    await onSubmit({ orgId });
  };

  // Nếu không có org nào, hiển thị message
  if (organizations.length === 0) {
    return (
      <Card className="rounded-[4px] border border-border bg-card py-0 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] ring-0">
        <CardHeader className="space-y-2 border-b border-border px-6 py-6">
          <p className="text-[12px] leading-[1.4] font-normal tracking-[0.1px] text-muted-foreground">
            ERP Platform
          </p>
          <CardTitle className="text-[24px] leading-[1.15] font-semibold tracking-normal text-foreground">
            Select Organization
          </CardTitle>
          <CardDescription className="text-[14px] leading-[1.5] font-normal text-muted-foreground">
            Choose the organization you want to work with.
          </CardDescription>
        </CardHeader>
        <CardContent className="px-6 py-6">
          <div className="text-center text-muted-foreground py-8">
            <p className="mb-4">No organizations available.</p>
            <p className="text-sm">Please contact support if you believe this is an error.</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="rounded-[4px] border border-border bg-card py-0 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] ring-0">
      <CardHeader className="space-y-2 border-b border-border px-6 py-6">
        <p className="text-[12px] leading-[1.4] font-normal tracking-[0.1px] text-muted-foreground">
          ERP Platform
        </p>
        <CardTitle className="text-[24px] leading-[1.15] font-semibold tracking-normal text-foreground">
          Select Organization
        </CardTitle>
        <CardDescription className="text-[14px] leading-[1.5] font-normal text-muted-foreground">
          Choose the organization you want to work with.
        </CardDescription>
      </CardHeader>

      <CardContent className="px-6 py-6">
        <div className="space-y-4">
          {organizations.map((org) => (
            <div
              key={org.id}
              className="flex items-center justify-between p-4 border border-border rounded-[4px] hover:bg-muted/50 transition-colors"
            >
              <div className="flex-1">
                <h3 className="text-[16px] font-semibold text-foreground">{org.name}</h3>
                <p className="text-[14px] text-muted-foreground mt-1">{org.description}</p>
                <div className="flex gap-4 mt-2">
                  <span className="inline-block px-2 py-1 text-[12px] bg-muted rounded text-muted-foreground">
                    Role: {org.role}
                  </span>
                  {org.permissions && org.permissions.length > 0 && (
                    <span className="inline-block px-2 py-1 text-[12px] bg-muted rounded text-muted-foreground">
                      Permissions: {org.permissions.length}
                    </span>
                  )}
                </div>
              </div>
              <Button
                onClick={() => handleSelectOrg(org.id)}
                disabled={isSubmitting}
                className="ml-4 flex-shrink-0"
                size="sm"
              >
                {isSubmitting ? 'Selecting...' : 'Select'}
              </Button>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
};

export default SelectOrgForm;
