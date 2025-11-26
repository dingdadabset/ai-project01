import api from './index'
import type { User } from '@/types'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
  nickname?: string
}

export interface AuthResponse {
  token: string
  user: User
}

export const authApi = {
  // Login user
  login: (data: LoginRequest) =>
    api.post<AuthResponse>('/auth/login', data),

  // Register new user
  register: (data: RegisterRequest) =>
    api.post<AuthResponse>('/auth/register', data),

  // Get current user
  me: () =>
    api.get<User>('/auth/me')
}

export default authApi
