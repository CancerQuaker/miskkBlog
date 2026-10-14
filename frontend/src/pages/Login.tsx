import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Button, Card, Input, Modal, Tabs, message } from 'antd';
import { GithubOutlined, MailOutlined, QqOutlined, WechatOutlined, LockOutlined, UserOutlined, SafetyOutlined } from '@ant-design/icons';
import { authApi } from '../api/auth';
import { useAuthStore } from '../stores/authStore';
import { ChibiImage } from '../components/Chibi';

/** GitHub OAuth：跳转到授权页（需在用户服务配置 client-id） */
export function githubAuthHref() {
  // client-id 未配置时后端会返回提示；此处跳转仅作占位
  return 'https://github.com/login/oauth/authorize?client_id=YOUR_CLIENT_ID';
}

export default function Login() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const { loginSuccess } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [account, setAccount] = useState('');
  const [password, setPassword] = useState('');
  const [qqOpen, setQqOpen] = useState(false);

  const afterLogin = async (token: string, user: any) => {
    loginSuccess(token, user);
    message.success(`欢迎回来，${user.nickname} 捏~`);
    navigate(params.get('redirect') || '/');
  };

  const doLogin = async () => {
    if (!account || !password) {
      message.warning('请输入账号和密码');
      return;
    }
    setLoading(true);
    try {
      const vo = await authApi.login(account.trim(), password);
      await afterLogin(vo.token, vo.user);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container" style={{ maxWidth: 460, paddingTop: 60, position: 'relative' }}>
      <ChibiImage
        name="paimon"
        size={92}
        style={{ position: 'absolute', top: -6, right: 18, zIndex: 2, animation: 'heroFloat 3.4s ease-in-out infinite', filter: 'drop-shadow(0 6px 14px rgba(0,0,0,.14))' }}
      />
      <Card className="fade-in" style={{ borderRadius: 18 }}>
        <div style={{ textAlign: 'center', marginBottom: 20 }}>
          <h1 style={{ margin: 0, color: 'var(--primary)' }}>欢迎回来捏~</h1>
          <p style={{ color: 'var(--text-sub)' }}>登录后可以评论、点赞、追更文章</p>
        </div>

        <Tabs
          centered
          items={[
            {
              key: 'account',
              label: '账号密码',
              children: (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                  <Input
                    size="large"
                    prefix={<UserOutlined style={{ color: 'var(--text-faint)' }} />}
                    placeholder="邮箱 / 用户名"
                    value={account}
                    onChange={(e) => setAccount(e.target.value)}
                    onPressEnter={doLogin}
                  />
                  <Input.Password
                    size="large"
                    prefix={<LockOutlined style={{ color: 'var(--text-faint)' }} />}
                    placeholder="密码"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    onPressEnter={doLogin}
                  />
                  <Button type="primary" size="large" loading={loading} onClick={doLogin} block>
                    登录
                  </Button>
                  <p style={{ textAlign: 'center', color: 'var(--text-sub)', margin: 0 }}>
                    还没有账号？<Link to="/register" style={{ color: 'var(--primary)' }}>注册一个捏~</Link>
                  </p>
                </div>
              ),
            },
            {
              key: 'oauth',
              label: '第三方登录',
              children: (
                <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
                  <Button size="large" icon={<GithubOutlined />} onClick={() => message.info('GitHub 登录需要在用户服务配置 Client-ID，详见 README')}>
                    GitHub 登录
                  </Button>
                  <Button size="large" icon={<QqOutlined />} style={{ color: '#12B7F5', borderColor: '#12B7F5' }} onClick={() => setQqOpen(true)}>
                    QQ 登录
                  </Button>
                  <Button size="large" icon={<WechatOutlined />} style={{ color: '#07C160', borderColor: '#07C160' }} onClick={() => setQqOpen(true)}>
                    微信登录
                  </Button>
                  <p style={{ textAlign: 'center', color: 'var(--text-faint)', fontSize: 12.5, margin: 0 }}>
                    <SafetyOutlined /> 第三方登录需要在后端配置对应的 OAuth 应用
                  </p>
                </div>
              ),
            },
          ]}
        />
      </Card>

      <Modal open={qqOpen} onCancel={() => setQqOpen(false)} footer={null} title="呜？">
        <p>QQ / 微信登录需要企业资质审核，暂时没有开放捏~</p>
        <p>先用 <MailOutlined /> <Link to="/register" style={{ color: 'var(--primary)' }}>邮箱注册</Link> 叭！</p>
      </Modal>
    </div>
  );
}
