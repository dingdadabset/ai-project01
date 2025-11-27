import api from './index'

export interface UploadResponse {
  success: boolean
  url?: string
  path?: string
  name?: string
  size?: number
  error?: string
}

export interface FileInfo {
  name: string
  url: string
  size: number
}

export interface ListFilesResponse {
  success: boolean
  files: FileInfo[]
  error?: string
}

const uploadApi = {
  // Upload a single file
  upload(file: File, type: string = 'images') {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', type)
    return api.post<UploadResponse>('/uploads', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // Upload multiple files
  uploadBatch(files: File[], type: string = 'images') {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    formData.append('type', type)
    return api.post<{
      success: boolean
      files: UploadResponse[]
      errors?: string[]
    }>('/uploads/batch', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // List files in a directory
  listFiles(type: string) {
    return api.get<ListFilesResponse>(`/uploads/list/${type}`)
  },

  // Delete a file
  deleteFile(type: string, filename: string) {
    return api.delete(`/uploads/${type}/${filename}`)
  },

  // Get file URL
  getFileUrl(type: string, filename: string) {
    return `/api/uploads/${type}/${filename}`
  },

  // Get background image URL
  getBackgroundUrl(bgId: string) {
    return `/themes/anime-girls/static/images/backgrounds/${bgId}.svg`
  }
}

export default uploadApi
