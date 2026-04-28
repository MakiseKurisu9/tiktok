# TikTok Backend Platform

//This hasn't been update for a while:( because I'm looking for job(it's very hard

A full-featured short-video community backend built with Spring Boot 3, covering the core business domains of a production-grade video platform — user management, video publishing, social graph, content feed, and a real-time hot-ranking engine.

中文版位于英文版结尾

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Redis Key Design](#redis-key-design)
- [Hot Rank Algorithm](#hot-rank-algorithm)
- [API Reference](#api-reference)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Configuration](#configuration)
- [Design Decisions](#design-decisions)

---

## Overview

This project reimplements the backend of a TikTok-style platform as a monolithic Spring Boot service. It is designed to demonstrate production patterns — layered security, cache-resilient data access, time-decay ranking, and cursor-based feed pagination — rather than toy CRUD. The codebase covers approximately 5,000 lines of business logic across five controllers, four service layers, and a suite of infrastructure utilities.

**What it does well:**

- Dual-interceptor authentication pipeline separating public and protected routes
- Cache-resilient video retrieval (null-object pattern + logical expiry) via a reusable `CacheClient`
- ZSet-based, cursor-driven Feed that is immune to insertion drift
- Time-decay hot ranking recalculated nightly via `@Scheduled`
- Snowflake ID generation for globally unique, time-sortable entity IDs
- Aliyun OSS integration for video and avatar binary storage

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                        Client                           │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP
┌────────────────────────▼────────────────────────────────┐
│              Spring MVC  (Controllers)                  │
│   /login  /index  /customer  /video  /upload            │
├─────────────────────────────────────────────────────────┤
│         Interceptor Chain                               │
│  PublicInterceptor → token refresh (all routes)         │
│  TokenInterceptor  → auth guard (protected routes)      │
├─────────────────────────────────────────────────────────┤
│              Service Layer                              │
│  LoginService  IndexService  CustomerService            │
│  VideoService  (+ HotRank scheduler)                    │
├─────────────────────────────────────────────────────────┤
│  Infrastructure Utilities                               │
│  CacheClient · SnowflakeIdWorker · AliOSSUtil           │
│  JwtUtils · PasswordUtils · UserHolder (ThreadLocal)    │
├──────────────────┬──────────────────┬───────────────────┤
│   MySQL 8        │   Redis           │  Aliyun OSS       │
│   (MyBatis)      │   (Redisson +     │  (video/avatar    │
│   Primary store  │    StringRedis)   │   binary store)   │
└──────────────────┴──────────────────┴───────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.4, Spring MVC |
| ORM | MyBatis 3.0 + PageHelper |
| Primary DB | MySQL 8.0 (InnoDB, utf8mb4) |
| Cache / Data structure store | Redis + Redisson (distributed lock) |
| Object storage | Aliyun OSS SDK 3.17 |
| Auth | JWT (JJWT) + BCrypt password hashing |
| Captcha | Kaptcha 2.3 (image CAPTCHA) |
| Mail | Spring Mail (SMTP / Gmail) |
| ID generation | Snowflake algorithm (custom implementation) |
| Build | Maven 3.9, Java 17 |
| Scheduled tasks | Spring `@Scheduled` |

---

## Features

### Authentication & Security

- **Dual-interceptor pipeline** — `PublicInterceptor` runs on every request to silently refresh token TTL when a valid session exists; `TokenInterceptor` guards protected endpoints and returns 401 on missing or expired sessions.
- **Token storage in Redis** — JWT tokens are stored server-side under `user:token:{userId}`, enabling forceful session revocation (logout, ban) without waiting for JWT natural expiry. TTL is sliding: refreshed to 24 hours on every authenticated request.
- **Registration challenge** — Kaptcha image CAPTCHA validated first; Gmail SMTP email verification code validated second. Both codes are stored in Redis with a 5-minute TTL and deleted on successful consumption.
- **Password hashing** — BCrypt with default cost factor; plaintext passwords are never persisted.

### Video Publishing & Storage

- Videos and cover images are uploaded to **Aliyun OSS**. The OSS key is stored in `file_protect` to support future access-control revocation without re-uploading.
- Video entities carry a **Snowflake ID** as primary key — 64-bit, time-ordered, collision-safe under distributed concurrency without a centralized sequence table.

### Content Feed (Social Graph)

- When a user follows someone, subsequent video publications from that creator are **pushed** into the follower's inbox ZSet (`feed:{followerId}`) with `ZADD score=publishTimestamp member=videoId`.
- Feed retrieval uses **cursor-based scroll** (`ZREVRANGEBYSCORE max {lastMinScore} LIMIT 0 10`) rather than offset pagination. This eliminates the drift problem where inserted items shift the page window, causing duplicates or gaps on refresh.

### Hot Rank Engine

The ranking pipeline runs as a nightly `@Scheduled(cron = "0 0 0 * * ?")` job:

1. Fetch all videos from MySQL.
2. Compute a **time-decay hot score** per video (see [Hot Rank Algorithm](#hot-rank-algorithm)).
3. Take the top 10 by score.
4. Atomically replace `video:hot:top10` in Redis (delete → ZADD → EXPIRE 1 day).

A separate job (same schedule, fires every 3rd day of the year) computes **trending videos** — videos created in the past 72 hours with a hot score exceeding the `HOT_LIMIT` threshold — and preloads them into the video cache for low-latency reads.

### Cache Resilience (`CacheClient`)

Two cache strategies are encapsulated in a reusable utility:

| Problem | Strategy | Implementation |
|---|---|---|
| Cache penetration | Null-object caching | Store empty string on DB miss, TTL = 2 min |
| Cache breakdown (hot key expiry) | Logical expiry | Key never physically expires; expiry timestamp stored in value; background thread (Redisson tryLock) rebuilds on logical miss; stale value served in the meantime |

### Search

- Full-text video search with keyword matching against title and description.
- **Search history** per user stored in a Redis List (`search:history:{userId}`, TTL = 7 days). Supports delete-by-term.

### Watch History

- Redis List `video:history:{userId}`. On each view: remove existing entry for the video ID (dedup), left-push (newest first), trim to 100 items, reset TTL to 7 days. O(1) amortised per operation.

### Social

- Follow / unfollow with follower and following count maintained on the `user` row.
- Paginated follower and following lists via PageHelper.
- Favourite collections — create, update, delete named playlists; add/remove videos; list videos in a collection.
- Video likes tracked in `video_like`; like state per viewer resolved at query time.
- Comment system with parent/root threading for reply chains.

---

## Project Structure

```
TikTok/
└── src/main/java/org/example/tiktok/
    ├── config/
    │   ├── CaptchaConfig.java       # Kaptcha producer bean
    │   ├── JacksonConfig.java       # ObjectMapper customisation
    │   ├── MvcConfig.java           # Interceptor registration
    │   ├── RedisConfig.java         # Redisson client bean
    │   └── WebExceptionAdvice.java  # Global @RestControllerAdvice
    ├── controller/
    │   ├── CustomerController.java  # /customer — profile, follow, favourites
    │   ├── FileUploadController.java# /upload — OSS proxy
    │   ├── IndexController.java     # /index — discovery, search, hot
    │   ├── LoginController.java     # /login — auth, registration
    │   └── VideoController.java     # /video — CRUD, feed, comments
    ├── dto/                         # Request/response transfer objects
    ├── entity/                      # JPA-style POJOs mapped to MySQL tables
    ├── interceptor/
    │   ├── PublicInterceptor.java   # TTL refresh on all routes
    │   └── TokenInterceptor.java    # Auth guard on protected routes
    ├── mapper/                      # MyBatis mapper interfaces + XML
    ├── service/
    │   ├── impl/                    # Business logic implementations
    │   └── *.java                   # Service interfaces
    └── utils/
        ├── AliOSSUtil.java          # OSS upload/delete helpers
        ├── CacheClient.java         # Cache penetration/breakdown strategies
        ├── HotRank.java             # Scheduled ranking engine
        ├── JwtUtils.java            # Token sign / parse
        ├── PasswordUtils.java       # BCrypt wrapper
        ├── SnowflakeIdWorker.java   # Distributed ID generation
        ├── SystemConstant.java      # Redis key prefixes and shared constants
        └── UserHolder.java          # ThreadLocal user context
```

---

## Database Schema

Twelve tables — no application-level foreign keys to keep test isolation simple; referential integrity is enforced in the service layer.

| Table | Purpose |
|---|---|
| `user` | Account, profile, aggregate follow/follower counts |
| `video` | Video metadata, aggregate engagement counters |
| `file_protect` | OSS key registry for uploaded binaries |
| `video_type` | Content category taxonomy |
| `video_type_relation` | Video ↔ category (N:M) |
| `comment` | Threaded comments with `parent_id` / `root_id` |
| `follow` | Directed follow edges |
| `favourite` | Named playlists owned by a user |
| `favourite_video_relation` | Playlist ↔ video (N:M) |
| `user_favourite_relation` | User ↔ playlist ownership |
| `video_like` | Like events |
| `video_share` | Share events with optional IP logging |
| `subscribe_video_type` | User ↔ preferred category subscriptions |

**Indexes created at initialisation:**

```sql
CREATE INDEX idx_video_publisher_id        ON video(publisher_id);
CREATE INDEX idx_follow_follow_id          ON follow(follow_id);
CREATE INDEX idx_follow_follower_id        ON follow(follower_id);
CREATE INDEX idx_video_like_video_id       ON video_like(video_id);
CREATE INDEX idx_video_share_video_id      ON video_share(video_id);
CREATE INDEX idx_video_type_relation_vid   ON video_type_relation(video_id);
CREATE INDEX idx_subscribe_user_id         ON subscribe_video_type(user_id);
CREATE INDEX idx_favourite_create_user_id  ON favourite(create_user_id);
```

---

## Redis Key Design

| Key pattern | Type | TTL | Purpose |
|---|---|---|---|
| `user:captcha:{uuid}` | String | 5 min | Image CAPTCHA code |
| `user:mailCode:{email}` | String | 5 min | Email verification code |
| `user:token:{userId}` | String | 24 h (sliding) | JWT session store |
| `customer:user:{userId}` | String (JSON) | — | User profile cache |
| `index:video:{videoId}` | String (JSON) | varies | Video detail cache |
| `video:history:{userId}` | List | 7 days | Watch history (newest-first, max 100) |
| `video:views:{videoId}` | String | — | View counter (flushed to MySQL by scheduler) |
| `video:hot:top10` | ZSet | 1 day | Nightly hot-rank (score = hotScore) |
| `feed:{userId}` | ZSet | — | Follow Feed inbox (score = publish timestamp) |
| `search:history:{userId}` | List | 7 days | Recent search terms |
| `follows:{userId}` | String (JSON) | — | Follow relationship cache |
| `user:model:{userId}` | String (JSON) | — | User preference model |

---

## Hot Rank Algorithm

Score is computed per video using an **exponential time-decay** model with a 12-hour half-life:

```
weight      = likes × 1.0
            + shares × 1.5
            + views × 0.1
            + favourites × 2.0

age_seconds = seconds since video.createTime

decay       = 0.5 ^ (age_seconds / 43200)   // 43200 = 12 h in seconds

hot_score   = weight × decay
```

The weights reflect expected value to the platform: a save (`favourite`) signals stronger intent than a passive view, and a share has the highest organic reach multiplier. The half-life of 12 hours means a video must continually attract engagement to remain on the chart — a viral spike decays to half-rank within half a day.

---

## API Reference

All endpoints return a unified envelope:

```json
{
  "success": true,
  "message": "ok",
  "data": { ... },
  "total": 42
}
```

### Authentication — `/login`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/login/captcha.jpg/{uuid}` | — | Generate image CAPTCHA |
| POST | `/login/sendMail` | — | Send email verification code |
| POST | `/login/registry` | — | Register new account |
| POST | `/login/logIn` | — | Login; returns JWT |
| POST | `/login/findPassword` | — | Reset password via email code |

### Discovery — `/index`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/index/video/type/{typeId}` | Optional | Videos by category (paginated) |
| GET | `/index/types` | — | All content categories |
| GET | `/index/video/{videoId}` | Optional | Video detail |
| GET | `/index/search` | Optional | Full-text video search |
| GET | `/index/search/history` | Required | User's search history |
| POST | `/index/search/history/delete` | Required | Remove a search term |
| POST | `/index/share/{videoId}` | Optional | Record share event |
| GET | `/index/video/user` | Optional | Videos published by a user |
| GET | `/index/video/hot/rank` | — | Top-10 hot rank list |
| GET | `/index/video/hot` | — | Trending videos (3-day window, paginated) |
| GET | `/index/video/similar` | Optional | Similar videos by type |
| GET | `/index/pushVideos` | Required | Recommended feed |

### User & Social — `/customer`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/customer/getInfo/{userId}` | Required | User profile |
| PUT | `/customer` | Required | Update profile / avatar |
| GET | `/customer/follow` | Required | Paginated following list |
| GET | `/customer/followers` | Required | Paginated follower list |
| POST | `/customer/{id}/{isFollow}` | Required | Follow or unfollow |
| GET | `/customer/favourites` | Required | List user's playlists |
| GET | `/customer/favourites/{favouriteId}` | Required | Playlist detail |
| POST | `/customer/favourites` | Required | Create or update playlist |
| DELETE | `/customer/favourites/{favouriteId}` | Required | Delete playlist |
| POST | `/customer/subscribe` | Required | Update category subscriptions |
| GET | `/customer/subscribe` | Required | Get subscribed categories |
| POST | `/customer/updateUserModel` | Required | Update recommendation profile |

### Video — `/video`

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/video` | Required | Publish or update video |
| GET | `/video` | Required | List own videos (paginated) |
| DELETE | `/video/delete/{videoId}` | Required | Delete video |
| POST | `/video/star/{videoId}` | Required | Like / unlike video |
| POST | `/video/history/{videoId}` | Required | Record watch event |
| GET | `/video/history` | Required | Watch history |
| GET | `/video/follow/feed` | Required | Cursor-based follow feed |
| GET | `/video/favourite/{favouriteTableId}` | Required | Videos in a playlist |
| POST | `/video/favourite/{fid}/{videoId}` | Required | Add video to playlist |
| POST | `/video/comment` | Required | Post or reply to comment |
| GET | `/video/index/comment/by/video` | Optional | Top-level comments for video |
| GET | `/video/index/comment/by/rootComment` | Optional | Replies to a comment |
| POST | `/video/comment/like/{commentId}` | Required | Like a comment |
| DELETE | `/video/comment/{commentId}` | Required | Delete a comment |

### File Upload — `/upload`

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/upload` | Required | Upload video file to OSS |
| POST | `/customer/upload/avatar` | Required | Upload avatar to OSS |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.9+
- MySQL 8.0
- Redis (standalone or with Redisson-compatible cluster)
- An Aliyun OSS bucket with an access key
- A Gmail account with App Password enabled (for SMTP)

### 1. Clone

```bash
git clone https://github.com/MakiseKurisu9/TikTok.git
cd TikTok
```

### 2. Create the database

```bash
mysql -u root -p < TikTok/src/main/resources/initialize.sql
```

This creates the `tiktok` database, all tables, and the recommended indexes.

### 3. Set environment variables

All secrets are externalised — the application will refuse to start if any of the following are missing:

```bash
export DB_PASSWORD=your_mysql_password
export REDIS_PASSWORD=your_redis_password
export JWT_SECRET=a_random_256bit_string
export MAIL_USERNAME=your_gmail_address
export MAIL_PASSWORD=your_gmail_app_password
export ALIYUN_OSS_ACCESS_KEY_ID=your_oss_key_id
export ALIYUN_OSS_ACCESS_KEY_SECRET=your_oss_key_secret
```

See [Environment Variables](#environment-variables) for the full reference.

### 4. Configure endpoints

Edit `TikTok/src/main/resources/application.yaml` for your local MySQL and Redis hosts:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tiktok?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
  data:
    redis:
      host: 127.0.0.1
      port: 6379
```

### 5. Build and run

```bash
cd TikTok
mvn clean package -DskipTests
java -jar target/TikTok-0.0.1-SNAPSHOT.jar
```

The service starts on **port 8080**.

### Optional: Docker Compose for dependencies

If you want to bring up MySQL, Redis, and any other services locally with Docker:

```yaml
# docker-compose.yml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: tiktok
    ports:
      - "3306:3306"

  redis:
    image: redis:7
    command: redis-server --requirepass ${REDIS_PASSWORD}
    ports:
      - "6379:6379"
```

```bash
docker compose up -d
# then run initialize.sql against the container
docker exec -i <mysql-container> mysql -uroot -p${DB_PASSWORD} < TikTok/src/main/resources/initialize.sql
```

---

## Environment Variables

| Variable | Required | Description |
|---|---|---|
| `DB_PASSWORD` | Yes | MySQL root password |
| `REDIS_PASSWORD` | Yes | Redis AUTH password |
| `JWT_SECRET` | Yes | HMAC signing secret for JWT (min 32 chars recommended) |
| `MAIL_USERNAME` | Yes | Gmail address used as SMTP sender |
| `MAIL_PASSWORD` | Yes | Gmail App Password (not your account password) |
| `ALIYUN_OSS_ACCESS_KEY_ID` | Yes | Aliyun RAM user access key ID |
| `ALIYUN_OSS_ACCESS_KEY_SECRET` | Yes | Aliyun RAM user access key secret |

---

## Configuration

Key application properties (all in `application.yaml`):

| Property | Default | Notes |
|---|---|---|
| `server.port` | `8080` | HTTP listener port |
| `jwt.expire` | `86400000` | Token TTL in milliseconds (24 h) |
| `spring.servlet.multipart.max-file-size` | `500MB` | Max upload size per file |
| `mybatis.configuration.map-underscore-to-camel-case` | `true` | Snake-case column ↔ camelCase field mapping |
| `pagehelper.helper-dialect` | `mysql` | Pagination dialect |

---

## Design Decisions

**Why JWT stored in Redis instead of stateless JWT?**
Pure stateless JWT cannot be revoked before natural expiry. For a platform that needs logout, session invalidation on password change, and the ability to ban accounts immediately, a server-side session store is necessary. Redis provides sub-millisecond lookups, so the cost of checking the store on every request is negligible. The token still carries a signed payload for tamper detection; Redis adds revocability on top.

**Why ZSet for the Feed instead of a relational query?**
A relational `ORDER BY publish_time DESC LIMIT ? OFFSET ?` approach suffers from page drift: if new content is inserted between two page fetches, the offset shifts and the client either skips rows or sees duplicates. A ZSet cursor (passing the score of the last seen item as the upper bound for the next query) is anchored to content, not position, so it is stable regardless of concurrent inserts.

**Why Snowflake IDs instead of MySQL AUTO_INCREMENT?**
AUTO_INCREMENT requires a round-trip to the database to obtain the ID, creates a hotspot on the sequence lock under high write concurrency, and leaks business volume information. Snowflake IDs are generated locally, time-ordered (good for index locality), and carry no dependency on the database.

**Why a reusable `CacheClient` instead of inline Redis calls?**
Consistency. Both cache strategies (null-object for penetration, logical expiry for breakdown) involve non-trivial coordination logic. Centralising them in a single utility ensures every caller gets the same behaviour and makes future changes (e.g. switching to a different lock provider) a one-line edit.

**Why BCrypt over SHA/MD5?**
BCrypt is adaptive — its cost factor can be increased as hardware gets faster, keeping brute-force attacks proportionally expensive. SHA and MD5 are fast by design, which is exactly the wrong property for password storage.

---

---

# TikTok 短视频后端平台（中文文档）

基于 Spring Boot 3 构建的功能完整的短视频社区后端，覆盖生产级视频平台的核心业务领域——用户管理、视频发布、社交图谱、内容 Feed 流以及实时热榜引擎。

---

## 目录

- [项目概述](#项目概述)
- [系统架构](#系统架构)
- [技术栈](#技术栈)
- [功能特性](#功能特性)
- [项目结构](#项目结构)
- [数据库设计](#数据库设计)
- [Redis Key 设计](#redis-key-设计)
- [热榜算法](#热榜算法)
- [API 接口文档](#api-接口文档)
- [快速开始](#快速开始)
- [环境变量](#环境变量)
- [配置说明](#配置说明)
- [设计决策](#设计决策)

---

## 项目概述

本项目以单体 Spring Boot 服务的形式还原了 TikTok 风格平台的后端，旨在展示生产级开发模式——分层安全体系、缓存高可用数据访问、时间衰减热度排名以及游标分页 Feed 流——而非简单的 CRUD 练习。代码库涵盖约 5,000 行业务逻辑，分布在五个 Controller、四个 Service 层以及一套基础设施工具类中。

**核心亮点：**

- 双拦截器认证流水线，公开路由与受保护路由完全分离
- 通过可复用的 `CacheClient` 实现缓存穿透与缓存击穿双重防护
- 基于 ZSet 的游标驱动 Feed 流，彻底免疫分页漂移问题
- 每日定时重算的时间衰减热榜（`@Scheduled`）
- Snowflake ID 生成全局唯一、时间有序的实体主键
- 阿里云 OSS 集成，用于视频和头像的二进制文件存储

---

## 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                        客户端                            │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP
┌────────────────────────▼────────────────────────────────┐
│              Spring MVC（Controller 层）                 │
│   /login  /index  /customer  /video  /upload            │
├─────────────────────────────────────────────────────────┤
│                    拦截器链                               │
│  PublicInterceptor → Token 续期（全部路由）              │
│  TokenInterceptor  → 鉴权守卫（受保护路由）              │
├─────────────────────────────────────────────────────────┤
│                   Service 层                             │
│  LoginService  IndexService  CustomerService            │
│  VideoService（+ HotRank 定时任务）                      │
├─────────────────────────────────────────────────────────┤
│                 基础设施工具类                            │
│  CacheClient · SnowflakeIdWorker · AliOSSUtil           │
│  JwtUtils · PasswordUtils · UserHolder（ThreadLocal）   │
├──────────────────┬──────────────────┬───────────────────┤
│   MySQL 8        │   Redis           │  阿里云 OSS        │
│  （MyBatis）     │  （Redisson +     │ （视频/头像        │
│   主数据存储      │   StringRedis）   │  二进制存储）      │
└──────────────────┴──────────────────┴───────────────────┘
```

---

## 技术栈

| 层级 | 技术选型 |
|---|---|
| 框架 | Spring Boot 3.4、Spring MVC |
| ORM | MyBatis 3.0 + PageHelper |
| 主数据库 | MySQL 8.0（InnoDB、utf8mb4） |
| 缓存 / 数据结构存储 | Redis + Redisson（分布式锁） |
| 对象存储 | 阿里云 OSS SDK 3.17 |
| 认证 | JWT（JJWT）+ BCrypt 密码哈希 |
| 验证码 | Kaptcha 2.3（图形验证码） |
| 邮件 | Spring Mail（SMTP / Gmail） |
| ID 生成 | Snowflake 算法（自定义实现） |
| 构建工具 | Maven 3.9、Java 17 |
| 定时任务 | Spring `@Scheduled` |

---

## 功能特性

### 认证与安全

- **双拦截器流水线** —— `PublicInterceptor` 在所有请求上运行，当存在有效会话时静默刷新 Token TTL；`TokenInterceptor` 守卫受保护端点，会话缺失或过期时返回 401。
- **Token 存储于 Redis** —— JWT Token 以 `user:token:{userId}` 为 key 存储在服务端，支持强制注销（退出、封禁）而无需等待 JWT 自然过期。TTL 为滑动续期：每次认证请求后重置为 24 小时。
- **注册双重验证** —— 先验证 Kaptcha 图形验证码，再验证 Gmail SMTP 邮件验证码。两种验证码均以 5 分钟 TTL 存入 Redis，验证成功后立即删除。
- **密码哈希** —— BCrypt 默认成本因子，明文密码从不落盘。

### 视频发布与存储

- 视频文件和封面图片上传至**阿里云 OSS**，OSS Key 记录在 `file_protect` 表中，支持未来在不重新上传文件的情况下吊销访问权限。
- 视频实体以 **Snowflake ID** 作为主键——64 位、时间有序、分布式并发下无冲突，无需中心化序列表。

### 内容 Feed 流（社交图谱）

- 用户关注某创作者后，该创作者后续发布的视频会被**推送**到关注者的收件箱 ZSet（`feed:{followerId}`），通过 `ZADD score=发布时间戳 member=videoId` 写入。
- Feed 拉取采用**游标滚动**（`ZREVRANGEBYSCORE max {lastMinScore} LIMIT 0 10`），而非 offset 分页。这彻底消除了分页漂移问题——新插入的内容不会导致翻页时出现重复或遗漏。

### 热榜引擎

排行榜计算流水线以每日 `@Scheduled(cron = "0 0 0 * * ?")` 定时任务运行：

1. 从 MySQL 拉取所有视频。
2. 对每个视频计算**时间衰减热度分**（见[热榜算法](#热榜算法)）。
3. 取热度分最高的 Top 10。
4. 原子替换 Redis 中的 `video:hot:top10`（先删后写，设 TTL 1 天）。

另有一个独立任务（相同调度，每年第 3n 天执行）计算**热门视频**——72 小时内发布且热度分超过 `HOT_LIMIT` 阈值的视频——并预加载到视频缓存中以实现低延迟读取。

### 缓存高可用（`CacheClient`）

两种缓存策略封装在可复用工具类中：

| 问题 | 策略 | 实现方式 |
|---|---|---|
| 缓存穿透 | 缓存空对象 | DB 未命中时写入空字符串，TTL = 2 分钟 |
| 缓存击穿（热点 Key 过期） | 逻辑过期 | Key 不设物理过期，过期时间戳存入 value；逻辑过期时后台线程（Redisson tryLock）重建缓存，当前请求继续返回旧值 |

### 搜索

- 对视频标题和描述进行关键词全文搜索。
- 每用户**搜索历史**存储在 Redis List 中（`search:history:{userId}`，TTL = 7 天），支持按词删除。

### 观看历史

- Redis List `video:history:{userId}`。每次观看：先移除该视频 ID 的已有条目（去重），再左推（最新在前），裁剪至 100 条，重置 TTL 为 7 天。均摊时间复杂度 O(1)。

### 社交功能

- 关注 / 取消关注，`user` 表实时维护关注数与粉丝数聚合字段。
- 通过 PageHelper 实现粉丝列表和关注列表分页。
- 收藏夹——创建、更新、删除命名播放列表；添加 / 移除视频；列出收藏夹内视频。
- 视频点赞记录在 `video_like` 表；当前用户的点赞状态在查询时实时解析。
- 评论系统支持 `parent_id` / `root_id` 树形结构的多级回复链。

---

## 项目结构

```
TikTok/
└── src/main/java/org/example/tiktok/
    ├── config/
    │   ├── CaptchaConfig.java       # Kaptcha Producer Bean
    │   ├── JacksonConfig.java       # ObjectMapper 自定义配置
    │   ├── MvcConfig.java           # 拦截器注册
    │   ├── RedisConfig.java         # Redisson 客户端 Bean
    │   └── WebExceptionAdvice.java  # 全局 @RestControllerAdvice
    ├── controller/
    │   ├── CustomerController.java  # /customer — 用户、关注、收藏夹
    │   ├── FileUploadController.java# /upload — OSS 上传代理
    │   ├── IndexController.java     # /index — 发现、搜索、热榜
    │   ├── LoginController.java     # /login — 认证、注册
    │   └── VideoController.java     # /video — CRUD、Feed、评论
    ├── dto/                         # 请求 / 响应传输对象
    ├── entity/                      # 映射 MySQL 表的 POJO 实体
    ├── interceptor/
    │   ├── PublicInterceptor.java   # 全路由 TTL 续期
    │   └── TokenInterceptor.java   # 受保护路由鉴权守卫
    ├── mapper/                      # MyBatis Mapper 接口 + XML
    ├── service/
    │   ├── impl/                    # 业务逻辑实现
    │   └── *.java                   # Service 接口
    └── utils/
        ├── AliOSSUtil.java          # OSS 上传 / 删除工具
        ├── CacheClient.java         # 缓存穿透 / 击穿防护策略
        ├── HotRank.java             # 定时热榜引擎
        ├── JwtUtils.java            # Token 签发 / 解析
        ├── PasswordUtils.java       # BCrypt 封装
        ├── SnowflakeIdWorker.java   # 分布式 ID 生成
        ├── SystemConstant.java      # Redis Key 前缀及共享常量
        └── UserHolder.java          # ThreadLocal 用户上下文
```

---

## 数据库设计

共十二张表，应用层不设外键约束以简化测试隔离；引用完整性由 Service 层保证。

| 表名 | 用途 |
|---|---|
| `user` | 账号、资料、关注 / 粉丝聚合计数 |
| `video` | 视频元数据、互动指标聚合 |
| `file_protect` | 上传二进制文件的 OSS Key 注册表 |
| `video_type` | 内容分类体系 |
| `video_type_relation` | 视频 ↔ 分类（N:M） |
| `comment` | 支持 `parent_id` / `root_id` 的树形评论 |
| `follow` | 有向关注边 |
| `favourite` | 用户创建的命名播放列表 |
| `favourite_video_relation` | 播放列表 ↔ 视频（N:M） |
| `user_favourite_relation` | 用户 ↔ 播放列表归属 |
| `video_like` | 点赞事件 |
| `video_share` | 分享事件，含可选 IP 记录 |
| `subscribe_video_type` | 用户 ↔ 偏好分类订阅 |

**初始化时创建的索引：**

```sql
CREATE INDEX idx_video_publisher_id        ON video(publisher_id);
CREATE INDEX idx_follow_follow_id          ON follow(follow_id);
CREATE INDEX idx_follow_follower_id        ON follow(follower_id);
CREATE INDEX idx_video_like_video_id       ON video_like(video_id);
CREATE INDEX idx_video_share_video_id      ON video_share(video_id);
CREATE INDEX idx_video_type_relation_vid   ON video_type_relation(video_id);
CREATE INDEX idx_subscribe_user_id         ON subscribe_video_type(user_id);
CREATE INDEX idx_favourite_create_user_id  ON favourite(create_user_id);
```

---

## Redis Key 设计

| Key 模式 | 类型 | TTL | 用途 |
|---|---|---|---|
| `user:captcha:{uuid}` | String | 5 分钟 | 图形验证码 |
| `user:mailCode:{email}` | String | 5 分钟 | 邮件验证码 |
| `user:token:{userId}` | String | 24 小时（滑动） | JWT 会话存储 |
| `customer:user:{userId}` | String（JSON） | — | 用户资料缓存 |
| `index:video:{videoId}` | String（JSON） | 不固定 | 视频详情缓存 |
| `video:history:{userId}` | List | 7 天 | 观看历史（最新在前，最多 100 条） |
| `video:views:{videoId}` | String | — | 播放量计数器（定时刷回 MySQL） |
| `video:hot:top10` | ZSet | 1 天 | 每日热榜（score = 热度分） |
| `feed:{userId}` | ZSet | — | 关注 Feed 收件箱（score = 发布时间戳） |
| `search:history:{userId}` | List | 7 天 | 近期搜索词 |
| `follows:{userId}` | String（JSON） | — | 关注关系缓存 |
| `user:model:{userId}` | String（JSON） | — | 用户偏好模型 |

---

## 热榜算法

每个视频的热度分采用**指数时间衰减**模型，半衰期为 12 小时：

```
加权分  = 点赞数 × 1.0
        + 分享数 × 1.5
        + 播放量 × 0.1
        + 收藏数 × 2.0

发布时长（秒）= 当前时间 - video.createTime（秒）

衰减因子 = 0.5 ^ (发布时长 / 43200)   // 43200 = 12 小时换算为秒

热度分   = 加权分 × 衰减因子
```

权重设计反映了内容对平台的预期价值：「收藏」代表比被动观看更强的用户意图；「分享」具有最高的自然传播乘数。12 小时半衰期意味着视频必须持续获得互动才能留在榜单——爆发式流量在半天内会衰减至一半热度。

---

## API 接口文档

所有接口返回统一响应体：

```json
{
  "success": true,
  "message": "ok",
  "data": { ... },
  "total": 42
}
```

### 认证接口 — `/login`

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/login/captcha.jpg/{uuid}` | 无需 | 生成图形验证码 |
| POST | `/login/sendMail` | 无需 | 发送邮件验证码 |
| POST | `/login/registry` | 无需 | 注册新账号 |
| POST | `/login/logIn` | 无需 | 登录，返回 JWT |
| POST | `/login/findPassword` | 无需 | 通过邮件验证码重置密码 |

### 发现接口 — `/index`

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/index/video/type/{typeId}` | 可选 | 按分类获取视频（分页） |
| GET | `/index/types` | 无需 | 获取全部内容分类 |
| GET | `/index/video/{videoId}` | 可选 | 视频详情 |
| GET | `/index/search` | 可选 | 全文视频搜索 |
| GET | `/index/search/history` | 需要 | 用户搜索历史 |
| POST | `/index/search/history/delete` | 需要 | 删除某条搜索词 |
| POST | `/index/share/{videoId}` | 可选 | 记录分享事件 |
| GET | `/index/video/user` | 可选 | 某用户发布的视频 |
| GET | `/index/video/hot/rank` | 无需 | Top 10 热榜 |
| GET | `/index/video/hot` | 无需 | 近 3 日热门视频（分页） |
| GET | `/index/video/similar` | 可选 | 同类相似视频 |
| GET | `/index/pushVideos` | 需要 | 推荐 Feed |

### 用户与社交 — `/customer`

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| GET | `/customer/getInfo/{userId}` | 需要 | 获取用户资料 |
| PUT | `/customer` | 需要 | 更新资料 / 头像 |
| GET | `/customer/follow` | 需要 | 关注列表（分页） |
| GET | `/customer/followers` | 需要 | 粉丝列表（分页） |
| POST | `/customer/{id}/{isFollow}` | 需要 | 关注或取消关注 |
| GET | `/customer/favourites` | 需要 | 用户收藏夹列表 |
| GET | `/customer/favourites/{favouriteId}` | 需要 | 收藏夹详情 |
| POST | `/customer/favourites` | 需要 | 创建或更新收藏夹 |
| DELETE | `/customer/favourites/{favouriteId}` | 需要 | 删除收藏夹 |
| POST | `/customer/subscribe` | 需要 | 更新分类订阅 |
| GET | `/customer/subscribe` | 需要 | 获取已订阅分类 |
| POST | `/customer/updateUserModel` | 需要 | 更新推荐偏好模型 |

### 视频接口 — `/video`

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/video` | 需要 | 发布或更新视频 |
| GET | `/video` | 需要 | 列出本人视频（分页） |
| DELETE | `/video/delete/{videoId}` | 需要 | 删除视频 |
| POST | `/video/star/{videoId}` | 需要 | 点赞 / 取消点赞 |
| POST | `/video/history/{videoId}` | 需要 | 记录观看事件 |
| GET | `/video/history` | 需要 | 观看历史 |
| GET | `/video/follow/feed` | 需要 | 游标滚动关注 Feed |
| GET | `/video/favourite/{favouriteTableId}` | 需要 | 收藏夹内的视频 |
| POST | `/video/favourite/{fid}/{videoId}` | 需要 | 添加视频到收藏夹 |
| POST | `/video/comment` | 需要 | 发表或回复评论 |
| GET | `/video/index/comment/by/video` | 可选 | 视频的顶级评论 |
| GET | `/video/index/comment/by/rootComment` | 可选 | 某评论的子回复 |
| POST | `/video/comment/like/{commentId}` | 需要 | 点赞评论 |
| DELETE | `/video/comment/{commentId}` | 需要 | 删除评论 |

### 文件上传 — `/upload`

| 方法 | 路径 | 鉴权 | 说明 |
|---|---|---|---|
| POST | `/upload` | 需要 | 上传视频文件到 OSS |
| POST | `/customer/upload/avatar` | 需要 | 上传头像到 OSS |

---

## 快速开始

### 环境要求

- Java 17+
- Maven 3.9+
- MySQL 8.0
- Redis（单机或 Redisson 兼容集群）
- 已创建 Bucket 的阿里云 OSS 及 AccessKey
- 已开启「应用专用密码」的 Gmail 账号（用于 SMTP）

### 第一步：克隆项目

```bash
git clone https://github.com/MakiseKurisu9/TikTok.git
cd TikTok
```

### 第二步：初始化数据库

```bash
mysql -u root -p < TikTok/src/main/resources/initialize.sql
```

该脚本将创建 `tiktok` 数据库、所有数据表以及推荐索引。

### 第三步：配置环境变量

所有密钥均已外部化——若以下任意变量缺失，应用将拒绝启动：

```bash
export DB_PASSWORD=你的MySQL密码
export REDIS_PASSWORD=你的Redis密码
export JWT_SECRET=随机生成的256位字符串
export MAIL_USERNAME=你的Gmail地址
export MAIL_PASSWORD=你的Gmail应用专用密码
export ALIYUN_OSS_ACCESS_KEY_ID=阿里云RAM用户AccessKeyId
export ALIYUN_OSS_ACCESS_KEY_SECRET=阿里云RAM用户AccessKeySecret
```

完整参考见[环境变量](#环境变量)章节。

### 第四步：修改服务地址

编辑 `TikTok/src/main/resources/application.yaml`，按实际情况修改 MySQL 和 Redis 的连接地址：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tiktok?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
  data:
    redis:
      host: 127.0.0.1
      port: 6379
```

### 第五步：构建并启动

```bash
cd TikTok
mvn clean package -DskipTests
java -jar target/TikTok-0.0.1-SNAPSHOT.jar
```

服务启动后监听 **8080 端口**。

### 可选：用 Docker Compose 拉起依赖服务

```yaml
# docker-compose.yml
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: tiktok
    ports:
      - "3306:3306"

  redis:
    image: redis:7
    command: redis-server --requirepass ${REDIS_PASSWORD}
    ports:
      - "6379:6379"
```

```bash
docker compose up -d
# 向容器内的 MySQL 导入初始化 SQL
docker exec -i <mysql容器名> mysql -uroot -p${DB_PASSWORD} < TikTok/src/main/resources/initialize.sql
```

---

## 环境变量

| 变量名 | 是否必填 | 说明 |
|---|---|---|
| `DB_PASSWORD` | 是 | MySQL root 密码 |
| `REDIS_PASSWORD` | 是 | Redis AUTH 密码 |
| `JWT_SECRET` | 是 | JWT HMAC 签名密钥（建议不少于 32 字符） |
| `MAIL_USERNAME` | 是 | 用作 SMTP 发件人的 Gmail 地址 |
| `MAIL_PASSWORD` | 是 | Gmail 应用专用密码（非账号登录密码） |
| `ALIYUN_OSS_ACCESS_KEY_ID` | 是 | 阿里云 RAM 用户 AccessKeyId |
| `ALIYUN_OSS_ACCESS_KEY_SECRET` | 是 | 阿里云 RAM 用户 AccessKeySecret |

---

## 配置说明

关键应用配置项（均位于 `application.yaml`）：

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `server.port` | `8080` | HTTP 监听端口 |
| `jwt.expire` | `86400000` | Token 有效期，单位毫秒（24 小时） |
| `spring.servlet.multipart.max-file-size` | `500MB` | 单文件最大上传大小 |
| `mybatis.configuration.map-underscore-to-camel-case` | `true` | 下划线字段名 ↔ 驼峰属性名自动映射 |
| `pagehelper.helper-dialect` | `mysql` | 分页方言 |

---

## 设计决策

**为什么将 JWT 存入 Redis，而非使用纯无状态 JWT？**
纯无状态 JWT 在自然过期之前无法撤销。对于需要注销、密码修改后失效以及即时封禁账号的平台而言，服务端会话存储是必要的。Redis 的亚毫秒级查询使每次请求的校验开销可以忽略不计。Token 本身依然携带签名 Payload 用于防篡改；Redis 在此基础上叠加了可撤销能力。

**为什么用 ZSet 实现 Feed，而非关系型查询？**
关系型 `ORDER BY publish_time DESC LIMIT ? OFFSET ?` 存在分页漂移问题：两次翻页之间若有新内容插入，offset 会整体偏移，导致客户端看到重复内容或遗漏条目。ZSet 游标（将上一页最后一条的 score 作为下次查询的上界）锚定在内容上而非位置上，因此对并发插入天然免疫。

**为什么使用 Snowflake ID，而非 MySQL AUTO_INCREMENT？**
AUTO_INCREMENT 需要一次数据库往返才能获取 ID，在高写入并发下会在序列锁上产生热点，且会泄露业务量信息。Snowflake ID 在本地生成，时间有序（有利于索引局部性），且完全不依赖数据库。

**为什么将缓存逻辑封装到 `CacheClient`，而非在业务代码中内联调用 Redis？**
一致性。缓存穿透（空对象）和缓存击穿（逻辑过期）两种策略都涉及非平凡的协调逻辑。将其集中在单一工具类中，确保所有调用方获得相同的行为，也使未来的变更（如切换锁提供方）只需修改一处。

**为什么使用 BCrypt，而非 SHA/MD5？**
BCrypt 具有自适应性——随着硬件性能提升，可以增加其成本因子，使暴力破解攻击的代价始终保持相应比例的昂贵。SHA 和 MD5 在设计上追求高速，这恰恰是密码存储最不应具备的特性。
