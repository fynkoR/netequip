export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  employeeId?: number;
}

export interface UserInfo {
  username: string;
  roles: string[];
  employeeId?: number;
  fullName?: string;
}
