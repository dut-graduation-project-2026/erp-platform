'use client';

/**
 * @file page.tsx (auth/login)
 * @description Route handler cho trang /login.
 * Page này chỉ có nhiệm vụ:
 * 1. Kết nối Hook (logic) với Component (UI)
 * 2. Xử lý auto-login qua URL params (cho testing/demo)
 *
 * Không chứa business logic, không chứa UI phức tạp.
 */

import { Suspense, useEffect } from 'react';
import { useSearchParams } from 'next/navigation';

import LoginForm from '@/features/auth/components/login/LoginForm';
import { useLogin } from '@/features/auth/hooks/useLogin';
import type { LoginFormValues } from '@/features/auth/types/auth.types';

// ─── LOADING SKELETON ─────────────────────────────────────────────────────────

const LoginSkeleton = () => (
  <div className="w-full max-w-sm animate-pulse rounded-[4px] border border-border bg-card p-6 shadow-sm">
    <div className="mb-4 h-4 w-24 rounded bg-muted" />
    <div className="mb-2 h-7 w-32 rounded bg-muted" />
    <div className="mb-6 h-4 w-full rounded bg-muted" />
    <div className="mb-4 space-y-2">
      <div className="h-4 w-16 rounded bg-muted" />
      <div className="h-10 w-full rounded bg-muted" />
    </div>
    <div className="mb-6 space-y-2">
      <div className="h-4 w-20 rounded bg-muted" />
      <div className="h-10 w-full rounded bg-muted" />
    </div>
    <div className="h-10 w-full rounded bg-muted" />
  </div>
);

// ─── MAIN CONTENT ─────────────────────────────────────────────────────────────

const LoginPageContent = () => {
  const { loading, serverError, handleLogin } = useLogin();
  const searchParams = useSearchParams();

  // Auto-login nếu URL có params: /login?email=...&password=...
  // Hữu ích cho môi trường testing và E2E tests
  useEffect(() => {
    const email = searchParams.get('email');
    const password = searchParams.get('password');

    if (email && password && !loading) {
      handleLogin({ email, password, rememberMe: false });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Chỉ chạy 1 lần khi mount

  const handleFormSubmit = async (values: LoginFormValues): Promise<void> => {
    await handleLogin(values);
  };

  return (
    <LoginForm
      onSubmit={handleFormSubmit}
      isSubmitting={loading}
      serverError={serverError}
    />
  );
};

// ─── PAGE EXPORT ──────────────────────────────────────────────────────────────
// Bọc trong Suspense vì useSearchParams() cần Suspense boundary trong Next.js

const LoginPage = () => {
  return (
    <Suspense fallback={<LoginSkeleton />}>
      <LoginPageContent />
    </Suspense>
  );
};

export default LoginPage;
