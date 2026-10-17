import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Button, Card, Input, message } from 'antd';
import { MailOutlined, LockOutlined, UserOutlined, SafetyOutlined, SendOutlined } from '@ant-design/icons';
import { authApi } from '../api/auth';
import { useAuthStore } from '../stores/authStore';

export default function Register() {
  const navigate = useNavigate();
  const { loginSuccess } = useAuthStore();
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [nickname, setNickname] = useState('');
  const [loading, setLoading] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => () => { if (timerRef.current) clearInterval(timerRef.current); }, []);

  const sendCode = async () => {
    if (!email.trim()) {
      message.warning('请先输入邮箱');
      return;
    }
    const devCode = await authApi.sendCode(email.trim());
    message.success('验证码已发送捏~（未配置邮件服务时验证码见响应/后端日志）');
    if (devCode) message.info(`[本地开发] 验证码：${devCode}`, 8);
    setCountdown(60);
    timerRef.current = setInterval(() => {
      setCountdown((c) => {
        if (c <= 1 && timerRef.current) {
          clearInterval(timerRef.current);
          return 0;
        }
        return c - 1;
      });
    }, 1000);
  };

  const doRegister = async () => {
    if (!email || !code || !password || !confirm) {
      message.warning('请填写完整信息');
      return;
    }
    if (password !== confirm) {
      message.warning('两次输入的密码不一致捏');
      return;
    }
    setLoading(true);
    try {
      const user = await authApi.register({ email: email.trim(), code, password, nickname: nickname || undefined });
      // 注册成功直接请求登录
      const vo = await authApi.login(email.trim(), password);
      loginSuccess(vo.token, vo.user);
      message.success(`注册成功，欢迎加入，${user.nickname} 捏~`);
      navigate('/');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container" style={{ maxWidth: 460, paddingTop: 60 }}>
      <Card className="fade-in" style={{ borderRadius: 18 }}>
        <div style={{ textAlign: 'center', marginBottom: 20 }}>
          <h1 style={{ margin: 0, color: 'var(--primary)' }}>加入迷失客客の博客</h1>
          <p style={{ color: 'var(--text-sub)' }}>邮箱注册，一分钟就能摸小狐狸捏~</p>
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
          <Input
            size="large"
            prefix={<UserOutlined style={{ color: 'var(--text-faint)' }} />}
            placeholder="昵称（选填，不填自动生成）"
            maxLength={16}
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
          />
          <Input
            size="large"
            prefix={<MailOutlined style={{ color: 'var(--text-faint)' }} />}
            placeholder="邮箱"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <div style={{ display: 'flex', gap: 10 }}>
            <Input
              size="large"
              prefix={<SafetyOutlined style={{ color: 'var(--text-faint)' }} />}
              placeholder="6 位验证码"
              maxLength={6}
              value={code}
              onChange={(e) => setCode(e.target.value)}
            />
            <Button size="large" icon={<SendOutlined />} disabled={countdown > 0} onClick={sendCode}>
              {countdown > 0 ? `${countdown}s` : '获取验证码'}
            </Button>
          </div>
          <Input.Password
            size="large"
            prefix={<LockOutlined style={{ color: 'var(--text-faint)' }} />}
            placeholder="密码（6-32 位）"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <Input.Password
            size="large"
            prefix={<LockOutlined style={{ color: 'var(--text-faint)' }} />}
            placeholder="确认密码"
            value={confirm}
            onChange={(e) => setConfirm(e.target.value)}
          />
          <Button type="primary" size="large" loading={loading} onClick={doRegister} block>
            注册并登录
          </Button>
          <p style={{ textAlign: 'center', color: 'var(--text-sub)', margin: 0 }}>
            已有账号？<Link to="/login" style={{ color: 'var(--primary)' }}>去登录</Link>
          </p>
        </div>
      </Card>
    </div>
  );
}
