import {
  useInfiniteQuery,
  useMutation,
  useQueryClient,
  type InfiniteData,
  type QueryClient,
} from '@tanstack/react-query'
import { useMemo } from 'react'
import { postsApi } from '../api/endpoints'
import type { Page, Post } from '../api/types'

type PostsCache = Post | InfiniteData<Page<Post>> | undefined

function mapCachedPosts(client: QueryClient, update: (post: Post) => Post) {
  client.setQueriesData<PostsCache>({ queryKey: ['posts'] }, (data) => {
    if (!data) return data
    if ('pages' in data) {
      return {
        ...data,
        pages: data.pages.map((page) => ({ ...page, content: page.content.map(update) })),
      }
    }
    return update(data)
  })
}

export function usePostList(key: unknown[], fetchPage: (page: number) => Promise<Page<Post>>) {
  const query = useInfiniteQuery({
    queryKey: ['posts', ...key],
    queryFn: ({ pageParam }) => fetchPage(pageParam),
    initialPageParam: 0,
    getNextPageParam: (last) => (last.last ? undefined : last.number + 1),
  })

  // A post created while scrolling shifts later pages, so the same id can appear twice.
  const posts = useMemo(() => {
    const byId = new Map<number, Post>()
    query.data?.pages.forEach((page) => page.content.forEach((post) => byId.set(post.id, post)))
    return [...byId.values()]
  }, [query.data])

  return { ...query, posts }
}

export function useToggleLike() {
  const client = useQueryClient()

  return useMutation({
    mutationFn: ({ id, liked }: { id: number; liked: boolean }) =>
      liked ? postsApi.like(id) : postsApi.unlike(id),
    onMutate: async ({ id, liked }) => {
      await client.cancelQueries({ queryKey: ['posts'] })
      const setLiked = (value: boolean) =>
        mapCachedPosts(client, (post) =>
          post.id === id && post.likedByMe !== value
            ? { ...post, likedByMe: value, likeCount: post.likeCount + (value ? 1 : -1) }
            : post,
        )
      setLiked(liked)
      return { rollback: () => setLiked(!liked) }
    },
    onError: (_error, _vars, context) => context?.rollback(),
  })
}

export function changeCommentCount(client: QueryClient, postId: number, delta: number) {
  mapCachedPosts(client, (post) =>
    post.id === postId ? { ...post, commentCount: post.commentCount + delta } : post,
  )
}

export function useDeletePost() {
  const client = useQueryClient()
  return useMutation({
    mutationFn: postsApi.remove,
    onSuccess: () => {
      // Skips post details: the open one no longer exists, and refetching it would 404.
      client.invalidateQueries({ queryKey: ['posts'], predicate: (query) => query.queryKey[1] !== 'detail' })
      client.invalidateQueries({ queryKey: ['profile'] })
    },
  })
}
