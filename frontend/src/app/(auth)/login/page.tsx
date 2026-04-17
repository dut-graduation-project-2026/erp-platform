"use client";

import LoginForm, { LoginFormValues } from "@/features/auth/components/auth/login/LoginForm";
import { useLogin } from "@/hooks/useAuth";

const LoginPage = () => {
  const { loading, handleLogin } = useLogin();

  const onSubmit = async ({ email, password }: LoginFormValues) => {
    await handleLogin(email, password);
  };

  return <LoginForm onSubmit={onSubmit} isSubmitting={loading} />;
};

export default LoginPage;
