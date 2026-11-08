import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { ApiError, errorMessage, getToken, setToken } from '../api/client'
import { authApi } from '../api/endpoints'
import { AuthLayout, Field, SubmitButton } from '../components/AuthLayout'

export function LoginPage() {
  const navigate = useNavigate()
  const client = useQueryClient()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')

  const login = useMutation({
    mutationFn: () => authApi.login({ username, password }),
    onSuccess: ({ token }) => {
      setToken(token)
      client.clear()
      navigate('/')
    },
  })

  if (getToken()) return <Navigate to="/" replace />

  const wrongCredentials = login.error instanceof ApiError && login.error.status === 401

  return (
    <AuthLayout title="Welcome back" footer={{ text: 'New here?', link: '/register', label: 'Create an account' }}>
      <form
        className="space-y-4"
        onSubmit={(event) => {
          event.preventDefault()
          login.mutate()
        }}
      >
        <Field label="Username" value={username} onChange={setUsername} autoComplete="username" />
        <Field label="Password" type="password" value={password} onChange={setPassword} autoComplete="current-password" />
        {login.isError && (
          <p role="alert" className="text-sm text-like">
            {wrongCredentials ? 'Wrong username or password.' : errorMessage(login.error)}
          </p>
        )}
        <SubmitButton pending={login.isPending}>{login.isPending ? 'Signing in…' : 'Sign in'}</SubmitButton>
      </form>
    </AuthLayout>
  )
}
