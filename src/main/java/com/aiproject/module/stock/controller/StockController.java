package com.aiproject.module.stock.controller;

import com.aiproject.module.stock.model.Stock;
import com.aiproject.module.stock.service.StockService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Stock Controller
 * REST API endpoints for stock market data
 */
@Slf4j
@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * Create or update a stock
     */
    @PostMapping
    public ResponseEntity<Stock> createOrUpdateStock(@RequestBody Map<String, Object> request) {
        log.info("POST /api/stocks - Creating/updating stock");
        
        Stock.StockMarket market = null;
        if (request.get("market") != null) {
            market = Stock.StockMarket.valueOf((String) request.get("market"));
        }
        
        Stock stock = stockService.createOrUpdateStock(
                (String) request.get("symbol"),
                (String) request.get("name"),
                (String) request.get("nameCn"),
                market,
                request.get("price") != null ? new BigDecimal(request.get("price").toString()) : null,
                request.get("changeAmount") != null ? new BigDecimal(request.get("changeAmount").toString()) : null,
                request.get("changePercent") != null ? new BigDecimal(request.get("changePercent").toString()) : null,
                request.get("high") != null ? new BigDecimal(request.get("high").toString()) : null,
                request.get("low") != null ? new BigDecimal(request.get("low").toString()) : null,
                request.get("open") != null ? new BigDecimal(request.get("open").toString()) : null,
                request.get("prevClose") != null ? new BigDecimal(request.get("prevClose").toString()) : null,
                request.get("volume") != null ? ((Number) request.get("volume")).longValue() : null,
                request.get("marketCap") != null ? new BigDecimal(request.get("marketCap").toString()) : null,
                request.get("peRatio") != null ? new BigDecimal(request.get("peRatio").toString()) : null
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(stock);
    }

    /**
     * Get stock by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Stock> getStockById(@PathVariable Long id) {
        log.info("GET /api/stocks/{}", id);
        Stock stock = stockService.getStockById(id);
        return ResponseEntity.ok(stock);
    }

    /**
     * Get stock by symbol
     */
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<Stock> getStockBySymbol(@PathVariable String symbol) {
        log.info("GET /api/stocks/symbol/{}", symbol);
        Stock stock = stockService.getStockBySymbol(symbol);
        return ResponseEntity.ok(stock);
    }

    /**
     * List all stocks with pagination
     */
    @GetMapping
    public ResponseEntity<IPage<Stock>> listStocks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/stocks - page: {}, size: {}", page, size);
        IPage<Stock> stocks = stockService.listStocks(page, size);
        return ResponseEntity.ok(stocks);
    }

    /**
     * List hot stocks
     */
    @GetMapping("/hot")
    public ResponseEntity<List<Stock>> listHotStocks(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/stocks/hot - limit: {}", limit);
        List<Stock> stocks = stockService.listHotStocks(limit);
        return ResponseEntity.ok(stocks);
    }

    /**
     * List stocks by market
     */
    @GetMapping("/market/{market}")
    public ResponseEntity<IPage<Stock>> listStocksByMarket(
            @PathVariable String market,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/stocks/market/{}", market);
        Stock.StockMarket stockMarket = Stock.StockMarket.valueOf(market.toUpperCase());
        IPage<Stock> stocks = stockService.listStocksByMarket(stockMarket, page, size);
        return ResponseEntity.ok(stocks);
    }

    /**
     * Search stocks by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<IPage<Stock>> searchStocks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/stocks/search - keyword: {}", keyword);
        IPage<Stock> stocks = stockService.searchStocks(keyword, page, size);
        return ResponseEntity.ok(stocks);
    }

    /**
     * List top gainers
     */
    @GetMapping("/gainers")
    public ResponseEntity<List<Stock>> listGainers(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/stocks/gainers - limit: {}", limit);
        List<Stock> stocks = stockService.listGainers(limit);
        return ResponseEntity.ok(stocks);
    }

    /**
     * List top losers
     */
    @GetMapping("/losers")
    public ResponseEntity<List<Stock>> listLosers(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/stocks/losers - limit: {}", limit);
        List<Stock> stocks = stockService.listLosers(limit);
        return ResponseEntity.ok(stocks);
    }

    /**
     * Update stock price
     */
    @PutMapping("/{id}/price")
    public ResponseEntity<Stock> updatePrice(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("PUT /api/stocks/{}/price", id);
        Stock stock = stockService.updatePrice(
                id,
                new BigDecimal(request.get("price").toString()),
                new BigDecimal(request.get("changeAmount").toString()),
                new BigDecimal(request.get("changePercent").toString())
        );
        return ResponseEntity.ok(stock);
    }

    /**
     * Set stock as hot
     */
    @PutMapping("/{id}/hot")
    public ResponseEntity<Stock> setHot(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        log.info("PUT /api/stocks/{}/hot", id);
        boolean isHot = (Boolean) request.getOrDefault("isHot", true);
        int hotRank = (Integer) request.getOrDefault("hotRank", 0);
        Stock stock = stockService.setHot(id, isHot, hotRank);
        return ResponseEntity.ok(stock);
    }

    /**
     * Delete a stock
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        log.info("DELETE /api/stocks/{}", id);
        stockService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }
}
