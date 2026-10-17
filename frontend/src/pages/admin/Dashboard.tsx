import { useEffect, useRef, useState } from 'react';
import { Card, Col, Row, Spin, Statistic } from 'antd';
import {
  EyeOutlined, FileTextOutlined, LikeOutlined, MessageOutlined,
  ThunderboltOutlined, UserOutlined,
} from '@ant-design/icons';
import * as echarts from 'echarts';
import { adminApi, DashboardVO, SystemInfo } from '../../api/admin';
import { UsageProgress } from '../../components/Sidebar';
import { useThemeStore, resolveTheme, resolvePrimary } from '../../stores/themeStore';

function fmtUptime(sec: number) {
  const d = Math.floor(sec / 86400);
  const h = Math.floor((sec % 86400) / 3600);
  const m = Math.floor((sec % 3600) / 60);
  return d > 0 ? `${d}天${h}小时${m}分` : `${h}小时${m}分`;
}

export default function Dashboard() {
  const [data, setData] = useState<DashboardVO | null>(null);
  const [sys, setSys] = useState<SystemInfo | null>(null);
  const postChartRef = useRef<HTMLDivElement>(null);
  const commentChartRef = useRef<HTMLDivElement>(null);
  const topChartRef = useRef<HTMLDivElement>(null);
  const mode = useThemeStore((s) => s.mode);
  const accent = useThemeStore((s) => s.accent);
  const resolvedTheme = resolveTheme(mode);

  useEffect(() => {
    adminApi.dashboard().then(setData).catch(() => {});
    const loadSys = () => adminApi.system().then(setSys).catch(() => {});
    loadSys();
    const timer = setInterval(loadSys, 5000);
    return () => clearInterval(timer);
  }, []);

  // 图表渲染；ECharts 画布无法消费 var(--primary)，改在 JS 侧按主题色覆盖链解析实际主色
  useEffect(() => {
    const a = data?.articles;
    if (!a) return;
    const primary = resolvePrimary(accent, resolvedTheme);

    const mk = (ref: React.RefObject<HTMLDivElement>, option: echarts.EChartsOption) => {
      if (!ref.current) return null;
      const chart = echarts.init(ref.current);
      chart.setOption(option);
      const onResize = () => chart.resize();
      window.addEventListener('resize', onResize);
      return chart;
    };

    const charts: (echarts.ECharts | null)[] = [];

    charts.push(mk(postChartRef, {
      title: { text: '近 30 天发布趋势', left: 'center', textStyle: { fontSize: 14 } },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: a.dailyPosts.map((d) => d.date.slice(5)) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{
        data: a.dailyPosts.map((d) => d.count),
        type: 'line', smooth: true, areaStyle: { opacity: 0.25 },
        color: primary,
      }],
      grid: { left: 40, right: 20, top: 40, bottom: 28 },
    }));

    charts.push(mk(commentChartRef, {
      title: { text: '近 14 天评论数', left: 'center', textStyle: { fontSize: 14 } },
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: a.dailyComments.map((d) => d.date.slice(5)) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{ data: a.dailyComments.map((d) => d.count), type: 'bar', color: primary, barWidth: '50%' }],
      grid: { left: 40, right: 20, top: 40, bottom: 28 },
    }));

    charts.push(mk(topChartRef, {
      title: { text: '浏览量 Top10', left: 'center', textStyle: { fontSize: 14 } },
      tooltip: { trigger: 'axis' },
      yAxis: {
        type: 'category',
        data: [...a.topArticles].reverse().map((t) => t.title.length > 12 ? t.title.slice(0, 12) + '…' : t.title),
      },
      xAxis: { type: 'value' },
      series: [{ data: [...a.topArticles].reverse().map((t) => t.views), type: 'bar', color: primary }],
      grid: { left: 130, right: 30, top: 40, bottom: 28 },
    }));

    return () => charts.forEach((c) => c?.dispose());
  }, [data, resolvedTheme, accent]);

  const a = data?.articles;

  return (
    <div>
      <Row gutter={[16, 16]}>
        <Col xs={12} sm={8} lg={4}><Card className="stat-card"><Statistic title="文章总数" value={a?.totalArticles ?? '-'} prefix={<FileTextOutlined style={{ color: 'var(--primary)' }} />} /></Card></Col>
        <Col xs={12} sm={8} lg={4}><Card className="stat-card"><Statistic title="总浏览量" value={a?.totalViews ?? '-'} prefix={<EyeOutlined style={{ color: 'var(--primary)' }} />} /></Card></Col>
        <Col xs={12} sm={8} lg={4}><Card className="stat-card"><Statistic title="总点赞" value={a?.totalLikes ?? '-'} prefix={<LikeOutlined style={{ color: 'var(--primary)' }} />} /></Card></Col>
        <Col xs={12} sm={8} lg={4}><Card className="stat-card"><Statistic title="总评论" value={a?.totalComments ?? '-'} prefix={<MessageOutlined style={{ color: 'var(--primary)' }} />} /></Card></Col>
        <Col xs={12} sm={8} lg={4}><Card className="stat-card"><Statistic title="本月发布" value={a?.monthPublished ?? '-'} prefix={<ThunderboltOutlined style={{ color: 'var(--primary)' }} />} /></Card></Col>
        <Col xs={12} sm={8} lg={4}><Card className="stat-card"><Statistic title="用户数" value={data?.users?.totalUsers ?? '-'} prefix={<UserOutlined style={{ color: 'var(--primary)' }} />} /></Card></Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}><Card><div ref={postChartRef} style={{ height: 280 }} /></Card></Col>
        <Col xs={24} lg={12}><Card><div ref={commentChartRef} style={{ height: 280 }} /></Card></Col>
        <Col xs={24} lg={12}><Card><div ref={topChartRef} style={{ height: 320 }} /></Card></Col>

        {/* 系统监控 */}
        <Col xs={24} lg={12}>
          <Card title="🖥️ 系统监控（5 秒自动刷新）" extra={sys && <span style={{ color: 'var(--text-faint)', fontSize: 12 }}>{sys.osName} · Java {sys.javaVersion} · 已运行 {fmtUptime(sys.uptimeSeconds)}</span>}>
            {!sys ? <Spin /> : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                <UsageProgress percent={sys.cpuLoad} label={`CPU（${sys.cpuCores} 核）`} />
                <UsageProgress percent={sys.memUsage} label={`内存 ${sys.memUsed}MB / ${sys.memTotal}MB`} />
                {sys.disks?.map((d, i) => (
                  <UsageProgress key={i} percent={d.usage} label={`磁盘 ${d.name} · ${d.used}GB / ${d.total}GB`} />
                ))}
                <UsageProgress percent={sys.jvmMax ? (sys.jvmUsed / sys.jvmMax) * 100 : 0}
                  label={`JVM 堆内存 ${sys.jvmUsed}MB / ${sys.jvmMax}MB`} />
              </div>
            )}
          </Card>
        </Col>
      </Row>
    </div>
  );
}
