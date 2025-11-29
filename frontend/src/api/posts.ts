import api from './index'
import type { Post, PostRequest, PaginatedResponse } from '@/types'

export const postApi = {
  // Create a new post
  create: (data: PostRequest, authorId: number = 1) =>
    api.post<Post>(`/posts?authorId=${authorId}`, data),

  // Update a post
  update: (id: number, data: PostRequest) =>
    api.put<Post>(`/posts/${id}`, data),

  // Delete a post
  delete: (id: number) =>
    api.delete(`/posts/${id}`),

  // Get post by ID
  getById: (id: number) =>
    api.get<Post>(`/posts/${id}`),

  // Get post by slug
  getBySlug: (slug: string) =>
    api.get<Post>(`/posts/slug/${slug}`),

  // List all posts with pagination
  list: (page: number = 0, size: number = 10) =>
    api.get<PaginatedResponse<Post>>(`/posts?page=${page}&size=${size}`),

  // List published posts
  listPublished: (page: number = 0, size: number = 10) =>
    api.get<PaginatedResponse<Post>>(`/posts/published?page=${page}&size=${size}`),

  // List posts by category
  listByCategory: (categoryId: number, page: number = 0, size: number = 10) =>
    api.get<PaginatedResponse<Post>>(`/posts/category/${categoryId}?page=${page}&size=${size}`),

  // List posts by tag
  listByTag: (tagId: number, page: number = 0, size: number = 10) =>
    api.get<PaginatedResponse<Post>>(`/posts/tag/${tagId}?page=${page}&size=${size}`),

  // Search posts
  search: (keyword: string, page: number = 0, size: number = 10) =>
    api.get<PaginatedResponse<Post>>(`/posts/search?keyword=${keyword}&page=${page}&size=${size}`)
}

export default postApi
