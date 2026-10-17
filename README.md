# 迷失客客の博客 🐱

一个二次元风格的全栈博客系统：**Vite + React 18** 前端，**Spring Boot 3.3 微服务（Spring Cloud Alibaba + Nacos）** 后端，MySQL + Redis 数据层，Knife4j 接口文档。

> ✨ 特色：魔法星星鼠标指针（拖尾 + 左键掉落文字彩蛋）、自定义右键菜单、Live2D 看板娘（仙狐小姐，本地模型）、樱花特效、亮/暗/护眼/跟随系统四主题、音乐播放器（网易云歌单 + 歌词同步）、文章评论点赞分享与在线时长统计、友链申请审核、ECharts 数据看板与系统资源监控。

---

## 目录结构

```
miskkBlog/
├── pom.xml                     # Maven 父 POM（仓库根目录，IDEA 直接打开根目录即可导入）
├── backend/                    # 后端（Maven 多模块微服务）
│   ├── miskk-common/           # 公共模块：Result/JWT/拦截器/公共配置
│   ├── miskk-gateway/          # 网关 :8100 —— 统一入口（controller → handler → FeignClient 转发）、JWT 全局鉴权、CORS
│   ├── miskk-service-user/     # 用户服务 :8101 —— 注册登录/OAuth/资料/头像上传
│   └── miskk-service-blog/     # 博客服务 :8102 —— 文章/分类/标签/评论/点赞/浏览/分享/时长 + 友链 + 管理端（统计聚合/系统监控，双数据源）
├── frontend/                   # 前端（Vite 6 + React 18 + TS + AntD 5）
├── sql/                        # MySQL 建库脚本（三库：user / article / friend）
└── docker/
    ├── docker-compose.yml      # 挂载本机已构建产物（jar/dist）启动全栈；亦可只起基础设施
    └── docker-compose.auto.yml # 一键编译+部署：拉代码即起全栈（容器内构建），见「生产部署」
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

有 Docker（`docker-compose.yml` 已含全套服务，本地开发只起这三个即可）：

```bash
cd docker
cp .env.example .env     # 本地开发建议把 MYSQL_PASSWORD 设为 123456（与后端默认连接参数一致）
# 编辑 docker-compose.yml，放开 mysql / redis / nacos 注释掉的 ports（后端要从宿主机连接）
docker compose up -d mysql redis nacos    # MySQL(3306) + Redis(6379) + Nacos(8848)，首次启动自动执行 sql/ 建库
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

Vite 开发服务器把 `/api` 代理到网关 `http://localhost:8100`。

### 5. 默认账号

| 用户名 | 密码 | 说明 |
|--------|------|------|
| admin | admin123 | 管理员（可进 `/admin` 后台） |

注册普通用户：邮箱验证码方式；**未配置 SMTP 时验证码会直接在响应与用户服务日志中返回**，方便本地调试。

