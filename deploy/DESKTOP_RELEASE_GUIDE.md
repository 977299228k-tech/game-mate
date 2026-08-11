# AI电竞经理桌面版发布手册

## 发布边界

客户最终只会收到 `AI电竞经理-Setup-<版本>-x64.exe`。不向客户发送项目目录、`src`、Spring Boot JAR、Docker 配置、`.env`、数据库或平台 AI Key。

客户端包只收录：

- Vite 编译、压缩和混淆后的 `dist`。
- esbuild 压缩后的 Electron 启动与预加载文件。
- Electron 运行时和必要的生产依赖。

核心业务、用户数据、提示词、模型路由和平台密钥只运行在云端 Spring Boot 服务中。

## 1. 部署云端后台

1. 将 `deploy/.env.example` 复制为 `deploy/.env`，替换所有密码和密钥。
2. 云服务器执行：

   ```powershell
   docker compose --env-file .env up -d --build
   ```

3. Compose 会启动内部 API 和第一层 Nginx 安全网关；只有网关绑定 `127.0.0.1:8080`，Spring Boot 不再暴露宿主机端口。
4. 使用云平台负载均衡器、Cloudflare 或宿主机 HTTPS 网关，将 `https://api.你的域名` 转发到 `127.0.0.1:8080`。
5. 不要将 MySQL 的 3306 端口开放到公网。
6. `GAME_MATE_ALLOWED_ORIGINS` 保留 `gamemate://app`，并只加入你自己的网站域名。

### 两层接口防火墙

- 第一层 `deploy/gateway/nginx.conf`：限制单 IP 连接数、普通接口频率、登录注册频率和上传频率；拒绝危险 HTTP 方法、扫描路径和异常请求体。
- 第二层 `ApplicationFirewallFilter`：即使请求绕过网关，Spring Boot 仍会再次限制登录爆破、接口洪泛、危险路径和超大请求。

可在 `.env` 调整第二层阈值：

```dotenv
GAME_MATE_FIREWALL_ENABLED=true
GAME_MATE_FIREWALL_GENERAL_PER_MINUTE=240
GAME_MATE_FIREWALL_AUTH_PER_FIVE_MINUTES=12
```

生产环境不要关闭第二层防火墙。如果多台 API 实例共同运行，应再使用 Redis 或云 WAF 做跨实例统一计数。

## 2. 生成 Windows 安装包

在 `game-mate` 目录打开 PowerShell：

```powershell
$env:DESKTOP_API_BASE_URL='https://api.你的域名'
$env:DESKTOP_UPDATE_URL='https://download.你的域名/ai-esports-manager'
$env:DESKTOP_UPDATE_CHANNEL='latest'
npm.cmd run desktop:dist
```

安装包生成在 `game-mate/release/`。对外仅发布 `AI电竞经理-Setup-*.exe`。

## 3. Windows 数字签名

正式发布时使用可信的代码签名证书：

```powershell
$env:CSC_LINK='C:\secure\codesign-certificate.pfx'
$env:CSC_KEY_PASSWORD='证书密码'
$env:WINDOWS_PUBLISHER_NAME='证书中的公司名'
$env:REQUIRE_CODE_SIGNING='true'
npm.cmd run desktop:dist
```

密码应放在 CI/CD 的 Secret 或安全的本机环境变量中，不要写入项目文件或 Git 仓库。

## 4. 自动更新

配置 `DESKTOP_UPDATE_URL` 后，打包会生成安装程序、`latest.yml` 和 blockmap 文件。将这些文件一起上传到更新地址。

客户端启动 8 秒后自动检查，发现新版本后自动下载，并在退出软件时安装。更新地址必须使用 HTTPS，且每个正式版本必须使用同一份代码签名证书。

## 5. 安全检查

每次发布前确认：

- `desktop-dist/runtime-config.json` 只包含公开 API 和更新地址。
- `release/win-unpacked/resources/app.asar` 中不存在 `src`、`.env`、`game-mate-server` 或 sourcemap。
- 客户端搜索不到 `AI_API_KEY`、`DB_PASSWORD`、`JWT_SECRET` 的真实值。
- 云端 CORS 白名单不包含 `*`。
- Spring Boot 容器没有 `ports` 公网映射，只有 `gateway` 绑定宿主机回环地址。
- Nginx 网关配置通过 `nginx -t`，应用防火墙测试全部通过。
- 所有对外接口和更新下载均使用 HTTPS。
