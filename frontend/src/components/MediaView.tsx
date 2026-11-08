import { ImageOff, Volume2, VolumeX } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { mediaUrl } from '../api/client'
import type { MediaType } from '../api/types'
import { useInView } from '../lib/useInView'

interface MediaViewProps {
  url: string
  type: MediaType
  alt: string
  className?: string
}

function Video({ url, className }: { url: string; className: string }) {
  const { ref: wrapper, inView } = useInView<HTMLDivElement>('0px', 0.5)
  const video = useRef<HTMLVideoElement>(null)
  const [muted, setMuted] = useState(true)

  useEffect(() => {
    if (inView) video.current?.play().catch(() => undefined)
    else video.current?.pause()
  }, [inView])

  return (
    <div ref={wrapper} className="relative size-full">
      <video ref={video} src={url} muted={muted} loop playsInline preload="metadata" className={className} />
      <button
        onClick={(event) => {
          event.preventDefault()
          event.stopPropagation()
          setMuted(!muted)
        }}
        aria-label={muted ? 'Unmute' : 'Mute'}
        className="absolute right-2 bottom-2 rounded-full bg-black/60 p-1.5 text-white"
      >
        {muted ? <VolumeX className="size-4" /> : <Volume2 className="size-4" />}
      </button>
    </div>
  )
}

export function MediaView({ url, type, alt, className = 'size-full object-cover' }: MediaViewProps) {
  const [failed, setFailed] = useState(false)
  const src = mediaUrl(url)

  if (failed) {
    return (
      <div className="grid size-full min-h-40 place-items-center bg-surface-2 text-muted">
        <ImageOff className="size-7" aria-label="Media unavailable" />
      </div>
    )
  }
  if (type === 'VIDEO') return <Video url={src} className={className} />
  return <img src={src} alt={alt} loading="lazy" onError={() => setFailed(true)} className={className} />
}
