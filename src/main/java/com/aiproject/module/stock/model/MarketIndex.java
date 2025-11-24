package com.aiproject.module.stock.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Market Index Model
 * Represents market indices like S&P 500, NASDAQ, Dow Jones
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketIndex {

    /**
     * Index name (e.g., "S&P 500", "NASDAQ", "Dow Jones")
     */
    private String name;

    /**
     * Index symbol (e.g., "^GSPC", "^IXIC", "^DJI")
     */
    private String symbol;

    /**
     * Current index value
     */
    private BigDecimal value;

    /**
     * Change from previous close
     */
    private BigDecimal change;

    /**
     * Percentage change from previous close
     */
    private BigDecimal changePercent;

    /**
     * Top gainers in this index
     */
    private List<String> topGainers;

    /**
     * Top losers in this index
     */
    private List<String> topLosers;
}
