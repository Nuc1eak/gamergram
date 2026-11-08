import { useQuery } from '@tanstack/react-query'
import { Compass } from 'lucide-react'
import { useState } from 'react'
import { gamesApi, postsApi } from '../api/endpoints'
import { InfiniteSentinel } from '../components/InfiniteSentinel'
import { MasonryGrid, MasonrySkeleton } from '../components/MasonryGrid'
import { SearchBox } from '../components/SearchBox'
import { EmptyState, ErrorState } from '../components/States'
import { usePostList } from '../lib/usePosts'

function Chip({ active, onClick, children }: { active: boolean; onClick: () => void; children: string }) {
  return (
    <button
      onClick={onClick}
      aria-pressed={active}
      className={`shrink-0 rounded-full px-4 py-1.5 text-sm font-medium transition-colors ${
        active ? 'bg-accent text-on-accent' : 'bg-surface-2 hover:bg-line'
      }`}
    >
      {children}
    </button>
  )
}

export function ExplorePage() {
  const [gameId, setGameId] = useState<number | null>(null)
  const games = useQuery({ queryKey: ['games', ''], queryFn: () => gamesApi.search() })
  const list = usePostList(gameId ? ['game', gameId] : ['explore'], (page) =>
    gameId ? postsApi.byGame(gameId, page) : postsApi.explore(page),
  )

  return (
    <div className="mx-auto max-w-[1400px] px-3 py-4 sm:px-6">
      <div className="mb-3 lg:hidden">
        <SearchBox />
      </div>
      <div className="sticky top-14 z-10 -mx-3 mb-4 flex gap-2 overflow-x-auto bg-bg px-3 py-2 sm:-mx-6 sm:px-6 md:top-0">
        <Chip active={gameId === null} onClick={() => setGameId(null)}>
          All games
        </Chip>
        {games.data?.map((game) => (
          <Chip key={game.id} active={gameId === game.id} onClick={() => setGameId(game.id)}>
            {game.title}
          </Chip>
        ))}
      </div>

      {list.isPending && <MasonrySkeleton />}
      {list.isError && <ErrorState error={list.error} onRetry={() => list.refetch()} />}
      {list.isSuccess && !list.posts.length && (
        <EmptyState icon={Compass} title="Nothing here yet">
          Be the first to post.
        </EmptyState>
      )}
      {list.posts.length > 0 && <MasonryGrid posts={list.posts} />}
      <InfiniteSentinel
        hasNextPage={list.hasNextPage}
        isFetching={list.isFetchingNextPage}
        fetchNextPage={list.fetchNextPage}
      />
    </div>
  )
}
