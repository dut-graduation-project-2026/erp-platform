import { BaseMetadataEntity } from "@/types/base";

export interface User extends BaseMetadataEntity{
    id: string;
    fullName: string;
    email: string;
}
