import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button, Card, Input, Modal, Popconfirm, Select, Space, Table, Tag, message } from 'antd';
import { PlusOutlined, EyeOutlined, EditOutlined, DeleteOutlined, SendOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { ArticleCard, CategoryVO, adminArticleApi } from '../../api/article';
import { articleApi } from '../../api/article';

const statusMap: Record<number, { text: string; color: string }> = {
  0: { text: '草稿', color: 'default' },
  1: { text: '已发布', color: 'pink' },
  2: { text: '已隐藏', color: 'orange' },
};

export default function ArticleManage() {
  const navigate = useNavigate();
  const [list, setList] = useState<ArticleCard[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(1);
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState<number | undefined>();
  const [categories, setCategories] = useState<CategoryVO[]>([]);
  const [loading, setLoading] = useState(false);

  const load = () => {
    setLoading(true);
    adminArticleApi.page({ page, size: 10, keyword: keyword || undefined, status })
      .then((d) => {
        setList(d.records || []);
        setTotal(d.total);
      })
      .finally(() => setLoading(false));
  };

  useEffect(load, [page, keyword, status]);
  useEffect(() => { articleApi.categories().then(setCategories).catch(() => {}); }, []);

  const toggleStatus = async (row: ArticleCard, next: number) => {
    await adminArticleApi.updateStatus(row.id, next);
    message.success(next === 1 ? '已发布捏~' : next === 2 ? '已隐藏' : '已转为草稿');
    load();
  };

  const remove = async (row: ArticleCard) => {
    await adminArticleApi.remove(row.id);
    message.success('已删除');
    load();
  };

  return (
    <Card
      title="文章管理"
      extra={
        <Space>
          <Input.Search
            placeholder="搜索标题/摘要"
            allowClear
            onSearch={(v) => { setPage(1); setKeyword(v); }}
            style={{ width: 200 }}
          />
          <Select
            placeholder="状态"
            allowClear
            style={{ width: 110 }}
            value={status}
            onChange={(v) => { setPage(1); setStatus(v); }}
            options={[
              { value: 0, label: '草稿' },
              { value: 1, label: '已发布' },
              { value: 2, label: '已隐藏' },
            ]}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/admin/article/edit')}>
            写文章
          </Button>
        </Space>
      }
    >
      <Table
        rowKey="id"
        loading={loading}
        dataSource={list}
        pagination={{ current: page, total, pageSize: 10, onChange: setPage }}
        scroll={{ x: 900 }}
        columns={[
          {
            title: '标题',
            dataIndex: 'title',
            render: (v, row) => (
              <Link to={`/article/${row.id}`} style={{ color: 'var(--text-main)' }}>
                {row.pinned && <Tag color="pink">置顶</Tag>}
                {v}
              </Link>
            ),
          },
          { title: '分类', dataIndex: 'categoryName', width: 100 },
          {
            title: '状态',
            dataIndex: 'status',
            width: 90,
            render: (s) => <Tag color={statusMap[s]?.color}>{statusMap[s]?.text}</Tag>,
          },
          { title: '浏览', dataIndex: 'views', width: 70 },
          { title: '点赞', dataIndex: 'likes', width: 70 },
          { title: '评论', dataIndex: 'comments', width: 70 },
          {
            title: '发布时间',
            dataIndex: 'publishedAt',
            width: 120,
            render: (t) => t ? dayjs(t).format('YYYY-MM-DD') : '-',
          },
          {
            title: '操作',
            width: 230,
            render: (_, row) => (
              <Space size="small">
                {row.status !== 1 ? (
                  <Button size="small" type="primary" icon={<SendOutlined />} onClick={() => toggleStatus(row, 1)}>发布</Button>
                ) : (
                  <Button size="small" onClick={() => toggleStatus(row, 2)}>隐藏</Button>
                )}
                <Button size="small" icon={<EditOutlined />} onClick={() => navigate(`/admin/article/edit?id=${row.id}`)}>编辑</Button>
                <Popconfirm title="确定删除这篇文章吗？" onConfirm={() => remove(row)} okText="删除" cancelText="取消">
                  <Button size="small" danger icon={<DeleteOutlined />} />
                </Popconfirm>
              </Space>
            ),
          },
        ]}
      />
    </Card>
  );
}
