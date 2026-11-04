# 飘落物视觉优化 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将樱花调整为低存在感的柔和背景装饰，同时保证长期运行不增加 DOM 节点。

**Architecture:** 保留 `SakuraEffect` 的 DOM + rAF 和现有时间步长。用深度生成尺寸、速度与透明度，花瓣离屏后重置同一节点；CSS 只负责层级与合成，不增加滤镜。

**Tech Stack:** React 18、TypeScript、现有 CSS、一次性 Node 行为验证。

**Spec:** `docs/superpowers/specs/2026-11-04-sakura-visual-design.md`

## Global Constraints
- 不新增依赖，不改变飘落物开关、窄屏禁用和主题色逻辑。
- 默认 12 片、7–15px、透明度 0.12–0.28、下落 15–42px/秒。
- 不使用阴影、模糊或发光滤镜；`pointer-events: none` 保持不变。

## Review Focus
- 极浅或极深花瓣：参数边界分别由深度 0/1 的确定性测试覆盖。
- 花瓣反复离屏：回收必须复用节点，测试连续回收后的 DOM 数量。
- 页面掉帧与后台恢复：保留并通过现有 `getAnimationStep` 的 60/30/120 FPS 与补偿上限验证。
- 正文阅读：浏览器检查亮色、暗色与长正文区域，花瓣不得成为视觉主体。
- 特效重开：关闭时清除节点，重开恢复默认数量。

---

### Task 1: 樱花视觉参数与节点回收

**Files:**
- Modify: `frontend/src/components/SakuraEffect.tsx`
- Modify: `frontend/src/styles/global.css:1064`
- Modify: `docs/superpowers/specs/2026-11-04-sakura-visual-design.md`

**Interfaces:**
- Consumes: 现有 `getAnimationStep(elapsedMs: number): number`。
- Produces: `createPetalMotion(depth: number, random: () => number): Pick<Petal, 'size' | 'vy' | 'vx' | 'vr' | 'opacity'>`；深度限制在 0–1，返回值满足设计范围。

- [ ] **Step 1: 写失败验证**
用现有 TSX 转译加载方式导入 `SakuraEffect.tsx`，断言 `createPetalMotion` 存在；随后断言深度 0/1 的尺寸、透明度与速度边界，以及 `random() = 0.5` 时横向速度和旋转速度为 0。回收验证要求同一节点重复重置且舞台子节点数保持 12。

- [ ] **Step 2: 运行验证并确认 RED**
预期先因 `createPetalMotion` 尚未导出而断言失败，而不是测试脚本语法或加载错误。

- [ ] **Step 3: 实现视觉与回收**
在 `SakuraEffect.tsx` 中实现参数函数，将默认数量改为 12；创建与回收共用参数重置逻辑，离屏仅重置原节点。SVG 改为柔和粉色花瓣与小高光，按深度设置尺寸、透明度和慢速运动。CSS 保留 `will-change: transform`，移除固定高透明度并增加 `contain: layout style`，不增加滤镜。

- [ ] **Step 4: 运行行为验证与构建**
运行 Step 1 的参数/回收验证、既有 60/30/120 FPS 时间步长验证和 `npm run build`；全部退出码为 0。

- [ ] **Step 5: 浏览器检查**
检查首页花瓣视觉、正文阅读、暗色主题、开关重开和窗口尺寸；测试后恢复用户原有主题与特效状态。

- [ ] **Step 6: 审查并提交**
运行 `git diff --check`、查看最终差异，提交设计、计划和两个实现文件，不推送。
