import api from './index'

export interface ExternalTool {
  id: number
  name: string
  description?: string
  url: string
  icon?: string
  iconBgColor?: string
  category: string
  displayOrder: number
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export interface ExternalToolRequest {
  name: string
  description?: string
  url: string
  icon?: string
  iconBgColor?: string
  category?: string
  displayOrder?: number
  isActive?: boolean
}

export interface PaginatedResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const externalToolApi = {
  // Create a new tool
  create: (data: ExternalToolRequest) =>
    api.post<ExternalTool>('/external-tools', data),

  // Get tool by ID
  getById: (id: number) =>
    api.get<ExternalTool>(`/external-tools/${id}`),

  // List all active tools (for public display)
  listActive: () =>
    api.get<ExternalTool[]>('/external-tools/active'),

  // List all tools with pagination (for admin)
  list: (page: number = 0, size: number = 20) =>
    api.get<PaginatedResponse<ExternalTool>>(`/external-tools?page=${page}&size=${size}`),

  // List tools by category
  listByCategory: (category: string) =>
    api.get<ExternalTool[]>(`/external-tools/category/${category}`),

  // Search tools
  search: (keyword: string) =>
    api.get<ExternalTool[]>(`/external-tools/search?keyword=${keyword}`),

  // Update tool
  update: (id: number, data: Partial<ExternalToolRequest>) =>
    api.put<ExternalTool>(`/external-tools/${id}`, data),

  // Delete tool
  delete: (id: number) =>
    api.delete(`/external-tools/${id}`),

  // Initialize default tools
  init: () =>
    api.post<{ message: string }>('/external-tools/init')
}

export default externalToolApi
