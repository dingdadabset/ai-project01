# AI Project 01

A modular Spring Boot application inspired by [Halo](https://github.com/halo-dev/halo)'s architecture, featuring hot news and stock market information modules.

## 项目简介 (Project Introduction)

本项目参考了 Halo 开源项目的技术架构，采用模块化设计，提供热点新闻和股市行情功能。

This project is inspired by the technical architecture of the Halo open-source project, featuring a modular design with hot news and stock market functionality.

## 技术架构 (Technical Architecture)

### 核心技术栈 (Core Technology Stack)

- **Java 17**: Modern Java features and performance
- **Spring Boot 3.1.5**: Enterprise-grade application framework
- **Maven**: Dependency management and build tool
- **Lombok**: Reduce boilerplate code
- **Spring Cache**: In-memory caching for performance
- **Spring WebFlux**: Reactive HTTP client for external API calls

### 架构设计 (Architecture Design)

Following Halo's modular architecture principles:

```
ai-project01/
├── src/main/java/com/aiproject/
│   ├── Application.java                    # Main application entry point
│   └── module/
│       ├── news/                           # News module
│       │   ├── controller/                 # REST API controllers
│       │   ├── service/                    # Business logic layer
│       │   └── model/                      # Data models
│       └── stock/                          # Stock market module
│           ├── controller/                 # REST API controllers
│           ├── service/                    # Business logic layer
│           └── model/                      # Data models
└── src/main/resources/
    └── application.properties              # Configuration
```

## 功能模块 (Functional Modules)

### 1. 热点新闻模块 (Hot News Module)

提供热点新闻获取和分类功能。

**API Endpoints:**

- `GET /api/news/hot?limit=10` - 获取热点新闻
- `GET /api/news/category/{category}?limit=10` - 按分类获取新闻
- `GET /api/news?limit=20` - 获取所有新闻

**Features:**
- Hot/trending news articles
- Category-based news filtering (technology, business, world, sports, etc.)
- Caching for improved performance
- Extensible design for integration with external news APIs

### 2. 股市行情模块 (Stock Market Module)

提供实时股票行情和市场指数信息。

**API Endpoints:**

- `GET /api/stock/quote/{symbol}` - 获取指定股票行情
- `GET /api/stock/trending?limit=10` - 获取热门股票
- `GET /api/stock/gainers?limit=10` - 获取涨幅榜
- `GET /api/stock/losers?limit=10` - 获取跌幅榜
- `GET /api/stock/indices` - 获取市场指数概览

**Features:**
- Real-time stock quotes (simulated, ready for API integration)
- Trending stocks tracking
- Top gainers and losers
- Major market indices (S&P 500, NASDAQ, Dow Jones)
- Caching for improved performance

## 快速开始 (Quick Start)

### 前置要求 (Prerequisites)

- Java 17 or higher
- Maven 3.6+

### 构建和运行 (Build and Run)

1. Clone the repository:
```bash
git clone https://github.com/dingdadabset/ai-project01.git
cd ai-project01
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

4. The application will start on `http://localhost:8080`

### 测试 API (Testing API)

使用 curl 或浏览器测试 API：

```bash
# Get hot news
curl http://localhost:8080/api/news/hot

# Get technology news
curl http://localhost:8080/api/news/category/technology

# Get stock quote for Apple
curl http://localhost:8080/api/stock/quote/AAPL

# Get trending stocks
curl http://localhost:8080/api/stock/trending

# Get market indices
curl http://localhost:8080/api/stock/indices
```

## 设计理念 (Design Principles)

### 借鉴 Halo 的优秀设计 (Inspired by Halo's Best Practices)

1. **模块化设计 (Modular Design)**: 
   - Clear separation of concerns with distinct modules
   - Each module is self-contained with its own models, services, and controllers

2. **服务层模式 (Service Layer Pattern)**:
   - Business logic encapsulated in service classes
   - Controllers remain thin, delegating to services

3. **RESTful API 设计 (RESTful API Design)**:
   - Clean, intuitive API endpoints
   - Proper HTTP methods and status codes
   - Query parameters for filtering and pagination

4. **缓存策略 (Caching Strategy)**:
   - Spring Cache annotations for performance optimization
   - Configurable cache TTL and eviction policies

5. **可扩展性 (Extensibility)**:
   - Easy to add new modules
   - Ready for integration with external APIs
   - Plugin-friendly architecture

## 扩展指南 (Extension Guide)

### 集成真实 API (Integrating Real APIs)

The current implementation uses mock data. To integrate real APIs:

#### News APIs:
- [NewsAPI](https://newsapi.org/)
- [The Guardian API](https://open-platform.theguardian.com/)
- RSS feeds from major news sources

#### Stock Market APIs:
- [Alpha Vantage](https://www.alphavantage.co/)
- [Yahoo Finance API](https://www.yahoofinanceapi.com/)
- [IEX Cloud](https://iexcloud.io/)

### 添加新模块 (Adding New Modules)

1. Create module package structure under `com.aiproject.module/`
2. Define models in `model/` package
3. Implement business logic in `service/` package
4. Create REST endpoints in `controller/` package
5. Add caching and configuration as needed

## 性能优化 (Performance Optimization)

- **Caching**: Spring Cache with configurable TTL
- **Async Processing**: Ready for @Async methods
- **Connection Pooling**: WebFlux for efficient HTTP calls
- **Lazy Loading**: Load data on-demand

## 未来改进 (Future Improvements)

- [ ] Integration with real news and stock APIs
- [ ] Database persistence (H2, PostgreSQL)
- [ ] User authentication and authorization
- [ ] WebSocket for real-time updates
- [ ] Admin dashboard UI
- [ ] Docker containerization
- [ ] Kubernetes deployment
- [ ] Metrics and monitoring
- [ ] Internationalization (i18n)
- [ ] Rate limiting and API throttling

## 贡献 (Contributing)

Contributions are welcome! Please feel free to submit a Pull Request.

## 许可证 (License)

This project is open source and available under the MIT License.

## 致谢 (Acknowledgments)

- [Halo](https://github.com/halo-dev/halo) - For the excellent architectural inspiration
- Spring Boot community for the amazing framework
- All contributors and users of this project
