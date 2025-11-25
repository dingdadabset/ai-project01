import api from './index'
import type { Category } from '@/types'

export const categoryApi = {
  create: (data: { name: string; description?: string }) =>
    api.post<Category>('/categories', data),

  delete: (id: number) =>
    api.delete(`/categories/${id}`),

  getById: (id: number) =>
    api.get<Category>(`/categories/${id}`),

  list: () =>
    api.get<Category[]>('/categories')
}

export default categoryApi
