# 主题色星空闪烁 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将随机星空调整为首页横幅四角星的同款闪烁效果。

**Architecture:** React 仅创建和清理节点，CSS 直接复用 `.hero-star` 的 `clip-path` 与 `starTwinkle` 关键帧；随机尺寸和负延迟由组件一次生成。

**Tech Stack:** React 18、TypeScript、CSS 变量与 keyframes、一次性 Node 验证。

**Spec:** `docs/superpowers/specs/2026-11-05-themed-falling-stars-design.md`

## Global Constraints
- 默认 48 颗、12–22px，不新增依赖，不使用 rAF。
- 2.6 秒周期，透明度 0.25–0.85，缩放 0.7–1.15，旋转 0–45°；主题色为 `var(--primary)`。

## Review Focus
- 星星位置固定，缩放旋转和明暗节奏与横幅一致。
- 随机负延迟不得造成整片同步闪烁；正文保持可读。
- 主题切换、关闭清理、重开和减少动态效果偏好不得回归。

---

### Task 1: 复用横幅星空动画

**Files:**
- Modify: `frontend/src/components/SakuraEffect.tsx`
- Modify: `frontend/src/styles/global.css`

**Interfaces:**
- Consumes: 现有 `sakura` 开关、`.hero-star` 轮廓和 `starTwinkle`。
- Produces: 48 个使用横幅同款动画、随机位置和负延迟的四角星节点。

- [ ] **Step 1: 写并运行失败验证**
断言 48 颗、2.6 秒周期、CSS 四角星轮廓和 `starTwinkle` 复用、零 rAF；原 SVG 与仅透明度动画应失败。

- [ ] **Step 2: 实现同款动画**
组件移除 SVG、随机周期和自定义透明度；CSS 使用横幅轮廓、主题色与 `starTwinkle`，减少动态效果偏好下静态显示。

- [ ] **Step 3: 验证并提交**
重跑节点与动画验证、`npm run build`，浏览器对照横幅检查形状、缩放旋转、主题色、正文和开关，本地提交，不推送。
