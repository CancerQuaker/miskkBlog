import { http, request } from './client';

export interface FriendLink {
  id: number;
  name: string;
  avatar: string | null;
  url: string;
  description: string | null;
  status: number;
  sort: number;
  createdAt?: string;
}

export interface FriendApply extends FriendLink {
  contact: string;
  reason: string | null;
  reviewNote: string | null;
  reviewedAt: string | null;
  linkId: number | null;
}

export const friendApi = {
  links: () => request<FriendLink[]>(http.get('/friend/links')),
  notes: () => request<Record<string, any>>(http.get('/friend/apply/notes')),
  apply: (data: { name: string; url: string; avatar?: string; description?: string; contact: string; reason?: string }) =>
    request<FriendApply>(http.post('/friend/apply', data)),
};

export const adminFriendApi = {
  applies: (params: Record<string, any>) =>
    request<import('./article').PageData<FriendApply>>(http.get('/friend/admin/applies', { params })),
  review: (id: number, approved: boolean, note?: string) =>
    request<FriendApply>(http.post(`/friend/admin/applies/${id}/review`, { approved, note })),
  links: (params: Record<string, any>) =>
    request<import('./article').PageData<FriendLink>>(http.get('/friend/admin/links', { params })),
  updateLink: (id: number, data: Partial<FriendLink>) =>
    request<FriendLink>(http.put(`/friend/admin/links/${id}`, data)),
  removeLink: (id: number) => request<void>(http.delete(`/friend/admin/links/${id}`)),
};
