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

// ─── 2. API RESPONSE TYPES ────────────────────────────────────────────────────

import type { User } from '@/types/user';

/**
 * Response trả về từ POST /api/v1/auth/login
 * Backend Spring Boot set HttpOnly Cookies cho token,
 * JSON body trả về đối tượng User (UserResponse DTO)
 */
export type LoginApiResponse = User;

// ─── 3. HOOK RETURN TYPE ──────────────────────────────────────────────────────

export interface UseLoginReturn {
  loading: boolean;
  serverError: string | null; // Lỗi từ API (credentials sai, network error, ...)
  handleLogin: (values: LoginFormValues) => Promise<void>;
}
