import { mediaUrl } from '../api/client'
import { hueOf } from '../lib/format'

export function Avatar({ name, url, size = 36 }: { name: string; url: string | null; size?: number }) {
  const style = { width: size, height: size }
  if (url) {
    return <img src={mediaUrl(url)} alt="" style={style} className="shrink-0 rounded-full object-cover" />
  }
  return (
    <span
      aria-hidden
      style={{ ...style, fontSize: size * 0.42, background: `hsl(${hueOf(name)} 55% 32%)` }}
      className="grid shrink-0 place-items-center rounded-full font-display font-semibold text-white uppercase"
    >
      {name[0]}
    </span>
  )
}
