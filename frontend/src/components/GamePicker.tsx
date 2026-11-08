import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Gamepad2, Plus, X } from 'lucide-react'
import { useDeferredValue, useState } from 'react'
import { errorMessage } from '../api/client'
import { gamesApi } from '../api/endpoints'
import type { Game } from '../api/types'

interface GamePickerProps {
  value: Game | null
  onChange: (game: Game | null) => void
}

export function GamePicker({ value, onChange }: GamePickerProps) {
  const client = useQueryClient()
  const [text, setText] = useState('')
  const query = useDeferredValue(text.trim())

  const games = useQuery({
    queryKey: ['games', query],
    enabled: query.length > 0,
    queryFn: () => gamesApi.search(query),
  })
  const create = useMutation({
    mutationFn: gamesApi.create,
    onSuccess: (game) => {
      client.invalidateQueries({ queryKey: ['games'] })
      onChange(game)
      setText('')
    },
  })

  if (value) {
    return (
      <div className="flex items-center gap-2 rounded-xl bg-surface-2 px-3 py-2 text-sm">
        <Gamepad2 className="size-4 text-accent" />
        <span className="min-w-0 flex-1 truncate font-medium">{value.title}</span>
        <button type="button" aria-label="Remove game" onClick={() => onChange(null)} className="text-muted hover:text-text">
          <X className="size-4" />
        </button>
      </div>
    )
  }

  const exactMatch = games.data?.some((game) => game.title.toLowerCase() === query.toLowerCase())

  return (
    <div>
      <input
        value={text}
        maxLength={150}
        onChange={(event) => setText(event.target.value)}
        placeholder="Tag a game (optional)"
        aria-label="Tag a game"
        className="w-full rounded-xl bg-surface-2 px-3 py-2 text-sm outline-none placeholder:text-muted"
      />
      {query && games.data && (
        <ul className="mt-1 max-h-36 overflow-y-auto rounded-xl border border-line">
          {games.data.slice(0, 5).map((game) => (
            <li key={game.id}>
              <button
                type="button"
                onClick={() => {
                  onChange(game)
                  setText('')
                }}
                className="block w-full truncate px-3 py-2 text-left text-sm hover:bg-surface-2"
              >
                {game.title}
              </button>
            </li>
          ))}
          {!exactMatch && (
            <li>
              <button
                type="button"
                disabled={create.isPending}
                onClick={() => create.mutate(query)}
                className="flex w-full items-center gap-2 px-3 py-2 text-left text-sm text-accent hover:bg-surface-2"
              >
                <Plus className="size-4 shrink-0" />
                <span className="truncate">Add “{query}”</span>
              </button>
            </li>
          )}
        </ul>
      )}
      {create.isError && <p className="mt-1 text-xs text-like">{errorMessage(create.error)}</p>}
    </div>
  )
}
