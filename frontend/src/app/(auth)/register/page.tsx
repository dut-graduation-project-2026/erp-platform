'use client';

import { Suspense } from 'react';
import RegisterForm from '@/features/auth/components/register/RegisterForm';
import { useRegister } from '@/features/auth/hooks/useRegister';
import type { RegisterFormValues } from '@/features/auth/types/auth.types';

const RegisterSkeleton = () => (
  <div className="w-full max-w-sm animate-pulse rounded-[4px] border border-border bg-card p-6 shadow-sm">
    <div className="mb-4 h-4 w-24 rounded bg-muted" />
    <div className="mb-2 h-7 w-32 rounded bg-muted" />
    <div className="mb-6 h-4 w-full rounded bg-muted" />
    <div className="mb-4 space-y-2">
      <div className="h-4 w-16 rounded bg-muted" />
      <div className="h-10 w-full rounded bg-muted" />
    </div>
    <div className="h-10 w-full rounded bg-muted" />
  </div>
);

const RegisterPageContent = () => {
  const { loading, serverError, handleRegister } = useRegister();

  const handleFormSubmit = async (values: RegisterFormValues): Promise<void> => {
    await handleRegister(values);
  };

  return (
    <RegisterForm
      onSubmit={handleFormSubmit}
      isSubmitting={loading}
      serverError={serverError}
    />
  );
};

const RegisterPage = () => {
  return (
    <Suspense fallback={<RegisterSkeleton />}>
      <RegisterPageContent />
    </Suspense>
  );
};

export default RegisterPage;
