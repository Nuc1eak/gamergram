import { request } from './client'
import type {
  Comment,
  Game,
  Me,
  MediaType,
  Page,
  Post,
  PublicProfile,
  RelationType,
  UserGame,
  UserSummary,
} from './types'

const pageQuery = (page: number, size: number) => `page=${page}&size=${size}`
const enc = encodeURIComponent

export const authApi = {
  login: (body: { username: string; password: string }) =>
    request<{ token: string }>('/api/auth/login', { method: 'POST', body }),
  register: (body: { username: string; email: string; password: string }) =>
    request<Me>('/api/auth/register', { method: 'POST', body }),
}

export const postsApi = {
  feed: (page: number, size = 10) => request<Page<Post>>(`/api/posts/feed?${pageQuery(page, size)}`),
  explore: (page: number, size = 24) => request<Page<Post>>(`/api/posts/explore?${pageQuery(page, size)}`),
  byUser: (userId: number, page: number, size = 24) =>
    request<Page<Post>>(`/api/posts/user/${userId}?${pageQuery(page, size)}`),
  byGame: (gameId: number, page: number, size = 24) =>
    request<Page<Post>>(`/api/posts/game/${gameId}?${pageQuery(page, size)}`),
  get: (id: number) => request<Post>(`/api/posts/${id}`),
  create: (body: { mediaUrl: string; mediaType: MediaType; caption?: string; gameId?: number }) =>
    request<Post>('/api/posts', { method: 'POST', body }),
  remove: (id: number) => request<void>(`/api/posts/${id}`, { method: 'DELETE' }),
  like: (id: number) => request<void>(`/api/posts/${id}/likes`, { method: 'POST' }),
  unlike: (id: number) => request<void>(`/api/posts/${id}/likes`, { method: 'DELETE' }),
}

export const commentsApi = {
  list: (postId: number, page: number, size = 20) =>
    request<Page<Comment>>(`/api/posts/${postId}/comments?${pageQuery(page, size)}`),
  create: (postId: number, body: string) =>
    request<Comment>(`/api/posts/${postId}/comments`, { method: 'POST', body: { body } }),
  remove: (postId: number, commentId: number) =>
    request<void>(`/api/posts/${postId}/comments/${commentId}`, { method: 'DELETE' }),
}

export const usersApi = {
  me: () => request<Me>('/api/users/me'),
  updateMe: (body: { avatarUrl?: string; bio?: string }) =>
    request<Me>('/api/users/me', { method: 'PATCH', body }),
  byUsername: (username: string) => request<PublicProfile>(`/api/users/by-username/${enc(username)}`),
  search: (query: string, size = 6) =>
    request<Page<UserSummary>>(`/api/users?query=${enc(query)}&size=${size}`),
  follow: (id: number) => request<void>(`/api/users/${id}/follow`, { method: 'POST' }),
  unfollow: (id: number) => request<void>(`/api/users/${id}/follow`, { method: 'DELETE' }),
  followers: (id: number, page: number, size = 30) =>
    request<Page<UserSummary>>(`/api/users/${id}/followers?${pageQuery(page, size)}`),
  following: (id: number, page: number, size = 30) =>
    request<Page<UserSummary>>(`/api/users/${id}/following?${pageQuery(page, size)}`),
}

export const gamesApi = {
  search: (title = '') => request<Game[]>(`/api/games?title=${enc(title)}`),
  get: (id: number) => request<Game>(`/api/games/${id}`),
  create: (title: string) => request<Game>('/api/games', { method: 'POST', body: { title } }),
  ofUser: (userId: number, type: RelationType) =>
    request<UserGame[]>(`/api/users/${userId}/games?type=${type}`),
  addToMine: (gameId: number, relationType: RelationType) =>
    request<void>('/api/me/games', { method: 'POST', body: { gameId, relationType } }),
  removeFromMine: (gameId: number) => request<void>(`/api/me/games/${gameId}`, { method: 'DELETE' }),
}
