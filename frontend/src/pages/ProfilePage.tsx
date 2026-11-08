import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Camera, Gamepad2, Grid3x3, Heart, ImageOff, MessageCircle, Star, Trash2, UserX } from 'lucide-react'
import { useRef, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ApiError, errorMessage, uploadMedia } from '../api/client'
import { gamesApi, postsApi, usersApi } from '../api/endpoints'
import type { PublicProfile, RelationType } from '../api/types'
import { useCurrentUser } from '../auth/useMe'
import { Avatar } from '../components/Avatar'
import { FollowButton } from '../components/FollowButton'
import { InfiniteSentinel } from '../components/InfiniteSentinel'
import { MediaFrame } from '../components/PinMedia'
import { Modal, ModalHeader } from '../components/Modal'
import { PostLink } from '../components/PostLink'
import { EmptyState, ErrorState, Skeleton, Spinner } from '../components/States'
import { compactCount, hueOf } from '../lib/format'
import { usePostList } from '../lib/usePosts'

type Tab = 'posts' | RelationType
type ListKind = 'followers' | 'following'

function PostsGrid({ userId }: { userId: number }) {
  const list = usePostList(['user', userId], (page) => postsApi.byUser(userId, page))

  if (list.isError) return <ErrorState error={list.error} onRetry={() => list.refetch()} />
  if (list.isSuccess && !list.posts.length) return <EmptyState icon={ImageOff} title="No posts yet" />

  return (
    <>
      <div className="grid grid-cols-3 gap-0.5 sm:gap-1">
        {list.isPending && Array.from({ length: 6 }, (_, i) => <Skeleton key={i} className="aspect-square rounded-none" />)}
        {list.posts.map((post) => (
          <PostLink key={post.id} id={post.id} label="Open post" className="group relative aspect-square overflow-hidden bg-surface-2">
            <MediaFrame post={post} />
            <span className="absolute inset-0 hidden items-center justify-center gap-5 bg-black/55 font-semibold text-white group-hover:flex">
              <span className="flex items-center gap-1.5">
                <Heart className="size-5 fill-white" /> {compactCount(post.likeCount)}
              </span>
              <span className="flex items-center gap-1.5">
                <MessageCircle className="size-5 fill-white" /> {compactCount(post.commentCount)}
              </span>
            </span>
          </PostLink>
        ))}
      </div>
      <InfiniteSentinel hasNextPage={list.hasNextPage} isFetching={list.isFetchingNextPage} fetchNextPage={list.fetchNextPage} />
    </>
  )
}

function GamesGrid({ userId, type, isOwner }: { userId: number; type: RelationType; isOwner: boolean }) {
  const client = useQueryClient()
  const queryKey = ['user-games', userId, type]
  const games = useQuery({ queryKey, queryFn: () => gamesApi.ofUser(userId, type) })
  const remove = useMutation({
    mutationFn: gamesApi.removeFromMine,
    onSuccess: () => client.invalidateQueries({ queryKey: ['user-games', userId] }),
  })

  if (games.isPending) return <Spinner />
  if (games.isError) return <ErrorState error={games.error} onRetry={() => games.refetch()} />
  if (!games.data.length) {
    return (
      <EmptyState icon={Gamepad2} title={type === 'FAVORITE' ? 'No favorites yet' : 'Wishlist is empty'}>
        {isOwner && 'Open a game page to add it here.'}
      </EmptyState>
    )
  }

  return (
    <ul className="grid grid-cols-2 gap-3 p-3 sm:grid-cols-3 md:grid-cols-4 md:p-0">
      {games.data.map(({ id, game }) => (
        <li key={id} className="group relative">
          <Link
            to={`/games/${game.id}`}
            style={{ background: `linear-gradient(150deg, hsl(${hueOf(game.title)} 65% 38%), hsl(${hueOf(game.title) + 40} 60% 14%))` }}
            className="flex aspect-[3/4] items-end rounded-2xl p-3 font-display font-semibold text-white"
          >
            {game.title}
          </Link>
          {isOwner && (
            <button
              aria-label={`Remove ${game.title}`}
              onClick={() => remove.mutate(game.id)}
              className="absolute top-2 right-2 rounded-full bg-black/60 p-1.5 text-white sm:opacity-0 sm:group-hover:opacity-100 sm:focus:opacity-100"
            >
              <Trash2 className="size-4" />
            </button>
          )}
        </li>
      ))}
    </ul>
  )
}

