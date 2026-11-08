import { useQuery } from '@tanstack/react-query'
import { Compass } from 'lucide-react'
import { Link } from 'react-router-dom'
import { postsApi, usersApi } from '../api/endpoints'
import type { UserSummary } from '../api/types'
import { useCurrentUser } from '../auth/useMe'
import { Avatar } from '../components/Avatar'
import { FollowButton } from '../components/FollowButton'
import { InfiniteSentinel } from '../components/InfiniteSentinel'
import { PostCard } from '../components/PostCard'
import { SearchBox } from '../components/SearchBox'
import { EmptyState, ErrorState, Skeleton } from '../components/States'
import { usePostList } from '../lib/usePosts'

function Suggestions() {
  const me = useCurrentUser()
  const { data = [] } = useQuery({
    queryKey: ['following', 'suggestions', me.id],
    queryFn: async () => {
      const [explore, following] = await Promise.all([postsApi.explore(0, 50), usersApi.following(me.id, 0, 200)])
      const followed = new Set(following.content.map((user) => user.id))
      const authors = new Map<number, UserSummary>()
      explore.content.forEach((post) => {
        if (post.authorId !== me.id && !followed.has(post.authorId)) {
          authors.set(post.authorId, {
            id: post.authorId,
            username: post.authorUsername,
            avatarUrl: post.authorAvatarUrl,
          })
        }
      })
      return [...authors.values()].slice(0, 5)
    },
  })

  if (!data.length) return null
  return (
    <section className="mt-6">
      <h2 className="mb-3 text-sm font-semibold text-muted">Players to follow</h2>
      <ul className="space-y-3">
        {data.map((user) => (
          <li key={user.id} className="flex items-center gap-3">
            <Link to={`/u/${user.username}`}>
              <Avatar name={user.username} url={user.avatarUrl} />
            </Link>
            <Link to={`/u/${user.username}`} className="min-w-0 flex-1 truncate text-sm font-medium hover:underline">
              {user.username}
            </Link>
            <FollowButton userId={user.id} following={false} compact />
          </li>
        ))}
      </ul>
    </section>
  )
}

export function FeedPage() {
  const me = useCurrentUser()
  const feed = usePostList(['feed'], (page) => postsApi.feed(page))

  return (
    <div className="mx-auto flex max-w-[920px] justify-center gap-10 sm:px-4 sm:py-6">
      <div className="w-full max-w-[520px] min-w-0 space-y-5">
        {feed.isPending && <Skeleton className="aspect-square sm:rounded-2xl" />}
        {feed.isError && <ErrorState error={feed.error} onRetry={() => feed.refetch()} />}
        {feed.isSuccess && !feed.posts.length && (
          <EmptyState icon={Compass} title="Your feed is empty">
            Follow players to see their posts here.{' '}
            <Link to="/explore" className="font-semibold text-accent hover:underline">
              Explore posts
            </Link>
          </EmptyState>
        )}
        {feed.posts.map((post) => (
          <PostCard key={post.id} post={post} />
        ))}
        <InfiniteSentinel
          hasNextPage={feed.hasNextPage}
          isFetching={feed.isFetchingNextPage}
          fetchNextPage={feed.fetchNextPage}
        />
      </div>

      <aside className="sticky top-6 hidden w-72 shrink-0 self-start lg:block">
        <Link to={`/u/${me.username}`} className="mb-5 flex items-center gap-3">
          <Avatar name={me.username} url={me.avatarUrl} size={44} />
          <span className="truncate font-semibold">{me.username}</span>
        </Link>
        <SearchBox />
        <Suggestions />
      </aside>
    </div>
  )
}
