import { LoginRequest, RegisterRequest } from "@/types/auth";
import type { User } from "@/types/user";
import { apiGet, apiPost } from "@/utils/apiRequest";

export const login = (payload: LoginRequest): Promise<void> =>
  apiPost<void>("/auth/login", payload, { skipAuth: true });

export const register = (payload: RegisterRequest): Promise<void> =>
  apiPost<void>("/auth/register", payload, { skipAuth: true });

export const refreshToken = (): Promise<void> =>
  apiPost<void>("/auth/refresh", null, { skipAuth: true });

export const logout = (): Promise<void> =>
  apiPost<void>("/auth/logout", null, { skipAuth: true });

export const getCurrentUser = async (): Promise<User> =>
  apiGet<User>("/auth/me");