function PeopleDialog({ userId, kind, onClose }: { userId: number; kind: ListKind; onClose: () => void }) {
  const people = useInfiniteQuery({
    queryKey: ['people', kind, userId],
    queryFn: ({ pageParam }) => (kind === 'followers' ? usersApi.followers : usersApi.following)(userId, pageParam),
    initialPageParam: 0,
    getNextPageParam: (last) => (last.last ? undefined : last.number + 1),
  })
  const items = people.data?.pages.flatMap((page) => page.content) ?? []
  const title = kind === 'followers' ? 'Followers' : 'Following'

  return (
    <Modal label={title} onClose={onClose}>
      <ModalHeader title={title} onClose={onClose} />
      <div className="max-h-[60dvh] overflow-y-auto p-2">
        {people.isPending && <Spinner />}
        {people.isError && <ErrorState error={people.error} onRetry={() => people.refetch()} />}
        {people.isSuccess && !items.length && <p className="py-8 text-center text-sm text-muted">Nobody yet.</p>}
        {items.map((user) => (
          <Link key={user.id} to={`/u/${user.username}`} onClick={onClose} className="flex items-center gap-3 rounded-lg p-2 hover:bg-surface-2">
            <Avatar name={user.username} url={user.avatarUrl} />
            <span className="truncate text-sm font-medium">{user.username}</span>
          </Link>
        ))}
        <InfiniteSentinel hasNextPage={people.hasNextPage} isFetching={people.isFetchingNextPage} fetchNextPage={people.fetchNextPage} />
      </div>
    </Modal>
  )
}

const MAX_BIO = 500

function EditProfileDialog({ onClose }: { onClose: () => void }) {
  const me = useCurrentUser()
  const client = useQueryClient()
  const input = useRef<HTMLInputElement>(null)
  const [bio, setBio] = useState(me.bio ?? '')
  const [avatar, setAvatar] = useState<File | null>(null)
  const [preview, setPreview] = useState<string | null>(null)

  const save = useMutation({
    mutationFn: async () => {
      const uploaded = avatar ? await uploadMedia(avatar) : null
      return usersApi.updateMe({ bio, avatarUrl: uploaded?.url })
    },
    onSuccess: (updated) => {
      client.setQueryData(['me'], updated)
      client.invalidateQueries({ queryKey: ['profile'] })
      client.invalidateQueries({ queryKey: ['posts'] })
      onClose()
    },
  })

  return (
    <Modal label="Edit profile" onClose={onClose}>
      <ModalHeader title="Edit profile" onClose={onClose} />
      <form
        className="space-y-4 p-4"
        onSubmit={(event) => {
          event.preventDefault()
          save.mutate()
        }}
      >
        <button type="button" onClick={() => input.current?.click()} className="flex items-center gap-3 text-sm font-medium text-accent">
          {preview ? (
            <img src={preview} alt="" className="size-16 rounded-full object-cover" />
          ) : (
            <Avatar name={me.username} url={me.avatarUrl} size={64} />
          )}
          <span className="flex items-center gap-1.5">
            <Camera className="size-4" /> Change photo
          </span>
        </button>
        <input
          ref={input}
          type="file"
          hidden
          accept="image/jpeg,image/png,image/webp,image/gif"
          onChange={(event) => {
            const file = event.target.files?.[0]
            if (!file) return
            if (preview) URL.revokeObjectURL(preview)
            setPreview(URL.createObjectURL(file))
            setAvatar(file)
          }}
        />
        <div>
          <textarea
            value={bio}
            onChange={(event) => setBio(event.target.value)}
            maxLength={MAX_BIO}
            rows={4}
            placeholder="Mains, ranks, what you're playing"
            aria-label="Bio"
            className="w-full resize-none rounded-xl bg-surface-2 px-3 py-2 text-sm outline-none placeholder:text-muted"
          />
          <p className="text-right text-xs text-muted">
            {bio.length} / {MAX_BIO}
          </p>
        </div>
        {save.isError && <p className="text-sm text-like">{errorMessage(save.error)}</p>}
        <button disabled={save.isPending} className="w-full rounded-xl bg-accent py-2.5 font-semibold text-on-accent disabled:opacity-50">
          {save.isPending ? 'Saving…' : 'Save'}
        </button>
      </form>
    </Modal>
  )
}

