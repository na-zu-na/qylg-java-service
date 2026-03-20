# 项目 README

## 项目简介
本项目是一个基于 Spring Boot 4 的全栈电商与即时通讯系统，集成了微信登录、商品搜索、订单管理、定制服务及高可靠 IM 聊天功能。系统采用 Elasticsearch + MySQL 双引擎架构，利用 Redisson 实现高性能缓存与分布式锁，并通过 WebSocket + Redis 构建毫秒级即时通讯能力。

---

## 核心功能模块

### 1. 用户认证与安全
- **微信 OAuth2.0 登录**：完整实现授权码模式，自动注册/登录，JWT Token 生成与黑名单机制（Redis）。
- **全局鉴权拦截器**：基于 `HandlerInterceptor` 的 Token 校验，支持 Bearer 格式解析与上下文用户信息透传。
- **文件上传**：集成 Cloudflare R2 对象存储，支持头像上传、UUID 防重命名及流式传输。

### 2. 商品与搜索系统
- **多级混合搜索架构**：
    - **ES 召回**：支持标题短语优先匹配（权重 10.0）、拼音容错搜索（`title.pinyin`）、多字段加权（标题>锚文本>内容）。
    - **DB 回捞**：基于 ES 返回的 ID 列表批量加载完整商品详情，内存保序重组确保相关性排序不乱。
    - **兜底策略**：ES 故障或无结果时自动降级为 MySQL `LIKE` 模糊查询。
- **智能推荐**：融合随机打乱与销量热门标记，支持 Redis 缓存预热与动态失效。
- **定制商品**：支持风格/材质多对多关联查询（PostgreSQL 数组聚合），预算自然语言解析（如“500 以内”转为区间查询）。

### 3. 订单与交易闭环
- **高精度金额计算**：全程使用 `BigDecimal`，后端权威计算总价防篡改。
- **快照机制**：订单明细锁定下单瞬间的商品单价，保障交易公平性。
- **聚合查询优化**：利用 PostgreSQL `json_agg` 一次性组装订单主表 + 明细列表，消除 N+1 查询问题。
- **定制订单**：支持非结构化需求解析（颜色/图案 JSONB 存储），状态机驱动生命周期管理。

### 4. 高可靠即时通讯 (IM)
- **WebSocket 全双工通信**：支持用户 - 客服实时消息推送，会话自动创建与历史消息回溯。
- **智能客服路由**：
    - **负载均衡**：基于 Redis ZSet 实现最小负载分配算法。
    - **熔断迁移**：指定客服繁忙/离线时自动无缝切换至空闲客服，保留历史会话上下文。
- **可靠投递机制 (At-Least-Once)**：
    - **ACK 确认**：客户端回执驱动的状态流转。
    - **指数退避重试**：Redis 延迟队列（ZSet）+ Lua 脚本原子调度，支持 5 次超时重推。
    - **死信归档**：重试耗尽后标记失败并告警，防止消息丢失。

### 5. 性能优化亮点
- **缓存策略**：Cache Aside 模式，空值防御防穿透，动态 Key 隔离不同业务场景。
- **数据库特性最大化**：
    - PostgreSQL `JSONB` 存储复杂字段（定制需求/颜色列表）。
    - SQL 层聚合统计（订单总价/数量），避免应用层内存累加。
- **并发控制**：Redis 分布式锁（Lua 脚本）解决客服分配竞态条件，双重检查锁定处理用户重复注册。

---

## 技术栈
| 类别 | 技术选型                                |
| :--- |:------------------------------------|
| **后端框架** | Spring Boot 4, Spring MVC           |
| **数据库** | PostgreSQL (JSONB/数组聚合)      |
| **搜索引擎** | Elasticsearch (全文检索/拼音分词)           |
| **缓存/中间件** | Redis (Redisson 客户端), Cloudflare R2 |
| **通讯协议** | WebSocket, RESTful API              |
| **安全认证** | JWT, OAuth2.0 (微信)                  |
| **ORM 工具** | MyBatis-Plus (LambdaQueryWrapper)   |
| **工具库** | Lombok, Jackson, AWS SDK v2         |

---

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+ / PostgreSQL 14+
- Redis 5.0+
- Elasticsearch 9.x+

### 安装步骤
1. **克隆代码**
   ```bash
   git clone <repository-url>
   cd <project-directory>
   ```

2. **配置环境变量**
   新增 `application.yml` 中的数据库连接、Redis 地址、微信 AppID/Secret 及 R2 存储凭证。

3. **启动服务**
   ```bash
   mvn spring-boot:run
   ```

4. **初始化数据**
   执行 `schema.sql` 创建表结构，导入 `data.sql` 初始商品与测试用户数据。

---

## 目录结构
```
src/main/java/com/example/project
├── config          # 配置类 (Redisson, WebSocket, Swagger)
├── controller      # REST 接口入口
├── service         # 业务逻辑实现 (含缓存/事务控制)
├── mapper          # MyBatis 数据访问层
├── entity          # 数据库实体映射
├── dto             # 数据传输对象
├── utils            # 工具类 (JWT, UUID, BigDecimal 处理)
└── interceptor     # 全局鉴权拦截器
└── websocket      # websocket关键类
└── search         # es repository

```

---

## 接口文档
启动项目后访问 `http://localhost:8080/swagger-ui.html` 查看完整的 API 定义与测试用例。

## 贡献指南
欢迎提交 Issue 和 Pull Request。请确保代码符合 Alibaba Java 规范，并通过单元测试覆盖核心逻辑。

---

## 许可证
MIT License