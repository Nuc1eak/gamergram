import { useNavigate, useParams } from 'react-router-dom'
import { Modal } from '../components/Modal'
import { PostDetail } from '../components/PostDetail'
import { X } from 'lucide-react'

export function PostPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  return (
    <div className="mx-auto max-w-5xl overflow-hidden border-line bg-surface md:my-6 md:rounded-2xl md:border">
      <PostDetail id={Number(id)} onDeleted={() => navigate('/')} />
    </div>
  )
}

export function PostModal() {
  const { id } = useParams()
  const navigate = useNavigate()
  const close = () => navigate(-1)
  return (
    <Modal label="Post" onClose={close} className="relative max-w-6xl">
      <button onClick={close} aria-label="Close" className="absolute top-3.5 right-3 z-10 rounded-full bg-black/60 p-1 text-white md:bg-transparent md:text-muted md:hover:text-text">
        <X className="size-5" />
      </button>
      <PostDetail id={Number(id)} onDeleted={close} />
    </Modal>
  )
}
