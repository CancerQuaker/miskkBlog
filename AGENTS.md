# AGENTS.md

迷失客客の博客：二次元风格全栈博客系统。前端 Vite 6 + React 18 + TS + AntD 5，后端 Spring Boot 3.3 微服务（Spring Cloud Alibaba + Nacos + OpenFeign），MySQL 三库 + Redis。详细架构图、配置表见 `README.md`。

## 目录结构

- `backend/miskk-common` — 公共模块：Result/JWT/拦截器/公共配置。**DTO/VO/Feign 公共请求头/knife4j 配置统一放这里，不要在服务模块重复定义**
- `backend/miskk-gateway` — 网关 :8100，唯一对外入口
- `backend/miskk-service-user` — 用户服务 :8101
- `backend/miskk-service-blog` — 博客服务 :8102（文章/友链/管理端，双数据源）
- `frontend/` — Vite + React；`/api` 代理到网关 :8100
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

# 单元测试（gateway，工具型：密码哈希/内部 token/登录 token）
mvn -pl backend/miskk-gateway test
mvn -pl backend/miskk-gateway test -Dtest=SecurityToolTest#generatePasswordHash    # 生成 BCrypt 密码哈希（写入 SQL 种子）
mvn -pl backend/miskk-gateway test -Dtest=SecurityToolTest#generateInternalToken   # 生成随机内部 token（写入 docker/.env）
```

gateway 之外暂无测试；整包构建仍带 `-DskipTests`。

Vite dev server 长时间运行后偶发吐出新旧混杂的模块代码（文件明明改了、页面仍跑旧逻辑，刷新无效，`curl` 模块内容可证实）：杀掉 5173 进程重启 `npm run dev` 即可，不必排查代码。

## 架构规则（改动前必读）

- **API 风格分两层**：blog/user 服务对外接口**全部 POST + JSON 请求体**；网关是 Spring MVC 风格（controller → handler → FeignClient 转发），对前端保持原 GET/PUT/DELETE 风格。加接口时两层都要写。
- **鉴权**：JWT 的签发与解析都只在网关（登录校验通过后由网关生成 token，`JWT_SECRET` 仅网关配置），身份以单个 `X-User-Info` 请求头（LoginUser 序列化 JSON：userId/role/ip，网关侧 `UserContext.toJson()` 生成）经 Feign 下发，下游用拦截器还原 ThreadLocal 并校验 `@RequireLogin / @RequireAdmin`（注解在 miskk-common）。
- **内部接口**：`/internal/**` 只接受带 `X-Internal-Token` 的调用；Feign 服务间直连（不走网关），公共头已封装在 miskk-common 的 Feign 配置里。
- **双数据源**：博客服务 miskk_article 为主库，friend 库用 `@DS("friend")` 路由，友链事务也绑定 friend 连接。
- **浏览量/时长**：Redis 削峰（SADD 按访客按天去重 + Hash 缓冲，30 秒批量落库）；前端每 20 秒心跳 + sendBeacon 兜底。改动文章/时长逻辑时注意这条异步链路。
- **前端**：状态用 zustand（`src/stores`），站点配置在 `src/config/site.ts`。
- **主题系统双轨**：CSS 变量按 `html[data-theme='light|dark|eye']` 三块定义（`src/styles/global.css`），AntD token 在 `main.tsx` 同步切换。默认主题色 `#80a942`（草绿）。
- **主题色覆盖链**：用户自选主题色存 zustand `accent`（null = 用主题内置色），在 `main.tsx` 以行内 CSS 变量覆盖 `--primary / --primary-strong / --primary-weak / --primary-light`（行内样式优先级高于所有主题块）。组件一律消费 `var(--primary*)`，不要硬编码主题色；SVG 的 `fill / stopColor` 想吃 `var()` 必须写进 `style` 属性，写表现属性不生效。
- **看板娘**：oh-my-live2d 挂在独立根元素 `#oml2d-stage`（DOM 里没有 `#oml2d` 包装层），选择器别写错。模型为本地文件 `frontend/public/live2d/senko/`（仙狐小姐），交互动作由 `senko.model3.json` 的命中区/动作组定义（`TapHead` / `TapBody` 命名须符合 Cubism 规范）。


## 编码约定
- **Git 流程**：修改的代码先本地提交（每完成一个任务即 commit）；勿 push，推送远程需用户明确要求。
- Java 17 目标；Lombok 注解处理器已在父 POM 的 maven-compiler-plugin 显式配置（`proc: full`），JDK 23+ 必须保留。
- 前端 TypeScript 严格模式，提交前跑 `npm run build` 验证类型。
- 文档与注释使用中文。
- mysql、redis、nacos等用户名密码，查阅各后端模块中的配置文件获取
