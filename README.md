# AI 电竞经理 (Game Mate)

AI 游戏陪玩平台 —— 前后端分离项目。

## 技术栈

### 前端 (game-mate)
- Vue 3 + Vite
- Element Plus
- Pinia 状态管理
- Axios HTTP 请求

### 后端 (game-mate-server)
- Spring Boot 3.2 + Java 17
- MyBatis-Plus
- Spring Security + JWT
- MySQL

## 项目结构

```
├── game-mate/              # 前端项目
│   ├── src/
│   │   ├── api/            # API 接口
│   │   ├── router/         # 路由配置
│   │   ├── stores/         # Pinia 状态管理
│   │   ├── utils/          # 工具函数
│   │   └── views/          # 页面组件
│   ├── public/             # 静态资源
│   └── vite.config.js
│
├── game-mate-server/       # 后端项目
│   ├── src/main/java/com/gamemate/
│   │   ├── config/         # 配置类
│   │   ├── controller/     # 控制器
│   │   ├── dto/            # 数据传输对象
│   │   ├── entity/         # 实体类
│   │   ├── mapper/         # MyBatis Mapper
│   │   ├── service/        # 服务层
│   │   ├── util/           # 工具类
│   │   └── vo/             # 视图对象
│   └── src/main/resources/
│       ├── application.yml # 主配置
│       └── schema.sql      # 数据库建表脚本
│
└── README.md
```

## 快速开始

### 后端
```bash
cd game-mate-server
# 配置 MySQL 数据库连接（通过环境变量）
# DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET
./mvnw spring-boot:run
```

### 前端
```bash
cd game-mate
npm install
npm run dev
```

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| DB_URL | MySQL 连接地址 | jdbc:mysql://localhost:3306/game_mate |
| DB_USERNAME | 数据库用户名 | gamemate |
| DB_PASSWORD | 数据库密码 | - |
| JWT_SECRET | JWT 签名密钥 | - |
| AI_API_KEY | AI 服务 API Key | - |
| AI_API_URL | AI 服务地址 | 阿里云通义千问 API |
| AI_MODEL | AI 模型名称 | qwen3.8-max-preview |