function Stat({ value, label, onClick }: { value: number; label: string; onClick?: () => void }) {
  const content = (
    <>
      <strong className="font-semibold">{compactCount(value)}</strong> <span className="text-muted">{label}</span>
    </>
  )
  return onClick ? (
    <button onClick={onClick} className="hover:underline">
      {content}
    </button>
  ) : (
    <span>{content}</span>
  )
}

function Header({ profile, isOwner }: { profile: PublicProfile; isOwner: boolean }) {
  const [people, setPeople] = useState<ListKind | null>(null)
  const [editing, setEditing] = useState(false)

  return (
    <header className="flex gap-5 p-4 sm:gap-12 sm:px-6 sm:py-8">
      <div className="sm:hidden">
        <Avatar name={profile.username} url={profile.avatarUrl} size={80} />
      </div>
      <div className="hidden sm:block">
        <Avatar name={profile.username} url={profile.avatarUrl} size={140} />
      </div>
      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-3">
          <h1 className="truncate font-display text-2xl font-bold">{profile.username}</h1>
          {isOwner ? (
            <button onClick={() => setEditing(true)} className="rounded-lg bg-surface-2 px-4 py-1.5 text-sm font-semibold hover:bg-line">
              Edit profile
            </button>
          ) : (
            <FollowButton key={String(profile.followedByMe)} userId={profile.id} following={profile.followedByMe} />
          )}
        </div>
        <div className="mt-3 flex flex-wrap gap-x-6 gap-y-1 text-sm">
          <Stat value={profile.postCount} label={profile.postCount === 1 ? 'post' : 'posts'} />
          <Stat value={profile.followerCount} label={profile.followerCount === 1 ? 'follower' : 'followers'} onClick={() => setPeople('followers')} />
          <Stat value={profile.followingCount} label="following" onClick={() => setPeople('following')} />
        </div>
        {profile.bio && <p className="mt-3 text-sm break-words whitespace-pre-line">{profile.bio}</p>}
      </div>
      {people && <PeopleDialog userId={profile.id} kind={people} onClose={() => setPeople(null)} />}
      {editing && <EditProfileDialog onClose={() => setEditing(false)} />}
    </header>
  )
}

const TABS: { id: Tab; label: string; icon: typeof Grid3x3 }[] = [
  { id: 'posts', label: 'Posts', icon: Grid3x3 },
  { id: 'FAVORITE', label: 'Favorites', icon: Star },
  { id: 'WISHLIST', label: 'Wishlist', icon: Gamepad2 },
]

export function ProfilePage() {
  const { username = '' } = useParams()
  const me = useCurrentUser()
  const [tab, setTab] = useState<Tab>('posts')
  const profile = useQuery({
    queryKey: ['profile', username],
    queryFn: () => usersApi.byUsername(username),
    retry: (count, error) => !(error instanceof ApiError && error.status === 404) && count < 2,
  })

  if (profile.isPending) return <Spinner />
  if (profile.isError) {
    if (profile.error instanceof ApiError && profile.error.status === 404) {
      return <EmptyState icon={UserX} title="Player not found" />
    }
    return <ErrorState error={profile.error} onRetry={() => profile.refetch()} />
  }

  const isOwner = profile.data.id === me.id

  return (
    <div className="mx-auto max-w-4xl">
      <Header profile={profile.data} isOwner={isOwner} />
      <div role="tablist" className="flex justify-center gap-10 border-t border-line">
        {TABS.map(({ id, label, icon: Icon }) => (
          <button
            key={id}
            role="tab"
            aria-selected={tab === id}
            onClick={() => setTab(id)}
            className={`-mt-px flex items-center gap-1.5 border-t-2 py-3 text-sm font-semibold ${
              tab === id ? 'border-accent text-text' : 'border-transparent text-muted hover:text-text'
            }`}
          >
            <Icon className="size-4" /> {label}
          </button>
        ))}
      </div>
      {tab === 'posts' ? (
        <PostsGrid userId={profile.data.id} />
      ) : (
        <GamesGrid key={tab} userId={profile.data.id} type={tab} isOwner={isOwner} />
      )}
    </div>
  )
}
