# 主题色星空闪烁 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将四角星飘落改为随机固定位置、独立闪烁的主题色星空。

**Architecture:** React 仅创建和清理节点，位置与闪烁参数一次生成；CSS keyframes 负责透明度动画，不再使用 rAF。

**Tech Stack:** React 18、TypeScript、CSS 变量与 keyframes、一次性 Node 验证。

**Spec:** `docs/superpowers/specs/2026-11-05-themed-falling-stars-design.md`

## Global Constraints
- 默认 48 颗、12–22px、峰值透明度 0.18–0.42，不新增依赖。
- 不使用 JavaScript 持续动画循环；保留主题变量、开关与窄屏禁用。

## Review Focus
- 星星位置固定且周期错开，不得继续下落或同步闪烁。
- 主题切换改变星光颜色，正文保持可读。
- 关闭清理、重开重建和减少动态效果偏好不得回归。

---

### Task 1: 星空闪烁

**Files:**
- Modify: `frontend/src/components/SakuraEffect.tsx`
- Modify: `frontend/src/components/Navbar.tsx`
- Modify: `frontend/src/config/site.ts`
- Modify: `frontend/src/stores/themeStore.ts`
- Modify: `frontend/src/styles/global.css`

**Interfaces:**
- Consumes: 现有 `sakura` 开关与主题 CSS 变量。
- Produces: 48 个带随机百分比坐标、尺寸、透明度、周期和延迟的四角星节点。

- [ ] **Step 1: 写并运行失败验证**
断言生成 48 颗、坐标和动画参数合法、SVG 使用两个主题变量且没有调用 rAF；当前 12 颗飘落实现应失败。

- [ ] **Step 2: 实现静态分布与 CSS 闪烁**
移除 Petal 运动与 rAF，按随机参数创建节点；CSS 添加 `starfieldTwinkle` 和减少动态效果规则，同步全部可见文案。

- [ ] **Step 3: 验证并提交**
重跑节点与动画验证、`npm run build`，浏览器检查位置固定、随机闪烁、主题色、正文和开关，审查后本地提交，不推送。
