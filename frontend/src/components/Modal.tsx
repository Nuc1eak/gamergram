import { X } from 'lucide-react'
import { useEffect, useRef, type ReactNode } from 'react'

interface ModalProps {
  label: string
  onClose: () => void
  children: ReactNode
  className?: string
}

/** Native <dialog> provides the focus trap, Escape handling and top layer. */
export function Modal({ label, onClose, children, className = 'max-w-md' }: ModalProps) {
  const ref = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const dialog = ref.current
    if (dialog && !dialog.open) dialog.showModal()
  }, [])

  return (
    <dialog
      ref={ref}
      aria-label={label}
      onClose={onClose}
      onClick={(event) => event.target === ref.current && onClose()}
      className={`m-auto max-h-[92dvh] w-[calc(100%-2rem)] overflow-hidden rounded-2xl border border-line bg-surface text-text ${className}`}
    >
      {children}
    </dialog>
  )
}

export function ModalHeader({ title, onClose }: { title: string; onClose: () => void }) {
  return (
    <header className="flex items-center justify-between border-b border-line px-4 py-3">
      <h2 className="font-display font-semibold">{title}</h2>
      <button onClick={onClose} aria-label="Close" className="rounded-full p-1 text-muted hover:text-text">
        <X className="size-5" />
      </button>
    </header>
  )
}
