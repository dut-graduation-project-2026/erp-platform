/**
 * @file authService.ts (feature/auth)
 * @description Tầng API dành riêng cho feature Auth.
 * Nguyên tắc: Tách biệt hoàn toàn khỏi UI. Khi backend ready,
 * chỉ cần sửa file này, KHÔNG cần sửa component hay hook.
 *
 * Tất cả request đều đi qua api-client.ts đã cấu hình:
 * - withCredentials: true  → Gửi/nhận HttpOnly Cookie tự động
 * - X-Org-Id interceptor   → Auto-attach org context
 * - 401 interceptor        → Auto-refresh token
 */

import { apiClient } from '@/services/api-client';
import { API_ENDPOINTS } from '@/config/constants';
import type { LoginFormValues, LoginApiResponse, RegisterFormValues, RegisterApiResponse } from '@/features/auth/types/auth.types';
import type { User } from '@/types/user';
import type { UserOrganization } from '@/types/organization';

// ─── 1. LOGIN ─────────────────────────────────────────────────────────────────

/**
 * Gọi API đăng nhập.
 *
 * ✅ Production: Backend nhận email+password, xác thực, rồi:
 *   - Set HttpOnly Cookie: access_token, refresh_token (không bị JS đọc được)
 *   - Trả về JSON body: { user: {...} }
 *
 * ⚠️  Quan trọng: Token KHÔNG được lưu trong localStorage/sessionStorage
 *    vì nguy cơ XSS. Toàn bộ lifecycle token do backend quản lý qua cookie.
 */
export const loginApi = async (
  payload: Pick<LoginFormValues, 'email' | 'password'>
): Promise<LoginApiResponse> => {
  const response = await apiClient.post<LoginApiResponse>(
    API_ENDPOINTS.AUTH.LOGIN,
    payload
  );
  return response.data;
};

// ─── 1B. REGISTER ──────────────────────────────────────────────────────────────

/**
 * Gọi API đăng ký tài khoản.
 */
export const registerApi = async (
  payload: Omit<RegisterFormValues, 'confirmPassword' | 'acceptTerms'>
): Promise<RegisterApiResponse> => {
  const response = await apiClient.post<RegisterApiResponse>(
    API_ENDPOINTS.AUTH.REGISTER,
    payload
  );
  return response.data;
};

// ─── 2. LOGOUT ────────────────────────────────────────────────────────────────

/**
 * Gọi API đăng xuất.
 * Backend sẽ clear HttpOnly Cookie (access_token, refresh_token).
 */
export const logoutApi = async (): Promise<void> => {
  await apiClient.post(API_ENDPOINTS.AUTH.LOGOUT);
};

// ─── 3. GET CURRENT USER (dùng sau khi refresh page) ────────────────────────

/**
 * Lấy thông tin user hiện tại từ backend.
 * Được gọi khi app khởi động để kiểm tra trạng thái phiên đăng nhập.
 * Backend check cookie và trả về user info nếu token còn hợp lệ.
 */
export const fetchCurrentUserApi = async (): Promise<User> => {
  const response = await apiClient.get<User>(API_ENDPOINTS.AUTH.PROFILE);
  return response.data;
};

// ─── 4. GET USER ORGANIZATIONS ────────────────────────────────────────────────

/**
 * Lấy danh sách tổ chức mà user hiện tại tham gia.
 * Gọi sau khi login thành công để chuẩn bị dữ liệu cho màn hình Select-Org.
 */
export const fetchMyOrganizationsApi = async (): Promise<UserOrganization[]> => {
  const response = await apiClient.get<UserOrganization[]>(
    API_ENDPOINTS.ORGANIZATIONS.ME
  );
  return response.data;
};

export const fetchMyPermissionsApi = async (organizationId: string): Promise<string[]> => {
  const response = await apiClient.get<string[]>(`/organizations/${organizationId}/my-permissions`);
  return response.data;
};

// ─── 5. UPDATE PROFILE ──────────────────────────────────────────────────────────

export interface UpdateProfileRequest {
  firstName: string;
  lastName: string;
}

export interface UpdateProfileResponse {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
}

/**
 * Gọi API cập nhật thông tin cá nhân.
 */
export const updateProfileApi = async (
  userId: string,
  payload: UpdateProfileRequest
): Promise<UpdateProfileResponse> => {
  const response = await apiClient.put<UpdateProfileResponse>(
    `/users/${userId}`,
    payload
  );
  return response.data;
};

export interface ChangePasswordFormValues {
  oldPassword?: string;
  newPassword?: string;
}

/**
 * Gọi API đổi mật khẩu dành cho người dùng đã đăng nhập.
 */
export const changePasswordApi = async (
  userId: string,
  payload: ChangePasswordFormValues
): Promise<void> => {
  await apiClient.put(`/users/${userId}/change-password`, payload);
};

export interface ForgotPasswordRequest {
  email: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword?: string;
}

/**
 * Gửi yêu cầu lấy lại mật khẩu qua email.
 */
export const forgotPasswordApi = async (payload: ForgotPasswordRequest): Promise<string> => {
  const response = await apiClient.post<string>(API_ENDPOINTS.AUTH.FORGOT_PASSWORD, payload);
  return response.data;
};

/**
 * Đặt lại mật khẩu mới dùng reset token từ email.
 */
export const resetPasswordApi = async (payload: ResetPasswordRequest): Promise<string> => {
  const response = await apiClient.post<string>(API_ENDPOINTS.AUTH.RESET_PASSWORD, payload);
  return response.data;
};





