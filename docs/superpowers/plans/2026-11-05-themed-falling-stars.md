# 主题色星空闪烁 Implementation Plan

> **历史方案（已归档，请勿继续实施）**：本文记录旧的 DOM 四角星闪烁方案，不再描述当前实现。当前 Canvas 固定区域柔光尘见 `../specs/2026-11-07-canvas-dust-design.md`。

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让横幅同款闪烁星星在每轮最暗时随机换位，避免永久固定坐标。

**Architecture:** CSS 复用 `.hero-star` 的 `clip-path` 与 `starTwinkle`；React 通过 `animationiteration` 在迭代边界更新坐标，并在清理时移除监听。

**Tech Stack:** React 18、TypeScript、CSS 变量与 keyframes、一次性 Node 验证。

**Spec:** `docs/superpowers/specs/2026-11-05-themed-falling-stars-design.md`

## Global Constraints
- 默认 48 颗、12–22px、2.6 秒周期，不新增依赖，不使用 rAF 或定时器。
- 主题色为 `var(--primary)`；位置切换只发生在最低透明度边界。

## Review Focus
- 每轮结束坐标改变，动画形状、缩放旋转和明暗节奏与横幅一致。
- `randomStarX(random)` 按 40% 左边缘、40% 右边缘、20% 中央分配，避免中央阅读区过密。
- 随机负延迟不得造成整片同步换位；正文保持可读。
- 关闭时必须移除事件监听和节点，重开恢复 48 颗。

---

### Task 1: 闪烁后随机换位

**Files:**
- Modify: `frontend/src/components/SakuraEffect.tsx`
- Modify: `frontend/src/styles/global.css`（星空层使用 absolute，body 建立整页定位容器）

**Interfaces:**
- Consumes: 现有 `sakura` 开关和 `starTwinkle`。
- Produces: 48 个带 `animationiteration` 换位监听的四角星节点。

- [ ] **Step 1: 写并运行失败验证**
断言初始坐标随机，触发 `animationiteration` 后坐标改变，清理移除监听和节点；固定坐标实现应失败。

- [ ] **Step 2: 实现迭代换位**
为每颗星注册同一 `reposition` 回调，在迭代边界更新百分比坐标，清理时成对移除监听。

- [ ] **Step 3: 验证并提交**
重跑节点与动画验证、`npm run build`，浏览器检查随机换位、主题色和正文，本地提交，不推送。
