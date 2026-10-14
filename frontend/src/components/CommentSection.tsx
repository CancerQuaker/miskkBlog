import { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Input, Pagination, Tooltip, message } from 'antd';
import { LikeFilled, LikeOutlined, MessageOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { articleApi, CommentItem } from '../api/article';
import { useAuthStore } from '../stores/authStore';
import { DefaultAvatar } from './Common';

/**
 * 评论区：游客可读；登录后可发布/回复/删除自己的评论。
 */
export default function CommentSection({ articleId }: { articleId: number }) {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [comments, setComments] = useState<CommentItem[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [content, setContent] = useState('');
  const [replyTo, setReplyTo] = useState<{ comment: CommentItem; isChild: boolean } | null>(null);
  const inputRef = useRef<any>(null);

  const load = async (p = page) => {
    setLoading(true);
    try {
      const data = await articleApi.comments(articleId, p, 10);
      setComments(data.records || []);
      setTotal(data.total);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load(page);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [articleId, page]);

  const submit = async () => {
    if (!user) {
      message.info('登录后才能评论捏~');
      navigate('/login?redirect=' + encodeURIComponent(location.pathname));
      return;
    }
    if (!content.trim()) return;
    await articleApi.createComment({
      articleId,
      content: content.trim(),
      parentId: replyTo ? (replyTo.isChild ? (replyTo.comment.parentId || replyTo.comment.id) : replyTo.comment.id) : undefined,
      replyUserId: replyTo ? replyTo.comment.user.id : undefined,
    });
    setContent('');
    setReplyTo(null);
    message.success('评论成功捏~');
    load(1);
    setPage(1);
  };

  const del = async (c: CommentItem) => {
    await articleApi.deleteComment(c.id);
    message.success('已删除');
    load(page);
  };

  const startReply = (c: CommentItem, isChild: boolean) => {
    if (!user) {
      message.info('登录后才能回复捏~');
      navigate('/login?redirect=' + encodeURIComponent(location.pathname));
      return;
    }
    setReplyTo({ comment: c, isChild });
    setTimeout(() => inputRef.current?.focus(), 100);
  };

  const renderComment = (c: CommentItem, isChild = false) => (
    <div key={c.id} className={isChild ? '' : 'comment-item'} style={isChild ? { display: 'flex', gap: 10, padding: '10px 0' } : undefined}>
      <div className="comment-avatar">
        <DefaultAvatar user={c.user} size={isChild ? 30 : 38} />
      </div>
      <div className="comment-content">
        <div>
          <span className="who">{c.user.nickname}</span>
          {c.replyUser && <span style={{ color: 'var(--text-faint)', fontSize: 12.5 }}> 回复 @{c.replyUser.nickname}</span>}
          <span className="when">{dayjs(c.createdAt).format('YYYY-MM-DD HH:mm')}</span>
        </div>
        <div className="text">{c.content}</div>
        <div className="actions">
          <span onClick={() => startReply(c, isChild)}><MessageOutlined /> 回复</span>
          {user && (user.id === c.user.id || user.role === 1) && (
            <span onClick={() => del(c)} style={{ color: '#ff7875' }}>删除</span>
          )}
        </div>
        {c.children?.length > 0 && (
          <div style={{ paddingLeft: isChild ? 0 : 4, marginTop: 4 }}>
            {c.children.map((child) => renderComment(child, true))}
          </div>
        )}
      </div>
    </div>
  );

  return (
    <div className="card" style={{ padding: '18px 22px', marginTop: 20 }}>
      <h3 style={{ marginTop: 0, display: 'flex', alignItems: 'center', gap: 8 }}>
        <MessageOutlined style={{ color: 'var(--primary)' }} /> 评论 {total > 0 && <span style={{ color: 'var(--text-faint)', fontSize: 14 }}>({total})</span>}
      </h3>

      {replyTo && (
        <div style={{ background: 'var(--primary-weak)', borderRadius: 8, padding: '6px 12px', marginBottom: 8, fontSize: 13 }}>
          回复 <b>@{replyTo.comment.user.nickname}</b>：
          <a style={{ marginLeft: 8, color: 'var(--primary)', cursor: 'pointer' }} onClick={() => setReplyTo(null)}>取消</a>
        </div>
      )}

      <Input.TextArea
        ref={inputRef}
        rows={3}
        maxLength={1000}
        placeholder={user ? '说点什么吧捏~ (Ctrl+Enter 发送)' : '登录后即可评论捏~'}
        value={content}
        onChange={(e) => setContent(e.target.value)}
        onPressEnter={(e) => {
          if (e.ctrlKey || e.metaKey) submit();
        }}
      />
      <div style={{ marginTop: 10, textAlign: 'right' }}>
        <Button type="primary" onClick={submit} disabled={!content.trim()}>
          发表评论
        </Button>
      </div>

      <div style={{ marginTop: 12 }}>
        {loading ? <p style={{ color: 'var(--text-faint)' }}>加载中…</p>
          : comments.length === 0 ? <p style={{ color: 'var(--text-faint)' }}>还没有评论，来抢沙发捏~</p>
          : comments.map((c) => renderComment(c))}
      </div>

      {total > 10 && (
        <div style={{ textAlign: 'center', marginTop: 12 }}>
          <Pagination current={page} total={total} pageSize={10} onChange={setPage} size="small" />
        </div>
      )}
    </div>
  );
}

/** 点赞按钮（含动画） */
export function LikeButton({ articleId, liked, count, onChange }: {
  articleId: number; liked: boolean; count: number; onChange: (liked: boolean, count: number) => void;
}) {
  const { user } = useAuthStore();
  const navigate = useNavigate();
  const [bump, setBump] = useState(false);

  const toggle = async () => {
    if (!user) {
      message.info('登录后才能点赞捏~');
      navigate('/login?redirect=' + encodeURIComponent(location.pathname));
      return;
    }
    if (liked) {
      await articleApi.unlike(articleId);
      onChange(false, count - 1);
    } else {
      await articleApi.like(articleId);
      onChange(true, count + 1);
      setBump(true);
      setTimeout(() => setBump(false), 500);
    }
  };

  return (
    <Tooltip title={liked ? '取消点赞' : '点个赞再走捏~'}>
      <Button
        size="large"
        shape="round"
        onClick={toggle}
        icon={liked ? <LikeFilled style={{ color: 'var(--primary)' }} /> : <LikeOutlined />}
        style={{
          borderColor: liked ? 'var(--primary)' : undefined,
          color: liked ? 'var(--primary)' : undefined,
          transform: bump ? 'scale(1.25)' : 'scale(1)',
          transition: 'transform .3s cubic-bezier(.34,1.56,.64,1)',
        }}
      >
        {count}
      </Button>
    </Tooltip>
  );
}
