import { useQuery } from '@tanstack/react-query'
import { usersApi } from '../api/endpoints'

export function useMe() {
  return useQuery({ queryKey: ['me'], queryFn: usersApi.me, staleTime: 5 * 60_000 })
}

/** For components rendered under RequireAuth, where the profile is already loaded. */
export function useCurrentUser() {
  const { data } = useMe()
  if (!data) throw new Error('useCurrentUser used outside RequireAuth')
  return data
}
