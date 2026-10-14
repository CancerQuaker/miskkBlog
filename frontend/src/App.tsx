import { useEffect } from 'react';
import { Routes, Route, useLocation } from 'react-router-dom';
import { FloatButton } from 'antd';
import Navbar from './components/Navbar';
import StarCursor from './components/StarCursor';
import ContextMenu from './components/ContextMenu';
import SakuraEffect from './components/SakuraEffect';
import Live2DKanban from './components/Live2DKanban';
import Home from './pages/Home';
import Articles from './pages/Articles';
import ArticleDetailPage from './pages/ArticleDetailPage';
import Links from './pages/Links';
import Mine from './pages/Mine';
import About from './pages/About';
import Search from './pages/Search';
import Login from './pages/Login';
import Register from './pages/Register';
import AdminLayout from './pages/admin/AdminLayout';
import RequireAdmin from './pages/admin/RequireAdmin';
import Dashboard from './pages/admin/Dashboard';
import ArticleManage from './pages/admin/ArticleManage';
import ArticleEdit from './pages/admin/ArticleEdit';
import FriendReview from './pages/admin/FriendReview';
import Profile from './pages/admin/Profile';
import { Footer } from './components/Common';
import { ChibiImage } from './components/Chibi';
import { useAuthStore } from './stores/authStore';

/** 前台布局 */
function PortalLayout({ children }: { children: React.ReactNode }) {
  const location = useLocation();
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [location.pathname]);

  return (
    <>
      <Navbar />
      {children}
      <Footer />
    </>
  );
}

export default function App() {
  const refreshMe = useAuthStore((s) => s.refreshMe);
  const logout = useAuthStore((s) => s.logout);

  useEffect(() => {
    refreshMe();
    // 任何服务返回 401 时统一登出
    const onLogout = () => logout();
    window.addEventListener('miskk:logout', onLogout);
    return () => window.removeEventListener('miskk:logout', onLogout);
  }, [refreshMe, logout]);

  return (
    <>
      <StarCursor />
      <ContextMenu />
      <SakuraEffect />
      <Routes>
        {/* 管理后台（独立布局） */}
        <Route path="/admin" element={<RequireAdmin><AdminLayout /></RequireAdmin>}>
          <Route index element={<Dashboard />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="article" element={<ArticleManage />} />
          <Route path="article/edit" element={<ArticleEdit />} />
          <Route path="friend" element={<FriendReview />} />
          <Route path="profile" element={<Profile />} />
        </Route>

        {/* 前台页面 */}
        <Route path="/" element={<PortalLayout><Home /></PortalLayout>} />
        <Route path="/articles" element={<PortalLayout><Articles /></PortalLayout>} />
        <Route path="/article/:id" element={<PortalLayout><ArticleDetailPage /></PortalLayout>} />
        <Route path="/links" element={<PortalLayout><Links /></PortalLayout>} />
        <Route path="/mine" element={<PortalLayout><Mine /></PortalLayout>} />
        <Route path="/about" element={<PortalLayout><About /></PortalLayout>} />
        <Route path="/search" element={<PortalLayout><Search /></PortalLayout>} />
        <Route path="/login" element={<PortalLayout><Login /></PortalLayout>} />
        <Route path="/register" element={<PortalLayout><Register /></PortalLayout>} />

        <Route path="*" element={
          <PortalLayout>
            <div style={{ textAlign: 'center', padding: 100, color: 'var(--text-sub)' }}>
              <ChibiImage name="march7" size={130} style={{ animation: 'heroFloat 3.2s ease-in-out infinite' }} />
              <h1 style={{ margin: '12px 0 6px', color: 'var(--primary)' }}>404</h1>
              <p>呜？页面走丢了，三月七带你回主页吧~</p>
            </div>
          </PortalLayout>
        } />
      </Routes>
      <Live2DKanban />
      <FloatButton.BackTop style={{ right: 'auto', left: 24 }} />
    </>
  );
}
