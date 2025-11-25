import api from './index'
import type { Comment, PaginatedResponse } from '@/types'

export const commentApi = {
  create: (data: {
    postId: number
    userId?: number
    content: string
    guestName?: string
    guestEmail?: string
  }) => api.post<Comment>('/comments', data),

  delete: (id: number) =>
    api.delete(`/comments/${id}`),

  getById: (id: number) =>
    api.get<Comment>(`/comments/${id}`),

  getByPost: (postId: number, page: number = 0, size: number = 10) =>
    api.get<PaginatedResponse<Comment>>(`/comments/post/${postId}?page=${page}&size=${size}`),

  approve: (id: number) =>
    api.put<Comment>(`/comments/${id}/approve`)
}

export default commentApi
