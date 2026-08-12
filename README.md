# AI 电竞经理 (Game Mate)

AI 游戏陪玩平台 —— 基于 Vue 3 + Spring Boot 3 的全栈项目，支持 Windows 桌面客户端。

## 核心功能

| 模块 | 功能说明 |
|------|----------|
| AI 智能对话 | 接入阿里云通义千问大模型，支持流式响应（SSE）、上下文记忆、多游戏场景适配 |
| 游戏画面分析 | 支持截图上传，AI 视觉模型识别游戏画面并提供实时战术建议 |
| 自定义人格 | 6 种 AI 人格（温柔治愈/热血激昂/冷静分析/幽默风趣/严厉教练/ default），4 种声线可选 |
| 用户体系 | 手机号注册登录、JWT 鉴权、余额管理（小时制）、充值套餐、增值服务购买 |
| 高光时刻 | 游戏精彩瞬间记录、截图保存、按游戏分类浏览 |
| 桌面客户端 | Electron 打包的 Windows 安装程序，支持自动更新、代码混淆、NSIS 安装器 |
| 安全防护 | 双层防火墙（Nginx 限流 + Spring 过滤器）、CORS 白名单、IP 速率限制、认证频率限制 |

## 技术栈

### 前端 (game-mate)
- **Vue 3** + **Vite 5** — 组合式 API，响应式开发
- **Element Plus** — UI 组件库
- **Pinia** — 状态管理（userStore / aiChatStore / gameStore）
- **Vue Router 4** — 路由管理 + 鉴权守卫
- **Axios** — HTTP 请求 + 拦截器（自动 Token 注入 / 401 跳转）
- **Electron 43** + **electron-builder** — 桌面客户端打包
- **electron-updater** — 自动更新
- **javascript-obfuscator** — 代码混淆保护

### 后端 (game-mate-server)
- **Spring Boot 3.2** + **Java 17** — 主框架
- **MyBatis-Plus 3.5** — ORM + 自动填充
- **Spring Security** + **JWT (jjwt)** — 认证鉴权
- **MySQL 8.4** — 数据库
- **Spring Boot Actuator** — 健康检查
- **Lombok** — 简化样板代码

### 部署
- **Docker Compose** — 三容器编排（MySQL + API + Nginx）
- **Nginx** — 反向代理 + 限流 + 安全头

## 项目结构

