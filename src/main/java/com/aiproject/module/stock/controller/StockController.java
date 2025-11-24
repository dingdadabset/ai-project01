package com.aiproject.module.stock.controller;

import com.aiproject.module.stock.model.MarketIndex;
import com.aiproject.module.stock.model.StockQuote;
import com.aiproject.module.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Stock Controller
 * REST API endpoints for stock market functionality
 * Following Halo's RESTful API design pattern
 */
@Slf4j
@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * Get stock quote for a specific symbol
     * 
     * @param symbol Stock symbol (e.g., "AAPL")
     * @return Stock quote data
     */
    @GetMapping("/quote/{symbol}")
    public ResponseEntity<StockQuote> getStockQuote(@PathVariable String symbol) {
        log.info("GET /api/stock/quote/{}", symbol);
        StockQuote quote = stockService.getStockQuote(symbol);
        return ResponseEntity.ok(quote);
    }

    /**
     * Get trending/hot stocks
     * 
     * @param limit Optional limit parameter (default: 10)
     * @return List of trending stocks
     */
    @GetMapping("/trending")
    public ResponseEntity<List<StockQuote>> getTrendingStocks(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/stock/trending - limit: {}", limit);
        List<StockQuote> trendingStocks = stockService.getTrendingStocks(limit);
        return ResponseEntity.ok(trendingStocks);
    }

    /**
     * Get top gaining stocks
     * 
     * @param limit Optional limit parameter (default: 10)
     * @return List of top gainers
     */
    @GetMapping("/gainers")
    public ResponseEntity<List<StockQuote>> getTopGainers(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/stock/gainers - limit: {}", limit);
        List<StockQuote> topGainers = stockService.getTopGainers(limit);
        return ResponseEntity.ok(topGainers);
    }

    /**
     * Get top losing stocks
     * 
     * @param limit Optional limit parameter (default: 10)
     * @return List of top losers
     */
    @GetMapping("/losers")
    public ResponseEntity<List<StockQuote>> getTopLosers(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/stock/losers - limit: {}", limit);
        List<StockQuote> topLosers = stockService.getTopLosers(limit);
        return ResponseEntity.ok(topLosers);
    }

    /**
     * Get market indices overview
     * 
     * @return List of major market indices
     */
    @GetMapping("/indices")
    public ResponseEntity<List<MarketIndex>> getMarketIndices() {
        log.info("GET /api/stock/indices");
        List<MarketIndex> indices = stockService.getMarketIndices();
        return ResponseEntity.ok(indices);
    }
}
