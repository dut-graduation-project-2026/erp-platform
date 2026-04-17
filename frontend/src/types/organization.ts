export interface Organization {
  id: string;
  name: string;
  description?: string;
  logo?: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserOrganization extends Organization {
  role: string;
  permissions: string[];
}
