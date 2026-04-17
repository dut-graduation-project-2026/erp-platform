'use client';

import { useEffect, useState } from 'react';
import { useAuthStore } from '@/store/use-auth-store';
import { useRouter } from 'next/navigation';
import { useToast } from '@/hooks/useToast';
import { login as loginApi, getCurrentUser, getUserOrganizations } from '@/services/authService';

interface UseLoginReturn {
  loading: boolean;
  handleLogin: (email: string, password: string) => Promise<void>;
}

export const useLogin = (): UseLoginReturn => {
  const [loading, setLoading] = useState(false);
  const { setUser, setOrganizations } = useAuthStore();
  const { toastError } = useToast();
  const router = useRouter();

  const handleLogin = async (email: string, password: string) => {
    setLoading(true);

    try {
      await loginApi({ email, password });
      const user = await getCurrentUser();
      const organizations = await getUserOrganizations();
      setUser(user);
      setOrganizations(organizations);
      router.push('/select-org');
    } catch (err: unknown) {
      toastError(err, 'Sign in failed');
    } finally {
      setLoading(false);
    }
  };

  return { loading, handleLogin };
};

export const useRequireAuth = () => {
  const { user } = useAuthStore();
  const router = useRouter();

  useEffect(() => {
    if (!user) {
      router.push('/login');
    }
  }, [user, router]);

  return { isAuthenticated: !!user };
};
