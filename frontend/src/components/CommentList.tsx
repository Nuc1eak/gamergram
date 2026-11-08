import { useInfiniteQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Send, Trash2 } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { errorMessage } from '../api/client'
import { commentsApi } from '../api/endpoints'
import { useCurrentUser } from '../auth/useMe'
import { timeAgo } from '../lib/format'
import { changeCommentCount } from '../lib/usePosts'
import { Avatar } from './Avatar'
import { ErrorState, Spinner } from './States'

const MAX_COMMENT = 1000

export function CommentList({ postId }: { postId: number }) {
  const me = useCurrentUser()
  const client = useQueryClient()
  const queryKey = ['comments', postId]
  const comments = useInfiniteQuery({
    queryKey,
    queryFn: ({ pageParam }) => commentsApi.list(postId, pageParam),
    initialPageParam: 0,
    getNextPageParam: (last) => (last.last ? undefined : last.number + 1),
  })

  const remove = useMutation({
    mutationFn: (id: number) => commentsApi.remove(postId, id),
    onSuccess: () => {
      changeCommentCount(client, postId, -1)
      client.invalidateQueries({ queryKey })
    },
  })

  if (comments.isPending) return <Spinner />
  if (comments.isError) return <ErrorState error={comments.error} onRetry={() => comments.refetch()} />

  const items = comments.data.pages.flatMap((page) => page.content)
  if (!items.length) return <p className="py-8 text-center text-sm text-muted">No comments yet. Say something.</p>

  return (
    <ul className="space-y-4">
      {items.map((comment) => (
        <li key={comment.id} className="group flex gap-3">
          <Link to={`/u/${comment.authorUsername}`}>
            <Avatar name={comment.authorUsername} url={comment.authorAvatarUrl} size={32} />
          </Link>
          <div className="min-w-0 flex-1 text-sm">
            <p className="break-words whitespace-pre-line">
              <Link to={`/u/${comment.authorUsername}`} className="mr-1.5 font-semibold hover:underline">
                {comment.authorUsername}
              </Link>
              {comment.body}
            </p>
            <time dateTime={comment.createdAt} className="text-xs text-muted">
              {timeAgo(comment.createdAt)}
            </time>
          </div>
          {comment.authorId === me.id && (
            <button
              aria-label="Delete comment"
              onClick={() => remove.mutate(comment.id)}
              className="self-start p-1 text-muted hover:text-like sm:opacity-0 sm:group-hover:opacity-100 sm:focus:opacity-100"
            >
              <Trash2 className="size-4" />
            </button>
          )}
        </li>
      ))}
      {comments.hasNextPage && (
        <li>
          <button onClick={() => comments.fetchNextPage()} className="text-sm font-medium text-muted hover:text-text">
            Load more comments
          </button>
        </li>
      )}
    </ul>
  )
}

export function CommentForm({ postId }: { postId: number }) {
  const client = useQueryClient()
  const [body, setBody] = useState('')

  const create = useMutation({
    mutationFn: () => commentsApi.create(postId, body.trim()),
    onSuccess: () => {
      setBody('')
      changeCommentCount(client, postId, 1)
      client.invalidateQueries({ queryKey: ['comments', postId] })
    },
  })

  return (
    <form
      onSubmit={(event) => {
        event.preventDefault()
        if (body.trim()) create.mutate()
      }}
      className="border-t border-line p-3"
    >
      <div className="flex items-center gap-2">
        <input
          value={body}
          onChange={(event) => setBody(event.target.value)}
          maxLength={MAX_COMMENT}
          placeholder="Add a comment"
          aria-label="Add a comment"
          className="min-w-0 flex-1 bg-transparent text-sm outline-none placeholder:text-muted"
        />
        <button
          disabled={!body.trim() || create.isPending}
          aria-label="Post comment"
          className="p-1 text-accent disabled:opacity-40"
        >
          <Send className="size-5" />
        </button>
      </div>
      {create.isError && <p className="mt-1 text-xs text-like">{errorMessage(create.error)}</p>}
    </form>
  )
}
