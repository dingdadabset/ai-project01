<script setup lang="ts">
import { onMounted, ref, reactive } from 'vue'
import stockApi, { type Stock, type StockRequest } from '@/api/stocks'

const stocks = ref<Stock[]>([])
const showModal = ref(false)
const editingStock = ref<Stock | null>(null)
const isSubmitting = ref(false)
const isLoading = ref(true)

const markets = ['SH', 'SZ', 'HK', 'US', 'OTHER']

const formData = reactive<StockRequest & { isHot?: boolean; hotRank?: number }>({
  symbol: '',
  name: '',
  nameCn: '',
  market: 'SH',
  price: 0,
  changeAmount: 0,
  changePercent: 0,
  high: 0,
  low: 0,
  open: 0,
  prevClose: 0,
  volume: 0,
  marketCap: 0,
  peRatio: 0
})

const fetchStocks = async () => {
  isLoading.value = true
  try {
    const response = await stockApi.list(0, 50)
    stocks.value = response.data.records || []
  } catch (error) {
    console.error('Failed to fetch stocks:', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchStocks()
})

const openCreateModal = () => {
  editingStock.value = null
  Object.assign(formData, {
    symbol: '',
    name: '',
    nameCn: '',
    market: 'SH',
    price: 0,
    changeAmount: 0,
    changePercent: 0,
    high: 0,
    low: 0,
    open: 0,
    prevClose: 0,
    volume: 0,
    marketCap: 0,
    peRatio: 0
  })
  showModal.value = true
}

const openEditModal = (item: Stock) => {
  editingStock.value = item
  Object.assign(formData, {
    symbol: item.symbol,
    name: item.name,
    nameCn: item.nameCn || '',
    market: item.market,
    price: item.price,
    changeAmount: item.changeAmount,
    changePercent: item.changePercent,
    high: item.high,
    low: item.low,
    open: item.open,
    prevClose: item.prevClose,
    volume: item.volume,
    marketCap: item.marketCap,
    peRatio: item.peRatio
  })
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  editingStock.value = null
}

const submitStock = async () => {
  if (!formData.symbol.trim() || !formData.name.trim()) {
    alert('请输入股票代码和名称')
    return
  }
  
  isSubmitting.value = true
  try {
    await stockApi.createOrUpdate(formData)
    await fetchStocks()
    closeModal()
  } catch (error) {
    console.error('Failed to save stock:', error)
    alert('保存失败')
  } finally {
    isSubmitting.value = false
  }
}

const deleteStock = async (id: number) => {
  if (confirm('确定要删除这支股票吗?')) {
    await stockApi.delete(id)
    await fetchStocks()
  }
}

const toggleHot = async (item: Stock) => {
  await stockApi.setHot(item.id, !item.isHot, item.hotRank)
  await fetchStocks()
}

const formatPrice = (price: number) => {
  return price?.toFixed(2) || '-'
}

const formatPercent = (percent: number) => {
  if (!percent) return '-'
  const sign = percent > 0 ? '+' : ''
  return `${sign}${percent.toFixed(2)}%`
}

const getChangeClass = (percent: number) => {
  if (!percent) return ''
  return percent > 0 ? 'up' : 'down'
}
</script>

<template>
  <div class="stock-manager">
    <header class="page-header">
      <div>
        <h1>📈 股票管理</h1>
        <p>管理股票市场数据</p>
      </div>
      <button class="btn btn-primary" @click="openCreateModal">
        + 添加股票
      </button>
    </header>
    
    <div v-if="isLoading" class="loading">加载中...</div>
    
    <div v-else class="stocks-table">
      <table>
        <thead>
          <tr>
            <th>代码</th>
            <th>名称</th>
            <th>市场</th>
            <th>价格</th>
            <th>涨跌幅</th>
            <th>热门</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in stocks" :key="item.id">
            <td class="symbol">{{ item.symbol }}</td>
            <td>{{ item.nameCn || item.name }}</td>
            <td>{{ item.market }}</td>
            <td :class="getChangeClass(item.changePercent)">
              {{ formatPrice(item.price) }}
            </td>
            <td :class="getChangeClass(item.changePercent)">
              {{ formatPercent(item.changePercent) }}
            </td>
            <td>
              <button 
                class="hot-btn" 
                :class="{ active: item.isHot }"
                @click="toggleHot(item)"
              >
                {{ item.isHot ? '🔥' : '❄️' }}
              </button>
            </td>
            <td>
              <div class="actions">
                <button class="action-btn edit" @click="openEditModal(item)">编辑</button>
                <button class="action-btn delete" @click="deleteStock(item.id)">删除</button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- Modal -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ editingStock ? '编辑股票' : '添加股票' }}</h2>
          <button class="close-btn" @click="closeModal">×</button>
        </div>
        
        <div class="modal-body">
          <div class="form-row">
            <div class="form-group">
              <label>股票代码 *</label>
              <input v-model="formData.symbol" type="text" class="form-input" placeholder="600000" />
            </div>
            <div class="form-group">
              <label>市场 *</label>
              <select v-model="formData.market" class="form-input">
                <option v-for="market in markets" :key="market" :value="market">{{ market }}</option>
              </select>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>英文名称 *</label>
              <input v-model="formData.name" type="text" class="form-input" placeholder="Stock Name" />
            </div>
            <div class="form-group">
              <label>中文名称</label>
              <input v-model="formData.nameCn" type="text" class="form-input" placeholder="股票名称" />
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>当前价格</label>
              <input v-model.number="formData.price" type="number" step="0.01" class="form-input" />
            </div>
            <div class="form-group">
              <label>昨收价格</label>
              <input v-model.number="formData.prevClose" type="number" step="0.01" class="form-input" />
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>涨跌额</label>
              <input v-model.number="formData.changeAmount" type="number" step="0.01" class="form-input" />
            </div>
            <div class="form-group">
              <label>涨跌幅 (%)</label>
              <input v-model.number="formData.changePercent" type="number" step="0.01" class="form-input" />
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>最高价</label>
              <input v-model.number="formData.high" type="number" step="0.01" class="form-input" />
            </div>
            <div class="form-group">
              <label>最低价</label>
              <input v-model.number="formData.low" type="number" step="0.01" class="form-input" />
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>开盘价</label>
              <input v-model.number="formData.open" type="number" step="0.01" class="form-input" />
            </div>
            <div class="form-group">
              <label>成交量</label>
              <input v-model.number="formData.volume" type="number" class="form-input" />
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>市值</label>
              <input v-model.number="formData.marketCap" type="number" class="form-input" />
            </div>
            <div class="form-group">
              <label>市盈率</label>
              <input v-model.number="formData.peRatio" type="number" step="0.01" class="form-input" />
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="submitStock" :disabled="isSubmitting">
            {{ isSubmitting ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stock-manager {
  max-width: 1200px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-header h1 {
  font-size: 2rem;
  margin-bottom: 8px;
}

.page-header p {
  color: var(--text-secondary);
}

.stocks-table {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 700px;
}

th, td {
  padding: 16px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

th {
  background: var(--bg-secondary);
  font-weight: 600;
  color: var(--text-muted);
  font-size: 0.875rem;
}

.symbol {
  font-weight: 600;
  color: var(--color-primary);
}

.up { color: #10b981; }
.down { color: #ef4444; }

.hot-btn {
  background: none;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
}

.actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 6px 12px;
  border-radius: var(--radius-sm);
  border: none;
  font-size: 0.75rem;
  font-weight: 500;
  cursor: pointer;
}

.action-btn.edit {
  background: var(--color-primary);
  color: white;
}

.action-btn.delete {
  background: rgba(239, 68, 68, 0.2);
  color: #ef4444;
}

/* Modal styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  width: 100%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.close-btn {
  background: none;
  border: none;
  font-size: 2rem;
  color: var(--text-muted);
  cursor: pointer;
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: var(--text-secondary);
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  background: var(--bg-secondary);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  color: var(--text-primary);
  font-size: 1rem;
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.btn {
  padding: 12px 24px;
  border-radius: var(--radius-md);
  font-weight: 600;
  cursor: pointer;
  border: none;
}

.btn-primary {
  background: var(--color-primary);
  color: white;
}

.btn-secondary {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
