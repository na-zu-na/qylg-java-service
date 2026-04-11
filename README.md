# qylg-java-service

基于 Spring Boot 4 的后端服务，整合了以下几类能力：

- 商品商城：普通商品、定制商品、购物车、订单、评价
- AI 能力：AI 搜索商品、AI 智能填写定制订单
- 内容社区：文章发布、发现页、文章搜索、评论、点赞
- 客服聊天：WebSocket 实时会话、客服分配、会话列表、导出聊天记录
- 用户与后台：微信登录、后台登录、账号管理、用户状态管理
- 基础设施：PostgreSQL、Redis、Elasticsearch、Cloudflare R2

## 项目特点与亮点

### 业务特点

- 不是单一商城接口，而是把商品交易、内容社区和在线客服整合在一个系统里
- 同时支持普通商品和定制商品，两套业务链路并存
- 增加了 AI 商品检索和 AI 定制填单能力，降低搜索和下单门槛
- 从内容种草、商品浏览、下单购买到售后沟通，形成了完整业务闭环
- 既有用户侧能力，也有后台运营和管理能力

### 核心亮点

- 搜索能力完整：商品和文章都接入了 Elasticsearch，同时保留数据库兜底查询，兼顾搜索体验和系统可用性
- AI 搜索增强：支持把自然语言商品需求解析成结构化搜索条件，再结合现有搜索链路返回商品结果
- AI 智能填单：针对定制商品，AI 可根据用户描述自动回填用途、风格、材质、预算、颜色、纹样、尺寸和备注
- Redis 使用深入：不仅用于热点缓存，还承担登录态、Token 黑名单、客服分配、在线状态、消息重试等关键职责
- 客服聊天模块完整：不只是 WebSocket 收发消息，还包括会话创建、客服分配、未读数、已读处理、聊天导出和重试机制
- 定制业务建模清晰：围绕风格、材质、预算、图片、报价等定制场景字段进行了单独设计，不是把定制逻辑硬塞进普通商品表
- 后台能力成体系：支持商品、订单、文章、用户、系统账号等多类管理接口

### 关键技术点

- 统一鉴权：通过 JWT 拦截 `/api/**` 和 `/admin/**`，并结合 Redis 管理登录态与黑名单，实现可失效、可登出的令牌机制
- 搜索分层设计：ES 优先负责召回和排序，数据库负责兜底，降低外部搜索组件故障对主流程的影响
- AI + 搜索组合：先通过外部 AI 服务把自然语言解析成 `keyword`、`type`、`sort_by`、价格区间等结构化条件，再复用既有商品搜索能力
- AI 结果约束：定制订单智能填写不是直接盲信大模型返回，而是基于商品可选风格、材质和预设预算区间做候选约束与结果校验
- PostgreSQL JSONB 建模：用于图片、颜色、图案等非结构化字段，适合定制化业务扩展
- Redis + Lua 原子分配：客服分配过程通过 Lua 脚本保证原子性，避免并发下重复分配和负载不准
- ACK 重试机制：聊天消息发送后进入确认与延迟重试流程，提升实时消息投递的可靠性
- 缓存策略明确：热门文章、推荐商品等热点数据进入 Redis，减少数据库与搜索引擎压力

### 项目价值

- 适合作为“电商 + 内容 + IM 客服”一体化后端项目
- 既能体现常规 CRUD 和后台管理能力，也能体现搜索、缓存、即时通信、并发控制等进阶能力
- 新增 AI 能力后，也能体现大模型接入、结构化意图解析、候选约束校验和业务落地能力

## 技术栈

- Java 17
- Spring Boot 4.0.3
- Spring MVC
- MyBatis-Plus
- PostgreSQL
- Redis
- Redisson
- Elasticsearch
- WebClient
- WebSocket
- Thymeleaf
- JWT
- AWS SDK v2 for S3-compatible R2
- Lombok / Fastjson2 / Hutool

## 当前模块

### 1. 用户与鉴权

- `POST /auth/wechat-login`：微信登录
- `POST /admin/login`：后台账号登录
- `POST /admin/logout`：后台登出
- `/api/**` 与 `/admin/**` 默认走 JWT 拦截
- 鉴权头格式：`Authorization: Bearer <token>`
- Redis 中维护用户/管理员登录态，以及 token 黑名单

### 2. 商品商城

- 商城首页推荐与热门商品
- 普通商品搜索、详情、评价
- 定制商品列表、详情、模板配置
- AI 商品搜索
- 购物车新增与查询
- 普通订单创建、列表、详情、确认收货、评价
- 定制订单创建、列表、收货、确认、报价、图片补充
- AI 智能填写定制订单
- 后台商品管理、商品上下架、商品配置项管理