## 配置说明（环境变量 / application.yml）

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `NACOS_ADDR` | `127.0.0.1:8848` | Nacos 注册中心 |
| `NACOS_USERNAME` / `NACOS_PASSWORD` | 空 | Nacos 开启鉴权时服务注册用的账号密码（docker 部署配 `NACOS_AUTH_ENABLE` 时使用） |
| `MYSQL_ADDR` / `MYSQL_USER` / `MYSQL_PASSWORD` | `127.0.0.1:3306` / `root` / `123456` | MySQL 连接 |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | `127.0.0.1` / `6379` / 空 | Redis |
| `JWT_SECRET` | 内置开发值 | JWT 密钥（**生产必须替换**，≥32 字节；签发与解析都在网关，仅网关使用） |
| `INTERNAL_TOKEN` | `miskk-internal-token-dev` | 服务间内部接口令牌 |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USER` / `MAIL_PASSWORD` | 空 / `465` / 空 / 空 | 配置后验证码真实发信；为空则调试模式 |
| `MAIL_DEBUG` | `true` | 未配置邮件时在响应中返回验证码 |
| `GITHUB_CLIENT_ID` / `GITHUB_CLIENT_SECRET` | 空 | GitHub OAuth 登录；为空则提示未开放 |
| `UPLOAD_DIR` | `~/.miskk/uploads` | 图片上传目录（用户/博客服务共享） |

> QQ / 微信登录需要企业资质才能开通互联平台，本项目已预留接口与提示（`/auth/qq`、`/auth/wechat`），GitHub OAuth 已完整实现，配置 Client-ID 后即可使用。

### 6. 生产部署（Ubuntu 云服务器等）

全容器一键部署：后端在容器内 Maven 多阶段构建，前端 npm build 后由 nginx 托管并反代 `/api`。服务器上只需装好 Docker（含 compose 插件）：

```bash
git clone https://github.com/CancerQuaker/miskkBlog.git
cd miskkBlog/docker
cp .env.example .env    # 必填 MYSQL_PASSWORD / JWT_SECRET / INTERNAL_TOKEN；端口/Nacos 鉴权/JVM 等可选变量见文件内说明
bash deploy.sh          # 等价于 docker compose -f docker-compose.auto.yml up -d --build
```

说明：

- **本机已有构建产物**时（根目录 `mvn clean install -DskipTests` + `frontend` 下 `npm run build`），可改用同目录 `docker-compose.yml`：不做容器内编译，直接挂载 `backend/*/target/*.jar` 与 `frontend/dist` 启动同一套全栈（路径可在 `.env` 用 `GATEWAY_JAR_DIR` / `FRONTEND_DIST_DIR` 等变量改指别处；与 `.env` 通用；只想起基础设施时 `docker compose up -d mysql redis nacos`）。
- **安全模型**：默认只有 nginx 暴露 `SITE_PORT`（默认 80）端口，MySQL/Redis/Nacos/网关都在 compose 内部网络，云安全组只需放行 80/443。所有端口（宿主机侧与容器内侧，如 MySQL/Redis/Nginx）都可在 `.env` 自定义，MySQL/Redis/Nacos 的宿主机映射在 compose 里以注释预留。
- **首次构建**约需几分钟（容器内编译）；国内服务器可在 `.env` 配 `MAVEN_MIRROR` / `NPM_REGISTRY` 镜像加速。
- **数据备份**：`mysql-data` / `redis-data` 卷 + `docker/uploads/` 目录（上传图片）。
- **HTTPS**：宿主机再加一层 Caddy / certbot 终止证书，反代到 80 即可。
- **内存**：建议 2核4G 起步；2核2G 可给三个 Java 服务加 `JAVA_TOOL_OPTIONS=-Xmx256m`、Nacos `JVM_XMX=256m`。
- 首次启动自动执行 `sql/` 三个建库脚本（仅空数据卷时）；默认管理员 `admin / admin123`，部署后请尽快改密。

## 接口文档（Knife4j）

| 服务 | 地址 |
|------|------|
| 用户服务 | http://localhost:8101/doc.html |
| 博客服务 | http://localhost:8102/doc.html |

## 架构说明

```
                    ┌─────────────────┐
   浏览器 ──────────▶│  miskk-gateway  │ :8100
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

- **网关统一鉴权**：JWT 的签发与解析都只在网关（登录校验通过后由网关生成 token，密钥仅网关持有），用户身份通过 `X-User-Info` 请求头（LoginUser 序列化 JSON：userId/role/ip）下发；下游服务通过拦截器还原 ThreadLocal 并校验 `@RequireLogin / @RequireAdmin`。
- **内部接口保护**：`/internal/**` 只接受带 `X-Internal-Token` 的 Feign 调用。
- **浏览量削峰**：浏览经 Redis `SADD` 按访客按天去重 + Hash 缓冲增量，博客服务每 30 秒批量落库。
- **在线观看时长**：前端每 20 秒心跳上报（页面可见时），离开页面用 `sendBeacon` 兜底；按访客+日期聚合。
- **独立数据库**：三个业务库保持独立；博客服务通过 dynamic-datasource 同时连接 `miskk_article`（主）与 `miskk_friend`（`@DS("friend")` 路由，友链事务也绑定 friend 连接），用户统计仍经 Feign 调用用户服务。

## 前端设计说明

- 主题：`亮色 / 暗色 / 护眼 / 跟随系统`（默认主题色草绿 `#80a942`），AntD token + CSS 变量双轨同步；导航栏「外观」面板支持自定义主题色（8 个预设 + 取色器）与特效开关（樱花飘落 / 星星鼠标 / 看板娘），全部持久化。
- 星星光标 `StarCursor`：桌面端隐藏系统光标，旋转四角星跟随鼠标并沿途洒落拖尾小星星，左键掉落「富强/民主/文明/和谐…」彩色文字；右键呼出自定义右键菜单（`ContextMenu`）。
- 移动端适配：窄屏下导航精简（搜索/主题转图标、渐变胶囊登录按钮、汉堡抽屉菜单）、隐藏站名与看板娘、卡片单列布局。
- 看板娘：`oh-my-live2d` 加载本地模型「仙狐小姐」（`frontend/public/live2d/senko/`），视线跟随鼠标、闲置语录轮播；摸头睡觉、双击唱歌、点身体触发动作（由模型 `senko.model3.json` 命中区定义），移动端不加载。
- 音乐播放器：全局单例 `<audio>`，导航栏入口 + 悬浮面板；默认通过 Meting API 拉取网易云歌单（知更鸟《空气中的听觉幻术》，主 API 失败自动切换备用），支持歌词同步 / 播放模式 / 封面 / 音量记忆，可在 `src/config/site.ts` 切换为内置本地歌单。

## 致谢

- 前端布局与组件设计参考开源主题 [CuteLeaf/Firefly](https://github.com/CuteLeaf/Firefly)（MIT，基于 [fuwari](https://github.com/saicaca/fuwari)），本项目为 React + 后端驱动的独立实现。
- 后端代码为本项目全新编写，未参考任何现成博客系统源码。
