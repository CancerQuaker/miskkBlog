import { http, request } from './client';

export interface TagVO {
  id: number;
  name: string;
  color?: string | null;
  articleCount?: number;
}

export interface CategoryVO {
  id: number;
  name: string;
  description?: string | null;
  articleCount?: number;
}

export interface ArticleCard {
  id: number;
  title: string;
  summary: string | null;
  cover: string | null;
  categoryId: number;
  categoryName: string;
  tags: TagVO[] | null;
  author: { id: number; nickname: string; avatar: string | null } | null;
  views: number;
  likes: number;
  comments: number;
  shares: number;
  watchSeconds: number;
  pinned: boolean;
  /** 0 草稿 1 已发布 2 已隐藏（管理端使用） */
  status?: number;
  publishedAt: string | null;
  createdAt: string;
}

export interface ArticleDetail extends ArticleCard {
  content: string;
  wordCount: number;
  readingMinutes: number;
  liked?: boolean;
  prevId?: number;
  prevTitle?: string;
  nextId?: number;
  nextTitle?: string;
}

export interface PageData<T> {
  records: T[];
  total: number;
  page: number;
  size: number;
}

export interface CommentItem {
  id: number;
  articleId: number;
  content: string;
  parentId?: number | null;
  user: { id: number; nickname: string; avatar: string | null };
  replyUser?: { id: number; nickname: string; avatar: string | null } | null;
  createdAt: string;
  children: CommentItem[];
}

export const articleApi = {
  page: (params: Record<string, any>) =>
    request<PageData<ArticleCard>>(http.get('/article/articles', { params })),
  detail: (id: number | string) => request<ArticleDetail>(http.get(`/article/articles/${id}`)),
  search: (params: Record<string, any>) =>
    request<PageData<ArticleCard>>(http.get('/article/search', { params })),
  view: (id: number | string) => request<boolean>(http.post(`/article/articles/${id}/view`)),
  like: (id: number | string) => request<boolean>(http.post(`/article/articles/${id}/like`)),
  unlike: (id: number | string) => request<boolean>(http.delete(`/article/articles/${id}/like`)),
  share: (id: number | string, channel: string) =>
    request<void>(http.post(`/article/articles/${id}/share`, { articleId: id, channel })),
  watch: (id: number | string, seconds: number) =>
    request<void>(http.post(`/article/articles/${id}/watch`, { seconds })),
  comments: (articleId: number | string, page = 1, size = 10) =>
    request<PageData<CommentItem>>(http.get('/article/comments', { params: { articleId, page, size } })),
  createComment: (data: { articleId: number; content: string; parentId?: number; replyUserId?: number }) =>
    request<CommentItem>(http.post('/article/comments', data)),
  deleteComment: (id: number) => request<void>(http.delete(`/article/comments/${id}`)),
  categories: () => request<CategoryVO[]>(http.get('/article/categories')),
  tags: () => request<TagVO[]>(http.get('/article/tags')),
  uploadImage: (file: File) => {
    const form = new FormData();
    form.append('file', file);
    return request<string>(http.post('/article/upload/image', form));
  },
};

// ==================== 管理端 ====================

export interface ArticleSaveDTO {
  title: string;
  summary?: string;
  cover?: string;
  content: string;
  categoryId: number;
  tagNames?: string[];
  status: number;
  pinned?: boolean;
}

export const adminArticleApi = {
  page: (params: Record<string, any>) =>
    request<PageData<ArticleCard>>(http.get('/article/admin/articles', { params })),
  create: (data: ArticleSaveDTO) => request<ArticleDetail>(http.post('/article/admin/articles', data)),
  update: (id: number, data: ArticleSaveDTO) =>
    request<ArticleDetail>(http.put(`/article/admin/articles/${id}`, data)),
  remove: (id: number) => request<void>(http.delete(`/article/admin/articles/${id}`)),
  updateStatus: (id: number, status: number) =>
    request<void>(http.put(`/article/admin/articles/${id}/status`, { status })),
};
