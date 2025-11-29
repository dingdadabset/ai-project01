<script setup lang="ts">
import { onMounted, ref } from 'vue'
import stockApi, { type Stock, type PaginatedResponse } from '@/api/stocks'

const stocks = ref<Stock[]>([])
const hotStocks = ref<Stock[]>([])
const gainers = ref<Stock[]>([])
const losers = ref<Stock[]>([])
const isLoading = ref(true)
const currentPage = ref(0)
const totalPages = ref(0)
const searchKeyword = ref('')
const selectedMarket = ref<string>('')

const markets = [
  { value: '', label: '全部市场' },
  { value: 'SH', label: '上海' },
  { value: 'SZ', label: '深圳' },
  { value: 'HK', label: '香港' },
  { value: 'US', label: '美国' },
]

const fetchStocks = async () => {
  isLoading.value = true
  try {
    let response: { data: PaginatedResponse<Stock> }
    
    if (searchKeyword.value) {
      response = await stockApi.search(searchKeyword.value, currentPage.value, 20)
    } else if (selectedMarket.value) {
      response = await stockApi.listByMarket(selectedMarket.value, currentPage.value, 20)
    } else {
      response = await stockApi.list(currentPage.value, 20)
    }
    
    stocks.value = response.data.records || []
    totalPages.value = response.data.pages
  } catch (error) {
    console.error('Failed to fetch stocks:', error)
  } finally {
    isLoading.value = false
  }
}

const fetchHotStocks = async () => {
  try {
    const response = await stockApi.listHot(10)
    hotStocks.value = response.data || []
  } catch (error) {
    console.error('Failed to fetch hot stocks:', error)
  }
}

const fetchGainersLosers = async () => {
  try {
    const [gainersRes, losersRes] = await Promise.all([
      stockApi.listGainers(5),
      stockApi.listLosers(5)
    ])
    gainers.value = gainersRes.data || []
    losers.value = losersRes.data || []
  } catch (error) {
    console.error('Failed to fetch gainers/losers:', error)
  }
}

const handleSearch = () => {
  currentPage.value = 0
  fetchStocks()
}

const handleMarketChange = () => {
  currentPage.value = 0
  searchKeyword.value = ''
  fetchStocks()
}

const goToPage = (page: number) => {
  currentPage.value = page
  fetchStocks()
}

const formatPrice = (price: number) => {
  return price?.toFixed(2) || '-'
}

const formatPercent = (percent: number) => {
  if (!percent) return '-'
  const sign = percent > 0 ? '+' : ''
  return `${sign}${percent.toFixed(2)}%`
}

