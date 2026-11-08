import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { ApiError, errorMessage, getToken, setToken } from '../api/client'
import { authApi } from '../api/endpoints'
import { AuthLayout, Field, SubmitButton } from '../components/AuthLayout'

export function RegisterPage() {
  const navigate = useNavigate()
  const client = useQueryClient()
  const [username, setUsername] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const register = useMutation({
    mutationFn: async () => {
      await authApi.register({ username, email, password })
      return authApi.login({ username, password })
    },
    onSuccess: ({ token }) => {
      setToken(token)
      client.clear()
      navigate('/explore')
    },
  })

  if (getToken()) return <Navigate to="/" replace />

  const error = register.error
  // The conflict message names the taken field; its text is not otherwise shown.
  const conflict = error instanceof ApiError && error.status === 409 ? error.message.toLowerCase() : ''
  const usernameTaken = conflict.includes('username')
  const emailTaken = conflict.includes('email')

  return (
    <AuthLayout title="Join Gamergram" footer={{ text: 'Already have an account?', link: '/login', label: 'Sign in' }}>
      <form
        className="space-y-4"
        onSubmit={(event) => {
          event.preventDefault()
          register.mutate()
        }}
      >
        <Field
          label="Username"
          value={username}
          onChange={setUsername}
          autoComplete="username"
          minLength={3}
          maxLength={50}
          hint="3 to 50 characters"
          error={usernameTaken ? 'That username is taken.' : undefined}
        />
        <Field
          label="Email"
          type="email"
          value={email}
          onChange={setEmail}
          autoComplete="email"
          error={emailTaken ? 'That email is already registered.' : undefined}
        />
        <Field
          label="Password"
          type="password"
          value={password}
          onChange={setPassword}
          autoComplete="new-password"
          minLength={8}
          maxLength={100}
          hint="At least 8 characters"
        />
        {error && !usernameTaken && !emailTaken && (
          <p role="alert" className="text-sm text-like">
            {errorMessage(error)}
          </p>
        )}
        <SubmitButton pending={register.isPending}>
          {register.isPending ? 'Creating account…' : 'Create account'}
        </SubmitButton>
      </form>
    </AuthLayout>
  )
}
