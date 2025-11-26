package com.aiproject.module.stock.fetcher;

import com.aiproject.module.stock.mapper.StockMapper;
import com.aiproject.module.stock.model.Stock;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Stock Fetcher Service
 * Automatically fetches stock data from external sources (zhituapi.com)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockFetcherService {

    // zhituapi.com API configuration
    private static final String ZHITU_API_BASE_URL = "https://api.zhituapi.com";
    private static final String ZHITU_REALTIME_URL = ZHITU_API_BASE_URL + "/hs/real/time/";

    // Constants for simulated data generation (fallback)
    private static final long VOLUME_MIN = 10_000_000L;
    private static final long VOLUME_RANGE = 100_000_000L;
    private static final long MARKET_CAP_MIN = 1_000_000_000L;
    private static final long MARKET_CAP_RANGE = 10_000_000_000L;
    private static final double PE_RATIO_MIN = 5.0;
    private static final double PE_RATIO_RANGE = 50.0;

    @Value("${stock.api.token:}")
    private String apiToken;

    private final StockMapper stockMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Random random = new Random();

    /**
     * Fetch stock data every 5 minutes during market hours
     */
    @Scheduled(fixedRate = 300000, initialDelay = 10000)
    public void fetchStockData() {
        log.info("Starting scheduled stock data fetch...");
        try {
            // Try to fetch from zhituapi.com first
            List<Stock> fetched = fetchFromZhituApi();
            if (fetched.isEmpty()) {
                // Fallback to popular stocks
                fetchPopularStocks();
            }
            log.info("Stock data fetch completed successfully");
        } catch (Exception e) {
            log.error("Error fetching stock data: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual trigger for fetching stock data
     */
    public List<Stock> fetchStocksManually() {
        log.info("Manual stock fetch triggered");
        List<Stock> fetchedStocks = new ArrayList<>();
        try {
            // Try to fetch from zhituapi.com first
            fetchedStocks.addAll(fetchFromZhituApi());
            if (fetchedStocks.isEmpty()) {
                // Fallback to popular stocks if API fails
                log.warn("zhituapi.com fetch returned no data, using fallback popular stocks");
                fetchedStocks.addAll(fetchPopularStocks());
            }
        } catch (Exception e) {
            log.error("Error in manual stock fetch: {}", e.getMessage(), e);
            // Fallback to popular stocks on error
            fetchedStocks.addAll(fetchPopularStocks());
        }
        return fetchedStocks;
    }

    /**
     * Fetch stock data from zhituapi.com
     */
    private List<Stock> fetchFromZhituApi() {
        List<Stock> stockList = new ArrayList<>();
        
        // Check if API token is configured
        if (apiToken == null || apiToken.isEmpty()) {
            log.warn("Stock API token not configured, using fallback data");
            return stockList;
        }
        
        // Popular stock symbols to fetch real-time data for
        String[] popularSymbols = {
            "000001", "600519", "601318", "000858", "300750", 
            "000333", "601012", "002594", "600036", "600000",
            "000002", "601166", "600276", "600030", "601398"
        };
        
        log.info("Fetching stock data from zhituapi.com...");
        
        for (String symbol : popularSymbols) {
            try {
                Stock stock = fetchStockFromZhituApi(symbol);
                if (stock != null) {
                    stockList.add(stock);
                }
            } catch (Exception e) {
                log.warn("Error fetching stock {} from zhituapi: {}", symbol, e.getMessage());
            }
        }
        
        return stockList;
    }
    
    /**
     * Fetch single stock real-time data from zhituapi.com
     */
    private Stock fetchStockFromZhituApi(String symbol) {
        try {
            String url = ZHITU_REALTIME_URL + symbol + "?token=" + apiToken;
            log.debug("Fetching stock data for symbol: {}", symbol);
            
            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.isEmpty()) {
                log.warn("Empty response from zhituapi for symbol: {}", symbol);
                return null;
            }
            
            JsonNode jsonNode = objectMapper.readTree(response);
            
            // Parse the response based on zhituapi.com format
            // The API returns an object with stock data
            String stockName = jsonNode.has("name") ? jsonNode.get("name").asText() : symbol;
            String stockCode = jsonNode.has("code") ? jsonNode.get("code").asText() : symbol;
            
            BigDecimal price = parseDecimal(jsonNode, "price", "now", "trade");
            BigDecimal high = parseDecimal(jsonNode, "high", "max");
            BigDecimal low = parseDecimal(jsonNode, "low", "min");
            BigDecimal open = parseDecimal(jsonNode, "open", "todayStart");
            BigDecimal prevClose = parseDecimal(jsonNode, "preClose", "yestClose", "prevClose");
            Long volume = parseLong(jsonNode, "volume", "vol", "tradedVol");
            BigDecimal changeAmount = parseDecimal(jsonNode, "change", "updown");
            BigDecimal changePercent = parseDecimal(jsonNode, "pctChg", "percent", "updownPercent");
            
            // Calculate change if not provided
            if (changeAmount == null && price != null && prevClose != null && prevClose.compareTo(BigDecimal.ZERO) > 0) {
                changeAmount = price.subtract(prevClose).setScale(2, RoundingMode.HALF_UP);
            }
            if (changePercent == null && changeAmount != null && prevClose != null && prevClose.compareTo(BigDecimal.ZERO) > 0) {
                changePercent = changeAmount.divide(prevClose, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
            }
            
            // Determine market from symbol
            Stock.StockMarket market = determineMarket(stockCode);
            
            // Check if stock exists
            Stock stock = stockMapper.selectOne(
                    new LambdaQueryWrapper<Stock>().eq(Stock::getSymbol, stockCode));
            
            if (stock == null) {
                stock = Stock.builder()
                        .symbol(stockCode)
                        .name(stockName)
                        .nameCn(stockName)
                        .market(market)
                        .price(price != null ? price : BigDecimal.ZERO)
                        .changeAmount(changeAmount != null ? changeAmount : BigDecimal.ZERO)
                        .changePercent(changePercent != null ? changePercent : BigDecimal.ZERO)
                        .high(high)
                        .low(low)
                        .open(open)
                        .prevClose(prevClose)
                        .volume(volume != null ? volume : 0L)
                        .isHot(true)
                        .hotRank(random.nextInt(20) + 1)
                        .lastUpdated(LocalDateTime.now())
                        .build();
                stockMapper.insert(stock);
                log.info("Created stock from zhituapi: {} - {}", stockCode, stockName);
            } else {
                // Update existing stock with new data
                if (price != null) stock.setPrice(price);
                if (changeAmount != null) stock.setChangeAmount(changeAmount);
                if (changePercent != null) stock.setChangePercent(changePercent);
                if (high != null) stock.setHigh(high);
                if (low != null) stock.setLow(low);
                if (open != null) stock.setOpen(open);
                if (prevClose != null) stock.setPrevClose(prevClose);
                if (volume != null) stock.setVolume(volume);
                stock.setLastUpdated(LocalDateTime.now());
                stockMapper.updateById(stock);
                log.info("Updated stock from zhituapi: {} - {} ({}%)", stockCode, stockName, changePercent);
            }
            
            return stock;
        } catch (Exception e) {
            log.warn("Failed to fetch stock {} from zhituapi: {}", symbol, e.getMessage());
            return null;
        }
    }
    
    /**
     * Parse decimal value from JSON with multiple possible field names
     */
    private BigDecimal parseDecimal(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName) && !node.get(fieldName).isNull()) {
                try {
                    String value = node.get(fieldName).asText();
                    if (value != null && !value.isEmpty() && !value.equals("null")) {
                        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
                    }
                } catch (Exception e) {
                    // Try next field name
                }
            }
        }
        return null;
    }
    
    /**
     * Parse long value from JSON with multiple possible field names
     */
    private Long parseLong(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName) && !node.get(fieldName).isNull()) {
                try {
                    return node.get(fieldName).asLong();
                } catch (Exception e) {
                    // Try next field name
                }
            }
        }
        return null;
    }
    
    /**
     * Determine stock market from symbol
     */
    private Stock.StockMarket determineMarket(String symbol) {
        if (symbol == null || symbol.isEmpty()) {
            return Stock.StockMarket.OTHER;
        }
        // Hong Kong stocks: 5-digit codes starting with 0 (e.g., 00700, 09988)
        if (symbol.length() == 5 && symbol.startsWith("0")) {
            return Stock.StockMarket.HK;
        }
        // Shanghai stocks: 6-digit codes starting with 6
        if (symbol.startsWith("6") && symbol.length() == 6) {
            return Stock.StockMarket.SH;
        }
        // Shenzhen stocks: 6-digit codes starting with 0 or 3
        if ((symbol.startsWith("0") || symbol.startsWith("3")) && symbol.length() == 6) {
            return Stock.StockMarket.SZ;
        }
        // US stocks: uppercase letter symbols
        if (symbol.matches("[A-Z]+")) {
            return Stock.StockMarket.US;
        }
        return Stock.StockMarket.OTHER;
    }

    /**
     * Fetch popular stocks data (fallback when API fails)
     */
    private List<Stock> fetchPopularStocks() {
        List<Stock> stockList = new ArrayList<>();
        
        // Define popular stocks to track
        String[][] popularStocks = {
                // A股热门
                {"600519", "贵州茅台", "Kweichow Moutai", "SH"},
                {"601318", "中国平安", "Ping An Insurance", "SH"},
                {"000858", "五粮液", "Wuliangye", "SZ"},
                {"300750", "宁德时代", "CATL", "SZ"},
                {"000333", "美的集团", "Midea Group", "SZ"},
                {"601012", "隆基绿能", "LONGi Green Energy", "SH"},
                {"002594", "比亚迪", "BYD", "SZ"},
                {"600036", "招商银行", "CMB", "SH"},
                // 港股热门
                {"00700", "腾讯控股", "Tencent", "HK"},
                {"09988", "阿里巴巴", "Alibaba", "HK"},
                {"03690", "美团", "Meituan", "HK"},
                {"09888", "百度集团", "Baidu", "HK"},
                // 美股热门
                {"AAPL", "苹果", "Apple Inc.", "US"},
                {"GOOGL", "谷歌", "Alphabet Inc.", "US"},
                {"MSFT", "微软", "Microsoft", "US"},
                {"TSLA", "特斯拉", "Tesla Inc.", "US"},
                {"NVDA", "英伟达", "NVIDIA", "US"},
                {"META", "Meta", "Meta Platforms", "US"}
        };

        for (String[] stockData : popularStocks) {
            try {
                Stock stock = fetchOrCreateStock(stockData[0], stockData[1], stockData[2], stockData[3]);
                if (stock != null) {
                    stockList.add(stock);
                }
            } catch (Exception e) {
                log.warn("Error processing stock {}: {}", stockData[0], e.getMessage());
            }
        }

        return stockList;
    }

    /**
     * Fetch or create a stock with simulated price data
     * In production, this would call real stock APIs
     */
    private Stock fetchOrCreateStock(String symbol, String nameCn, String name, String marketCode) {
        Stock.StockMarket market = Stock.StockMarket.valueOf(marketCode);
        
        // Check if stock exists
        Stock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<Stock>().eq(Stock::getSymbol, symbol));
        
        // Generate realistic price data (simulated)
        BigDecimal basePrice = generateBasePrice(symbol, market);
        BigDecimal changePercent = generateChangePercent();
        BigDecimal changeAmount = basePrice.multiply(changePercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal price = basePrice.add(changeAmount);
        BigDecimal prevClose = basePrice;
        BigDecimal high = price.multiply(BigDecimal.valueOf(1.02)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal low = price.multiply(BigDecimal.valueOf(0.98)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal open = basePrice.multiply(BigDecimal.valueOf(1 + (random.nextDouble() - 0.5) * 0.02)).setScale(2, RoundingMode.HALF_UP);
        Long volume = (long) (random.nextDouble() * VOLUME_RANGE + VOLUME_MIN);
        BigDecimal marketCap = price.multiply(BigDecimal.valueOf(random.nextDouble() * MARKET_CAP_RANGE + MARKET_CAP_MIN)).setScale(0, RoundingMode.HALF_UP);
        BigDecimal peRatio = BigDecimal.valueOf(random.nextDouble() * PE_RATIO_RANGE + PE_RATIO_MIN).setScale(2, RoundingMode.HALF_UP);

        if (stock == null) {
            stock = Stock.builder()
                    .symbol(symbol)
                    .name(name)
                    .nameCn(nameCn)
                    .market(market)
                    .price(price)
                    .changeAmount(changeAmount)
                    .changePercent(changePercent)
                    .high(high)
                    .low(low)
                    .open(open)
                    .prevClose(prevClose)
                    .volume(volume)
                    .marketCap(marketCap)
                    .peRatio(peRatio)
                    .isHot(true)
                    .hotRank(random.nextInt(20) + 1)
                    .lastUpdated(LocalDateTime.now())
                    .build();
            stockMapper.insert(stock);
            log.info("Created stock: {} - {}", symbol, nameCn);
        } else {
            // Update existing stock with new price data
            stock.setPrice(price);
            stock.setChangeAmount(changeAmount);
            stock.setChangePercent(changePercent);
            stock.setHigh(high);
            stock.setLow(low);
            stock.setOpen(open);
            stock.setPrevClose(prevClose);
            stock.setVolume(volume);
            stock.setLastUpdated(LocalDateTime.now());
            stockMapper.updateById(stock);
            log.info("Updated stock: {} - {} ({}%)", symbol, nameCn, changePercent);
        }

        return stock;
    }

    /**
     * Generate a realistic base price based on stock symbol and market
     */
    private BigDecimal generateBasePrice(String symbol, Stock.StockMarket market) {
        // Use symbol hash to generate consistent base prices
        int hash = Math.abs(symbol.hashCode());
        
        switch (market) {
            case SH:
            case SZ:
                // A股价格范围: 10-2000
                return BigDecimal.valueOf((hash % 1990) + 10).setScale(2, RoundingMode.HALF_UP);
            case HK:
                // 港股价格范围: 50-500
                return BigDecimal.valueOf((hash % 450) + 50).setScale(2, RoundingMode.HALF_UP);
            case US:
                // 美股价格范围: 50-500
                return BigDecimal.valueOf((hash % 450) + 50).setScale(2, RoundingMode.HALF_UP);
            default:
                return BigDecimal.valueOf(100);
        }
    }

    /**
     * Generate a realistic change percent (-5% to +5%)
     */
    private BigDecimal generateChangePercent() {
        double change = (random.nextDouble() - 0.5) * 10; // -5% to +5%
        return BigDecimal.valueOf(change).setScale(2, RoundingMode.HALF_UP);
    }
}
