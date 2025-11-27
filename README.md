# Halo-Blog

一个基于 [Halo](https://github.com/halo-dev/halo) 架构设计的完整博客系统。

A complete blog system based on [Halo](https://github.com/halo-dev/halo)'s architecture.

## 项目简介 (Project Introduction)

本项目是一个功能完整的博客网站，参考 Halo 开源项目的技术架构和设计理念，采用 MVC 模块化设计。

This is a full-featured blog website that references the technical architecture and design philosophy of the Halo open-source project with MVC modular design.

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
- ✅ 分类管理（增删改查）
- ✅ 标签管理（增删改查）
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
- ✅ 文件信息管理
- ✅ 媒体库
- ✅ 图片尺寸记录

## 技术架构 (Technical Architecture)

### 核心技术栈 (Core Technology Stack)

- **Java 17**: Modern Java features
- **Spring Boot 3.1.5**: Enterprise application framework
- **MyBatis Plus 3.5.5**: Database abstraction layer (ORM)
- **Spring Security**: Authentication and authorization
- **MySQL 8.0+**: Primary database (production)
- **H2 Database**: Embedded database (development/testing)
- **Maven**: Build tool
- **Lombok**: Code generation
- **CommonMark**: Markdown processing

### MVC 模块化设计 (MVC Modular Design)

Following Halo's modular architecture with MVC pattern:

```
src/main/java/com/aiproject/
├── Application.java                    # Main application
├── config/                             # Configuration classes
│   └── MybatisPlusConfig.java          # MyBatis Plus configuration
├── common/                             # Common utilities
│   └── initializer/                    # Data initialization
└── module/                             # Feature modules
    ├── post/                           # Post module
    │   ├── model/                      # Post entity & DTOs
    │   ├── mapper/                     # MyBatis Plus Mapper
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
- MySQL 8.0+ (推荐) / MySQL 8.0+ (recommended)

### 数据库设置 (Database Setup)

1. 安装并启动 MySQL 数据库 / Install and start MySQL database

2. 创建数据库 / Create database:
```sql
CREATE DATABASE halo_blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. 导入数据库表结构 / Import database schema:
```bash
mysql -u root -p halo_blog < src/main/resources/db/schema.sql
```

4. 配置数据库用户（默认使用 root/root）/ Configure database user (default: root/root):
```sql
-- 如果需要创建新用户 / If you need to create a new user:
-- CREATE USER 'root'@'localhost' IDENTIFIED BY 'root';
-- GRANT ALL PRIVILEGES ON halo_blog.* TO 'root'@'localhost';
-- FLUSH PRIVILEGES;
```

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

### 使用 H2 数据库开发 (Using H2 for Development)

如果你想使用嵌入式 H2 数据库进行开发测试，可以使用以下命令：
If you want to use embedded H2 database for development/testing:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

H2 Console 将在以下地址可用 / H2 Console will be available at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/blog;MODE=MySQL`
- Username: `sa`
- Password: (empty)

### 默认用户 (Default Users)

The system comes with pre-configured users:

- **Admin**: `admin` / `admin123`
- **Author**: `author` / `author123`

### 数据库配置 (Database Configuration)

MySQL 数据库配置（本地开发）/ MySQL database configuration (local development):
- JDBC URL: `jdbc:mysql://localhost:3306/halo_blog`
- Username: `root`
- Password: `root`

## API 文档 (API Documentation)

### 文章 API (Post APIs)

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/posts | 创建文章 |
| GET | /api/posts | 获取文章列表 |
| GET | /api/posts/{id} | 获取文章详情 |
| GET | /api/posts/slug/{slug} | 通过slug获取文章 |
| GET | /api/posts/published | 获取已发布文章 |
| GET | /api/posts/category/{id} | 按分类获取文章 |
| GET | /api/posts/search?keyword= | 搜索文章 |
| PUT | /api/posts/{id} | 更新文章 |
| DELETE | /api/posts/{id} | 删除文章 |

### 分类 API (Category APIs)

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/categories | 创建分类 |
| GET | /api/categories | 获取所有分类 |
| GET | /api/categories/{id} | 获取分类详情 |
| PUT | /api/categories/{id} | 更新分类 |
| DELETE | /api/categories/{id} | 删除分类 |

### 标签 API (Tag APIs)

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/tags | 创建标签 |
| GET | /api/tags | 获取所有标签 |
| GET | /api/tags/{id} | 获取标签详情 |
| GET | /api/tags/slug/{slug} | 通过slug获取标签 |
| PUT | /api/tags/{id} | 更新标签 |
| DELETE | /api/tags/{id} | 删除标签 |

### 评论 API (Comment APIs)

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/comments | 创建评论 |
| GET | /api/comments/post/{postId} | 获取文章评论 |
| GET | /api/comments/{id} | 获取评论详情 |
| PUT | /api/comments/{id}/approve | 审核评论 |
| DELETE | /api/comments/{id} | 删除评论 |

### 用户 API (User APIs)

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/users | 创建用户 |
| GET | /api/users | 获取所有用户 |
| GET | /api/users/{id} | 获取用户详情 |
| GET | /api/users/username/{username} | 通过用户名获取用户 |
| PUT | /api/users/{id} | 更新用户 |
| PUT | /api/users/{id}/status | 更新用户状态 |
| PUT | /api/users/{id}/role | 更新用户角色 |
| DELETE | /api/users/{id} | 删除用户 |

### 页面 API (Page APIs)

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/pages | 创建页面 |
| GET | /api/pages | 获取所有页面 |
| GET | /api/pages/{id} | 获取页面详情 |
| GET | /api/pages/slug/{slug} | 通过slug获取页面 |
| GET | /api/pages/published | 获取已发布页面 |
| PUT | /api/pages/{id} | 更新页面 |
| DELETE | /api/pages/{id} | 删除页面 |

### 附件 API (Attachment APIs)

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/attachments | 创建附件记录 |
| GET | /api/attachments | 获取所有附件 |
| GET | /api/attachments/{id} | 获取附件详情 |
| GET | /api/attachments/uploader/{id} | 按上传者获取附件 |
| GET | /api/attachments/type/{type} | 按类型获取附件 |
| PUT | /api/attachments/{id} | 更新附件 |
| DELETE | /api/attachments/{id} | 删除附件 |

## 开发计划 (Development Roadmap)

### 已完成 (Completed)
- [x] 文章管理模块（完整CRUD）
- [x] 分类模块（完整CRUD）
- [x] 标签模块（完整CRUD）
- [x] 评论系统（完整CRUD）
- [x] 用户管理（完整CRUD）
- [x] 页面管理（完整CRUD）
- [x] 附件管理（完整CRUD）
- [x] MySQL 数据库持久化
- [x] MyBatis Plus ORM 集成
- [x] REST API 接口
- [x] 示例数据初始化
- [x] 主题系统（上传、切换、设置管理）
- [x] 自定义背景功能（一键切换、自定义上传）
- [x] 前端界面（Vue 3 + TypeScript）
- [x] 管理后台
- [x] 文件上传实现

### 计划中 (Planned)
- [ ] 插件系统
- [ ] RSS 订阅
- [ ] 全文搜索（Elasticsearch）
- [ ] 缓存优化（Redis）
- [ ] SEO 优化
- [ ] 社交媒体集成
- [ ] 邮件通知
- [ ] 数据迁移工具

## 主题系统 (Theme System)

本项目支持完整的主题系统，包括主题上传、切换、设置管理和自定义背景图片功能。

### 主题上传功能 (Theme Upload Feature)

#### 如何上传主题 (How to Upload a Theme)

1. **准备主题包**: 将主题文件打包为 ZIP 格式，确保 ZIP 根目录包含 `theme.yaml` 配置文件
2. **进入主题管理**: 访问 `/admin/themes` 或点击管理后台的"Theme Management"
3. **上传主题**: 点击右上角的 "📦 Upload Theme" 按钮，选择 ZIP 文件
4. **激活主题**: 上传成功后，点击主题卡片上的 "Activate" 按钮激活主题

#### 主题包结构 (Theme Package Structure)

```
my-theme.zip
├── theme.yaml          # 主题配置文件（必需）
├── screenshot.png      # 主题截图（推荐）
├── templates/          # 模板文件
├── static/            # 静态资源
│   ├── css/
│   ├── js/
│   └── images/
│       └── backgrounds/  # 背景图片目录
└── i18n/              # 国际化文件
    ├── en.yaml
    └── zh-CN.yaml
```

#### theme.yaml 配置示例 (theme.yaml Example)

```yaml
id: my-theme
name: My Custom Theme
version: 1.0.0
author:
  name: Your Name
  website: https://example.com
description: A beautiful custom theme
screenshot: screenshot.png
requires: "1.0.0"

settings:
  - group: background
    label: Background Settings
    items:
      - name: currentBackground
        label: Current Background
        type: select
        defaultValue: bg1
        options:
          - label: Background 1
            value: bg1
      - name: customBackgroundUrl
        label: Custom Background URL
        type: text
        description: Enter custom background image URL
        defaultValue: ""
      - name: backgroundOpacity
        label: Background Opacity
        type: select
        defaultValue: "0.85"
        options:
          - label: 100%
            value: "1"
          - label: 85%
            value: "0.85"
```

### 自定义背景功能 (Custom Background Feature)

#### 一键切换背景 (One-Click Background Switch)

1. **进入主题设置**: 在主题管理页面，点击主题卡片上的 "Settings" 按钮
2. **选择预设背景**: 在 "Background Settings" 中选择预设的背景图片
3. **上传自定义背景**: 
   - 点击页面右下角的 "🖼️" 浮动按钮打开背景选择器
   - 选择预设背景或点击 "Upload Custom" 上传自己的图片
   - 支持 JPG、PNG、GIF、WebP、SVG 格式
4. **调整背景效果**: 
   - 设置背景透明度 (Background Opacity)
   - 启用/禁用背景虚化效果 (Blur Effect)

#### 背景显示特性 (Background Display Features)

- **图层最上层显示**: 背景图片在最顶层，但使用虚化效果 (backdrop-filter: blur)
- **文字可读性保护**: 虚化叠加层确保文字清晰可读
- **响应式设计**: 背景自适应各种屏幕尺寸
- **性能优化**: 使用 CSS 硬件加速，不影响页面性能

#### 背景图片 API (Background Image API)

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/uploads | 上传单个文件 |
| POST | /api/uploads/batch | 批量上传文件 |
| GET | /api/uploads/backgrounds | 获取背景图片列表 |
| GET | /api/uploads/{type}/{filename} | 获取指定文件 |
| DELETE | /api/uploads/{type}/{filename} | 删除文件 |

### 内置主题 (Built-in Themes)

1. **Default Theme**: 简洁响应式默认主题
2. **Anime Girls Theme**: 二次元美少女主题，内置20张精美背景图片

## 贡献 (Contributing)

欢迎提交 Issue 和 Pull Request！

## 许可证 (License)

MIT License

## 致谢 (Acknowledgments)

- [Halo](https://github.com/halo-dev/halo) - 优秀的博客系统架构设计
- Spring Boot 社区
- MyBatis Plus 社区
- 所有贡献者
