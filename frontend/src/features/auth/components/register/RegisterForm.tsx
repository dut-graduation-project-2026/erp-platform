'use client';

/**
 * @file RegisterForm.tsx
 * @description Component UI hoàn chỉnh cho form đăng ký tài khoản.
 */

import { useState, useEffect } from 'react';
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

import { registerSchema, type RegisterFormValues } from '@/features/auth/types/auth.types';

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

interface RegisterFormProps {
  onSubmit: (values: RegisterFormValues) => Promise<void>;
  isSubmitting?: boolean;
  serverError?: string | null;
}

// ─── COMPONENT ────────────────────────────────────────────────────────────────

const RegisterForm = ({
  onSubmit,
  isSubmitting = false,
  serverError = null,
}: RegisterFormProps) => {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const searchParams = useSearchParams();
  const emailParam = searchParams.get('email') || '';
  const isEmailLocked = !!searchParams.get('email');

  // Khởi tạo react-hook-form với Zod resolver
  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<RegisterFormValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      firstName: '',
      lastName: '',
      email: emailParam,
      password: '',
      confirmPassword: '',
      acceptTerms: false,
    },
    mode: 'onTouched', // Validate khi user blur khỏi field
  });

  useEffect(() => {
    if (emailParam) {
      setValue('email', emailParam);
    }
  }, [emailParam, setValue]);

  return (
    <Card className="rounded-[4px] border border-border bg-card py-0 shadow-[0px_1px_3px_rgba(0,0,0,0.12)] ring-0">
      {/* ── HEADER ── */}
      <CardHeader className="space-y-2 border-b border-border px-6 py-6">
        <p className="text-[12px] leading-[1.4] font-normal tracking-[0.1px] text-muted-foreground">
          ERP Platform
        </p>
        <CardTitle className="text-[24px] leading-[1.15] font-semibold tracking-normal text-foreground">
          Create account
        </CardTitle>
        <CardDescription className="text-[14px] leading-[1.5] font-normal text-muted-foreground">
          Create your ERP workspace account to get started.
        </CardDescription>
      </CardHeader>

      <CardContent className="px-6 py-6">
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
          id="register-form"
          className="space-y-4"
          onSubmit={handleSubmit(onSubmit)}
          noValidate
        >
          {/* First Name & Last Name Grid */}
          <div className="grid grid-cols-2 gap-4">
            {/* First Name */}
            <div className="space-y-1.5">
              <Label
                htmlFor="register-firstName"
                className="text-[14px] leading-[1.4] font-semibold text-foreground"
              >
                First name
              </Label>
              <Input
                id="register-firstName"
                type="text"
                placeholder="John"
                aria-invalid={!!errors.firstName}
                aria-describedby={errors.firstName ? 'register-firstName-error' : undefined}
                className={`h-10 rounded-[4px] border px-3 py-2 text-[14px] leading-[1.5] text-foreground placeholder:text-muted-foreground placeholder:opacity-60 focus-visible:ring-2 focus-visible:ring-ring/30 focus-visible:ring-offset-2 ${
                  errors.firstName
                    ? 'border-red-400 bg-red-50 focus-visible:border-red-500 focus-visible:ring-red-200'
                    : 'border-input bg-white focus-visible:border-ring'
                }`}
                disabled={isSubmitting}
                {...register('firstName')}
              />
              {errors.firstName && (
                <p
                  id="register-firstName-error"
                  role="alert"
                  className="flex items-center gap-1 text-[12px] text-red-600"
                >
                  <AlertCircleIcon />
                  {errors.firstName.message}
                </p>
              )}
            </div>

            {/* Last Name */}
            <div className="space-y-1.5">
              <Label
                htmlFor="register-lastName"
                className="text-[14px] leading-[1.4] font-semibold text-foreground"
              >
                Last name
              </Label>
              <Input
                id="register-lastName"
                type="text"
                placeholder="Doe"
                aria-invalid={!!errors.lastName}
                aria-describedby={errors.lastName ? 'register-lastName-error' : undefined}
                className={`h-10 rounded-[4px] border px-3 py-2 text-[14px] leading-[1.5] text-foreground placeholder:text-muted-foreground placeholder:opacity-60 focus-visible:ring-2 focus-visible:ring-ring/30 focus-visible:ring-offset-2 ${
                  errors.lastName
                    ? 'border-red-400 bg-red-50 focus-visible:border-red-500 focus-visible:ring-red-200'
                    : 'border-input bg-white focus-visible:border-ring'
                }`}
                disabled={isSubmitting}
                {...register('lastName')}
              />
              {errors.lastName && (
                <p
                  id="register-lastName-error"
                  role="alert"
                  className="flex items-center gap-1 text-[12px] text-red-600"
                >
                  <AlertCircleIcon />
                  {errors.lastName.message}
                </p>
              )}
            </div>
          </div>

          {/* Email Field */}
          <div className="space-y-1.5">
            <Label
              htmlFor="register-email"
              className="text-[14px] leading-[1.4] font-semibold text-foreground"
            >
              Email
            </Label>
            <Input
              id="register-email"
              type="email"
              autoComplete="email"
              placeholder="you@company.com"
              aria-invalid={!!errors.email}
              aria-describedby={errors.email ? 'register-email-error' : undefined}
              readOnly={isEmailLocked}
              className={`h-10 rounded-[4px] border px-3 py-2 text-[14px] leading-[1.5] text-foreground placeholder:text-muted-foreground placeholder:opacity-60 focus-visible:ring-2 focus-visible:ring-ring/30 focus-visible:ring-offset-2 ${
                isEmailLocked
                  ? 'bg-slate-100 border-slate-200 cursor-not-allowed text-slate-500'
                  : errors.email
                    ? 'border-red-400 bg-red-50 focus-visible:border-red-500 focus-visible:ring-red-200'
                    : 'border-input bg-white focus-visible:border-ring'
              }`}
              disabled={isSubmitting}
              {...register('email')}
            />
            {errors.email && (
              <p
                id="register-email-error"
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
            <Label
              htmlFor="register-password"
              className="text-[14px] leading-[1.4] font-semibold text-foreground"
            >
              Password
            </Label>
            <div className="relative">
              <Input
                id="register-password"
                type={showPassword ? 'text' : 'password'}
                autoComplete="new-password"
                placeholder="••••••••"
                aria-invalid={!!errors.password}
                aria-describedby={errors.password ? 'register-password-error' : undefined}
                className={`h-10 rounded-[4px] border px-3 py-2 pr-10 text-[14px] leading-[1.5] text-foreground placeholder:text-muted-foreground placeholder:opacity-60 focus-visible:ring-2 focus-visible:ring-ring/30 focus-visible:ring-offset-2 ${
                  errors.password
                    ? 'border-red-400 bg-red-50 focus-visible:border-red-500 focus-visible:ring-red-200'
                    : 'border-input bg-white focus-visible:border-ring'
                }`}
                disabled={isSubmitting}
                {...register('password')}
              />
              <button
                type="button"
                onClick={() => setShowPassword((prev) => !prev)}
                aria-label={showPassword ? 'Hide Password' : 'Show Password'}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground transition-colors hover:text-foreground focus:outline-none focus-visible:ring-2 focus-visible:ring-ring rounded-sm"
                tabIndex={-1}
              >
                {showPassword ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
            {errors.password && (
              <p
                id="register-password-error"
                role="alert"
                className="flex items-center gap-1 text-[12px] text-red-600"
              >
                <AlertCircleIcon />
                {errors.password.message}
              </p>
            )}
          </div>

          {/* Confirm Password Field */}
          <div className="space-y-1.5">
            <Label
              htmlFor="register-confirmPassword"
              className="text-[14px] leading-[1.4] font-semibold text-foreground"
            >
              Confirm password
            </Label>
            <div className="relative">
              <Input
                id="register-confirmPassword"
                type={showConfirmPassword ? 'text' : 'password'}
                autoComplete="new-password"
                placeholder="••••••••"
                aria-invalid={!!errors.confirmPassword}
                aria-describedby={errors.confirmPassword ? 'register-confirmPassword-error' : undefined}
                className={`h-10 rounded-[4px] border px-3 py-2 pr-10 text-[14px] leading-[1.5] text-foreground placeholder:text-muted-foreground placeholder:opacity-60 focus-visible:ring-2 focus-visible:ring-ring/30 focus-visible:ring-offset-2 ${
                  errors.confirmPassword
                    ? 'border-red-400 bg-red-50 focus-visible:border-red-500 focus-visible:ring-red-200'
                    : 'border-input bg-white focus-visible:border-ring'
                }`}
                disabled={isSubmitting}
                {...register('confirmPassword')}
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword((prev) => !prev)}
                aria-label={showConfirmPassword ? 'Hide Password' : 'Show Password'}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground transition-colors hover:text-foreground focus:outline-none focus-visible:ring-2 focus-visible:ring-ring rounded-sm"
                tabIndex={-1}
              >
                {showConfirmPassword ? <EyeOffIcon /> : <EyeIcon />}
              </button>
            </div>
            {errors.confirmPassword && (
              <p
                id="register-confirmPassword-error"
                role="alert"
                className="flex items-center gap-1 text-[12px] text-red-600"
              >
                <AlertCircleIcon />
                {errors.confirmPassword.message}
              </p>
            )}
          </div>

          {/* Accept Terms Checkbox */}
          <div className="space-y-1.5">
            <div className="flex items-start gap-2 pt-1">
              <Checkbox
                id="register-acceptTerms"
                className="h-4 w-4 mt-0.5 rounded-[2px] border-[#999999] data-[state=checked]:border-primary data-[state=checked]:bg-primary focus-visible:ring-ring/30"
                disabled={isSubmitting}
                onCheckedChange={(checked) =>
                  setValue('acceptTerms', checked === true, { shouldValidate: true })
                }
              />
              <Label
                htmlFor="register-acceptTerms"
                className="cursor-pointer text-[12px] leading-[1.4] font-normal tracking-[0.1px] text-muted-foreground"
              >
                I agree to the Terms and Privacy Policy
              </Label>
            </div>
            {errors.acceptTerms && (
              <p
                id="register-acceptTerms-error"
                role="alert"
                className="flex items-center gap-1 text-[12px] text-red-600"
              >
                <AlertCircleIcon />
                {errors.acceptTerms.message}
              </p>
            )}
          </div>

          {/* Submit Button */}
          <Button
            id="register-submit-btn"
            type="submit"
            className="mt-1 h-10 w-full rounded-[4px] bg-primary px-4 py-2 text-[14px] leading-none font-semibold text-primary-foreground transition-colors hover:bg-[#004499] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring disabled:cursor-not-allowed disabled:opacity-60"
            disabled={isSubmitting}
            aria-busy={isSubmitting}
          >
            {isSubmitting ? (
              <span className="flex items-center justify-center gap-2">
                <LoaderIcon />
                Registering...
              </span>
            ) : (
              'Create account'
            )}
          </Button>

          {/* Login Link */}
          <p className="pt-1 text-center text-[12px] leading-[1.4] text-muted-foreground">
            Already have an account?{' '}
            <Link
              href="/login"
              className="text-[#0099ff] underline underline-offset-2 hover:text-[#004499] focus:outline-none focus-visible:ring-2 focus-visible:ring-ring rounded"
            >
              Sign in
            </Link>
          </p>
        </form>
      </CardContent>
    </Card>
  );
};

export default RegisterForm;
export type { RegisterFormProps };
