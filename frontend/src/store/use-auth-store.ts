import { create } from "zustand";
import { persist } from "zustand/middleware";

import type { User } from "@/types/user";
import type { UserOrganization } from "@/types/organization";

export interface AuthStore {
  user: User | null;
  organizations: UserOrganization[];
  permissions: string[];
  currentOrgId: string | null;
  setUser: (user: User) => void;
  setOrganizations: (organizations: UserOrganization[]) => void;
  setPermissions: (permissions: string[]) => void;
  setCurrentOrgId: (orgId: string | null) => void;
  logout: () => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthStore>()(
  persist(
    (set) => ({
      user: null,
      organizations: [],
      permissions: [],
      currentOrgId: null,
      setUser: (user) => set({ user }),
      setOrganizations: (organizations) => set({ organizations }),
      setPermissions: (permissions) => set({ permissions }),
      setCurrentOrgId: (currentOrgId) => set({ currentOrgId }),
      logout: () => set({ user: null, organizations: [], permissions: [], currentOrgId: null }),
      clearAuth: () => set({ user: null, organizations: [], permissions: [], currentOrgId: null }),
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        user: state.user,
        permissions: state.permissions,
        organizations: state.organizations,
        currentOrgId: state.currentOrgId,
      }),
    },
  ),
);
