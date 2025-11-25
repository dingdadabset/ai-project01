import api from './index'
import type { User, PaginatedResponse } from '@/types'

export const userApi = {
  create: (data: {
    username: string
    password: string
    email: string
    nickname?: string
    description?: string
    role?: string
  }) => api.post<User>('/users', data),

  update: (id: number, data: {
    nickname?: string
    description?: string
    avatar?: string
  }) => api.put<User>(`/users/${id}`, data),

  delete: (id: number) =>
    api.delete(`/users/${id}`),

  getById: (id: number) =>
    api.get<User>(`/users/${id}`),

  getByUsername: (username: string) =>
    api.get<User>(`/users/username/${username}`),

  list: () =>
    api.get<User[]>('/users'),

  listPaginated: (page: number = 0, size: number = 10) =>
    api.get<PaginatedResponse<User>>(`/users/page?page=${page}&size=${size}`),

  updateStatus: (id: number, status: string) =>
    api.put<User>(`/users/${id}/status`, { status }),

  updateRole: (id: number, role: string) =>
    api.put<User>(`/users/${id}/role`, { role })
}

export default userApi