```
AI电竞经理源码/
├── game-mate/                       # 前端 + 桌面客户端
│   ├── electron/
│   │   ├── main.mjs                 # Electron 主进程
│   │   └── preload.mjs              # 预加载脚本
│   ├── scripts/
│   │   └── build-desktop.mjs        # 桌面端构建脚本
│   ├── src/
│   │   ├── api/                     # API 接口层
│   │   │   ├── chat.js              # 聊天接口（含 SSE 流式）
│   │   │   ├── game.js              # 游戏接口
│   │   │   ├── highlight.js         # 高光接口
│   │   │   ├── order.js             # 订单接口
│   │   │   ├── plan.js              # 套餐接口
│   │   │   ├── user.js              # 用户接口
│   │   │   └── userData.js          # 用户数据接口
│   │   ├── config/
│   │   │   └── runtime.js           # 运行时配置（桌面/Web）
│   │   ├── router/
│   │   │   └── index.js             # 路由 + 鉴权守卫
│   │   ├── stores/
│   │   │   ├── aiChatStore.js       # AI 聊天状态
│   │   │   ├── gameStore.js         # 游戏状态
│   │   │   └── userStore.js         # 用户状态
│   │   ├── utils/
│   │   │   └── request.js           # Axios 实例 + 拦截器
│   │   ├── views/
│   │   │   ├── AiChat.vue           # AI 对话页（核心页面）
│   │   │   ├── Highlights.vue       # 高光时刻页
│   │   │   ├── Home.vue             # 首页 / 游戏选择
│   │   │   ├── Login.vue            # 登录页
│   │   │   ├── Privacy.vue          # 隐私政策
│   │   │   ├── Recharge.vue         # 充值页
│   │   │   ├── Register.vue         # 注册页
│   │   │   └── Settings.vue         # 设置页
│   │   ├── App.vue                  # 根组件
│   │   ├── main.js                  # 入口
│   │   └── style.css                # 全局样式
│   ├── public/games/                # 游戏图片素材
│   ├── electron-builder.config.cjs  # Electron 打包配置
│   ├── vite.config.js               # Vite 配置
│   └── package.json
│
├── game-mate-server/                # 后端服务
│   ├── src/main/java/com/gamemate/
│   │   ├── common/                  # 通用类
│   │   │   ├── GlobalExceptionHandler.java   # 全局异常处理
│   │   │   ├── Result.java                   # 统一响应格式
│   │   │   └── UnauthorizedException.java    # 未授权异常
│   │   ├── config/                  # 配置类
│   │   │   ├── AiConfig.java               # AI 模型配置
│   │   │   ├── AppConfig.java              # 应用配置
│   │   │   ├── ApplicationFirewallFilter.java  # 应用防火墙
│   │   │   ├── DataInitializer.java        # 数据初始化
│   │   │   ├── FileUploadConfig.java       # 文件上传配置
│   │   │   ├── JwtAuthenticationFilter.java    # JWT 过滤器
│   │   │   ├── MyMetaObjectHandler.java    # MyBatis 自动填充
│   │   │   ├── SecurityConfig.java         # Spring Security 配置
│   │   │   ├── WebConfig.java              # CORS + 请求日志
│   │   │   └── WebMvcConfig.java           # 静态资源 + CORS
│   │   ├── controller/              # 控制器
│   │   │   ├── ChatController.java          # 聊天（含 SSE 流式）
│   │   │   ├── GameController.java          # 游戏
│   │   │   ├── HighlightController.java     # 高光
│   │   │   ├── OrderController.java         # 订单
│   │   │   ├── PlanController.java          # 套餐
│   │   │   ├── UserController.java          # 用户
│   │   │   └── UserDataController.java      # 用户数据
│   │   ├── dto/                     # 数据传输对象
│   │   ├── entity/                  # 数据库实体
│   │   ├── mapper/                  # MyBatis Mapper
│   │   ├── service/                 # 服务层
│   │   │   └── impl/                # 服务实现
│   │   ├── util/
│   │   │   └── JwtUtil.java               # JWT 工具
│   │   ├── vo/                      # 视图对象
│   │   └── GameMateApplication.java # 启动类
│   ├── src/main/resources/
│   │   ├── application.yml          # 主配置
│   │   ├── application-dev.yml      # 开发环境
│   │   ├── application-prod.yml     # 生产环境
│   │   └── schema.sql               # 建表 + 初始数据
│   ├── src/test/                    # 单元测试
│   ├── Dockerfile
│   └── pom.xml
│
├── deploy/                          # 部署配置
│   ├── docker-compose.yml           # 三容器编排
│   ├── gateway/nginx.conf           # Nginx 配置
│   ├── .env.example                 # 环境变量模板
│   └── DESKTOP_RELEASE_GUIDE.md     # 桌面端发布指南
│
└── README.md
```

## 快速开始

### 环境要求
- Node.js 18+ / pnpm
- Java 17+ / Maven
- MySQL 8.0+
- 阿里云通义千问 API Key（可选，不配置时 AI 功能不可用）

### 后端启动

```bash
cd game-mate-server

# 方式一：本地开发
# 1. 创建数据库并执行 schema.sql
mysql -u root -p < src/main/resources/schema.sql
# 2. 配置环境变量
export DB_PASSWORD=your_password
export JWT_SECRET=your-secret-key-at-least-32-bytes
export AI_API_KEY=your-qwen-api-key
# 3. 启动
./mvnw spring-boot:run

# 方式二：Docker 部署（推荐）
cd ../deploy
cp .env.example .env
# 编辑 .env 填写密码和 API Key
docker compose up -d
```

### 前端启动

```bash
cd game-mate
pnpm install    # 或 npm install
pnpm run dev    # Web 开发模式
```

### 桌面客户端打包

```bash
cd game-mate
pnpm install

# 构建 Windows 安装包 (.exe)
pnpm run desktop:dist

# 产物位于 release/ 目录
```

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_URL` | MySQL 连接地址 | `jdbc:mysql://localhost:3306/game_mate...` |
| `DB_USERNAME` | 数据库用户名 | `gamemate` |
| `DB_PASSWORD` | 数据库密码 | 无 |
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码（Docker） | 无 |
| `JWT_SECRET` | JWT 签名密钥 | `default-dev-secret-key-...` |
| `JWT_EXPIRATION` | Token 有效期（毫秒） | `86400000`（24小时） |
| `AI_ENABLED` | 是否启用 AI | `false` |
| `AI_PROVIDER` | AI 服务商 | `qwen` |
| `AI_API_KEY` | 通义千问 API Key | 无 |
| `AI_API_URL` | AI 接口地址 | `https://dashscope.aliyuncs.com/...` |
| `AI_MODEL` | 文本模型 | `qwen-plus` |
| `AI_VISION_MODEL` | 视觉模型 | `qwen-vl-plus` |
| `GAME_MATE_ALLOWED_ORIGINS` | CORS 白名单（逗号分隔） | `http://localhost:3000,...` |
| `GAME_MATE_FIREWALL_ENABLED` | 应用防火墙开关 | `true` |
| `GAME_MATE_FIREWALL_GENERAL_PER_MINUTE` | 通用 API 限流（次/分钟） | `240` |
| `GAME_MATE_FIREWALL_AUTH_PER_FIVE_MINUTES` | 认证 API 限流（次/5分钟） | `12` |

