# 迷失客客の博客 🐱

一个二次元风格的全栈博客系统：**Vite + React 18** 前端，**Spring Boot 3.3 微服务（Spring Cloud Alibaba + Nacos）** 后端，MySQL + Redis 数据层，Knife4j 接口文档。

> ✨ 特色：猫咪鼠标指针（左键「喵~」右键歪头「？」）、Live2D 看板娘、樱花特效、亮/暗/护眼/跟随系统四主题、音乐播放器、文章评论点赞分享与在线时长统计、友链申请审核、ECharts 数据看板与系统资源监控。

---

## 目录结构

```
miskkBlog/
├── pom.xml                     # Maven 父 POM（仓库根目录，IDEA 直接打开根目录即可导入）
├── backend/                    # 后端（Maven 多模块微服务）
│   ├── miskk-common/           # 公共模块：Result/JWT/拦截器/公共配置
│   ├── miskk-gateway/          # 网关 :8080 —— 统一入口（controller → handler → FeignClient 转发）、JWT 全局鉴权、CORS
│   ├── miskk-service-user/     # 用户服务 :8101 —— 注册登录/OAuth/资料/头像上传
│   └── miskk-service-blog/     # 博客服务 :8102 —— 文章/分类/标签/评论/点赞/浏览/分享/时长 + 友链 + 管理端（统计聚合/系统监控，双数据源）
├── frontend/                   # 前端（Vite 6 + React 18 + TS + AntD 5）
├── sql/                        # MySQL 建库脚本（三库：user / article / friend）
├── docker/docker-compose.yml   # MySQL + Redis + Nacos 一键启动
└── tools/                      # 密码哈希生成小工具
```

## 技术栈

| 端 | 技术 |
|------|------|
| 前端 | Vite 6 · React 18 · TypeScript · Ant Design 5 · React Router 6 · ECharts · Axios · react-markdown · @uiw/react-md-editor · oh-my-live2d · zustand |
| 后端 | Spring Boot 3.3.13 · Spring Cloud 2023.0.6 · Spring Cloud Alibaba 2023.0.3.4（Nacos）· OpenFeign · Gateway · MyBatis-Plus 3.5.17 · dynamic-datasource（双数据源）· Hutool · jjwt · Knife4j · oshi |
| 数据 | MySQL 8（三库分离）· Redis 7（验证码 / 浏览量缓冲 / 用户名片缓存） |

## 快速开始

### 0. 环境要求

- JDK 17+（本项目在 JDK 24 上验证编译通过）
- Node.js 18+（在 Node 24 验证）
- Maven 3.8+、MySQL 8、Redis、Nacos 2.2+

### 1. 启动基础设施

有 Docker：

```bash
cd docker
docker compose up -d      # MySQL(3306) + Redis(6379) + Nacos(8848)
```

