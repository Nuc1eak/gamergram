import { Crosshair, Gamepad2, Ghost, Joystick, Swords, Trophy, type LucideIcon } from 'lucide-react'
import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { Logo } from './Logo'

const TILES: { icon: LucideIcon; hue: number; ratio: string }[][] = [
  [
    { icon: Swords, hue: 340, ratio: 'aspect-[3/4]' },
    { icon: Ghost, hue: 200, ratio: 'aspect-square' },
    { icon: Crosshair, hue: 25, ratio: 'aspect-[3/5]' },
  ],
  [
    { icon: Trophy, hue: 84, ratio: 'aspect-[4/5]' },
    { icon: Gamepad2, hue: 265, ratio: 'aspect-[3/5]' },
    { icon: Joystick, hue: 170, ratio: 'aspect-square' },
  ],
  [
    { icon: Joystick, hue: 45, ratio: 'aspect-square' },
    { icon: Crosshair, hue: 310, ratio: 'aspect-[3/4]' },
    { icon: Swords, hue: 145, ratio: 'aspect-[4/5]' },
  ],
]

function Collage() {
  return (
    <div aria-hidden className="relative hidden overflow-hidden bg-surface lg:block">
      <div className="absolute inset-0 -top-24 flex gap-4 p-4">
        {TILES.map((column, index) => (
          <div key={index} className={`flex flex-1 flex-col gap-4 ${index === 1 ? '-mt-14' : ''}`}>
            {column.map(({ icon: Icon, hue, ratio }, tile) => (
              <div
                key={tile}
                className={`grid place-items-center rounded-2xl ${ratio}`}
                style={{ background: `linear-gradient(150deg, hsl(${hue} 70% 42%), hsl(${hue + 40} 65% 16%))` }}
              >
                <Icon className="size-14 text-white/80" strokeWidth={1.25} />
              </div>
            ))}
          </div>
        ))}
      </div>
      <div className="absolute inset-0 bg-gradient-to-t from-surface from-20% via-transparent via-50% to-transparent" />
      <p className="absolute right-10 bottom-10 left-10 font-display text-4xl leading-tight font-bold">
        Every clutch, screenshot and wishlist in one place.
      </p>
    </div>
  )
}

interface AuthLayoutProps {
  title: string
  footer: { text: string; link: string; label: string }
  children: ReactNode
}

export function AuthLayout({ title, footer, children }: AuthLayoutProps) {
  return (
    <div className="grid min-h-dvh lg:grid-cols-2">
      <Collage />
      <main className="flex items-center justify-center p-6">
        <div className="rise w-full max-w-sm">
          <Logo />
          <h1 className="mt-8 mb-6 font-display text-3xl font-bold">{title}</h1>
          {children}
          <p className="mt-6 text-sm text-muted">
            {footer.text}{' '}
            <Link to={footer.link} className="font-semibold text-accent hover:underline">
              {footer.label}
            </Link>
          </p>
        </div>
      </main>
    </div>
  )
}

interface FieldProps {
  label: string
  value: string
  onChange: (value: string) => void
  type?: string
  autoComplete?: string
  minLength?: number
  maxLength?: number
  hint?: string
  error?: string
}

export function Field({ label, onChange, hint, error, ...input }: FieldProps) {
  return (
    <label className="block">
      <span className="mb-1 block text-sm font-medium">{label}</span>
      <input
        {...input}
        required
        onChange={(event) => onChange(event.target.value)}
        aria-invalid={Boolean(error)}
        className="w-full rounded-xl border border-line bg-surface px-3 py-2.5 outline-none focus:border-accent aria-invalid:border-like"
      />
      {(error || hint) && <span className={`mt-1 block text-xs ${error ? 'text-like' : 'text-muted'}`}>{error ?? hint}</span>}
    </label>
  )
}

export function SubmitButton({ pending, children }: { pending: boolean; children: ReactNode }) {
  return (
    <button
      disabled={pending}
      className="w-full rounded-xl bg-accent py-2.5 font-semibold text-on-accent transition-opacity hover:opacity-90 disabled:opacity-50"
    >
      {children}
    </button>
  )
}
