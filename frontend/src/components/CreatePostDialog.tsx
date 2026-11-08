import { useMutation, useQueryClient } from '@tanstack/react-query'
import { ImagePlus } from 'lucide-react'
import { useRef, useState } from 'react'
import { errorMessage, uploadMedia } from '../api/client'
import { postsApi } from '../api/endpoints'
import type { Game } from '../api/types'
import { GamePicker } from './GamePicker'
import { Modal, ModalHeader } from './Modal'

const ACCEPTED = ['image/jpeg', 'image/png', 'image/webp', 'image/gif', 'video/mp4', 'video/webm']
const MAX_BYTES = 100 * 1024 * 1024
const MAX_CAPTION = 2200

export function CreatePostDialog({ onClose }: { onClose: () => void }) {
  const client = useQueryClient()
  const input = useRef<HTMLInputElement>(null)
  const [file, setFile] = useState<File | null>(null)
  const [preview, setPreview] = useState<string | null>(null)
  const [caption, setCaption] = useState('')
  const [game, setGame] = useState<Game | null>(null)
  const [progress, setProgress] = useState(0)
  const [fileError, setFileError] = useState('')
  const [dragging, setDragging] = useState(false)

  const chooseFile = (candidate?: File) => {
    if (!candidate) return
    if (!ACCEPTED.includes(candidate.type)) return setFileError('Use a JPG, PNG, WebP, GIF, MP4 or WebM file.')
    if (candidate.size > MAX_BYTES) return setFileError('Files can be up to 100 MB.')
    setFileError('')
    if (preview) URL.revokeObjectURL(preview)
    setPreview(URL.createObjectURL(candidate))
    setFile(candidate)
  }

  const publish = useMutation({
    mutationFn: async () => {
      const media = await uploadMedia(file!, setProgress)
      return postsApi.create({
        mediaUrl: media.url,
        mediaType: media.mediaType,
        caption: caption.trim() || undefined,
        gameId: game?.id,
      })
    },
    onSuccess: () => {
      client.invalidateQueries({ queryKey: ['posts'] })
      client.invalidateQueries({ queryKey: ['profile'] })
      onClose()
    },
  })

  return (
    <Modal label="New post" onClose={onClose} className="max-w-lg">
      <ModalHeader title="New post" onClose={onClose} />
      <form
        className="max-h-[calc(92dvh-3.5rem)] space-y-3 overflow-y-auto p-4"
        onSubmit={(event) => {
          event.preventDefault()
          publish.mutate()
        }}
      >
        {file && preview ? (
          <div className="relative overflow-hidden rounded-xl bg-black">
            {file.type.startsWith('video/') ? (
              <video src={preview} controls className="max-h-72 w-full" />
            ) : (
              <img src={preview} alt="Selected file preview" className="max-h-72 w-full object-contain" />
            )}
            <button
              type="button"
              onClick={() => input.current?.click()}
              className="absolute top-2 right-2 rounded-lg bg-black/70 px-2.5 py-1 text-xs font-medium text-white"
            >
              Replace
            </button>
          </div>
        ) : (
          <button
            type="button"
            onClick={() => input.current?.click()}
            onDragOver={(event) => {
              event.preventDefault()
              setDragging(true)
            }}
            onDragLeave={() => setDragging(false)}
            onDrop={(event) => {
              event.preventDefault()
              setDragging(false)
              chooseFile(event.dataTransfer.files[0])
            }}
            className={`flex w-full flex-col items-center gap-2 rounded-xl border-2 border-dashed py-14 transition-colors ${
              dragging ? 'border-accent bg-surface-2' : 'border-line hover:border-muted'
            }`}
          >
            <ImagePlus className="size-9 text-muted" strokeWidth={1.5} />
            <span className="font-medium">Drop a screenshot or clip</span>
            <span className="text-sm text-muted">or click to browse. Up to 100 MB.</span>
          </button>
        )}
        <input
          ref={input}
          type="file"
          hidden
          accept={ACCEPTED.join(',')}
          onChange={(event) => chooseFile(event.target.files?.[0])}
        />
        {fileError && <p className="text-sm text-like">{fileError}</p>}

        <div>
          <textarea
            value={caption}
            onChange={(event) => setCaption(event.target.value)}
            maxLength={MAX_CAPTION}
            rows={3}
            placeholder="Write a caption"
            aria-label="Caption"
            className="w-full resize-none rounded-xl bg-surface-2 px-3 py-2 text-sm outline-none placeholder:text-muted"
          />
          <p className="text-right text-xs text-muted">
            {caption.length} / {MAX_CAPTION}
          </p>
        </div>

        <GamePicker value={game} onChange={setGame} />

        {publish.isPending && (
          <div
            role="progressbar"
            aria-label="Uploading"
            aria-valuenow={Math.round(progress * 100)}
            className="h-1.5 overflow-hidden rounded-full bg-surface-2"
          >
            <div className="h-full bg-accent transition-[width]" style={{ width: `${progress * 100}%` }} />
          </div>
        )}
        {publish.isError && <p className="text-sm text-like">{errorMessage(publish.error)}</p>}

        <button
          disabled={!file || publish.isPending}
          className="w-full rounded-xl bg-accent py-2.5 font-semibold text-on-accent transition-opacity hover:opacity-90 disabled:opacity-40"
        >
          {publish.isPending ? 'Posting…' : 'Share'}
        </button>
      </form>
    </Modal>
  )
}
