package com.aiproject.module.stock.mapper;

import com.aiproject.module.stock.model.Stock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Stock Mapper
 */
@Mapper
public interface StockMapper extends BaseMapper<Stock> {
}
