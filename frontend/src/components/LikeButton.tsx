import { Heart } from 'lucide-react'
import type { Post } from '../api/types'
import { useToggleLike } from '../lib/usePosts'

export function LikeButton({ post, className = 'size-6' }: { post: Post; className?: string }) {
  const toggle = useToggleLike()
  return (
    <button
      aria-label={post.likedByMe ? 'Unlike' : 'Like'}
      aria-pressed={post.likedByMe}
      onClick={(event) => {
        event.preventDefault()
        event.stopPropagation()
        toggle.mutate({ id: post.id, liked: !post.likedByMe })
      }}
      className="transition-transform hover:scale-110 active:scale-90"
    >
      <Heart className={`${className} ${post.likedByMe ? 'fill-like text-like' : ''}`} />
    </button>
  )
}
