import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { usersApi } from '../api/endpoints'

interface FollowButtonProps {
  userId: number
  following: boolean
  compact?: boolean
}

export function FollowButton({ userId, following, compact = false }: FollowButtonProps) {
  const client = useQueryClient()
  const [optimistic, setOptimistic] = useState<boolean | null>(null)
  const active = optimistic ?? following

  const mutation = useMutation({
    mutationFn: (next: boolean) => (next ? usersApi.follow(userId) : usersApi.unfollow(userId)),
    onMutate: setOptimistic,
    onError: () => setOptimistic(null),
    onSuccess: () => {
      client.invalidateQueries({ queryKey: ['profile'] })
      client.invalidateQueries({ queryKey: ['following'] })
      client.invalidateQueries({ queryKey: ['posts', 'feed'] })
    },
  })

  if (compact) {
    return (
      <button
        onClick={() => mutation.mutate(!active)}
        className={`text-sm font-semibold ${active ? 'text-muted' : 'text-accent'} hover:underline`}
      >
        {active ? 'Following' : 'Follow'}
      </button>
    )
  }

  return (
    <button
      onClick={() => mutation.mutate(!active)}
      className={`rounded-lg px-5 py-1.5 text-sm font-semibold transition-colors ${
        active ? 'bg-surface-2 text-text hover:bg-line' : 'bg-accent text-on-accent hover:opacity-90'
      }`}
    >
      {active ? 'Following' : 'Follow'}
    </button>
  )
}
