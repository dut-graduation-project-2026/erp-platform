'use client';

import { useEffect, useState } from 'react';
import { useAuth as useAuthContext } from '@/contexts/AuthContext';
import { useRouter } from 'next/navigation';
import { useToast } from '@/hooks/useToast';

interface UseLoginReturn {
  loading: boolean;
  handleLogin: (email: string, password: string) => Promise<void>;
}

export const useLogin = (): UseLoginReturn => {
  const [loading, setLoading] = useState(false);
  const { login } = useAuthContext();
  const { toastError } = useToast();
  const router = useRouter();

  const handleLogin = async (email: string, password: string) => {
    setLoading(true);

    try {
      await login(email, password);
      router.push('/dashboard');
    } catch (err: unknown) {
      toastError(err, 'Sign in failed');
    } finally {
      setLoading(false);
    }
  };

  return { loading, handleLogin };
};

export const useRequireAuth = () => {
  const { isAuthenticated, isLoading } = useAuthContext();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, isLoading, router]);

  return { isAuthenticated, isLoading };
};

export { useAuthContext as useAuth };
