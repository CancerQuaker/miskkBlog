import { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import {
  Button, Card, Col, Form, Input, Row, Select, Space, Spin, Switch, Upload, message,
} from 'antd';
import { PlusOutlined, PictureOutlined, SendOutlined, SaveOutlined } from '@ant-design/icons';
import MDEditor from '@uiw/react-md-editor';
import { articleApi, CategoryVO, adminArticleApi, ArticleDetail } from '../../api/article';

/**
 * 文章编辑页（新增 / 编辑），Markdown 编辑器 + 封面上传 + 标签。
 */
export default function ArticleEdit() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const id = params.get('id');
  const [form] = Form.useForm();
  const [content, setContent] = useState('');
  const [categories, setCategories] = useState<CategoryVO[]>([]);
  const [tagOptions, setTagOptions] = useState<{ label: string; value: string }[]>([]);
  const [cover, setCover] = useState('');
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(!!id);

  useEffect(() => {
    articleApi.categories().then(setCategories).catch(() => {});
    articleApi.tags().then((tags) => setTagOptions(tags.map((t) => ({ label: t.name, value: t.name })))).catch(() => {});
    if (id) {
      articleApi.detail(id)
        .then((d: ArticleDetail) => {
          form.setFieldsValue({
            title: d.title,
            summary: d.summary,
            categoryId: d.categoryId,
            tagNames: d.tags?.map((t) => t.name),
            pinned: d.pinned,
          });
          setContent(d.content);
          setCover(d.cover || '');
        })
        .finally(() => setLoading(false));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id]);

  const uploadCover = async (file: File) => {
    const url = await articleApi.uploadImage(file);
    setCover(url);
    message.success('封面上传成功');
  };

  const save = async (status: number) => {
    const values = await form.validateFields();
    setSaving(true);
    try {
      const payload = {
        ...values,
        content,
        cover: cover || undefined,
        status,
        pinned: !!values.pinned,
      };
      if (id) {
        await adminArticleApi.update(Number(id), payload);
        message.success('保存成功捏~');
      } else {
        await adminArticleApi.create(payload);
        message.success(status === 1 ? '发布成功捏~' : '已保存为草稿');
      }
      navigate('/admin/article');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: 100 }}><Spin size="large" /></div>;

  return (
    <Card title={id ? '编辑文章' : '写文章'}>
      <Form form={form} layout="vertical">
        <Form.Item name="title" label="标题" rules={[{ required: true, message: '标题不能为空' }]}>
          <Input placeholder="给文章起个可爱的名字捏~" maxLength={100} size="large" />
        </Form.Item>
        <Row gutter={16}>
          <Col xs={24} md={8}>
            <Form.Item name="categoryId" label="分类" rules={[{ required: true, message: '请选择分类' }]}>
              <Select placeholder="选择分类" options={categories.map((c) => ({ value: c.id, label: c.name }))} />
            </Form.Item>
          </Col>
          <Col xs={24} md={10}>
            <Form.Item name="tagNames" label="标签（可输入新建）">
              <Select mode="tags" placeholder="输入或选择标签" options={tagOptions} maxTagCount={4} />
            </Form.Item>
          </Col>
          <Col xs={24} md={6}>
            <Form.Item name="pinned" label="置顶" valuePropName="checked">
              <Switch />
            </Form.Item>
          </Col>
        </Row>
        <Form.Item name="summary" label="摘要（留空则自动截取正文）">
          <Input.TextArea rows={2} maxLength={200} showCount placeholder="一句话摘要" />
        </Form.Item>
        <Form.Item label="封面">
          {cover ? (
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <img src={cover} alt="封面" style={{ width: 180, borderRadius: 10, objectFit: 'cover' }} />
              <Button onClick={() => setCover('')}>移除</Button>
            </div>
          ) : (
            <Upload
              accept="image/*"
              showUploadList={false}
              beforeUpload={(file) => {
                uploadCover(file).catch((e) => message.error(e.message));
                return false;
              }}
            >
              <Button icon={<PictureOutlined />}>上传封面</Button>
            </Upload>
          )}
        </Form.Item>
      </Form>

      <div style={{ marginBottom: 8, color: 'var(--text-sub)' }}>正文（Markdown）</div>
      <div data-color-mode="light">
        <MDEditor
          value={content}
          onChange={(v) => setContent(v || '')}
          height={520}
          preview="edit"
          extraCommands={[]}
        />
      </div>

      <div style={{ marginTop: 16, textAlign: 'right' }}>
        <Space>
          <Button icon={<SaveOutlined />} onClick={() => save(0)} disabled={saving}>存草稿</Button>
          <Button type="primary" icon={<SendOutlined />} onClick={() => save(1)} loading={saving}>发布</Button>
        </Space>
      </div>
    </Card>
  );
}
