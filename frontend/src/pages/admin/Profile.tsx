import { useState } from 'react';
import { Button, Card, Col, Form, Input, message, Row, Upload } from 'antd';
import { UploadOutlined, LockOutlined } from '@ant-design/icons';
import { authApi } from '../../api/auth';
import { useAuthStore } from '../../stores/authStore';

/**
 * 个人资料：头像/昵称/简介/社交信息 + 修改密码。
 */
export default function Profile() {
  const { user, refreshMe } = useAuthStore();
  const [form] = Form.useForm();
  const [pwdForm] = Form.useForm();
  const [saving, setSaving] = useState(false);

  const save = async () => {
    const values = await form.validateFields();
    setSaving(true);
    try {
      await authApi.updateProfile(values);
      await refreshMe();
      message.success('资料已更新捏~');
    } finally {
      setSaving(false);
    }
  };

  const changePwd = async () => {
    const { oldPassword, newPassword } = await pwdForm.validateFields();
    await authApi.updatePassword(oldPassword, newPassword);
    pwdForm.resetFields();
    message.success('密码修改成功，下次登录请使用新密码');
  };

  if (!user) return null;

  return (
    <Row gutter={16}>
      <Col xs={24} lg={14}>
        <Card title="个人资料">
          <Form form={form} layout="vertical" initialValues={{
            nickname: user.nickname, bio: user.bio, email: user.email,
            qq: user.qq, wechat: user.wechat, github: user.github,
          }}>
            <Form.Item label="头像">
              <Upload
                accept="image/*"
                showUploadList={false}
                beforeUpload={async (file) => {
                  try {
                    const url = await authApi.uploadImage(file);
                    await authApi.updateProfile({ avatar: url });
                    await refreshMe();
                    message.success('头像已更新捏~');
                  } catch (e: any) {
                    message.error(e.message);
                  }
                  return false;
                }}
              >
                <Button icon={<UploadOutlined />}>上传新头像</Button>
              </Upload>
            </Form.Item>
            <Form.Item name="nickname" label="昵称" rules={[{ required: true, message: '昵称不能为空' }]}>
              <Input maxLength={16} />
            </Form.Item>
            <Form.Item name="bio" label="个性签名">
              <Input.TextArea rows={2} maxLength={255} />
            </Form.Item>
            <Form.Item name="email" label="邮箱">
              <Input />
            </Form.Item>
            <Form.Item name="qq" label="QQ">
              <Input maxLength={20} />
            </Form.Item>
            <Form.Item name="wechat" label="微信">
              <Input maxLength={64} />
            </Form.Item>
            <Form.Item name="github" label="GitHub 用户名">
              <Input maxLength={64} />
            </Form.Item>
            <Button type="primary" onClick={save} loading={saving}>保存修改</Button>
          </Form>
        </Card>
      </Col>
      <Col xs={24} lg={10}>
        <Card title="修改密码">
          <Form form={pwdForm} layout="vertical">
            <Form.Item name="oldPassword" label="原密码" rules={[{ required: true, message: '请输入原密码' }]}>
              <Input.Password prefix={<LockOutlined />} />
            </Form.Item>
            <Form.Item name="newPassword" label="新密码（6-32 位）" rules={[
              { required: true, message: '请输入新密码' },
              { min: 6, max: 32, message: '长度为 6-32 位' },
            ]}>
              <Input.Password prefix={<LockOutlined />} />
            </Form.Item>
            <Button onClick={changePwd}>确认修改</Button>
          </Form>
          <p style={{ color: 'var(--text-faint)', fontSize: 12.5, marginTop: 12 }}>
            提示：使用 GitHub 等第三方登录的账号设置密码后即可用密码登录。
          </p>
        </Card>
      </Col>
    </Row>
  );
}
