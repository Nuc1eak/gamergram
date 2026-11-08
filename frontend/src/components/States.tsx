import { Loader2, type LucideIcon } from 'lucide-react'
import type { ReactNode } from 'react'
import { errorMessage } from '../api/client'

export function Spinner({ fullScreen = false }: { fullScreen?: boolean }) {
  return (
    <div role="status" aria-label="Loading" className={`grid place-items-center ${fullScreen ? 'h-dvh' : 'py-10'}`}>
      <Loader2 className="size-6 animate-spin text-muted" />
    </div>
  )
}

export function ErrorState({ error, onRetry }: { error: unknown; onRetry?: () => void }) {
  return (
    <div className="px-6 py-16 text-center">
      <p className="text-muted">{errorMessage(error)}</p>
      {onRetry && (
        <button onClick={onRetry} className="mt-4 font-medium text-accent hover:underline">
          Try again
        </button>
      )}
    </div>
  )
}

export function EmptyState(props: { icon: LucideIcon; title: string; children?: ReactNode }) {
  const Icon = props.icon
  return (
    <div className="mx-auto max-w-sm px-6 py-16 text-center">
      <Icon className="mx-auto size-10 text-muted" strokeWidth={1.5} />
      <h2 className="mt-4 font-display text-xl font-semibold">{props.title}</h2>
      {props.children && <div className="mt-2 text-muted">{props.children}</div>}
    </div>
  )
}

export function Skeleton({ className = '' }: { className?: string }) {
  return <div className={`animate-pulse rounded-xl bg-surface-2 ${className}`} />
}
