export type MediaType = 'PHOTO' | 'VIDEO'
export type RelationType = 'FAVORITE' | 'WISHLIST'

export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  last: boolean
}

export interface Post {
  id: number
  authorId: number
  authorUsername: string
  authorAvatarUrl: string | null
  gameId: number | null
  gameTitle: string | null
  mediaUrl: string
  mediaType: MediaType
  caption: string | null
  createdAt: string
  likeCount: number
  commentCount: number
  likedByMe: boolean
}

export interface Comment {
  id: number
  authorId: number
  authorUsername: string
  authorAvatarUrl: string | null
  body: string
  createdAt: string
}

export interface Me {
  id: number
  username: string
  email: string
  avatarUrl: string | null
  bio: string | null
  createdAt: string
}

export interface UserSummary {
  id: number
  username: string
  avatarUrl: string | null
}

export interface PublicProfile extends UserSummary {
  bio: string | null
  postCount: number
  followerCount: number
  followingCount: number
  followedByMe: boolean
}

export interface Game {
  id: number
  title: string
  description: string | null
}

export interface UserGame {
  id: number
  game: Game
  relationType: RelationType
}

export interface UploadedMedia {
  url: string
  mediaType: MediaType
}
