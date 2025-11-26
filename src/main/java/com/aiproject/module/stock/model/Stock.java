package com.aiproject.module.stock.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Stock Entity
 * Represents a stock market item
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("stocks")
public class Stock {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String symbol;

    private String name;

    @TableField("name_cn")
    private String nameCn;

    private StockMarket market;

    private BigDecimal price;

    @TableField("change_amount")
    private BigDecimal changeAmount;

    @TableField("change_percent")
    private BigDecimal changePercent;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal open;

    @TableField("prev_close")
    private BigDecimal prevClose;

    private Long volume;

    @TableField("market_cap")
    private BigDecimal marketCap;

    @TableField("pe_ratio")
    private BigDecimal peRatio;

    @TableField("is_hot")
    @Builder.Default
    private Boolean isHot = false;

    @TableField("hot_rank")
    @Builder.Default
    private Integer hotRank = 0;

    @TableField(value = "last_updated", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastUpdated;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public enum StockMarket {
        SH,      // 上海证券交易所
        SZ,      // 深圳证券交易所
        HK,      // 香港交易所
        US,      // 美国股市
        OTHER    // 其他
    }
}
