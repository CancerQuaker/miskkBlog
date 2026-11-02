# Vditor Markdown 编辑器改造 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 用默认即时渲染的 Vditor 替换文章页源码编辑器，并提供图片上传、三种编辑模式、写作辅助和按文章隔离的草稿缓存。

**Architecture:** 在组件层封装 Vditor 的创建、销毁、主题切换、上传和缓存，通过 ref 暴露保存所需的最小接口。文章编辑页仅在数据加载完成后挂载编辑器，继续复用现有文章 API 和前台 Markdown 渲染，不修改后端。

**Tech Stack:** React 18、TypeScript 5.6、Vite 6、Ant Design 5、Vditor。

**Spec:** `docs/superpowers/specs/2026-11-02-vditor-editor-design.md`

## Global Constraints

- 默认使用 IR 模式，允许切换 SV 和 WYSIWYG；保留 CommonMark/GFM，不新增前台不支持的扩展语法。
- 正文图片复用 `articleApi.uploadImage(file: File): Promise<string>`，后端接口和 Markdown 存储格式不变。
- 新文章与已有文章使用不同缓存键，保存成功后清理对应缓存。
- 不为前端新增测试脚手架；使用 `npm run build` 和真实页面验证。
- 每完成一个任务本地提交，不 push；不暂存或修改现有后端工作区改动。

## Review Focus

- React StrictMode 重复挂载不得留下重复实例或覆盖正文：Task 1 在构建后通过页面挂载检查，Task 2 验证新建与编辑页初始化。
- 异步文章加载不得被空初始值或缓存覆盖：Task 2 在加载完成后挂载，并用已有文章验证正文及缓存恢复。
- 多图片上传必须保留文件顺序且单张失败可提示：Task 1 用自定义 `upload.handler` 顺序插入，Task 2 验证实际上传链路。
- 工具栏输入后立即保存不得丢失最后一个字符：Task 2 保存时从 ref 读取编辑器最新值并验证。
- 保存后旧草稿不得污染下一篇文章：Task 2 按缓存键清理，并验证新建/编辑草稿互不影响。

---

### Task 1: 封装 Vditor React 编辑器

**Files:**
- Create: `frontend/src/components/MarkdownEditor.tsx`
- Create: `frontend/src/styles/markdown-editor.css`
- Modify: `frontend/package.json`
- Modify: `frontend/package-lock.json`

**Interfaces:**
- Consumes: `articleApi.uploadImage` 的同签名上传函数由调用方通过 prop 传入。
- Produces: 默认导出 `MarkdownEditor`，props 为 `{ initialValue: string; onChange(value: string): void; onUpload(file: File): Promise<string>; cacheKey: string }`。
- Produces: `MarkdownEditorHandle`，包含 `getValue(): string`、`clearCache(): void`、`focus(): void`；组件通过 `forwardRef` 暴露。

- [ ] **Step 1: 替换编辑器依赖**
  在 `frontend` 执行 `npm uninstall @uiw/react-md-editor` 和 `npm install vditor`，预期 `package.json` 只保留 Vditor，锁文件同步更新。

- [ ] **Step 2: 实现编辑器生命周期与受控接口**
  在 `MarkdownEditor.tsx` 中以 `cacheKey` 为初始化边界创建实例，使用 `initialValue` 初始化、`input` 回调同步正文；回调和上传函数通过 ref 保持最新，输入时不重建实例。卸载时销毁实例，并处理 StrictMode 下异步 `after` 回调晚于清理的情况。初始化结束后把包含缓存恢复结果的 `getValue()` 回传 `onChange`。

- [ ] **Step 3: 配置写作功能**
  默认 `mode: 'ir'`、`lang: 'zh_CN'`，启用标题、排版、列表、任务、代码、链接、表格、上传、撤销重做、模式切换、预览、全屏和大纲工具；启用计数、底部尺寸拖动和工具栏固定。缓存使用 `cacheKey`，不启用 Mermaid、数学公式等扩展。

- [ ] **Step 4: 接入图片上传**
  配置 `upload.handler` 逐张调用最新 `onUpload`，按文件原顺序通过 `insertValue` 插入 `![安全文件名](url)`；全部成功返回 `null`，失败返回可读错误并用 Ant Design `message.error` 提示。保留 `accept: 'image/*'`，使工具栏、粘贴和拖拽走同一上传逻辑。

- [ ] **Step 5: 适配主题和后台样式**
  引入 Vditor 样式及 `markdown-editor.css`，根据 `html[data-theme]` 的 `dark` 状态调用 `setTheme`；用 CSS 变量调整边框、背景、工具栏、正文宽度和聚焦样式，护眼模式保持浅色。监听主题属性变化并在卸载时断开 observer。

- [ ] **Step 6: 构建并提交组件**
  运行 `cd frontend && npm run build`，预期 TypeScript 与 Vite 构建成功；仅提交 Task 1 文件，提交信息为 `feat: 封装 Vditor Markdown 编辑器组件`。

### Task 2: 接入文章编辑页并验证完整流程

**Files:**
- Modify: `frontend/src/pages/admin/ArticleEdit.tsx`
- Modify: `frontend/src/styles/markdown-editor.css`（仅页面验证需要微调时）

**Interfaces:**
- Consumes: Task 1 的 `MarkdownEditor`、`MarkdownEditorHandle`。
- Consumes: 现有 `articleApi.uploadImage`、`adminArticleApi.create/update` 保持不变。

- [ ] **Step 1: 替换文章页编辑器**
  删除 `@uiw/react-md-editor` 用法，在已有文章加载完成或新文章直接可用时渲染 `MarkdownEditor`。传入 `initialValue={content}`、`onChange={setContent}`、`onUpload={articleApi.uploadImage}`，缓存键为 `article-new` 或 `article-${id}`，并以相同 key 保证路由文章切换时重新初始化。

- [ ] **Step 2: 保存最新正文并清理草稿**
  通过 `MarkdownEditorHandle` 在校验后读取 `getValue()`，将其用于创建/更新 payload；只有 API 保存成功后才调用 `clearCache()`，随后保持现有提示和跳转。保存失败时保留缓存与页面正文。

- [ ] **Step 3: 运行标准构建**
  在 `frontend` 运行 `npm run build`，预期无类型错误且 Vite 构建成功；搜索 `@uiw/react-md-editor`，预期前端源码与依赖清单无残留。

- [ ] **Step 4: 验证真实写作流程**
  启动前端开发服务器，在浏览器登录后台并检查：新文章默认 IR 输入与排版、SV/WYSIWYG 切换、工具栏和全屏；粘贴或选择图片后生成正确 Markdown URL；刷新能恢复对应草稿；编辑已有文章正文不丢失且缓存隔离；保存后草稿清除，文章详情仍由现有渲染器正确展示。主题切换后编辑器保持可读。

- [ ] **Step 5: 提交页面接入**
  仅提交 Task 2 文件，提交信息为 `feat: 文章页接入 Vditor 写作体验`。
