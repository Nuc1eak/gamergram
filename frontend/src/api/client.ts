import type { UploadedMedia } from './types'

const API_URL: string = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
const TOKEN_KEY = 'gamergram.token'

export const getToken = () => localStorage.getItem(TOKEN_KEY)
export const setToken = (token: string) => localStorage.setItem(TOKEN_KEY, token)
export const clearToken = () => localStorage.removeItem(TOKEN_KEY)

export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

// Friendly UI text by status; backend messages are written for developers.
const STATUS_MESSAGES: Record<number, string> = {
  400: 'That request was not valid.',
  401: 'Please sign in again.',
  403: "You don't have permission to do that.",
  404: 'Not found.',
  409: 'That already exists.',
  413: 'That file is too large.',
}

export function errorMessage(error: unknown): string {
  if (error instanceof ApiError) return STATUS_MESSAGES[error.status] ?? 'Something went wrong. Try again.'
  return "Can't reach the server. Check your connection and try again."
}

/** Uploaded files come back as relative paths served by the API, not the frontend. */
export function mediaUrl(url: string): string
export function mediaUrl(url: string | null): string | null
export function mediaUrl(url: string | null) {
  return url?.startsWith('/') ? API_URL + url : url
}

function handleUnauthorized(path: string) {
  if (path.startsWith('/api/auth') || !getToken()) return
  clearToken()
  window.location.assign('/login')
}

async function toApiError(response: Response) {
  const body = await response.json().catch(() => null)
  return new ApiError(response.status, body?.message ?? response.statusText, body?.fieldErrors)
}

export async function request<T>(path: string, options: { method?: string; body?: unknown } = {}): Promise<T> {
  const token = getToken()
  const hasBody = options.body !== undefined
  const response = await fetch(API_URL + path, {
    method: options.method ?? 'GET',
    headers: {
      ...(hasBody ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: hasBody ? JSON.stringify(options.body) : undefined,
  })

  if (response.status === 401) handleUnauthorized(path)
  if (!response.ok) throw await toApiError(response)
  if (response.status === 204) return undefined as T
  return response.json()
}

/** Uses XHR because fetch cannot report upload progress. */
export function uploadMedia(file: File, onProgress?: (fraction: number) => void) {
  return new Promise<UploadedMedia>((resolve, reject) => {
    const xhr = new XMLHttpRequest()
    const form = new FormData()
    form.append('file', file)

    xhr.open('POST', `${API_URL}/api/media`)
    xhr.setRequestHeader('Authorization', `Bearer ${getToken()}`)
    xhr.responseType = 'json'
    xhr.upload.onprogress = (event) => {
      if (event.lengthComputable) onProgress?.(event.loaded / event.total)
    }
    xhr.onload = () => {
      if (xhr.status === 201) return resolve(xhr.response)
      if (xhr.status === 401) handleUnauthorized('/api/media')
      reject(new ApiError(xhr.status, xhr.response?.message ?? 'Upload failed'))
    }
    xhr.onerror = () => reject(new TypeError('Network error'))
    xhr.send(form)
  })
}