## API 接口

### 公开接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/user/login` | 用户登录 |
| POST | `/api/user/register` | 用户注册 |
| GET | `/api/game/list` | 游戏列表 |
| GET | `/api/game/preset` | 预设游戏 |
| GET | `/uploads/**` | 静态资源访问 |

### 认证接口（需 Token）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/info` | 用户信息 |
| POST | `/api/user/logout` | 退出登录 |
| GET | `/api/chat/messages` | 聊天记录 |
| POST | `/api/chat/messages-with-personality` | 发送消息（带人格） |
| POST | `/api/chat/messages-with-personality/stream` | 流式对话（SSE） |
| POST | `/api/chat/analyze-with-query` | 截图分析 |
| GET | `/api/game/custom` | 自定义游戏 |
| POST | `/api/game/custom` | 添加自定义游戏 |
| GET | `/api/order/list` | 订单列表 |
| POST | `/api/order/create` | 创建订单 |
| POST | `/api/order/pay` | 支付订单 |
| GET | `/api/plan/list` | 套餐列表 |
| GET | `/api/highlight/list` | 高光列表 |
| POST | `/api/highlight/upload` | 上传高光截图 |
| GET | `/api/userdata/settings` | 用户设置 |
| PUT | `/api/userdata/settings` | 更新设置 |
| GET | `/api/userdata/extras` | 增值服务 |

## 数据库表结构

| 表名 | 说明 |
|------|------|
| `user` | 用户表（账号/密码/昵称/余额/人格设置） |
| `game` | 预设游戏表（10 款主流游戏） |
| `custom_game` | 用户自定义游戏表 |
| `plan` | 充值套餐表（体验/基础/标准/高级/至尊） |
| `extra_service` | 增值服务表（战术大师/情感陪伴/记忆增强/专属语音） |
| `user_extra_service` | 用户-增值服务关联表 |
| `order` | 订单表 |
| `order_extra` | 订单-增值服务关联表 |
| `chat_message` | 聊天消息表 |
| `highlight` | 高光时刻表 |
| `user_settings` | 用户设置表（人格/声线/功能开关） |

## 安全架构

### 双层防火墙
1. **Nginx 网关层** — IP 连接数限制、请求速率限制（通用 API 10r/s、认证 API 6r/m、上传 2r/s）、HTTP 方法白名单、敏感路径拦截
2. **应用层** — `ApplicationFirewallFilter` 实现 JSON 体积限制、认证频率限制、IP 黑名单机制

### 认证流程
1. 用户登录 → 返回 JWT Token
2. 前端存储 Token 到 localStorage
3. Axios 拦截器自动注入 `Authorization: Bearer <token>`
4. `JwtAuthenticationFilter` 解析 Token 并设置 userId
5. `SecurityConfig` 配置路径权限，未认证返回 401 JSON

### 安全响应头
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `Referrer-Policy: no-referrer`
- `Permissions-Policy: camera=(), geolocation=(), payment=()`

## Docker 部署

```bash
cd deploy
cp .env.example .env
# 编辑 .env，至少填写：
#   DB_PASSWORD=<强密码>
#   MYSQL_ROOT_PASSWORD=<强密码>
#   JWT_SECRET=<至少32位随机字符串>
#   AI_API_KEY=<通义千问Key>

docker compose up -d
```

三个容器：
- **mysql** — MySQL 8.4，自动执行 `schema.sql` 建表
- **api** — Spring Boot 后端，依赖 MySQL 健康检查
- **gateway** — Nginx 反向代理，依赖 API 健康检查

服务地址：`http://localhost:8080`

## 下载安装

| 版本 | 下载地址 |
|------|----------|
| v1.0.1（最新） | [GitHub Release v1.0.1](https://github.com/977299228k-tech/game-mate/releases/tag/v1.0.1) |
| v1.0.0 | [GitHub Release v1.0.0](https://github.com/977299228k-tech/game-mate/releases/tag/v1.0.0) |

### 系统要求
- Windows 10/11（64 位）
- 至少 200MB 可用磁盘空间
- 需要网络连接
