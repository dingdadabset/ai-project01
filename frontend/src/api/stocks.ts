import api from './index'

export interface Stock {
  id: number
  symbol: string
  name: string
  nameCn?: string
  market: string
  price: number
  changeAmount: number
  changePercent: number
  high: number
  low: number
  open: number
  prevClose: number
  volume: number
  marketCap: number
  peRatio: number
  isHot: boolean
  hotRank: number
  lastUpdated: string
  createdAt: string
}

export interface StockRequest {
  symbol: string
  name: string
  nameCn?: string
  market?: string
  price?: number
  changeAmount?: number
  changePercent?: number
  high?: number
  low?: number
  open?: number
  prevClose?: number
  volume?: number
  marketCap?: number
  peRatio?: number
}

export interface PaginatedResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export const stockApi = {
  // Create or update a stock
  createOrUpdate: (data: StockRequest) =>
    api.post<Stock>('/stocks', data),

  // Get stock by ID
  getById: (id: number) =>
    api.get<Stock>(`/stocks/${id}`),

  // Get stock by symbol
  getBySymbol: (symbol: string) =>
    api.get<Stock>(`/stocks/symbol/${symbol}`),

  // List all stocks with pagination
  list: (page: number = 0, size: number = 10) =>
    api.get<PaginatedResponse<Stock>>(`/stocks?page=${page}&size=${size}`),

  // List hot stocks
  listHot: (limit: number = 10) =>
    api.get<Stock[]>(`/stocks/hot?limit=${limit}`),

  // List stocks by market
  listByMarket: (market: string, page: number = 0, size: number = 10) =>
    api.get<PaginatedResponse<Stock>>(`/stocks/market/${market}?page=${page}&size=${size}`),

  // Search stocks
  search: (keyword: string, page: number = 0, size: number = 10) =>
    api.get<PaginatedResponse<Stock>>(`/stocks/search?keyword=${keyword}&page=${page}&size=${size}`),

  // List top gainers
  listGainers: (limit: number = 10) =>
    api.get<Stock[]>(`/stocks/gainers?limit=${limit}`),

  // List top losers
  listLosers: (limit: number = 10) =>
    api.get<Stock[]>(`/stocks/losers?limit=${limit}`),

  // Update stock price
  updatePrice: (id: number, price: number, changeAmount: number, changePercent: number) =>
    api.put<Stock>(`/stocks/${id}/price`, { price, changeAmount, changePercent }),

  // Set stock as hot
  setHot: (id: number, isHot: boolean, hotRank: number) =>
    api.put<Stock>(`/stocks/${id}/hot`, { isHot, hotRank }),

  // Delete stock
  delete: (id: number) =>
    api.delete(`/stocks/${id}`),

  // Fetch stock data from external sources
  fetchExternal: () =>
    api.post<{ message: string; count: number; stocks: Stock[] }>('/stocks/fetch')
}

export default stockApi
