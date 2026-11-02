import { forwardRef, useEffect, useImperativeHandle, useRef } from 'react';
import { message } from 'antd';
import Vditor from 'vditor';
import 'vditor/dist/index.css';
import '../styles/markdown-editor.css';

interface MarkdownEditorProps {
  initialValue: string;
  onChange: (value: string) => void;
  onUpload: (file: File) => Promise<string>;
  cacheKey: string;
}

export interface MarkdownEditorHandle {
  getValue: () => string;
  clearCache: () => void;
  focus: () => void;
}

function imageAlt(name: string) {
  return name.replace(/\.[^.]+$/, '').replace(/[\[\]\r\n]/g, '').trim() || '图片';
}

const MarkdownEditor = forwardRef<MarkdownEditorHandle, MarkdownEditorProps>(function MarkdownEditor(
  { initialValue, onChange, onUpload, cacheKey },
  ref,
) {
  const containerRef = useRef<HTMLDivElement>(null);
  const editorRef = useRef<Vditor | null>(null);
  const initialValueRef = useRef(initialValue);
  const onChangeRef = useRef(onChange);
  const onUploadRef = useRef(onUpload);
  initialValueRef.current = initialValue;
  onChangeRef.current = onChange;
  onUploadRef.current = onUpload;

  useImperativeHandle(ref, () => ({
    getValue: () => editorRef.current?.getValue() ?? initialValueRef.current,
    clearCache: () => editorRef.current?.clearCache(),
    focus: () => editorRef.current?.focus(),
  }), []);

  useEffect(() => {
    const container = containerRef.current;
    if (!container) return;

    let active = true;
    let ready = false;
    const applyTheme = () => {
      if (!active || !ready || !editorRef.current) return;
      const dark = document.documentElement.dataset.theme === 'dark';
      editorRef.current.setTheme(dark ? 'dark' : 'classic', dark ? 'dark' : 'light', dark ? 'github-dark' : 'github');
    };
    const observer = new MutationObserver(applyTheme);
    observer.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] });
    const mount = document.createElement('div');
    container.replaceChildren(mount);

    const editor = new Vditor(mount, {
      value: initialValueRef.current,
      mode: 'ir',
      lang: 'zh_CN',
      cdn: '/vditor',
      icon: 'ant',
      height: 620,
      minHeight: 480,
      placeholder: '开始写作吧，支持 Markdown、粘贴图片和拖拽上传…',
      tab: '  ',
      toolbar: [
        'headings', 'bold', 'italic', 'strike', 'quote', '|',
        'list', 'ordered-list', 'check', 'code', 'inline-code', 'link', 'table', 'upload', '|',
        'undo', 'redo', '|', 'edit-mode', 'both', 'preview', 'fullscreen', 'outline',
      ],
      toolbarConfig: { pin: true },
      counter: { enable: true, type: 'text' },
      resize: { enable: true, position: 'bottom' },
      cache: { enable: true, id: cacheKey },
      fullscreen: { index: 1200 },
      outline: { enable: false, position: 'right' },
      preview: {
        delay: 300,
        markdown: {
          inlineMath: false,
          mathBlockPreview: false,
          footnotes: false,
          toc: false,
          autoSpace: false,
          fixTermTypo: false,
        },
      },
      upload: {
        accept: 'image/*',
        multiple: true,
        handler: async (files) => {
          try {
            for (const file of files) {
              const url = await onUploadRef.current(file);
              if (!active) return '编辑器已关闭，上传已中止';
              editor.insertValue(`![${imageAlt(file.name)}](${url})\n`);
            }
            return '';
          } catch (error) {
            const reason = error instanceof Error ? error.message : '未知错误';
            const text = `图片上传失败：${reason}`;
            message.error(text);
            return text;
          }
        },
      },
      after: () => {
        ready = true;
        if (!active) {
          destroyEditor();
          return;
        }
        editorRef.current = editor;
        applyTheme();
        onChangeRef.current(editor.getValue());
      },
      input: (value) => {
        if (active) onChangeRef.current(value);
      },
    });

    const destroyEditor = () => {
      editor.disabledCache();
      editor.destroy();
      if (editorRef.current === editor) editorRef.current = null;
    };

    return () => {
      active = false;
      observer.disconnect();
      if (ready) destroyEditor();
      ready = false;
    };
  }, [cacheKey]);

  return <div ref={containerRef} className="markdown-editor" />;
});

export default MarkdownEditor;
