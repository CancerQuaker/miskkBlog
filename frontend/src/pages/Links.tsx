import { useEffect, useState } from 'react';
import { Button, Empty, Form, Input, Modal, Spin, message } from 'antd';
import { PlusOutlined, LinkOutlined } from '@ant-design/icons';
import { friendApi, FriendLink } from '../api/friend';
import { DefaultAvatar } from '../components/Common';

/**
 * 友链页：小伙伴列表 + 申请按钮 + 申请注意事项。
 */
export default function Links() {
  const [links, setLinks] = useState<FriendLink[]>([]);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [notes, setNotes] = useState<Record<string, any>>({});
  const [form] = Form.useForm();

  const load = () => {
    setLoading(true);
    friendApi.links().then(setLinks).finally(() => setLoading(false));
  };
  useEffect(load, []);

  const openApply = async () => {
    try {
      const n = await friendApi.notes();
      setNotes(n);
    } catch {
      setNotes({});
    }
    setOpen(true);
  };

  const submit = async () => {
    const values = await form.validateFields();
    await friendApi.apply(values);
    message.success('申请已提交，等管理员审核通过就会展示在这里捏~');
    setOpen(false);
    form.resetFields();
  };

  return (
    <div className="page-container">
      <div className="card fade-in" style={{ padding: '26px 30px', marginBottom: 20 }}>
        <h1 style={{ margin: 0, fontSize: 24 }}>🤝 友链</h1>
        <p style={{ color: 'var(--text-sub)', margin: '8px 0 0' }}>
          这里是迷失客客的小伙伴们，点击头像即可访问他们的站点。
        </p>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: 80 }}><Spin /></div>
      ) : links.length === 0 ? (
        <Empty description="还没有小伙伴，快来申请第一个友链叭~" />
      ) : (
        <div className="article-grid stagger">
          {links.map((l) => (
            <a
              key={l.id}
              href={l.url}
              target="_blank"
              rel="noreferrer"
              className="card card-hover friend-card"
            >
              <DefaultAvatar user={{ nickname: l.name, avatar: l.avatar }} size={56} />
              <div className="info">
                <div className="name">{l.name}</div>
                <div className="desc">{l.description || l.url}</div>
              </div>
              <LinkOutlined style={{ color: 'var(--text-faint)' }} />
            </a>
          ))}
        </div>
      )}

      <div style={{ textAlign: 'center', marginTop: 28 }}>
        <Button type="primary" size="large" shape="round" icon={<PlusOutlined />} onClick={openApply}>
          申请友链
        </Button>
      </div>

      <Modal
        open={open}
        title="申请友链"
        okText="提交申请"
        cancelText="取消"
        onOk={submit}
        onCancel={() => setOpen(false)}
        width={560}
      >
        <div style={{ background: 'var(--primary-weak)', borderRadius: 10, padding: '12px 16px', marginBottom: 16, fontSize: 13.5, lineHeight: 2 }}>
          <b>申请须知</b>
          <div>需要填写：{(notes.required || ['站点名称', '站点链接', '联系方式']).join('、')}</div>
          <div>选填：{(notes.optional || ['头像', '简介', '说明']).join('、')}</div>
          {(notes.rules || []).map((r: string, i: number) => <div key={i}>· {r}</div>)}
        </div>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="站点名称" rules={[{ required: true, message: '请输入站点名称' }]}>
            <Input placeholder="我的小站" maxLength={32} />
          </Form.Item>
          <Form.Item name="url" label="站点链接" rules={[{ required: true, message: '请输入站点链接' }]}>
            <Input placeholder="https://example.com" />
          </Form.Item>
          <Form.Item name="avatar" label="头像链接（选填）">
            <Input placeholder="https://example.com/avatar.png" />
          </Form.Item>
          <Form.Item name="description" label="站点简介（选填）">
            <Input placeholder="一句话介绍你的站点" maxLength={100} />
          </Form.Item>
          <Form.Item name="contact" label="联系方式" rules={[{ required: true, message: '留个 QQ 或邮箱方便通知审核结果捏' }]}>
            <Input placeholder="QQ / 邮箱" maxLength={64} />
          </Form.Item>
          <Form.Item name="reason" label="申请说明（选填）">
            <Input.TextArea rows={2} maxLength={200} placeholder="想对站长说的话~" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}
