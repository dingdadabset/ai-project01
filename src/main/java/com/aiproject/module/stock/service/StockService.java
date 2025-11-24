package com.aiproject.module.stock.service;

import com.aiproject.module.stock.model.MarketIndex;
import com.aiproject.module.stock.model.StockQuote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Stock Service
 * Handles business logic for fetching and managing stock market data
 * Inspired by Halo's service layer pattern
 */
@Slf4j
@Service
public class StockService {

    /**
     * Get real-time quote for a specific stock symbol
     * In production, this would call external APIs like Yahoo Finance, Alpha Vantage, etc.
     * 
     * @param symbol Stock symbol (e.g., "AAPL")
     * @return Stock quote data
     */
    @Cacheable(value = "stockQuote", key = "#symbol", unless = "#result == null")
    public StockQuote getStockQuote(String symbol) {
        log.info("Fetching stock quote for symbol: {}", symbol);
        
        // In production, call external API here
        return generateMockStockQuote(symbol);
    }

    /**
     * Get trending/hot stocks
     * 
     * @param limit Maximum number of stocks to return
     * @return List of trending stocks
     */
    @Cacheable(value = "trendingStocks", unless = "#result == null")
    public List<StockQuote> getTrendingStocks(int limit) {
        log.info("Fetching trending stocks with limit: {}", limit);
        
        List<StockQuote> stocks = generateMockStockList();
        
        return stocks.stream()
                .filter(stock -> stock.getIsTrending() != null && stock.getIsTrending())
                .sorted(Comparator.comparing(StockQuote::getVolume).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get top gainers
     * 
     * @param limit Maximum number of stocks to return
     * @return List of top gaining stocks
     */
    @Cacheable(value = "topGainers", unless = "#result == null")
    public List<StockQuote> getTopGainers(int limit) {
        log.info("Fetching top gainers with limit: {}", limit);
        
        List<StockQuote> stocks = generateMockStockList();
        
        return stocks.stream()
                .filter(stock -> stock.getChange().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(StockQuote::getChangePercent).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get top losers
     * 
     * @param limit Maximum number of stocks to return
     * @return List of top losing stocks
     */
    @Cacheable(value = "topLosers", unless = "#result == null")
    public List<StockQuote> getTopLosers(int limit) {
        log.info("Fetching top losers with limit: {}", limit);
        
        List<StockQuote> stocks = generateMockStockList();
        
        return stocks.stream()
                .filter(stock -> stock.getChange().compareTo(BigDecimal.ZERO) < 0)
                .sorted(Comparator.comparing(StockQuote::getChangePercent))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get market indices overview
     * 
     * @return List of major market indices
     */
    @Cacheable(value = "marketIndices", unless = "#result == null")
    public List<MarketIndex> getMarketIndices() {
        log.info("Fetching market indices");
        
        return generateMockMarketIndices();
    }

    /**
     * Generate mock stock quote
     * In production, replace with actual API calls
     */
    private StockQuote generateMockStockQuote(String symbol) {
        Map<String, String> symbolNames = Map.of(
                "AAPL", "Apple Inc.",
                "GOOGL", "Alphabet Inc.",
                "MSFT", "Microsoft Corporation",
                "AMZN", "Amazon.com Inc.",
                "TSLA", "Tesla Inc.",
                "META", "Meta Platforms Inc.",
                "NVDA", "NVIDIA Corporation",
                "NFLX", "Netflix Inc."
        );

        String name = symbolNames.getOrDefault(symbol.toUpperCase(), "Unknown Company");
        ThreadLocalRandom random = ThreadLocalRandom.current();
        BigDecimal basePrice = BigDecimal.valueOf(100 + random.nextDouble() * 400);
        BigDecimal change = BigDecimal.valueOf(-10 + random.nextDouble() * 20);
        
        // Prevent division by zero
        BigDecimal changePercent = basePrice.compareTo(BigDecimal.ZERO) > 0 
                ? change.divide(basePrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return StockQuote.builder()
                .symbol(symbol.toUpperCase())
                .name(name)
                .price(basePrice.setScale(2, RoundingMode.HALF_UP))
                .change(change.setScale(2, RoundingMode.HALF_UP))
                .changePercent(changePercent.setScale(2, RoundingMode.HALF_UP))
                .open(basePrice.subtract(BigDecimal.valueOf(random.nextDouble() * 5))
                        .setScale(2, RoundingMode.HALF_UP))
                .high(basePrice.add(BigDecimal.valueOf(random.nextDouble() * 10))
                        .setScale(2, RoundingMode.HALF_UP))
                .low(basePrice.subtract(BigDecimal.valueOf(random.nextDouble() * 10))
                        .setScale(2, RoundingMode.HALF_UP))
                .previousClose(basePrice.subtract(change).setScale(2, RoundingMode.HALF_UP))
                .volume((long) (1000000 + random.nextDouble() * 10000000))
                .marketCap(basePrice.multiply(BigDecimal.valueOf(1000000000))
                        .setScale(0, RoundingMode.HALF_UP))
                .fiftyTwoWeekHigh(basePrice.add(BigDecimal.valueOf(50))
                        .setScale(2, RoundingMode.HALF_UP))
                .fiftyTwoWeekLow(basePrice.subtract(BigDecimal.valueOf(50))
                        .setScale(2, RoundingMode.HALF_UP))
                .exchange("NASDAQ")
                .lastUpdate(LocalDateTime.now())
                .isTrending(random.nextBoolean())
                .build();
    }

    /**
     * Generate mock stock list
     */
    private List<StockQuote> generateMockStockList() {
        List<String> symbols = Arrays.asList("AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", 
                "META", "NVDA", "NFLX", "DIS", "INTC");
        
        return symbols.stream()
                .map(this::generateMockStockQuote)
                .collect(Collectors.toList());
    }

    /**
     * Generate mock market indices
     */
    private List<MarketIndex> generateMockMarketIndices() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        List<MarketIndex> indices = new ArrayList<>();

        indices.add(MarketIndex.builder()
                .name("S&P 500")
                .symbol("^GSPC")
                .value(BigDecimal.valueOf(4500 + random.nextDouble() * 500)
                        .setScale(2, RoundingMode.HALF_UP))
                .change(BigDecimal.valueOf(-50 + random.nextDouble() * 100)
                        .setScale(2, RoundingMode.HALF_UP))
                .changePercent(BigDecimal.valueOf(-1 + random.nextDouble() * 2)
                        .setScale(2, RoundingMode.HALF_UP))
                .topGainers(Arrays.asList("AAPL", "MSFT", "GOOGL"))
                .topLosers(Arrays.asList("TSLA", "META", "NFLX"))
                .build());

        indices.add(MarketIndex.builder()
                .name("NASDAQ")
                .symbol("^IXIC")
                .value(BigDecimal.valueOf(14000 + random.nextDouble() * 2000)
                        .setScale(2, RoundingMode.HALF_UP))
                .change(BigDecimal.valueOf(-100 + random.nextDouble() * 200)
                        .setScale(2, RoundingMode.HALF_UP))
                .changePercent(BigDecimal.valueOf(-1.5 + random.nextDouble() * 3)
                        .setScale(2, RoundingMode.HALF_UP))
                .topGainers(Arrays.asList("NVDA", "GOOGL", "AMZN"))
                .topLosers(Arrays.asList("INTC", "DIS", "NFLX"))
                .build());

        indices.add(MarketIndex.builder()
                .name("Dow Jones")
                .symbol("^DJI")
                .value(BigDecimal.valueOf(35000 + random.nextDouble() * 5000)
                        .setScale(2, RoundingMode.HALF_UP))
                .change(BigDecimal.valueOf(-200 + random.nextDouble() * 400)
                        .setScale(2, RoundingMode.HALF_UP))
                .changePercent(BigDecimal.valueOf(-0.8 + random.nextDouble() * 1.6)
                        .setScale(2, RoundingMode.HALF_UP))
                .topGainers(Arrays.asList("AAPL", "MSFT", "DIS"))
                .topLosers(Arrays.asList("INTC", "TSLA", "META"))
                .build());

        return indices;
    }
}
