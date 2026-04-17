import { create } from "zustand";
import { persist } from "zustand/middleware";

import type { User } from "@/types/user";
import type { UserOrganization } from "@/types/organization";

export interface AuthStore {
  user: User | null;
  organizations: UserOrganization[];
  currentOrgId: string | null;
  setUser: (user: User) => void;
  setOrganizations: (organizations: UserOrganization[]) => void;
  setCurrentOrgId: (orgId: string | null) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthStore>()(
  persist(
    (set) => ({
      user: null,
      organizations: [],
      currentOrgId: null,
      setUser: (user) => set({ user }),
      setOrganizations: (organizations) => set({ organizations }),
      setCurrentOrgId: (currentOrgId) => set({ currentOrgId }),
      clearAuth: () => set({ user: null, organizations: [], currentOrgId: null }),
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        currentOrgId: state.currentOrgId,
      }),
    },
  ),
);
