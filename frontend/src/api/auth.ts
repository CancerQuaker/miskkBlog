import { http, request } from './client';

export interface UserCard {
  id: number;
  nickname: string;
  avatar: string | null;
}

export interface UserVO {
  id: number;
  username?: string;
  nickname: string;
  avatar: string | null;
  bio?: string | null;
  email?: string | null;
  qq?: string | null;
  wechat?: string | null;
  github?: string | null;
  role?: number;
  createdAt?: string;
  lastLoginAt?: string;
}

export interface LoginVO {
  token: string;
  user: UserVO;
}

export const authApi = {
  sendCode: (email: string) => request<string>(http.post('/user/auth/code', { email })),
  register: (data: { email: string; code: string; password: string; nickname?: string }) =>
    request<UserVO>(http.post('/user/auth/register', data)),
  login: (account: string, password: string) =>
    request<LoginVO>(http.post('/user/auth/login', { account, password })),
  githubLogin: (code: string) => request<LoginVO>(http.post('/user/auth/github', { code })),
  me: () => request<UserVO>(http.get('/user/auth/me')),
  updateProfile: (data: Partial<UserVO>) => request<UserVO>(http.put('/user/user/me', data)),
  updatePassword: (oldPassword: string, newPassword: string) =>
    request<void>(http.put('/user/user/me/password', { oldPassword, newPassword })),
  getUser: (id: number) => request<UserVO>(http.get(`/user/users/${id}`)),
  uploadImage: (file: File) => {
    const form = new FormData();
    form.append('file', file);
    return request<string>(http.post('/user/upload/image', form));
  },
};
