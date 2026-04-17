export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
}

export interface TokenPayload {
  accessToken: string;
  refreshToken: string;
}

export interface AuthContextType {
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (
    fullName: string,
    email: string,
    password: string,
  ) => Promise<void>;
  logout: () => Promise<void>;
  refreshToken: () => Promise<void>;
}

export interface HttpAxiosResponse {
  success: boolean;
  message: string;
  statusCode: number;
  errors?: Record<string, string[]>;
}
