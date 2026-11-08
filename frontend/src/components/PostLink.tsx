import type { ReactNode } from 'react'
import { Link, useLocation } from 'react-router-dom'

/** Keeps the current page as the background so the post opens as a modal over it. */
export function PostLink(props: { id: number; className?: string; label?: string; children: ReactNode }) {
  const location = useLocation()
  const state = location.state as { background?: unknown } | null
  return (
    <Link
      to={`/p/${props.id}`}
      state={{ background: state?.background ?? location }}
      aria-label={props.label}
      className={props.className}
    >
      {props.children}
    </Link>
  )
}
