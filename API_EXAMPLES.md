# API Usage Examples

This document provides examples of how to use the implemented APIs.

## News Module APIs

### 1. Get Hot/Trending News

```bash
curl http://localhost:8080/api/news/hot?limit=5
```

**Response:**
```json
[
  {
    "id": "news-001",
    "title": "AI Breakthrough: New Model Surpasses Human Performance",
    "description": "Researchers announce a new AI model that achieves unprecedented results",
    "source": "TechCrunch",
    "author": "Jane Smith",
    "publishedAt": "2025-11-24T07:00:00",
    "category": "technology",
    "viewCount": 15000,
    "isHot": true
  }
]
```

### 2. Get News by Category

```bash
curl http://localhost:8080/api/news/category/technology?limit=10
```

Categories available: `technology`, `business`, `world`, `sports`

### 3. Get All News

```bash
curl http://localhost:8080/api/news?limit=20
```

## Stock Market Module APIs

### 1. Get Stock Quote

```bash
curl http://localhost:8080/api/stock/quote/AAPL
```

**Response:**
```json
{
  "symbol": "AAPL",
  "name": "Apple Inc.",
  "price": 283.84,
  "change": -9.12,
  "changePercent": -3.21,
  "volume": 3021511,
  "marketCap": 283836822557,
  "exchange": "NASDAQ"
}
```

Supported symbols: `AAPL`, `GOOGL`, `MSFT`, `AMZN`, `TSLA`, `META`, `NVDA`, `NFLX`

### 2. Get Trending Stocks

```bash
curl http://localhost:8080/api/stock/trending?limit=10
```

### 3. Get Top Gainers

```bash
curl http://localhost:8080/api/stock/gainers?limit=10
```

### 4. Get Top Losers

```bash
curl http://localhost:8080/api/stock/losers?limit=10
```

### 5. Get Market Indices

```bash
curl http://localhost:8080/api/stock/indices
```

**Response:**
```json
[
  {
    "name": "S&P 500",
    "symbol": "^GSPC",
    "value": 4712.41,
    "change": 37.89,
    "changePercent": -0.54,
    "topGainers": ["AAPL", "MSFT", "GOOGL"],
    "topLosers": ["TSLA", "META", "NFLX"]
  }
]
```

## Testing with Browser

You can also test the APIs directly in your browser by visiting:

- http://localhost:8080/api/news/hot
- http://localhost:8080/api/stock/quote/AAPL
- http://localhost:8080/api/stock/indices

## Next Steps

To integrate with real APIs:

1. **For News**: Register for API keys at NewsAPI.org or The Guardian API
2. **For Stock Data**: Use Alpha Vantage, Yahoo Finance API, or IEX Cloud
3. Update the service classes to call external APIs instead of using mock data
4. Add API key configuration in `application.properties`
