# Halo-Blog

一个基于 [Halo](https://github.com/halo-dev/halo) 架构设计的完整博客系统。

A complete blog system based on [Halo](https://github.com/halo-dev/halo)'s architecture.

## 项目简介 (Project Introduction)

本项目是一个功能完整的博客网站，参考 Halo 开源项目的技术架构和设计理念，采用模块化设计。

This is a full-featured blog website that references the technical architecture and design philosophy of the Halo open-source project with a modular design.

## 核心功能 (Core Features)

### 1. 文章管理 (Post Management)
- ✅ 创建、编辑、删除文章
- ✅ 文章状态管理（草稿、已发布、私密）
- ✅ Markdown 支持
- ✅ 文章分类和标签
- ✅ 文章搜索
- ✅ 浏览量统计
- ✅ 点赞功能
- ✅ 评论计数

### 2. 分类和标签 (Categories & Tags)
- ✅ 分类管理
- ✅ 标签管理
- ✅ 自动生成 URL slug
- ✅ 文章数量统计

### 3. 评论系统 (Comment System)
- ✅ 用户评论
- ✅ 访客评论（需提供昵称和邮箱）
- ✅ 评论审核
- ✅ 嵌套评论支持

### 4. 用户管理 (User Management)
- ✅ 用户注册和认证
- ✅ 角色管理（管理员、作者、订阅者）
- ✅ 用户状态管理
- ✅ 多作者支持

### 5. 页面管理 (Page Management)
- ✅ 静态页面创建
- ✅ 页面状态管理
- ✅ 浏览量统计

### 6. 附件管理 (Attachment Management)
- ✅ 文件上传
- ✅ 媒体库
- ✅ 图片尺寸记录

## 技术架构 (Technical Architecture)

### 核心技术栈 (Core Technology Stack)

- **Java 17**: Modern Java features
- **Spring Boot 3.1.5**: Enterprise application framework
- **Spring Data JPA**: Database abstraction layer
- **Spring Security**: Authentication and authorization
- **H2 Database**: Embedded database (development)
- **Hibernate**: ORM framework
- **Maven**: Build tool
- **Lombok**: Code generation
- **CommonMark**: Markdown processing

### 模块化设计 (Modular Design)

Following Halo's modular architecture:

```
src/main/java/com/aiproject/
├── Application.java                    # Main application
├── common/                             # Common utilities
│   └── initializer/                    # Data initialization
└── module/                             # Feature modules
    ├── post/                           # Post module
    │   ├── model/                      # Post entity & DTOs
    │   ├── repository/                 # Data access
    │   ├── service/                    # Business logic
    │   └── controller/                 # REST API
    ├── category/                       # Category module
    ├── tag/                            # Tag module
    ├── comment/                        # Comment module
    ├── user/                           # User module
    ├── page/                           # Page module
    └── attachment/                     # Attachment module
```

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

### 默认用户 (Default Users)

The system comes with pre-configured users:

- **Admin**: `admin` / `admin123`
- **Author**: `author` / `author123`

### 数据库控制台 (Database Console)

H2 Console is available at: `http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:file:./data/blog`
- Username: `sa`
- Password: (empty)

## API 文档 (API Documentation)

### 文章 API (Post APIs)

#### 创建文章 (Create Post)
```bash
POST /api/posts
Content-Type: application/json

{
  "title": "我的第一篇博客",
  "summary": "这是摘要",
  "content": "<p>文章内容</p>",
  "originalContent": "# 文章内容",
  "status": "PUBLISHED",
  "categoryId": 1,
  "tags": ["Java", "Spring Boot"],
  "allowComment": true
}
```

#### 获取文章列表 (List Posts)
```bash
GET /api/posts?page=0&size=10&sortBy=createdAt&direction=DESC
```

#### 获取已发布文章 (List Published Posts)
```bash
GET /api/posts/published?page=0&size=10
```

#### 获取文章详情 (Get Post by ID)
```bash
GET /api/posts/{id}
```

#### 通过 Slug 获取文章 (Get Post by Slug)
```bash
GET /api/posts/slug/{slug}
```

#### 搜索文章 (Search Posts)
```bash
GET /api/posts/search?keyword=Java&page=0&size=10
```

#### 按分类获取文章 (Get Posts by Category)
```bash
GET /api/posts/category/{categoryId}?page=0&size=10
```

#### 更新文章 (Update Post)
```bash
PUT /api/posts/{id}
Content-Type: application/json

{
  "title": "更新后的标题",
  "content": "<p>更新后的内容</p>",
  ...
}
```

#### 删除文章 (Delete Post)
```bash
DELETE /api/posts/{id}
```

### 分类 API (Category APIs)

#### 创建分类 (Create Category)
```bash
POST /api/categories
Content-Type: application/json

{
  "name": "技术",
  "description": "技术相关文章"
}
```

#### 获取所有分类 (List Categories)
```bash
GET /api/categories
```

#### 删除分类 (Delete Category)
```bash
DELETE /api/categories/{id}
```

### 评论 API (Comment APIs)

#### 创建评论 (Create Comment)
```bash
POST /api/comments
Content-Type: application/json

{
  "postId": 1,
  "userId": 1,              # Optional, for logged-in users
  "guestName": "访客",       # Required if userId is null
  "guestEmail": "guest@example.com",  # Required if userId is null
  "content": "很棒的文章！"
}
```

#### 获取文章评论 (Get Comments by Post)
```bash
GET /api/comments/post/{postId}?page=0&size=10
```

#### 审核评论 (Approve Comment)
```bash
PUT /api/comments/{id}/approve
```

#### 删除评论 (Delete Comment)
```bash
DELETE /api/comments/{id}
```

## 数据库设计 (Database Schema)

### 主要表结构 (Main Tables)

- **users** - 用户表
- **posts** - 文章表
- **categories** - 分类表
- **tags** - 标签表
- **post_tags** - 文章标签关联表
- **comments** - 评论表
- **pages** - 页面表
- **attachments** - 附件表

## 设计理念 (Design Principles)

### 借鉴 Halo 的架构 (Inspired by Halo)

1. **模块化设计**: 每个功能模块独立，易于维护和扩展
2. **分层架构**: Controller → Service → Repository
3. **RESTful API**: 清晰的 API 设计
4. **JPA/Hibernate**: 对象关系映射
5. **依赖注入**: 松耦合设计
6. **实体关系**: 使用 JPA 注解定义关系

## 开发计划 (Development Roadmap)

### 已完成 (Completed)
- [x] 文章管理模块
- [x] 分类和标签模块
- [x] 评论系统
- [x] 用户管理
- [x] 页面管理
- [x] 附件管理
- [x] 数据库持久化
- [x] REST API 接口
- [x] 示例数据初始化

### 计划中 (Planned)
- [ ] 主题系统
- [ ] 插件系统
- [ ] RSS 订阅
- [ ] 全文搜索（Elasticsearch）
- [ ] 缓存优化（Redis）
- [ ] 前端界面
- [ ] 管理后台
- [ ] 文件上传实现
- [ ] 图片处理
- [ ] SEO 优化
- [ ] 社交媒体集成
- [ ] 邮件通知
- [ ] 数据迁移工具

## 贡献 (Contributing)

欢迎提交 Issue 和 Pull Request！

## 许可证 (License)

MIT License

## 致谢 (Acknowledgments)

- [Halo](https://github.com/halo-dev/halo) - 优秀的博客系统架构设计
- Spring Boot 社区
- 所有贡献者
