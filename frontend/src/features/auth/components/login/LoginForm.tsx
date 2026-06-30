'use client';

/**
 * @file LoginForm.tsx
 * @description Component UI hoàn chỉnh cho form đăng nhập.
 *
 * Tính năng:
 * - Validation real-time với Zod + react-hook-form
 * - Hiển thị lỗi field-level (email/password không hợp lệ)
 * - Hiển thị lỗi server-side (sai credentials, network error)
 * - Show/hide password toggle
 * - Loading state với spinner
 * - Demo credentials cho môi trường dev
 * - Responsive, accessible (aria attributes)
 *
 * Nguyên tắc: Component này KHÔNG chứa business logic.
 * Toàn bộ logic nằm trong useLogin hook.
 */

import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import Link from 'next/link';
import { useSearchParams } from 'next/navigation';

import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';

import { loginSchema, type LoginFormValues } from '@/features/auth/types/auth.types';

// ─── ICONS (inline SVG để tránh dependency nặng) ─────────────────────────────

const EyeIcon = () => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
    className="h-4 w-4"
  >
    <path d="M2 12s3-7 10-7 10 7 10 7-3 7-10 7-10-7-10-7Z" />
    <circle cx="12" cy="12" r="3" />
  </svg>
);

const EyeOffIcon = () => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
    className="h-4 w-4"
  >
    <path d="M9.88 9.88a3 3 0 1 0 4.24 4.24" />
    <path d="M10.73 5.08A10.43 10.43 0 0 1 12 5c7 0 10 7 10 7a13.16 13.16 0 0 1-1.67 2.68" />
    <path d="M6.61 6.61A13.526 13.526 0 0 0 2 12s3 7 10 7a9.74 9.74 0 0 0 5.39-1.61" />
    <line x1="2" x2="22" y1="2" y2="22" />
  </svg>
);

const AlertCircleIcon = () => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
    className="h-4 w-4 shrink-0"
  >
    <circle cx="12" cy="12" r="10" />
    <line x1="12" x2="12" y1="8" y2="12" />
    <line x1="12" x2="12.01" y1="16" y2="16" />
  </svg>
);

const LoaderIcon = () => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    strokeWidth="2"
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden="true"
    className="h-4 w-4 animate-spin"
  >
    <path d="M21 12a9 9 0 1 1-6.219-8.56" />
  </svg>
);



// ─── PROPS ────────────────────────────────────────────────────────────────────

interface LoginFormProps {
  onSubmit: (values: LoginFormValues) => Promise<void>;
  isSubmitting?: boolean;
  serverError?: string | null;
}

// ─── COMPONENT ────────────────────────────────────────────────────────────────

