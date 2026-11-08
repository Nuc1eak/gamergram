import { Heart, MessageCircle, Trash2 } from 'lucide-react'
import { useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import type { Post } from '../api/types'
import { useCurrentUser } from '../auth/useMe'
import { plural, timeAgo } from '../lib/format'
import { useDeletePost, useToggleLike } from '../lib/usePosts'
import { Avatar } from './Avatar'
import { GameChip } from './GameChip'
import { LikeButton } from './LikeButton'
import { MediaView } from './MediaView'
import { PostLink } from './PostLink'

const DOUBLE_TAP_MS = 300

export function PostCard({ post }: { post: Post }) {
  const me = useCurrentUser()
  const toggleLike = useToggleLike()
  const deletePost = useDeletePost()
  const lastTap = useRef(0)
  const [hearts, setHearts] = useState(0)

  const onMediaClick = (event: React.MouseEvent) => {
    if (event.timeStamp - lastTap.current < DOUBLE_TAP_MS) {
      setHearts((count) => count + 1)
      if (!post.likedByMe) toggleLike.mutate({ id: post.id, liked: true })
    }
    lastTap.current = event.timeStamp
  }

  return (
    <article className="rise border-b border-line pb-5 sm:rounded-2xl sm:border sm:bg-surface sm:pb-0">
      <header className="flex items-center gap-3 p-3">
        <Link to={`/u/${post.authorUsername}`}>
          <Avatar name={post.authorUsername} url={post.authorAvatarUrl} />
        </Link>
        <div className="min-w-0 flex-1">
          <div className="flex items-baseline gap-1.5 text-sm">
            <Link to={`/u/${post.authorUsername}`} className="truncate font-semibold hover:underline">
              {post.authorUsername}
            </Link>
            <time dateTime={post.createdAt} className="shrink-0 text-muted">
              · {timeAgo(post.createdAt)}
            </time>
          </div>
          {post.gameId && post.gameTitle && <GameChip id={post.gameId} title={post.gameTitle} />}
        </div>
        {post.authorId === me.id && (
          <button
            aria-label="Delete post"
            onClick={() => window.confirm('Delete this post?') && deletePost.mutate(post.id)}
            className="rounded-full p-2 text-muted hover:text-like"
          >
            <Trash2 className="size-4.5" />
          </button>
        )}
      </header>

      <div onClick={onMediaClick} className="relative grid max-h-[75dvh] place-items-center overflow-hidden bg-black select-none">
        <MediaView
          url={post.mediaUrl}
          type={post.mediaType}
          alt={post.caption ?? ''}
          className="max-h-[75dvh] w-full object-contain"
        />
        {hearts > 0 && (
          <Heart key={hearts} className="heart-pop pointer-events-none absolute size-24 fill-white text-white drop-shadow-lg" />
        )}
      </div>

      <div className="space-y-1.5 p-3">
        <div className="flex items-center gap-4">
          <LikeButton post={post} />
          <PostLink id={post.id} label="Comments" className="transition-transform hover:scale-110">
            <MessageCircle className="size-6" />
          </PostLink>
        </div>
        <p className="text-sm font-semibold">{plural(post.likeCount, 'like')}</p>
        {post.caption && (
          <p className="text-sm break-words whitespace-pre-line">
            <Link to={`/u/${post.authorUsername}`} className="mr-1.5 font-semibold hover:underline">
              {post.authorUsername}
            </Link>
            {post.caption}
          </p>
        )}
        {post.commentCount > 0 && (
          <PostLink id={post.id} className="block text-sm text-muted hover:text-text">
            View {post.commentCount === 1 ? '1 comment' : `all ${post.commentCount} comments`}
          </PostLink>
        )}
      </div>
    </article>
  )
}
