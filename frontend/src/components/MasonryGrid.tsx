import { useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react'
import { mediaUrl } from '../api/client'
import type { Post } from '../api/types'
import { PinCard } from './PinCard'
import { Skeleton } from './States'

const MIN_COLUMN = 220
const FALLBACK_RATIO = 1
const MEASURE_TIMEOUT_MS = 5000

// height / width per media URL, kept across navigations so returning to a grid is instant.
const ratios = new Map<string, number>()

function measure(url: string, isVideo: boolean) {
  return new Promise<number>((resolve) => {
    const done = (width: number, height: number) => resolve(width && height ? height / width : FALLBACK_RATIO)
    window.setTimeout(() => done(0, 0), MEASURE_TIMEOUT_MS)

    if (isVideo) {
      const video = document.createElement('video')
      video.preload = 'metadata'
      video.onloadedmetadata = () => done(video.videoWidth, video.videoHeight)
      video.onerror = () => done(0, 0)
      video.src = url
    } else {
      const image = new Image()
      image.onload = () => done(image.naturalWidth, image.naturalHeight)
      image.onerror = () => done(0, 0)
      image.src = url
    }
  })
}

/** Returns how many leading posts have known proportions and can be placed. */
function useReadyCount(posts: Post[]) {
  const [, rerender] = useState(0)

  useEffect(() => {
    let cancelled = false
    const pending = posts.filter((post) => !ratios.has(post.mediaUrl))
    pending.forEach((post) => {
      measure(mediaUrl(post.mediaUrl), post.mediaType === 'VIDEO').then((ratio) => {
        ratios.set(post.mediaUrl, ratio)
        if (!cancelled) rerender((count) => count + 1)
      })
    })
    return () => {
      cancelled = true
    }
  }, [posts])

  const firstUnknown = posts.findIndex((post) => !ratios.has(post.mediaUrl))
  return firstUnknown === -1 ? posts.length : firstUnknown
}

function useWidth<T extends Element>() {
  const ref = useRef<T>(null)
  const [width, setWidth] = useState(0)

  useLayoutEffect(() => {
    const element = ref.current
    if (!element) return
    const observer = new ResizeObserver(([entry]) => setWidth(entry.contentRect.width))
    observer.observe(element)
    return () => observer.disconnect()
  }, [])

  return { ref, width }
}

/**
 * Places each post in the shortest column, in order. Heights come from media proportions
 * rather than the DOM, so loading another page never moves cards already on screen.
 */
export function MasonryGrid({ posts }: { posts: Post[] }) {
  const { ref, width } = useWidth<HTMLDivElement>()
  const readyCount = useReadyCount(posts)
  const columnCount = Math.min(5, Math.max(2, Math.floor(width / MIN_COLUMN)))

  const columns = useMemo(() => {
    const result: Post[][] = Array.from({ length: columnCount }, () => [])
    const heights = new Array<number>(columnCount).fill(0)
    posts.slice(0, readyCount).forEach((post) => {
      const shortest = heights.indexOf(Math.min(...heights))
      result[shortest].push(post)
      heights[shortest] += ratios.get(post.mediaUrl) ?? FALLBACK_RATIO
    })
    return result
  }, [posts, readyCount, columnCount])

  return (
    <div ref={ref} className="flex items-start gap-3 sm:gap-4">
      {columns.map((column, index) => (
        <div key={index} className="flex min-w-0 flex-1 flex-col gap-3 sm:gap-4">
          {column.map((post) => (
            <PinCard key={post.id} post={post} ratio={ratios.get(post.mediaUrl) ?? FALLBACK_RATIO} />
          ))}
          {readyCount < posts.length && <Skeleton className="aspect-[3/4]" />}
        </div>
      ))}
    </div>
  )
}

export function MasonrySkeleton() {
  const heights = ['aspect-[3/4]', 'aspect-square', 'aspect-[4/5]', 'aspect-[2/3]']
  return (
    <div className="grid grid-cols-2 items-start gap-3 sm:gap-4 lg:grid-cols-4" aria-hidden>
      {Array.from({ length: 8 }, (_, index) => (
        <Skeleton key={index} className={heights[(index * 3 + 1) % heights.length]} />
      ))}
    </div>
  )
}