const LoginForm = ({
  onSubmit,
  isSubmitting = false,
  serverError = null,
}: LoginFormProps) => {
  const [showPassword, setShowPassword] = useState(false);
  const searchParams = useSearchParams();
  const redirectParam = searchParams.get('redirect');
  const registerHref = redirectParam
    ? `/register?redirect=${encodeURIComponent(redirectParam)}`
    : '/register';

  // Khởi tạo react-hook-form với Zod resolver
  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: '',
      password: '',
      rememberMe: false,
    },
    mode: 'onTouched', // Validate khi user blur khỏi field
  });



  return (
    <Card className="rounded-[4px] border border-border bg-card py-0 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] ring-0">
      {/* ── HEADER ── */}
      <CardHeader className="space-y-2 border-b border-border px-6 py-6">
        <p className="text-[12px] leading-[1.4] font-normal tracking-[0.1px] text-muted-foreground">
          ERP Platform
        </p>
        <CardTitle className="text-[24px] leading-[1.15] font-semibold tracking-normal text-foreground">
          Login
        </CardTitle>
        <CardDescription className="text-[14px] leading-[1.5] font-normal text-muted-foreground">
          Enter your login credentials to access your workspace.
        </CardDescription>
      </CardHeader>

      <CardContent className="px-6 py-6">
        {/* ── DEMO CREDENTIALS (chỉ hiển thị trong dev) ── */}
        {/* {process.env.NODE_ENV === 'development' && (
          <div className="mb-4 rounded-[4px] border border-blue-200 bg-blue-50 p-3">
            <p className="mb-2 text-[11px] font-semibold uppercase tracking-wider text-blue-600">
              Demo Accounts
            </p>
            <div className="flex flex-wrap gap-1.5">
              {DEMO_CREDENTIALS.map((cred) => (
                <button
                  key={cred.email}
                  type="button"
                  onClick={() => fillDemoCredentials(cred)}
                  className="inline-flex items-center gap-1 rounded-[3px] border border-blue-200 bg-white px-2 py-1 text-[11px] font-medium text-blue-700 transition-colors hover:bg-blue-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-400"
                  title={`${cred.email} / ${cred.password}`}
                >
                  <span>{cred.label}</span>
                  <span className="text-blue-400">({cred.role})</span>
                </button>
              ))}
            </div>
          </div>
        )} */}

        {/* ── SERVER ERROR BANNER ── */}
        {serverError && (
          <div
            role="alert"
            aria-live="assertive"
            className="mb-4 flex items-start gap-2 rounded-[4px] border border-red-200 bg-red-50 px-3 py-2.5"
          >
            <AlertCircleIcon />
            <p className="text-[13px] leading-[1.4] text-red-700">{serverError}</p>
          </div>
        )}

        {/* ── FORM ── */}
        <form
          id="login-form"
          className="space-y-4"
          onSubmit={handleSubmit(onSubmit)}
          noValidate // Tắt browser native validation, dùng Zod thay thế
        >
          {/* Email Field */}
          <div className="space-y-1.5">
            <Label
              htmlFor="login-email"
              className="text-[14px] leading-[1.4] font-semibold text-foreground"
            >
              Email
            </Label>
            <Input
              id="login-email"
              type="email"
              autoComplete="email"
              placeholder="you@company.com"
              aria-invalid={!!errors.email}
              aria-describedby={errors.email ? 'login-email-error' : undefined}
              className={`h-10 rounded-[4px] border px-3 py-2 text-[14px] leading-[1.5] text-foreground placeholder:text-muted-foreground placeholder:opacity-60 focus-visible:ring-2 focus-visible:ring-ring/30 focus-visible:ring-offset-2 ${
                errors.email
                  ? 'border-red-400 bg-red-50 focus-visible:border-red-500 focus-visible:ring-red-200'
                  : 'border-input bg-white focus-visible:border-ring'
              }`}
              disabled={isSubmitting}
              {...register('email')}
            />
            {errors.email && (
              <p
                id="login-email-error"
                role="alert"
                className="flex items-center gap-1 text-[12px] text-red-600"
              >
                <AlertCircleIcon />
                {errors.email.message}
              </p>
            )}
          </div>

          {/* Password Field */}
          <div className="space-y-1.5">
            <div className="flex items-center justify-between">
              <Label
                htmlFor="login-password"
                className="text-[14px] leading-[1.4] font-semibold text-foreground"
              >
                Password
              </Label>
              <Link
                href="/forgot-password"
                className="text-[12px] leading-[1.4] text-[#0099ff] underline underline-offset-2 hover:text-[#004499] focus:outline-none focus-visible:ring-2 focus-visible:ring-ring rounded"
              >
                Forgot password?
              </Link>
            </div>
            <div className="relative">
              <Input
                id="login-password"
                type={showPassword ? 'text' : 'password'}
                autoComplete="current-password"
                placeholder="••••••••"
                aria-invalid={!!errors.password}
                aria-describedby={errors.password ? 'login-password-error' : undefined}
                className={`h-10 rounded-[4px] border px-3 py-2 pr-10 text-[14px] leading-[1.5] text-foreground placeholder:text-muted-foreground placeholder:opacity-60 focus-visible:ring-2 focus-visible:ring-ring/30 focus-visible:ring-offset-2 ${
                  errors.password
                    ? 'border-red-400 bg-red-50 focus-visible:border-red-500 focus-visible:ring-red-200'
                    : 'border-input bg-white focus-visible:border-ring'
                }`}
                disabled={isSubmitting}
                {...register('password')}
              />
              {/* Toggle hiện/ẩn mật khẩu */}
              <button
                type="button"
                onClick={() => setShowPassword((prev) => !prev)}
                aria-label={showPassword ? 'Hide Password' : 'Show Password'}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground transition-colors hover:text-foreground focus:outline-none focus-visible:ring-2 focus-visible:ring-ring rounded-sm"
                tabIndex={-1} // Bỏ qua khi tab để không làm gián đoạn flow
              >
                {showPassword ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
            {errors.password && (
              <p
                id="login-password-error"
                role="alert"
                className="flex items-center gap-1 text-[12px] text-red-600"
              >
                <AlertCircleIcon />
                {errors.password.message}
              </p>
            )}
          </div>

          {/* Remember Me */}
          <div className="flex items-center gap-2">
            <Checkbox
              id="login-remember-me"
              className="h-4 w-4 rounded-[2px] border-[#999999] data-[state=checked]:border-primary data-[state=checked]:bg-primary focus-visible:ring-ring/30"
              disabled={isSubmitting}
              onCheckedChange={(checked) =>
                setValue('rememberMe', checked === true)
              }
            />
            <Label
              htmlFor="login-remember-me"
              className="cursor-pointer text-[12px] leading-[1.4] font-normal tracking-[0.1px] text-muted-foreground"
            >
              Remember me
            </Label>
          </div>

          {/* Submit Button */}
          <Button
            id="login-submit-btn"
            type="submit"
            className="mt-1 h-10 w-full rounded-[4px] bg-primary px-4 py-2 text-[14px] leading-none font-semibold text-primary-foreground transition-colors hover:bg-[#004499] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring disabled:cursor-not-allowed disabled:opacity-60"
            disabled={isSubmitting}
            aria-busy={isSubmitting}
          >
            {isSubmitting ? (
              <span className="flex items-center justify-center gap-2">
                <LoaderIcon />
                Logging in...
              </span>
            ) : (
              'Login'
            )}
          </Button>

          {/* Register Link */}
          <p className="pt-1 text-center text-[12px] leading-[1.4] text-muted-foreground">
            Don&apos;t have an account?{' '}
            <Link
              href={registerHref}
              className="text-[#0099ff] underline underline-offset-2 hover:text-[#004499] focus:outline-none focus-visible:ring-2 focus-visible:ring-ring rounded"
            >
              Register now
            </Link>
          </p>
        </form>
      </CardContent>
    </Card>
  );
};

export default LoginForm;
export type { LoginFormProps };
