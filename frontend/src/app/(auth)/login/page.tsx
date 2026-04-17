"use client";

import { Suspense, useEffect } from "react";
import { useSearchParams } from "next/navigation";
import LoginForm, { LoginFormValues } from "@/features/auth/components/login/LoginForm";
import { useLogin } from "@/features/auth/hooks/useLogin";

const LoginPageContent = () => {
  const { loading, handleLogin } = useLogin();
  const searchParams = useSearchParams();

  useEffect(() => {
    const email = searchParams.get("email");
    const password = searchParams.get("password");

    if (email && password && !loading) {
      // Auto-login if email and password are in URL params
      handleLogin(email, password);
    }
  }, [searchParams, handleLogin, loading]);

  const onSubmit = async ({ email, password }: LoginFormValues) => {
    await handleLogin(email, password);
  };

  return <LoginForm onSubmit={onSubmit} isSubmitting={loading} />;
};

const LoginPage = () => {
  return (
    <Suspense fallback={<div className="flex items-center justify-center min-h-screen">Loading...</div>}>
      <LoginPageContent />
    </Suspense>
  );
};

export default LoginPage;
