export function Logo({ withName = true }: { withName?: boolean }) {
  return (
    <span className="inline-flex items-center gap-2.5">
      <img src="/logo.svg" alt="" className="size-8" />
      {withName && <span className="font-display text-xl font-bold tracking-tight">Gamergram</span>}
    </span>
  )
}
