package com.aiproject.module.stock.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Stock Quote Model
 * Represents real-time stock market data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockQuote {

    /**
     * Stock symbol/ticker (e.g., "AAPL", "GOOGL")
     */
    private String symbol;

    /**
     * Company name
     */
    private String name;

    /**
     * Current stock price
     */
    private BigDecimal price;

    /**
     * Price change from previous close
     */
    private BigDecimal change;

    /**
     * Percentage change from previous close
     */
    private BigDecimal changePercent;

    /**
     * Opening price for the day
     */
    private BigDecimal open;

    /**
     * Highest price for the day
     */
    private BigDecimal high;

    /**
     * Lowest price for the day
     */
    private BigDecimal low;

    /**
     * Previous closing price
     */
    private BigDecimal previousClose;

    /**
     * Trading volume
     */
    private Long volume;

    /**
     * Market capitalization
     */
    private BigDecimal marketCap;

    /**
     * 52-week high
     */
    private BigDecimal fiftyTwoWeekHigh;

    /**
     * 52-week low
     */
    private BigDecimal fiftyTwoWeekLow;

    /**
     * Stock exchange (e.g., "NASDAQ", "NYSE")
     */
    private String exchange;

    /**
     * Last update timestamp
     */
    private LocalDateTime lastUpdate;

    /**
     * Whether the stock is trending/hot
     */
    private Boolean isTrending;
}