const formatVolume = (volume: number) => {
  if (!volume) return '-'
  if (volume >= 100000000) return `${(volume / 100000000).toFixed(2)}亿`
  if (volume >= 10000) return `${(volume / 10000).toFixed(2)}万`
  return volume.toString()
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const getChangeClass = (percent: number) => {
  if (!percent) return ''
  return percent > 0 ? 'up' : 'down'
}

onMounted(async () => {
  await Promise.all([
    fetchStocks(),
    fetchHotStocks(),
    fetchGainersLosers()
  ])
})
</script>

<template>
  <div class="stocks-page">
    <div class="container">
      <header class="page-header">
        <h1>📈 股票行情</h1>
        <p>实时股票市场数据</p>
      </header>
      
      <!-- Market Overview -->
      <section class="market-overview">
        <div class="overview-grid">
          <!-- Hot Stocks -->
          <div class="overview-card">
            <h3>🔥 热门股票</h3>
            <div class="stock-list-mini">
              <div 
                v-for="stock in hotStocks" 
                :key="stock.id"
                class="stock-item-mini"
              >
                <div class="stock-name">
                  <span class="symbol">{{ stock.symbol }}</span>
                  <span class="name">{{ stock.nameCn || stock.name }}</span>
                </div>
                <div class="stock-price" :class="getChangeClass(stock.changePercent)">
                  <span class="price">{{ formatPrice(stock.price) }}</span>
                  <span class="change">{{ formatPercent(stock.changePercent) }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Top Gainers -->
          <div class="overview-card gainers">
            <h3>📈 涨幅榜</h3>
            <div class="stock-list-mini">
              <div 
                v-for="stock in gainers" 
                :key="stock.id"
                class="stock-item-mini"
              >
                <div class="stock-name">
                  <span class="symbol">{{ stock.symbol }}</span>
                  <span class="name">{{ stock.nameCn || stock.name }}</span>
                </div>
                <div class="stock-price up">
                  <span class="price">{{ formatPrice(stock.price) }}</span>
                  <span class="change">{{ formatPercent(stock.changePercent) }}</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Top Losers -->
          <div class="overview-card losers">
            <h3>📉 跌幅榜</h3>
            <div class="stock-list-mini">
              <div 
                v-for="stock in losers" 
                :key="stock.id"
                class="stock-item-mini"
              >
                <div class="stock-name">
                  <span class="symbol">{{ stock.symbol }}</span>
                  <span class="name">{{ stock.nameCn || stock.name }}</span>
                </div>
                <div class="stock-price down">
                  <span class="price">{{ formatPrice(stock.price) }}</span>
                  <span class="change">{{ formatPercent(stock.changePercent) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>
      
      <!-- Search and Filter -->
      <div class="filter-section">
        <div class="search-box">
          <input 
            v-model="searchKeyword"
            type="text"
            class="search-input"
            placeholder="搜索股票代码或名称..."
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">搜索</button>
        </div>
        
        <div class="market-filter">
          <select v-model="selectedMarket" @change="handleMarketChange" class="market-select">
            <option v-for="market in markets" :key="market.value" :value="market.value">
              {{ market.label }}
            </option>
          </select>
        </div>
      </div>
      
      <!-- Stock List -->
      <div v-if="isLoading" class="loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <div v-else-if="stocks.length === 0" class="empty-state">
        <span class="empty-icon">📊</span>
        <p>暂无股票数据</p>
      </div>
      
      <div v-else class="stocks-table-wrapper">
        <table class="stocks-table">
          <thead>
            <tr>
              <th>代码</th>
              <th>名称</th>
              <th>市场</th>
              <th>最新价</th>
              <th>涨跌额</th>
              <th>涨跌幅</th>
              <th>最高</th>
              <th>最低</th>
              <th>成交量</th>
              <th>最新爬取时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="stock in stocks" :key="stock.id">
              <td class="symbol">{{ stock.symbol }}</td>
              <td class="name">{{ stock.nameCn || stock.name }}</td>
              <td class="market">{{ stock.market }}</td>
              <td class="price" :class="getChangeClass(stock.changePercent)">
                {{ formatPrice(stock.price) }}
              </td>
              <td class="change" :class="getChangeClass(stock.changeAmount)">
                {{ stock.changeAmount > 0 ? '+' : '' }}{{ formatPrice(stock.changeAmount) }}
              </td>
              <td class="percent" :class="getChangeClass(stock.changePercent)">
                {{ formatPercent(stock.changePercent) }}
              </td>
              <td>{{ formatPrice(stock.high) }}</td>
              <td>{{ formatPrice(stock.low) }}</td>
              <td>{{ formatVolume(stock.volume) }}</td>
              <td class="last-updated">{{ formatDateTime(stock.lastUpdated) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- Pagination -->
      <div v-if="totalPages > 1" class="pagination">
        <button 
          class="page-btn"
          :disabled="currentPage === 0"
          @click="goToPage(currentPage - 1)"
        >
          上一页
        </button>
        <span class="page-info">{{ currentPage + 1 }} / {{ totalPages }}</span>
        <button 
          class="page-btn"
          :disabled="currentPage >= totalPages - 1"
          @click="goToPage(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stocks-page {
  min-height: 100vh;
  padding: 100px 0 60px;
}

.page-header {
  text-align: center;
  margin-bottom: 48px;
}

.page-header h1 {
  font-size: 2.5rem;
  margin-bottom: 8px;
}

.page-header p {
  color: var(--text-secondary);
}

/* Market Overview */
.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 48px;
}

.overview-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
}

.overview-card h3 {
  font-size: 1.125rem;
  margin-bottom: 16px;
}

.stock-list-mini {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stock-item-mini {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: var(--radius-sm);
}

.stock-name {
  display: flex;
  flex-direction: column;
}

.stock-name .symbol {
  font-weight: 600;
  font-size: 0.875rem;
}

.stock-name .name {
  font-size: 0.75rem;
  color: var(--text-muted);
}

.stock-price {
  text-align: right;
}

.stock-price .price {
  display: block;
  font-weight: 600;
}

.stock-price .change {
  font-size: 0.75rem;
}

.stock-price.up { color: #10b981; }
.stock-price.down { color: #ef4444; }

/* Filter Section */
.filter-section {
  display: flex;
  gap: 16px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.search-box {
  display: flex;
  flex: 1;
  min-width: 200px;
}

.search-input {
  flex: 1;
  padding: 12px 16px;
  background: var(--bg-card);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md) 0 0 var(--radius-md);
  color: var(--text-primary);
}

.search-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.search-btn {
  padding: 12px 24px;
  background: var(--color-primary);
  color: white;
  border: none;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
  cursor: pointer;
  font-weight: 500;
}

.market-select {
  padding: 12px 16px;
  background: var(--bg-card);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  cursor: pointer;
}

/* Stocks Table */
.stocks-table-wrapper {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow-x: auto;
}

.stocks-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 800px;
}

.stocks-table th,
.stocks-table td {
  padding: 16px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.stocks-table th {
  background: rgba(0, 0, 0, 0.2);
  font-weight: 600;
  color: var(--text-muted);
  font-size: 0.875rem;
  text-transform: uppercase;
}

.stocks-table .symbol {
  font-weight: 600;
  color: var(--color-primary);
}

.stocks-table .name {
  font-weight: 500;
}

.stocks-table .market {
  color: var(--text-muted);
}

.stocks-table .last-updated {
  color: var(--text-muted);
  font-size: 0.8rem;
  white-space: nowrap;
}

.stocks-table .up { color: #10b981; }
.stocks-table .down { color: #ef4444; }

/* Loading and Empty */
.loading,
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-icon {
  font-size: 4rem;
  display: block;
  margin-bottom: 16px;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 48px;
}

.page-btn {
  padding: 10px 20px;
  background: var(--bg-card);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.3s ease;
}

.page-btn:hover:not(:disabled) {
  background: var(--color-primary);
  border-color: var(--color-primary);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  color: var(--text-secondary);
}

@media (max-width: 768px) {
  .overview-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-section {
    flex-direction: column;
  }
}
</style>
