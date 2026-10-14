import axios from 'axios';
import { message } from 'antd';

/** 统一响应结构 */
export interface ApiResult<T = any> {
  code: number;
  message: string;
  data: T;
}

export const TOKEN_KEY = 'miskk-token';

export const http = axios.create({
  baseURL: '/api',
  timeout: 15000,
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (resp) => {
    const body = resp.data as ApiResult;
    if (body && typeof body.code === 'number' && body.code !== 0) {
      if (body.code === 401) {
        localStorage.removeItem(TOKEN_KEY);
        window.dispatchEvent(new CustomEvent('miskk:logout'));
      }
      return Promise.reject(new Error(body.message || '请求失败'));
    }
    return resp;
  },
  (error) => {
    const msg = error?.response?.data?.message || error.message || '网络异常，请稍后再试';
    return Promise.reject(new Error(msg));
  },
);

/** 携带统一错误提示的请求快捷方法 */
export async function request<T>(promise: Promise<{ data: ApiResult<T> }>): Promise<T> {
  try {
    const resp = await promise;
    return resp.data.data;
  } catch (e: any) {
    message.error(e?.message || '请求失败');
    throw e;
  }
}
