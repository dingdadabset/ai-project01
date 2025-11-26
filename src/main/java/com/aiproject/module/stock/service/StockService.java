package com.aiproject.module.stock.service;

import com.aiproject.module.stock.mapper.StockMapper;
import com.aiproject.module.stock.model.Stock;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Stock Service
 * Handles stock market business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockService extends ServiceImpl<StockMapper, Stock> {

    private final StockMapper stockMapper;

    /**
     * Create or update a stock
     */
    @Transactional
    public Stock createOrUpdateStock(String symbol, String name, String nameCn, Stock.StockMarket market,
                                     BigDecimal price, BigDecimal changeAmount, BigDecimal changePercent,
                                     BigDecimal high, BigDecimal low, BigDecimal open, BigDecimal prevClose,
                                     Long volume, BigDecimal marketCap, BigDecimal peRatio) {
        log.info("Creating/updating stock: {}", symbol);
        
        Stock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<Stock>().eq(Stock::getSymbol, symbol));
        
        if (stock == null) {
            stock = Stock.builder()
                    .symbol(symbol)
                    .name(name)
                    .nameCn(nameCn)
                    .market(market != null ? market : Stock.StockMarket.OTHER)
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
                    .lastUpdated(LocalDateTime.now())
                    .build();
            stockMapper.insert(stock);
        } else {
            if (name != null) stock.setName(name);
            if (nameCn != null) stock.setNameCn(nameCn);
            if (market != null) stock.setMarket(market);
            if (price != null) stock.setPrice(price);
            if (changeAmount != null) stock.setChangeAmount(changeAmount);
            if (changePercent != null) stock.setChangePercent(changePercent);
            if (high != null) stock.setHigh(high);
            if (low != null) stock.setLow(low);
            if (open != null) stock.setOpen(open);
            if (prevClose != null) stock.setPrevClose(prevClose);
            if (volume != null) stock.setVolume(volume);
            if (marketCap != null) stock.setMarketCap(marketCap);
            if (peRatio != null) stock.setPeRatio(peRatio);
            stock.setLastUpdated(LocalDateTime.now());
            stockMapper.updateById(stock);
        }
        
        return stock;
    }

    /**
     * Get stock by ID
     */
    public Stock getStockById(Long id) {
        Stock stock = stockMapper.selectById(id);
        if (stock == null) {
            throw new RuntimeException("Stock not found");
        }
        return stock;
    }

    /**
     * Get stock by symbol
     */
    public Stock getStockBySymbol(String symbol) {
        Stock stock = stockMapper.selectOne(
                new LambdaQueryWrapper<Stock>().eq(Stock::getSymbol, symbol));
        if (stock == null) {
            throw new RuntimeException("Stock not found");
        }
        return stock;
    }

    /**
     * List all stocks with pagination
     */
    public IPage<Stock> listStocks(int page, int size) {
        Page<Stock> stockPage = new Page<>(page + 1, size);
        return stockMapper.selectPage(stockPage,
                new LambdaQueryWrapper<Stock>().orderByDesc(Stock::getLastUpdated));
    }

    /**
     * List hot stocks
     */
    public List<Stock> listHotStocks(int limit) {
        return stockMapper.selectList(
                new LambdaQueryWrapper<Stock>()
                        .eq(Stock::getIsHot, true)
                        .orderByAsc(Stock::getHotRank)
                        .last("LIMIT " + limit));
    }

    /**
     * List stocks by market
     */
    public IPage<Stock> listStocksByMarket(Stock.StockMarket market, int page, int size) {
        Page<Stock> stockPage = new Page<>(page + 1, size);
        return stockMapper.selectPage(stockPage,
                new LambdaQueryWrapper<Stock>()
                        .eq(Stock::getMarket, market)
                        .orderByDesc(Stock::getLastUpdated));
    }

    /**
     * Search stocks by keyword
     */
    public IPage<Stock> searchStocks(String keyword, int page, int size) {
        Page<Stock> stockPage = new Page<>(page + 1, size);
        return stockMapper.selectPage(stockPage,
                new LambdaQueryWrapper<Stock>()
                        .like(Stock::getSymbol, keyword)
                        .or()
                        .like(Stock::getName, keyword)
                        .or()
                        .like(Stock::getNameCn, keyword)
                        .orderByDesc(Stock::getLastUpdated));
    }

    /**
     * List gainers (top stocks by change percent)
     */
    public List<Stock> listGainers(int limit) {
        return stockMapper.selectList(
                new LambdaQueryWrapper<Stock>()
                        .gt(Stock::getChangePercent, BigDecimal.ZERO)
                        .orderByDesc(Stock::getChangePercent)
                        .last("LIMIT " + limit));
    }

    /**
     * List losers (bottom stocks by change percent)
     */
    public List<Stock> listLosers(int limit) {
        return stockMapper.selectList(
                new LambdaQueryWrapper<Stock>()
                        .lt(Stock::getChangePercent, BigDecimal.ZERO)
                        .orderByAsc(Stock::getChangePercent)
                        .last("LIMIT " + limit));
    }

    /**
     * Update stock price
     */
    @Transactional
    public Stock updatePrice(Long id, BigDecimal price, BigDecimal changeAmount, BigDecimal changePercent) {
        Stock stock = getStockById(id);
        stock.setPrice(price);
        stock.setChangeAmount(changeAmount);
        stock.setChangePercent(changePercent);
        stock.setLastUpdated(LocalDateTime.now());
        stockMapper.updateById(stock);
        return stock;
    }

    /**
     * Set stock as hot
     */
    @Transactional
    public Stock setHot(Long id, boolean isHot, int hotRank) {
        Stock stock = getStockById(id);
        stock.setIsHot(isHot);
        stock.setHotRank(hotRank);
        stockMapper.updateById(stock);
        return stock;
    }

    /**
     * Delete stock
     */
    @Transactional
    public void deleteStock(Long id) {
        log.info("Deleting stock: {}", id);
        Stock stock = getStockById(id);
        stockMapper.deleteById(stock.getId());
    }
}
