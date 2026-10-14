import { http, request } from './client';

export interface DashboardVO {
  users?: { totalUsers: number; newUsersThisMonth: number } | null;
  articles?: {
    totalArticles: number;
    publishedArticles: number;
    totalComments: number;
    totalLikes: number;
    totalViews: number;
    totalShares: number;
    monthPublished: number;
    dailyPosts: { date: string; count: number }[];
    dailyComments: { date: string; count: number }[];
    topArticles: { id: number; title: string; views: number; likes: number; comments: number }[];
  } | null;
  friends?: { totalLinks: number; pendingApplies: number } | null;
}

export interface SystemInfo {
  cpuLoad: number;
  cpuCores: number;
  perCoreLoad: number[];
  memTotal: number;
  memUsed: number;
  memUsage: number;
  jvmMax: number;
  jvmTotal: number;
  jvmUsed: number;
  disks: { name: string; total: number; used: number; usage: number }[];
  osName: string;
  osArch: string;
  javaVersion: string;
  uptimeSeconds: number;
}

export const adminApi = {
  dashboard: () => request<DashboardVO>(http.get('/admin/dashboard/summary')),
  system: () => request<SystemInfo>(http.get('/admin/monitor/system')),
};
