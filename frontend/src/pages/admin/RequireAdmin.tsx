import { Navigate, useLocation } from 'react-router-dom';
import { ReactNode } from 'react';
import { Result, Button } from 'antd';
import { useAuthStore } from '../../stores/authStore';

/** 管理后台路由守卫：需要管理员登录 */
export default function RequireAdmin({ children }: { children: ReactNode }) {
  const { user } = useAuthStore();
  const location = useLocation();

  if (!user) {
    return <Navigate to={`/login?redirect=${encodeURIComponent(location.pathname)}`} replace />;
  }
  if (user.role !== 1) {
    return (
      <Result
        status="403"
        title="403"
        subTitle="呜？这里只有管理员才能进入哦~"
        extra={<Button type="primary" href="/">回到首页</Button>}
      />
    );
  }
  return <>{children}</>;
}
