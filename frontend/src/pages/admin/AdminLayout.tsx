import { Layout, Menu, Avatar } from 'antd';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  DashboardOutlined, FileTextOutlined, FormOutlined, TeamOutlined, UserOutlined,
  LogoutOutlined, HomeOutlined,
} from '@ant-design/icons';
import { Outlet } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';
import { CatLogo } from '../../components/Navbar';

const { Sider, Header, Content } = Layout;

export default function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();

  const selected = (() => {
    if (location.pathname.startsWith('/admin/article')) return '/admin/article';
    if (location.pathname.startsWith('/admin/friend')) return '/admin/friend';
    if (location.pathname.startsWith('/admin/profile')) return '/admin/profile';
    return '/admin/dashboard';
  })();

  return (
    <Layout className="admin-layout">
      <Sider theme="light" width={220} style={{ borderRight: '1px solid var(--divider)' }} breakpoint="lg" collapsedWidth="0">
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '18px 16px', color: 'var(--primary)', fontWeight: 700 }}>
          <CatLogo size={30} /> 迷失客客·管理
        </div>
        <Menu
          mode="inline"
          selectedKeys={[selected]}
          items={[
            { key: '/admin/dashboard', icon: <DashboardOutlined />, label: <Link to="/admin/dashboard">仪表盘</Link> },
            { key: '/admin/article', icon: <FormOutlined />, label: <Link to="/admin/article">文章管理</Link> },
            { key: '/admin/friend', icon: <TeamOutlined />, label: <Link to="/admin/friend">友链审核</Link> },
            { key: '/admin/profile', icon: <UserOutlined />, label: <Link to="/admin/profile">个人资料</Link> },
            { type: 'divider' },
            { key: 'home', icon: <HomeOutlined />, label: <Link to="/">返回博客</Link> },
            { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', danger: true },
          ]}
          onClick={({ key }) => {
            if (key === 'logout') {
              logout();
              navigate('/');
            }
          }}
        />
      </Sider>
      <Layout>
        <Header
          style={{
            background: 'var(--bg-card)', borderBottom: '1px solid var(--divider)',
            display: 'flex', alignItems: 'center', justifyContent: 'flex-end', gap: 10, paddingInline: 24, height: 56,
          }}
        >
          <Avatar size={30} src={user?.avatar || '/chibi/furina.png'} style={{ background: 'var(--primary-weak)' }}>
            {user?.nickname?.[0]}
          </Avatar>
          <span>{user?.nickname}</span>
        </Header>
        <Content className="admin-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
