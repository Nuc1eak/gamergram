import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Bookmark, Gamepad2, ImageOff, Star, type LucideIcon } from 'lucide-react'
import { useParams } from 'react-router-dom'
import { ApiError, errorMessage } from '../api/client'
import { gamesApi, postsApi } from '../api/endpoints'
import type { RelationType } from '../api/types'
import { useCurrentUser } from '../auth/useMe'
import { InfiniteSentinel } from '../components/InfiniteSentinel'
import { MasonryGrid, MasonrySkeleton } from '../components/MasonryGrid'
import { EmptyState, ErrorState, Spinner } from '../components/States'
import { hueOf, plural } from '../lib/format'
import { usePostList } from '../lib/usePosts'

function useMyRelation(userId: number, gameId: number) {
  const client = useQueryClient()
  const prefix = ['user-games', userId]

  const relation = useQuery({
    queryKey: [...prefix, 'relation', gameId],
    queryFn: async () => {
      const [favorites, wishlist] = await Promise.all([
        gamesApi.ofUser(userId, 'FAVORITE'),
        gamesApi.ofUser(userId, 'WISHLIST'),
      ])
      if (favorites.some(({ game }) => game.id === gameId)) return 'FAVORITE' as const
      if (wishlist.some(({ game }) => game.id === gameId)) return 'WISHLIST' as const
      return null
    },
  })

  const set = useMutation({
    mutationFn: (type: RelationType) =>
      relation.data === type ? gamesApi.removeFromMine(gameId) : gamesApi.addToMine(gameId, type),
    onSuccess: () => client.invalidateQueries({ queryKey: prefix }),
  })

  return { current: relation.data ?? null, set }
}

function RelationButton(props: { active: boolean; icon: LucideIcon; label: string; onClick: () => void }) {
  const Icon = props.icon
  return (
    <button
      onClick={props.onClick}
      aria-pressed={props.active}
      className={`flex items-center gap-1.5 rounded-lg px-4 py-1.5 text-sm font-semibold transition-colors ${
        props.active ? 'bg-accent text-on-accent' : 'bg-white/15 text-white hover:bg-white/25'
      }`}
    >
      <Icon className="size-4" /> {props.label}
    </button>
  )
}

export function GamePage() {
  const id = Number(useParams().id)
  const me = useCurrentUser()
  const game = useQuery({
    queryKey: ['game', id],
    queryFn: () => gamesApi.get(id),
    retry: (count, error) => !(error instanceof ApiError && error.status === 404) && count < 2,
  })
  const list = usePostList(['game', id], (page) => postsApi.byGame(id, page))
  const { current, set } = useMyRelation(me.id, id)

  if (game.isPending) return <Spinner />
  if (game.isError) {
    if (game.error instanceof ApiError && game.error.status === 404) {
      return <EmptyState icon={Gamepad2} title="Game not found" />
    }
    return <ErrorState error={game.error} onRetry={() => game.refetch()} />
  }

  const hue = hueOf(game.data.title)
  const total = list.data?.pages[0]?.totalElements

  return (
    <div className="mx-auto max-w-[1400px] px-3 py-4 sm:px-6">
      <header
        style={{ background: `linear-gradient(120deg, hsl(${hue} 65% 34%), hsl(${hue + 40} 60% 12%))` }}
        className="mb-6 rounded-2xl p-6 text-white sm:p-10"
      >
        <h1 className="font-display text-3xl font-bold sm:text-4xl">{game.data.title}</h1>
        {game.data.description && <p className="mt-2 max-w-2xl text-sm text-white/80">{game.data.description}</p>}
        {total !== undefined && <p className="mt-2 text-sm text-white/70">{plural(total, 'post')}</p>}
        <div className="mt-4 flex flex-wrap gap-2">
          <RelationButton active={current === 'FAVORITE'} icon={Star} label="Favorite" onClick={() => set.mutate('FAVORITE')} />
          <RelationButton active={current === 'WISHLIST'} icon={Bookmark} label="Wishlist" onClick={() => set.mutate('WISHLIST')} />
        </div>
        {set.isError && <p className="mt-2 text-sm">{errorMessage(set.error)}</p>}
      </header>

      {list.isPending && <MasonrySkeleton />}
      {list.isError && <ErrorState error={list.error} onRetry={() => list.refetch()} />}
      {list.isSuccess && !list.posts.length && <EmptyState icon={ImageOff} title="No posts for this game yet" />}
      {list.posts.length > 0 && <MasonryGrid posts={list.posts} />}
      <InfiniteSentinel hasNextPage={list.hasNextPage} isFetching={list.isFetchingNextPage} fetchNextPage={list.fetchNextPage} />
    </div>
  )
}
