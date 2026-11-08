import { Play } from 'lucide-react'
import { Link } from 'react-router-dom'
import type { Post } from '../api/types'
import { compactCount } from '../lib/format'
import { Avatar } from './Avatar'
import { LikeButton } from './LikeButton'
import { PostLink } from './PostLink'
import { MediaFrame } from './PinMedia'

export function PinCard({ post, ratio }: { post: Post; ratio: number }) {
  return (
    <article className="rise group min-w-0">
      <div className="relative overflow-hidden rounded-2xl bg-surface-2" style={{ aspectRatio: 1 / ratio }}>
        <PostLink id={post.id} label={`Open post by ${post.authorUsername}`} className="block size-full">
          <MediaFrame post={post} />
          <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-transparent to-transparent opacity-0 transition-opacity group-focus-within:opacity-100 group-hover:opacity-100" />
          {post.mediaType === 'VIDEO' && (
            <Play className="absolute top-3 left-3 size-5 fill-white text-white drop-shadow" aria-label="Video" />
          )}
        </PostLink>
        <div className="absolute right-3 bottom-3 flex items-center gap-1.5 rounded-full bg-black/55 px-2.5 py-1 text-sm font-semibold text-white">
          <LikeButton post={post} className="size-4.5" />
          {compactCount(post.likeCount)}
        </div>
      </div>

      <div className="mt-2 px-1">
        {post.caption && <p className="line-clamp-2 text-sm font-medium">{post.caption}</p>}
        <Link to={`/u/${post.authorUsername}`} className="mt-1 flex items-center gap-1.5 text-xs text-muted hover:text-text">
          <Avatar name={post.authorUsername} url={post.authorAvatarUrl} size={20} />
          <span className="truncate">{post.authorUsername}</span>
          {post.gameTitle && <span className="truncate text-accent">· {post.gameTitle}</span>}
        </Link>
      </div>
    </article>
  )
}
