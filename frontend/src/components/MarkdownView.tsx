import { useMemo, ReactNode } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import rehypeHighlight from 'rehype-highlight';

export interface TocItem {
  level: number;
  text: string;
  id: string;
}

const slug = (text: string) =>
  text.trim().toLowerCase().replace(/[^\p{L}\p{N}\s-]/gu, '').replace(/\s+/g, '-');

/** 从 markdown 提取标题目录 */
export function extractToc(markdown: string): TocItem[] {
  const items: TocItem[] = [];
  const lines = markdown.split('\n');
  let inCode = false;
  for (const line of lines) {
    if (line.trim().startsWith('```')) {
      inCode = !inCode;
      continue;
    }
    if (inCode) continue;
    const m = /^(#{1,4})\s+(.*)$/.exec(line);
    if (m) {
      const text = m[2].replace(/[*`~]/g, '').trim();
      items.push({ level: m[1].length, text, id: slug(text) });
    }
  }
  return items;
}

/**
 * Markdown 渲染（GFM + 代码高亮），标题带锚点 id 供目录跳转。
 */
export default function MarkdownView({ content }: { content: string }) {
  const heading = (level: number) => (props: { children?: ReactNode }) => {
    const text = extractText(props.children);
    const Tag = `h${level}` as any;
    return <Tag id={slug(text)}>{props.children}</Tag>;
  };

  return (
    <div className="markdown-body">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        rehypePlugins={[[rehypeHighlight, { detect: true, ignoreMissing: true }]]}
        components={{
          h1: heading(1),
          h2: heading(2),
          h3: heading(3),
          h4: heading(4),
          a: (props) => <a {...props} target="_blank" rel="noreferrer" />,
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  );
}

function extractText(node: any): string {
  if (typeof node === 'string') return node;
  if (Array.isArray(node)) return node.map(extractText).join('');
  if (node?.props?.children) return extractText(node.props.children);
  return '';
}
