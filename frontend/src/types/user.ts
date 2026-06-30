import { BaseMetadataEntity } from "@/types/base";
import type { UserOrganization } from "@/types/organization";

export interface User extends BaseMetadataEntity {
    id: string;
    firstName: string;
    lastName: string;
    email: string;
    avatarUrl?: string;
    organizations: UserOrganization[];
}
