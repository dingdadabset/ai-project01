package com.aiproject.module.stock.service;

import com.aiproject.module.stock.model.MarketIndex;
import com.aiproject.module.stock.model.StockQuote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for StockService
 */
@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Test
    void testGetStockQuote() {
        String symbol = "AAPL";
        StockQuote quote = stockService.getStockQuote(symbol);
        
        assertNotNull(quote);
        assertEquals(symbol.toUpperCase(), quote.getSymbol());
        assertNotNull(quote.getName());
        assertNotNull(quote.getPrice());
        assertTrue(quote.getPrice().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void testGetTrendingStocks() {
        List<StockQuote> trendingStocks = stockService.getTrendingStocks(5);
        
        assertNotNull(trendingStocks);
        assertTrue(trendingStocks.size() <= 5);
        
        // Verify all are marked as trending
        for (StockQuote stock : trendingStocks) {
            assertTrue(stock.getIsTrending());
            assertNotNull(stock.getSymbol());
        }
    }

    @Test
    void testGetTopGainers() {
        List<StockQuote> gainers = stockService.getTopGainers(5);
        
        assertNotNull(gainers);
        assertTrue(gainers.size() <= 5);
        
        // Verify all have positive change
        for (StockQuote stock : gainers) {
            assertTrue(stock.getChange().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    @Test
    void testGetTopLosers() {
        List<StockQuote> losers = stockService.getTopLosers(5);
        
        assertNotNull(losers);
        assertTrue(losers.size() <= 5);
        
        // Verify all have negative change
        for (StockQuote stock : losers) {
            assertTrue(stock.getChange().compareTo(BigDecimal.ZERO) < 0);
        }
    }

    @Test
    void testGetMarketIndices() {
        List<MarketIndex> indices = stockService.getMarketIndices();
        
        assertNotNull(indices);
        assertTrue(indices.size() > 0);
        
        // Verify indices have required fields
        for (MarketIndex index : indices) {
            assertNotNull(index.getName());
            assertNotNull(index.getSymbol());
            assertNotNull(index.getValue());
            assertTrue(index.getValue().compareTo(BigDecimal.ZERO) > 0);
        }
    }
}
