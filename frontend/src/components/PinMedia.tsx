import { useState } from 'react'
import type { Post } from '../api/types'
import { useInView } from '../lib/useInView'
import { MediaView } from './MediaView'

/** Grid tiles stay light: a video only attaches when it scrolls near the viewport. */
export function MediaFrame({ post }: { post: Post }) {
  const { ref, inView } = useInView<HTMLDivElement>('200px')
  const [seen, setSeen] = useState(false)
  if (inView && !seen) setSeen(true)

  return (
    <div ref={ref} className="size-full">
      {post.mediaType === 'PHOTO' || seen ? (
        <MediaView url={post.mediaUrl} type={post.mediaType} alt={post.caption ?? ''} />
      ) : null}
    </div>
  )
}
