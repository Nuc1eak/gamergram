import { useQuery } from '@tanstack/react-query'
import { Gamepad2, Search } from 'lucide-react'
import { useDeferredValue, useState } from 'react'
import { Link } from 'react-router-dom'
import { gamesApi, usersApi } from '../api/endpoints'
import { Avatar } from './Avatar'

export function SearchBox() {
  const [text, setText] = useState('')
  const [open, setOpen] = useState(false)
  const query = useDeferredValue(text.trim())

  const results = useQuery({
    queryKey: ['search', query],
    enabled: query.length > 0,
    queryFn: async () => {
      const [users, games] = await Promise.all([usersApi.search(query), gamesApi.search(query)])
      return { users: users.content, games: games.slice(0, 6) }
    },
  })

  const close = () => {
    setOpen(false)
    setText('')
  }
  const isEmpty = results.data && !results.data.users.length && !results.data.games.length

  return (
    <div
      className="relative"
      onBlur={(event) => !event.currentTarget.contains(event.relatedTarget) && setOpen(false)}
    >
      <label className="flex items-center gap-2 rounded-xl bg-surface-2 px-3 py-2">
        <Search className="size-4 text-muted" />
        <input
          value={text}
          onChange={(event) => {
            setText(event.target.value)
            setOpen(true)
          }}
          onFocus={() => setOpen(true)}
          placeholder="Search players and games"
          aria-label="Search players and games"
          className="w-full bg-transparent text-sm outline-none placeholder:text-muted"
        />
      </label>

      {open && query && (
        <div className="absolute inset-x-0 z-30 mt-2 max-h-96 overflow-y-auto rounded-xl border border-line bg-surface p-1 shadow-xl">
          {isEmpty && <p className="px-3 py-4 text-center text-sm text-muted">No results for “{query}”</p>}
          {results.data?.users.map((user) => (
            <Link
              key={user.id}
              to={`/u/${user.username}`}
              onClick={close}
              className="flex items-center gap-3 rounded-lg px-2 py-2 hover:bg-surface-2"
            >
              <Avatar name={user.username} url={user.avatarUrl} size={32} />
              <span className="truncate text-sm font-medium">{user.username}</span>
            </Link>
          ))}
          {results.data?.games.map((game) => (
            <Link
              key={game.id}
              to={`/games/${game.id}`}
              onClick={close}
              className="flex items-center gap-3 rounded-lg px-2 py-2 hover:bg-surface-2"
            >
              <span className="grid size-8 shrink-0 place-items-center rounded-full bg-surface-2 text-accent">
                <Gamepad2 className="size-4" />
              </span>
              <span className="truncate text-sm font-medium">{game.title}</span>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
