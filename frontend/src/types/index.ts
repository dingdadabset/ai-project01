// Post types
export interface Post {
  id: number
  title: string
  slug: string
  summary?: string
  content?: string
  thumbnail?: string
  status: 'DRAFT' | 'PUBLISHED' | 'PRIVATE' | 'SCHEDULED'
  authorName?: string
  categoryName?: string
  tags?: string[]
  viewCount: number
  likeCount: number
  commentCount: number
  allowComment: boolean
  publishedAt?: string
  createdAt: string
  updatedAt: string
}

export interface PostRequest {
  title: string
  summary?: string
  content?: string
  originalContent?: string
  thumbnail?: string
  status?: 'DRAFT' | 'PUBLISHED' | 'PRIVATE' | 'SCHEDULED'
  categoryId?: number
  tags?: string[]
  allowComment?: boolean
}

// Category types
export interface Category {
  id: number
  name: string
  slug: string
  description?: string
  postCount: number
  createdAt: string
  updatedAt: string
}

// Tag types
export interface Tag {
  id: number
  name: string
  slug: string
  postCount: number
  createdAt: string
}

// Comment types
export interface Comment {
  id: number
  postId: number
  userId?: number
  guestName?: string
  guestEmail?: string
  content: string
  parentId?: number
  ipAddress?: string
  userAgent?: string
  status: 'PENDING' | 'APPROVED' | 'SPAM' | 'DELETED'
  createdAt: string
}

// User types
export interface User {
  id: number
  username: string
  email: string
  nickname?: string
  avatar?: string
  description?: string
  role: 'ADMIN' | 'AUTHOR' | 'SUBSCRIBER'
  status: 'ACTIVE' | 'INACTIVE' | 'BANNED'
  createdAt: string
  updatedAt: string
}

// Page types
export interface Page {
  id: number
  title: string
  slug: string
  content?: string
  originalContent?: string
  authorId: number
  status: 'DRAFT' | 'PUBLISHED'
  viewCount: number
  createdAt: string
  updatedAt: string
}

// Attachment types
export interface Attachment {
  id: number
  name: string
  path: string
  url: string
  mediaType?: string
  suffix?: string
  size: number
  width: number
  height: number
  uploaderId: number
  type: 'IMAGE' | 'VIDEO' | 'AUDIO' | 'DOCUMENT' | 'OTHER'
  createdAt: string
}

// Pagination types
export interface PaginatedResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// View mode types
export type ViewMode = 'story' | 'list'
