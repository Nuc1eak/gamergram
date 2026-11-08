import { useQuery } from '@tanstack/react-query'
import { Trash2 } from 'lucide-react'
import { Link } from 'react-router-dom'
import { postsApi } from '../api/endpoints'
import { useCurrentUser } from '../auth/useMe'
import { plural, timeAgo } from '../lib/format'
import { useDeletePost } from '../lib/usePosts'
import { Avatar } from './Avatar'
import { CommentForm, CommentList } from './CommentList'
import { GameChip } from './GameChip'
import { LikeButton } from './LikeButton'
import { MediaView } from './MediaView'
import { ErrorState, Spinner } from './States'

export function PostDetail({ id, onDeleted }: { id: number; onDeleted: () => void }) {
  const me = useCurrentUser()
  const deletePost = useDeletePost()
  const query = useQuery({ queryKey: ['posts', 'detail', id], queryFn: () => postsApi.get(id) })

  if (query.isPending) return <Spinner />
  if (query.isError) return <ErrorState error={query.error} onRetry={() => query.refetch()} />
  const post = query.data

  return (
    <div className="grid max-h-[92dvh] grid-rows-[minmax(0,auto)_minmax(0,1fr)] md:h-[85dvh] md:grid-cols-[minmax(0,1.4fr)_minmax(320px,1fr)] md:grid-rows-1">
      <div className="grid max-h-[45dvh] min-h-0 place-items-center overflow-hidden bg-black md:max-h-none">
        <MediaView
          url={post.mediaUrl}
          type={post.mediaType}
          alt={post.caption ?? ''}
          className="max-h-[45dvh] w-full object-contain md:max-h-[85dvh]"
        />
      </div>

      <section className="flex min-h-0 flex-col">
        <header className="flex items-center gap-3 border-b border-line p-3 pr-12">
          <Link to={`/u/${post.authorUsername}`}>
            <Avatar name={post.authorUsername} url={post.authorAvatarUrl} />
          </Link>
          <div className="min-w-0 flex-1">
            <Link to={`/u/${post.authorUsername}`} className="block truncate text-sm font-semibold hover:underline">
              {post.authorUsername}
            </Link>
            {post.gameId && post.gameTitle && <GameChip id={post.gameId} title={post.gameTitle} />}
          </div>
          {post.authorId === me.id && (
            <button
              aria-label="Delete post"
              onClick={() => {
                if (window.confirm('Delete this post?')) deletePost.mutate(post.id, { onSuccess: onDeleted })
              }}
              className="rounded-full p-2 text-muted hover:text-like"
            >
              <Trash2 className="size-4.5" />
            </button>
          )}
        </header>

        <div className="min-h-0 flex-1 space-y-4 overflow-y-auto p-3">
          {post.caption && <p className="text-sm break-words whitespace-pre-line">{post.caption}</p>}
          <CommentList postId={post.id} />
        </div>

        <div className="flex items-center gap-3 border-t border-line p-3">
          <LikeButton post={post} />
          <span className="text-sm font-semibold">{plural(post.likeCount, 'like')}</span>
          <time dateTime={post.createdAt} className="ml-auto text-xs text-muted">
            {timeAgo(post.createdAt)}
          </time>
        </div>
        <CommentForm postId={post.id} />
      </section>
    </div>
  )
}
