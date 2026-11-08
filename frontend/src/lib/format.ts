export function timeAgo(iso: string) {
  const seconds = Math.max(0, (Date.now() - new Date(iso).getTime()) / 1000)
  if (seconds < 60) return 'now'
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m`
  if (seconds < 86400) return `${Math.floor(seconds / 3600)}h`
  if (seconds < 604800) return `${Math.floor(seconds / 86400)}d`
  return new Date(iso).toLocaleDateString('en', { month: 'short', day: 'numeric' })
}

export function compactCount(value: number) {
  return new Intl.NumberFormat('en', { notation: 'compact', maximumFractionDigits: 1 }).format(value)
}

export function plural(count: number, noun: string) {
  return `${compactCount(count)} ${noun}${count === 1 ? '' : 's'}`
}

/** Stable hue derived from a name, for covers and avatars that have no image. */
export function hueOf(text: string) {
  let hash = 0
  for (const char of text) hash = (hash * 31 + char.charCodeAt(0)) % 360
  return hash
}
