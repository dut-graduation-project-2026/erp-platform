import { apiClient } from "@/services/api-client";
import type { Organization } from "@/types/organization";

export const getOrganizations = async (): Promise<Organization[]> => {
  const response = await apiClient.get("/organizations");
  return response.data;
};

export const createOrganization = async (data: Omit<Organization, "id" | "createdAt" | "updatedAt">): Promise<Organization> => {
  const response = await apiClient.post("/organizations", data);
  return response.data;
};
