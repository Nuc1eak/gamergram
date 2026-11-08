import { Navigate, Outlet } from 'react-router-dom'
import { getToken } from '../api/client'
import { ErrorState, Spinner } from '../components/States'
import { useMe } from './useMe'

export function RequireAuth() {
  const me = useMe()
  if (!getToken()) return <Navigate to="/login" replace />
  if (me.isPending) return <Spinner fullScreen />
  if (me.isError) return <ErrorState error={me.error} onRetry={() => me.refetch()} />
  return <Outlet />
}
