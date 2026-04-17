import { BaseMetadataEntity } from "@/types/base";

export interface User extends BaseMetadataEntity{
    id: string;
    firstName: string;
    lastName: string;
    email: string;
    avatarUrl?: string;
    role: string; // e.g., 'system_admin', 'org_user'
    isSystemAdmin?: boolean; // 🟡 BƯỚC 2: Flag để phân luồng đăng nhập
    organizations?: unknown[]; // Temporarily unknown
}
