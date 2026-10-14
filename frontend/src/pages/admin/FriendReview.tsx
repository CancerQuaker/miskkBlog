import { useEffect, useState } from 'react';
import { Button, Card, Input, Modal, Popconfirm, Select, Space, Table, Tabs, Tag, message } from 'antd';
import { CheckOutlined, CloseOutlined, DeleteOutlined, EditOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { adminFriendApi, FriendApply, FriendLink } from '../../api/friend';
import { DefaultAvatar } from '../../components/Common';

const statusTag = (s: number) =>
  s === 0 ? <Tag color="gold">待审核</Tag> : s === 1 ? <Tag color="pink">已通过</Tag> : <Tag color="red">已驳回</Tag>;

export default function FriendReview() {
  const [applies, setApplies] = useState<FriendApply[]>([]);
  const [applyTotal, setApplyTotal] = useState(0);
  const [applyPage, setApplyPage] = useState(1);
  const [links, setLinks] = useState<FriendLink[]>([]);
  const [linkTotal, setLinkTotal] = useState(0);
  const [linkPage, setLinkPage] = useState(1);
  const [review, setReview] = useState<{ apply: FriendApply; approved: boolean } | null>(null);
  const [note, setNote] = useState('');
  const [editLink, setEditLink] = useState<FriendLink | null>(null);
  const [editForm, setEditForm] = useState<Partial<FriendLink>>({});

  const loadApplies = () =>
    adminFriendApi.applies({ page: applyPage, size: 10 }).then((d) => {
      setApplies(d.records || []);
      setApplyTotal(d.total);
    });
  const loadLinks = () =>
    adminFriendApi.links({ page: linkPage, size: 10 }).then((d) => {
      setLinks(d.records || []);
      setLinkTotal(d.total);
    });

  useEffect(() => { loadApplies(); }, [applyPage]);
  useEffect(() => { loadLinks(); }, [linkPage]);

  const doReview = async () => {
    if (!review) return;
    await adminFriendApi.review(review.apply.id, review.approved, note || undefined);
    message.success(review.approved ? '已通过，友链已展示捏~' : '已驳回');
    setReview(null);
    setNote('');
    loadApplies();
  };

  const saveLink = async () => {
    if (!editLink) return;
    await adminFriendApi.updateLink(editLink.id, editForm);
    message.success('已保存');
    setEditLink(null);
    loadLinks();
  };

  const removeLink = async (id: number) => {
    await adminFriendApi.removeLink(id);
    message.success('已删除');
    loadLinks();
  };

  return (
    <Card title="友链审核">
      <Tabs
        items={[
          {
            key: 'applies',
            label: '收到的申请',
            children: (
              <Table
                rowKey="id"
                dataSource={applies}
                pagination={{ current: applyPage, total: applyTotal, pageSize: 10, onChange: setApplyPage }}
                scroll={{ x: 900 }}
                columns={[
                  {
                    title: '站点',
                    dataIndex: 'name',
                    render: (v, row) => (
                      <Space>
                        <DefaultAvatar user={{ nickname: v, avatar: row.avatar }} size={34} />
                        <div>
                          <div style={{ fontWeight: 600 }}>{v}</div>
                          <a href={row.url} target="_blank" rel="noreferrer" style={{ color: 'var(--primary)', fontSize: 12 }}>{row.url}</a>
                        </div>
                      </Space>
                    ),
                  },
                  { title: '简介', dataIndex: 'description', ellipsis: true },
                  { title: '联系方式', dataIndex: 'contact', width: 130 },
                  { title: '状态', dataIndex: 'status', width: 90, render: statusTag },
                  { title: '申请时间', dataIndex: 'createdAt', width: 110, render: (t) => dayjs(t).format('MM-DD HH:mm') },
                  {
                    title: '操作',
                    width: 160,
                    render: (_, row) =>
                      row.status === 0 ? (
                        <Space>
                          <Button size="small" type="primary" icon={<CheckOutlined />}
                            onClick={() => { setReview({ apply: row, approved: true }); }}>通过</Button>
                          <Button size="small" danger icon={<CloseOutlined />}
                            onClick={() => { setReview({ apply: row, approved: false }); }}>驳回</Button>
                        </Space>
                      ) : (
                        <span style={{ color: 'var(--text-faint)', fontSize: 12 }}>{row.reviewNote || '已处理'}</span>
                      ),
                  },
                ]}
              />
            ),
          },
          {
            key: 'links',
            label: '全部友链',
            children: (
              <Table
                rowKey="id"
                dataSource={links}
                pagination={{ current: linkPage, total: linkTotal, pageSize: 10, onChange: setLinkPage }}
                scroll={{ x: 800 }}
                columns={[
                  {
                    title: '站点',
                    dataIndex: 'name',
                    render: (v, row) => (
                      <Space>
                        <DefaultAvatar user={{ nickname: v, avatar: row.avatar }} size={32} />
                        <a href={row.url} target="_blank" rel="noreferrer">{v}</a>
                      </Space>
                    ),
                  },
                  { title: '状态', dataIndex: 'status', width: 100, render: statusTag },
                  { title: '排序', dataIndex: 'sort', width: 70 },
                  { title: '简介', dataIndex: 'description', ellipsis: true },
                  {
                    title: '操作',
                    width: 150,
                    render: (_, row) => (
                      <Space>
                        <Button size="small" icon={<EditOutlined />} onClick={() => { setEditLink(row); setEditForm({ name: row.name, url: row.url, avatar: row.avatar || '', description: row.description || '', sort: row.sort, status: row.status }); }} />
                        <Popconfirm title="确定删除该友链吗？" onConfirm={() => removeLink(row.id)} okText="删除" cancelText="取消">
                          <Button size="small" danger icon={<DeleteOutlined />} />
                        </Popconfirm>
                      </Space>
                    ),
                  },
                ]}
              />
            ),
          },
        ]}
      />

      {/* 审核弹窗 */}
      <Modal
        open={!!review}
        title={review?.approved ? '通过友链申请' : '驳回友链申请'}
        okText="确认"
        cancelText="取消"
        onOk={doReview}
        onCancel={() => setReview(null)}
      >
        <p>站点：<b>{review?.apply.name}</b>（{review?.apply.url}）</p>
        <Input.TextArea
          rows={3}
          maxLength={200}
          placeholder={review?.approved ? '审核备注（选填）' : '驳回原因（选填）'}
          value={note}
          onChange={(e) => setNote(e.target.value)}
        />
      </Modal>

      {/* 编辑友链 */}
      <Modal
        open={!!editLink}
        title="编辑友链"
        okText="保存"
        cancelText="取消"
        onOk={saveLink}
        onCancel={() => setEditLink(null)}
      >
        <Space direction="vertical" style={{ width: '100%' }} size={10}>
          <Input placeholder="名称" value={editForm.name ?? ''} onChange={(e) => setEditForm({ ...editForm, name: e.target.value })} />
          <Input placeholder="链接" value={editForm.url ?? ''} onChange={(e) => setEditForm({ ...editForm, url: e.target.value })} />
          <Input placeholder="头像" value={editForm.avatar ?? ''} onChange={(e) => setEditForm({ ...editForm, avatar: e.target.value })} />
          <Input placeholder="简介" value={editForm.description ?? ''} onChange={(e) => setEditForm({ ...editForm, description: e.target.value })} />
          <Space>
            <Input type="number" placeholder="排序" style={{ width: 100 }} value={editForm.sort} onChange={(e) => setEditForm({ ...editForm, sort: Number(e.target.value) })} />
            <Select style={{ width: 110 }} value={editForm.status} onChange={(v) => setEditForm({ ...editForm, status: v })}
              options={[{ value: 1, label: '已通过' }, { value: 0, label: '待审核' }, { value: 2, label: '已驳回' }]} />
          </Space>
        </Space>
      </Modal>
    </Card>
  );
}
