import { useEffect, useRef, useState, useCallback } from 'react';

interface MenuState {
  x: number;
  y: number;
  visible: boolean;
  canCopy: boolean;
}

/**
 * 全站自定义右键菜单（替代浏览器默认菜单）：
 * - 返回：回到上一页
 * - 复制：复制当前选中的文字（无选中时置灰）
 * - 另存：把当前页面保存为 .html 文件
 * - 检查：高亮闪烁显示鼠标所指的元素（附带标签信息提示）
 * 关闭方式：左键点击任意处 / 滚动 / Esc / 再次右键会重新定位打开。
 */
export default function ContextMenu() {
  const [menu, setMenu] = useState<MenuState>({ x: 0, y: 0, visible: false, canCopy: false });
  const [toast, setToast] = useState<string | null>(null);
  const menuRef = useRef<HTMLDivElement>(null);
  const targetRef = useRef<Element | null>(null);
  const toastTimer = useRef<ReturnType<typeof setTimeout>>();

  const close = useCallback(() => setMenu((m) => ({ ...m, visible: false })), []);

  const showToast = useCallback((text: string, ms = 2200) => {
    setToast(text);
    clearTimeout(toastTimer.current);
    toastTimer.current = setTimeout(() => setToast(null), ms);
  }, []);

  useEffect(() => {
    const MENU_W = 200;
    const MENU_H = 220;

    const onContext = (e: MouseEvent) => {
      e.preventDefault();
      targetRef.current = e.target as Element;
      const sel = window.getSelection()?.toString().trim();
      let x = e.clientX;
      let y = e.clientY;
      if (x + MENU_W > window.innerWidth) x = window.innerWidth - MENU_W - 8;
      if (y + MENU_H > window.innerHeight) y = window.innerHeight - MENU_H - 8;
      setMenu({ x, y, visible: true, canCopy: !!sel });
    };

    const onDown = (e: MouseEvent) => {
      if (menuRef.current && menuRef.current.contains(e.target as Node)) return;
      close();
    };
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') close();
    };

    window.addEventListener('contextmenu', onContext);
    window.addEventListener('mousedown', onDown);
    window.addEventListener('scroll', close, true);
    window.addEventListener('resize', close);
    window.addEventListener('keydown', onKey);
    return () => {
      window.removeEventListener('contextmenu', onContext);
      window.removeEventListener('mousedown', onDown);
      window.removeEventListener('scroll', close, true);
      window.removeEventListener('resize', close);
      window.removeEventListener('keydown', onKey);
      clearTimeout(toastTimer.current);
    };
  }, [close]);

  /** 返回上一页 */
  const goBack = () => {
    close();
    window.history.back();
  };

  /** 复制选中文字 */
  const copySelection = async () => {
    close();
    const sel = window.getSelection()?.toString() || '';
    if (!sel.trim()) return;
    try {
      await navigator.clipboard.writeText(sel);
    } catch {
      const ta = document.createElement('textarea');
      ta.value = sel;
      document.body.appendChild(ta);
      ta.select();
      document.execCommand('copy');
      ta.remove();
    }
    showToast('📋 复制成功捏~');
  };

  /** 另存当前页面为 html 文件 */
  const savePage = () => {
    close();
    const html = '<!doctype html>\n' + document.documentElement.outerHTML;
    const blob = new Blob([html], { type: 'text/html;charset=utf-8' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = `${document.title || 'page'}.html`;
    a.click();
    setTimeout(() => URL.revokeObjectURL(a.href), 4000);
    showToast('💾 页面已保存~');
  };

  /** 检查：高亮鼠标所指元素并展示它的标签信息 */
  const inspect = () => {
    close();
    const el = targetRef.current as HTMLElement | null;
    if (!el || !el.tagName) return;
    const info = `<${el.tagName.toLowerCase()}${el.id ? `#${el.id}` : ''}${
      el.className && typeof el.className === 'string' ? `.${el.className.trim().split(/\s+/).join('.')}` : ''
    }>`;
    // 高亮闪烁
    const prevOutline = el.style.outline;
    const prevTransition = el.style.transition;
    el.style.transition = 'outline-color .18s ease';
    let count = 0;
    const blink = setInterval(() => {
      el.style.outline = count % 2 === 0 ? '3px solid var(--primary)' : '3px solid transparent';
      count += 1;
      if (count > 5) {
        clearInterval(blink);
        el.style.outline = prevOutline;
        el.style.transition = prevTransition;
      }
    }, 180);
    showToast(`🔍 ${info}（按 F12 打开开发者工具查看更多）`, 3200);
  };

  const items = [
    { key: 'back', icon: '↩', label: '返回', hint: 'Back', onClick: goBack },
    { key: 'copy', icon: '📋', label: '复制', hint: 'Ctrl+C', onClick: copySelection, disabled: !menu.canCopy },
    { key: 'save', icon: '💾', label: '另存', hint: 'Ctrl+S', onClick: savePage },
    { key: 'inspect', icon: '🔍', label: '检查', hint: 'F12', onClick: inspect },
  ];

  return (
    <>
      {menu.visible && (
        <div
          ref={menuRef}
          className="ctx-menu"
          style={{ left: menu.x, top: menu.y }}
          onContextMenu={(e) => e.preventDefault()}
        >
          <div className="ctx-menu-title">
            <span className="ctx-menu-star">✦</span> 小窝菜单
          </div>
          {items.map((it) => (
            <button
              key={it.key}
              className={`ctx-menu-item ${it.disabled ? 'disabled' : ''}`}
              disabled={it.disabled}
              onClick={it.onClick}
            >
              <span className="ctx-menu-icon">{it.icon}</span>
              <span className="ctx-menu-label">{it.label}</span>
              <span className="ctx-menu-hint">{it.hint}</span>
            </button>
          ))}
        </div>
      )}
      {toast && <div className="ctx-toast">{toast}</div>}
    </>
  );
}
