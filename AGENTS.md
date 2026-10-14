# AGENTS.md

迷失客客の博客：二次元风格全栈博客系统。前端 Vite 6 + React 18 + TS + AntD 5，后端 Spring Boot 3.3 微服务（Spring Cloud Alibaba + Nacos + OpenFeign），MySQL 三库 + Redis。详细架构图、配置表见 `README.md`。

## 目录结构

- `backend/miskk-common` — 公共模块：Result/JWT/拦截器/公共配置。**DTO/VO/Feign 公共请求头/knife4j 配置统一放这里，不要在服务模块重复定义**
- `backend/miskk-gateway` — 网关 :8080，唯一对外入口
- `backend/miskk-service-user` — 用户服务 :8101
- `backend/miskk-service-blog` — 博客服务 :8102（文章/友链/管理端，双数据源）
- `frontend/` — Vite + React；`/api` 代理到网关 :8080
- `sql/` — 三个库的建表脚本（miskk_user / miskk_article / miskk_friend）

## 常用命令

```bash
# 后端（父 POM 在仓库根目录，必须在根目录执行）
mvn clean install -DskipTests
mvn -pl backend/miskk-gateway spring-boot:run        # 或在 IDEA 运行各 *Application 主类
mvn -pl backend/miskk-service-user spring-boot:run
mvn -pl backend/miskk-service-blog spring-boot:run

# 前端
cd frontend && npm run dev        # http://localhost:5173
npm run build                     # tsc -b && vite build，类型检查也走这里
```

无测试套件，构建命令均带 `-DskipTests`。

## 架构规则（改动前必读）

- **API 风格分两层**：blog/user 服务对外接口**全部 POST + JSON 请求体**；网关是 Spring MVC 风格（controller → handler → FeignClient 转发），对前端保持原 GET/PUT/DELETE 风格。加接口时两层都要写。
- **鉴权**：JWT 只在网关解析，身份经 `X-User-Id / X-User-Role` 请求头下发，下游用拦截器还原 ThreadLocal 并校验 `@RequireLogin / @RequireAdmin`（注解在 miskk-common）。
- **内部接口**：`/internal/**` 只接受带 `X-Internal-Token` 的调用；Feign 服务间直连（不走网关），公共头已封装在 miskk-common 的 Feign 配置里。
- **双数据源**：博客服务 miskk_article 为主库，friend 库用 `@DS("friend")` 路由，友链事务也绑定 friend 连接。
- **浏览量/时长**：Redis 削峰（SADD 按访客按天去重 + Hash 缓冲，30 秒批量落库）；前端每 20 秒心跳 + sendBeacon 兜底。改动文章/时长逻辑时注意这条异步链路。
- **前端**：状态用 zustand（`src/stores`），站点配置在 `src/config/site.ts`；主题走 AntD token + CSS 变量双轨。

## 本机运行环境（Windows，这台机器实际用法）

- 不用 docker compose。MySQL 为手动启动的 mysqld：datadir `D:/miskk-infra/mysql-data`，账号 root/root（README 默认 root/123456 仅适用 docker）。
- Nacos 在 `D:\miskk-infra\nacos`（standalone）；Redis 为 Windows 服务。
- 网络受限：GitHub 克隆/下载走 ghfast 镜像前缀。
- 日志输出到仓库根 `logs/`（已 gitignore）；Knife4j 文档在各服务 `/doc.html`。

## 编码约定

- Java 17 目标（本机 JDK 24 验证）；Lombok 注解处理器已在父 POM 的 maven-compiler-plugin 显式配置（`proc: full`），JDK 23+ 必须保留。
- 前端 TypeScript 严格模式，提交前跑 `npm run build` 验证类型。
- 文档与注释使用中文。
