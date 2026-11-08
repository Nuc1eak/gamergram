import { Gamepad2 } from 'lucide-react'
import { Link } from 'react-router-dom'

export function GameChip({ id, title }: { id: number; title: string }) {
  return (
    <Link
      to={`/games/${id}`}
      className="inline-flex max-w-full items-center gap-1 text-xs font-medium text-accent hover:underline"
    >
      <Gamepad2 className="size-3.5 shrink-0" />
      <span className="truncate">{title}</span>
    </Link>
  )
}
