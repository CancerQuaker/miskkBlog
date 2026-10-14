import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Spin, Empty, Input, Pagination } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import { articleApi, ArticleCard as CardType } from '../api/article';
import ArticleCard from '../components/Common';

export default function Search() {
  const [params, setParams] = useSearchParams();
  const keyword = params.get('q') || '';
  const [input, setInput] = useState(keyword);
  const [articles, setArticles] = useState<CardType[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setInput(keyword);
    if (!keyword) {
      setArticles([]);
      setTotal(0);
      return;
    }
    setLoading(true);
    articleApi.search({ keyword, page, size: 9 })
      .then((d) => {
        setArticles(d.records || []);
        setTotal(d.total);
      })
      .finally(() => setLoading(false));
  }, [keyword, page]);

  return (
    <div className="page-container" style={{ maxWidth: 960 }}>
      <div className="card fade-in" style={{ padding: '26px 30px', marginBottom: 20 }}>
        <h1 style={{ margin: '0 0 14px', fontSize: 24 }}>🔍 搜索</h1>
        <Input.Search
          size="large"
          enterButton
          prefix={<SearchOutlined />}
          placeholder="输入关键词，搜索文章捏~"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onSearch={(v) => {
            setPage(1);
            setParams(v ? { q: v } : {});
          }}
        />
        {keyword && (
          <p style={{ margin: '12px 0 0', color: 'var(--text-sub)' }}>
            关键词「<b style={{ color: 'var(--primary)' }}>{keyword}</b>」共找到 {total} 篇文章
          </p>
        )}
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: 80 }}><Spin /></div>
      ) : keyword && articles.length === 0 ? (
        <Empty description={`没有找到与「${keyword}」相关的文章捏~`} />
      ) : (
        <div className="article-grid stagger">
          {articles.map((a) => <ArticleCard key={a.id} article={a} />)}
        </div>
      )}
      <div style={{ textAlign: 'center', marginTop: 24 }}>
        <Pagination current={page} total={total} pageSize={9} onChange={setPage} hideOnSinglePage />
      </div>
    </div>
  );
}