无 Docker：本机安装 MySQL / Redis，并从 [Nacos 发布页](https://github.com/alibaba/nacos/releases) 下载后以 `startup.cmd -m standalone` 启动。

### 2. 初始化数据库

```bash
mysql -uroot -p < sql/01_miskk_user.sql
mysql -uroot -p < sql/02_miskk_article.sql
mysql -uroot -p < sql/03_miskk_friend.sql
```

默认连接参数为 `127.0.0.1:3306` / `root` / `123456`，可用环境变量覆盖（见下文配置表）。

### 3. 启动后端

在仓库根目录执行（父 POM 就在根目录）：

```bash
mvn clean install -DskipTests

# 依次启动（或直接在 IDEA 中运行各模块的 *Application 主类）
mvn -pl backend/miskk-gateway         spring-boot:run
mvn -pl backend/miskk-service-user    spring-boot:run
mvn -pl backend/miskk-service-blog    spring-boot:run
```

各服务启动后自动注册到 Nacos（`127.0.0.1:8848`）。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev        # http://localhost:5173
```

Vite 开发服务器把 `/api` 代理到网关 `http://localhost:8080`。

### 5. 默认账号

| 用户名 | 密码 | 说明 |
|--------|------|------|
| admin | admin123 | 管理员（可进 `/admin` 后台） |

注册普通用户：邮箱验证码方式；**未配置 SMTP 时验证码会直接在响应与用户服务日志中返回**，方便本地调试。

## 配置说明（环境变量 / application.yml）

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `NACOS_ADDR` | `127.0.0.1:8848` | Nacos 注册中心 |
| `MYSQL_ADDR` / `MYSQL_USER` / `MYSQL_PASSWORD` | `127.0.0.1:3306` / `root` / `123456` | MySQL 连接 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | `127.0.0.1` / `6379` / 空 | Redis |
| `JWT_SECRET` | 内置开发值 | JWT 密钥（**生产必须替换**，≥32 字节，网关与各服务一致） |
| `INTERNAL_TOKEN` | `miskk-internal-token-dev` | 服务间内部接口令牌 |
| `MAIL_HOST` / `MAIL_USER` / `MAIL_PASSWORD` | 空 | 配置后验证码真实发信；为空则调试模式 |
| `MAIL_DEBUG` | `true` | 未配置邮件时在响应中返回验证码 |
| `GITHUB_CLIENT_ID` / `GITHUB_CLIENT_SECRET` | 空 | GitHub OAuth 登录；为空则提示未开放 |
| `UPLOAD_DIR` | `~/.miskk/uploads` | 图片上传目录（用户/博客服务共享） |

> QQ / 微信登录需要企业资质才能开通互联平台，本项目已预留接口与提示（`/auth/qq`、`/auth/wechat`），GitHub OAuth 已完整实现，配置 Client-ID 后即可使用。

## 接口文档（Knife4j）

| 服务 | 地址 |
|------|------|
| 用户服务 | http://localhost:8101/doc.html |
| 博客服务 | http://localhost:8102/doc.html |

## 架构说明

```
                    ┌─────────────────┐
   浏览器 ──────────▶│  miskk-gateway  │ :8080
   (Vite/静态资源)   │ JWT 鉴权拦截器   │
                    │ controller → handler → FeignClient(全 POST)
                    └───────┬─────────┘
              /api/user│  /api/article│  /api/friend│  /api/admin
                 ┌─────▼───┐  ┌──────▼──────────────┐
                 │  user   │  │    blog（合并服务）    │
                 │  :8101  │  │        :8102         │
                 └────┬────┘  │  文章 │ 友链 │ 管理端 │
                      │       └──────┬─────┬─────┬───┘
                      │  Feign(内部接口, X-Internal-Token)│
                      ▼              ▼     ▼     ▼
                   MySQL         MySQL    MySQL  oshi 监控
                  miskk_user   miskk_article(m主) miskk_friend(@DS)
                    └────────────┴──────────────┴──── Redis（验证码/浏览量缓冲/名片缓存）

                       全部服务注册到 Nacos :8848（lb:// 负载均衡调用）
```

设计要点：

- **网关统一鉴权**：JWT 在网关解析，用户身份通过 `X-User-Id / X-User-Role` 请求头下发；下游服务通过拦截器还原 ThreadLocal 并校验 `@RequireLogin / @RequireAdmin`。
- **内部接口保护**：`/internal/**` 只接受带 `X-Internal-Token` 的 Feign 调用。
- **浏览量削峰**：浏览经 Redis `SADD` 按访客按天去重 + Hash 缓冲增量，博客服务每 30 秒批量落库。
- **在线观看时长**：前端每 20 秒心跳上报（页面可见时），离开页面用 `sendBeacon` 兜底；按访客+日期聚合。
- **独立数据库**：三个业务库保持独立；博客服务通过 dynamic-datasource 同时连接 `miskk_article`（主）与 `miskk_friend`（`@DS("friend")` 路由，友链事务也绑定 friend 连接），用户统计仍经 Feign 调用用户服务。

## 前端设计说明

- 主题：`亮色(樱花粉) / 暗色(星云紫) / 护眼(抹茶绿) / 跟随系统`，AntD token + CSS 变量双轨同步；导航栏「外观」面板支持自定义主题色（8 个预设 + 取色器）与特效开关（飘落物 / 鼠标样式 / 看板娘），全部持久化。
- 猫咪光标 `NekoCursor`：桌面端隐藏系统光标，SVG 猫咪以插值动画跟随，左键眨眼「喵~」、右键歪头「？」、闲置 3 秒睡觉。
- 移动端适配：窄屏下导航精简（搜索/主题转图标、渐变胶囊登录按钮、汉堡抽屉菜单）、隐藏站名与看板娘、卡片单列布局。
- 看板娘：`oh-my-live2d` 加载 Live2D 模型，视线跟随鼠标、闲置语录轮播（模型从 CDN 加载，可替换为自有模型文件）。
- 音乐播放器：全局单例 `<audio>`，导航栏入口 + 悬浮面板，播放列表在 `src/config/site.ts` 中配置。

## 致谢

- 前端布局与组件设计参考开源主题 [CuteLeaf/Firefly](https://github.com/CuteLeaf/Firefly)（MIT，基于 [fuwari](https://github.com/saicaca/fuwari)），本项目为 React + 后端驱动的独立实现。
- 后端代码为本项目全新编写，未参考任何现成博客系统源码。