相关接口前缀：

- `/shop/**`
- `/api/cart/**`
- `/api/order/**`
- `/api/ai/**`

### AI 能力说明

- `POST /api/ai/search/product`
  根据用户自然语言描述解析商品检索条件，例如关键词、商品类型、排序方式、价格区间，再返回匹配商品
- `POST /api/ai/custom/product`
  根据定制商品模板和用户需求描述，智能回填定制订单表单
- AI 填单结果覆盖字段包括：
  `purpose`、`style`、`material`、`budgetRange`、`colors`、`patterns`、`size`、`remark`
- 定制填单会先读取当前商品支持的风格、材质等候选项，再把候选范围传给 AI 服务，最后对 AI 返回值做二次校验，避免返回超出业务范围的数据

### 3. 内容社区

- 文章列表
- 热门文章
- 发现页分类内容
- 文章详情
- 文章发布
- 我的文章
- 文章删除
- 文章点赞
- 文章评论
- 管理端文章状态管理
- 文章 ES 搜索

相关接口前缀：

- `/articles`
- `/discover/**`
- `/api/articles-detail`
- `/api/article/post`
- `/api/articles/{id}/like`
- `/api/articles/comments`
- `/admin/articles`

### 4. 在线客服聊天

- WebSocket 接入端点：`/ws/chat/{userId}`
- 会话查询与创建
- 客服会话列表
- 客服工作台
- 客服在线状态与接待状态
- 会话已读处理
- 聊天记录导出为 HTML
- Redis + Lua 做客服最小负载分配
- ACK 重试队列保证消息至少投递一次

相关接口前缀：

- `/api/chat/**`

### 5. 文件上传

- `POST /api/upload/avatar`
- 使用 Cloudflare R2 作为对象存储

## 项目结构

```text
src/main/java/com/cc/qylgjavaservice
├─ config        Spring / MyBatis / Redis / WebSocket 配置
├─ controller    HTTP 接口
├─ service       业务接口与实现
├─ mapper        MyBatis Mapper
├─ entity        实体定义
├─ dto           接口入参与出参
├─ search        Elasticsearch Repository
├─ utils         JWT、上下文、Redis 常量等工具
└─ websocket     WebSocket 聊天处理

src/main/resources
├─ application.yaml
├─ db/table.sql
├─ mapper/*.xml
├─ script/*.lua
└─ templates/chat.html
```

## 运行环境

启动前至少准备以下组件：

- JDK 17+
- Maven 3.9+
- PostgreSQL 14+
- Redis 6+
- Elasticsearch 8/9

## 配置说明

项目当前使用 [application.yaml] 保存配置，主要包括：

- PostgreSQL 连接
- Redis 连接
- Elasticsearch 连接
- AI 服务地址
- 微信 `appID` / `appSecret`
- JWT 密钥与过期时间
- Cloudflare R2 账号、Bucket、Endpoint

建议你在正式环境里做两件事：

1. 不要把真实密钥继续保留在仓库内。
2. 改为环境变量、启动参数或外部配置中心注入。

## 本地启动

### 1. 初始化数据库

执行表结构脚本：

- [src/main/resources/db/table.sql]

项目使用 PostgreSQL，建库后把 `application.yaml` 中的数据源改成你本机的连接信息。

### 2. 启动依赖服务

需要先启动：

- PostgreSQL
- Redis
- Elasticsearch
- AI 服务

如果你要测试上传功能，还需要准备 Cloudflare R2 或兼容的 S3 服务。

### 3. 启动项目

```bash
mvn spring-boot:run
```

或先打包再运行：

```bash
mvn clean package -DskipTests
java -jar target/qylg-java-service-0.0.1-SNAPSHOT.jar
```

## 搜索与缓存说明

- 商品和文章都接入了 Elasticsearch 搜索
- 商品支持 AI 自然语言搜索，AI 先做意图解析，再进入后端搜索与价格过滤流程
- 定制订单支持 AI 智能填写，结果会按候选范围做约束和清洗
- 搜索失败时，商品与文章都存在数据库兜底查询逻辑
- 热门文章、推荐商品等数据会进入 Redis 缓存
- 客服分配、在线状态、ACK 重试依赖 Redis 数据结构与 Lua 脚本

## 数据库核心表

表结构脚本中已经包含当前主要业务表，例如：

- `users`
- `products`
- `product_reviews`
- `shopping_cart`
- `orders`
- `order_items`
- `custom_orders`
- `articles`
- `article_comments`
- `article_likes`
- `conversations`
- `conversation_members`
- `chat_messages`
- `styles`
- `materials`
