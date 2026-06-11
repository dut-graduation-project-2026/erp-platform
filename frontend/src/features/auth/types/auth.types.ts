/**
 * @file auth.types.ts
 * @description Định nghĩa các kiểu dữ liệu dành riêng cho feature Auth/Login.
 * Tách biệt khỏi global types để dễ maintain và mở rộng.
 */

import { z } from 'zod';

// ─── 1. ZOD VALIDATION SCHEMA ────────────────────────────────────────────────
// Định nghĩa schema validate ở đây để tái sử dụng giữa Form và Hook

export const loginSchema = z.object({
  email: z
    .string()
    .min(1, 'Email không được để trống')
    .email('Email không đúng định dạng (ví dụ: user@company.com)'),
  password: z
    .string()
    .min(1, 'Mật khẩu không được để trống')
    .min(6, 'Mật khẩu phải có ít nhất 6 ký tự'),
  rememberMe: z.boolean(),
});

// Explicit type (không dùng z.infer để tránh conflict với .default() ở Zod v4)
export type LoginFormValues = {
  email: string;
  password: string;
  rememberMe: boolean;
};

export const registerSchema = z.object({
  firstName: z
    .string()
    .min(1, 'Name cannot be empty'),
  lastName: z
    .string()
    .min(1, 'Surname cannot be empty'),
  email: z
    .string()
    .min(1, 'Email cannot be empty')
    .email('Email is invalid (example: user@company.com)'),
  password: z
    .string()
    .min(1, 'Password cannot be empty')
    .min(8, 'Password must be at least 8 characters long'),
  confirmPassword: z
    .string()
    .min(1, 'Confirm password cannot be empty'),
  acceptTerms: z.boolean().refine(val => val === true, {
    message: 'You must agree to the Terms and Privacy Policy'
  }),
}).refine((data) => data.password === data.confirmPassword, {
  message: 'Passwords do not match',
  path: ['confirmPassword'],
});

export type RegisterFormValues = z.infer<typeof registerSchema>;

// ─── 2. API RESPONSE TYPES ────────────────────────────────────────────────────

import type { User } from '@/types/user';

/**
 * Response trả về từ POST /api/v1/auth/login
 * Backend Spring Boot set HttpOnly Cookies cho token,
 * JSON body trả về đối tượng User (UserResponse DTO)
 */
export type LoginApiResponse = User;
export type RegisterApiResponse = User;

// ─── 3. HOOK RETURN TYPE ──────────────────────────────────────────────────────

export interface UseLoginReturn {
  loading: boolean;
  serverError: string | null; // Lỗi từ API (credentials sai, network error, ...)
  handleLogin: (values: LoginFormValues) => Promise<void>;
}

export interface UseRegisterReturn {
  loading: boolean;
  serverError: string | null;
  handleRegister: (values: RegisterFormValues) => Promise<void>;
}
