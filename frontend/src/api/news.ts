import api from './index'

export interface News {
  id: number
  title: string
  summary?: string
  content?: string
  source?: string
  sourceUrl?: string
  thumbnail?: string
  category: string
  viewCount: number
  isHot: boolean
  hotScore: number
  publishedAt: string
  createdAt: string
  updatedAt: string
}

export interface NewsRequest {
  title: string
  summary?: string
  content?: string
  source?: string
  sourceUrl?: string
  thumbnail?: string
  category?: string
}

export interface PaginatedResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const newsApi = {
  // Create a new news item
  create: (data: NewsRequest) =>
    api.post<News>('/news', data),

  // Get news by ID
  getById: (id: number) =>
    api.get<News>(`/news/${id}`),

  // List all news with pagination
  list: (page: number = 0, size: number = 10) =>
    api.get<PaginatedResponse<News>>(`/news?page=${page}&size=${size}`),

  // List hot news
  listHot: (limit: number = 10) =>
    api.get<News[]>(`/news/hot?limit=${limit}`),

  // List news by category
  listByCategory: (category: string, page: number = 0, size: number = 10) =>
    api.get<PaginatedResponse<News>>(`/news/category/${category}?page=${page}&size=${size}`),

  // Search news
  search: (keyword: string, page: number = 0, size: number = 10) =>
    api.get<PaginatedResponse<News>>(`/news/search?keyword=${keyword}&page=${page}&size=${size}`),

  // Update news
  update: (id: number, data: Partial<NewsRequest & { isHot?: boolean; hotScore?: number }>) =>
    api.put<News>(`/news/${id}`, data),

  // Delete news
  delete: (id: number) =>
    api.delete(`/news/${id}`),

  // Set news as hot
  setHot: (id: number, isHot: boolean, hotScore: number) =>
    api.put<News>(`/news/${id}/hot`, { isHot, hotScore })
}

export default newsApi
