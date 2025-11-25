import api from './index'
import type { Tag, PaginatedResponse } from '@/types'

export const tagApi = {
  create: (data: { name: string }) =>
    api.post<Tag>('/tags', data),

  update: (id: number, data: { name: string }) =>
    api.put<Tag>(`/tags/${id}`, data),

  delete: (id: number) =>
    api.delete(`/tags/${id}`),

  getById: (id: number) =>
    api.get<Tag>(`/tags/${id}`),

  getBySlug: (slug: string) =>
    api.get<Tag>(`/tags/slug/${slug}`),

  list: () =>
    api.get<Tag[]>('/tags'),

  listPaginated: (page: number = 0, size: number = 10) =>
    api.get<PaginatedResponse<Tag>>(`/tags/page?page=${page}&size=${size}`)
}

export default tagApi
