import { useQueryClient } from '@tanstack/react-query'
import { Compass, Home, LogOut, Moon, PlusSquare, Sun, type LucideIcon } from 'lucide-react'
import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { clearToken } from '../api/client'
import { useCurrentUser } from '../auth/useMe'
import { useTheme } from '../lib/theme'
import { Avatar } from './Avatar'
import { CreatePostDialog } from './CreatePostDialog'
import { Logo } from './Logo'

const itemClass = 'flex items-center gap-4 rounded-xl p-3 transition-colors hover:bg-surface-2'

function NavItem({ to, icon: Icon, label }: { to: string; icon: LucideIcon; label: string }) {
  return (
    <NavLink to={to} end aria-label={label} className={({ isActive }) => `${itemClass} ${isActive ? 'font-semibold text-accent' : ''}`}>
      <Icon className="size-6 shrink-0" />
      <span className="hidden xl:inline">{label}</span>
    </NavLink>
  )
}

export function AppShell() {
  const me = useCurrentUser()
  const client = useQueryClient()
  const navigate = useNavigate()
  const { theme, toggle } = useTheme()
  const [creating, setCreating] = useState(false)
  const profilePath = `/u/${me.username}`

  const logout = () => {
    clearToken()
    client.clear()
    navigate('/login')
  }

  return (
    <div className="min-h-dvh md:pl-[76px] xl:pl-60">
      <aside className="fixed inset-y-0 left-0 z-20 hidden w-[76px] flex-col border-r border-line bg-bg p-3 md:flex xl:w-60">
        <NavLink to="/" aria-label="Gamergram home" className="mb-6 flex h-12 items-center px-1.5 xl:px-3">
          <span className="xl:hidden">
            <Logo withName={false} />
          </span>
          <span className="hidden xl:inline">
            <Logo />
          </span>
        </NavLink>
        <nav className="flex flex-1 flex-col gap-1">
          <NavItem to="/" icon={Home} label="Home" />
          <NavItem to="/explore" icon={Compass} label="Explore" />
          <button onClick={() => setCreating(true)} aria-label="Create" className={itemClass}>
            <PlusSquare className="size-6 shrink-0" />
            <span className="hidden xl:inline">Create</span>
          </button>
          <NavLink to={profilePath} aria-label="Profile" className={({ isActive }) => `${itemClass} ${isActive ? 'font-semibold text-accent' : ''}`}>
            <Avatar name={me.username} url={me.avatarUrl} size={24} />
            <span className="hidden xl:inline">Profile</span>
          </NavLink>
        </nav>
        <button onClick={toggle} aria-label={theme === 'dark' ? 'Use light theme' : 'Use dark theme'} className={itemClass}>
          {theme === 'dark' ? <Sun className="size-6 shrink-0" /> : <Moon className="size-6 shrink-0" />}
          <span className="hidden xl:inline">{theme === 'dark' ? 'Light theme' : 'Dark theme'}</span>
        </button>
        <button onClick={logout} aria-label="Log out" className={itemClass}>
          <LogOut className="size-6 shrink-0" />
          <span className="hidden xl:inline">Log out</span>
        </button>
      </aside>

      <header className="sticky top-0 z-20 flex h-14 items-center justify-between border-b border-line bg-bg/90 px-4 backdrop-blur md:hidden">
        <NavLink to="/" aria-label="Gamergram home">
          <Logo />
        </NavLink>
        <div className="flex items-center gap-1">
          <button onClick={toggle} aria-label={theme === 'dark' ? 'Use light theme' : 'Use dark theme'} className="p-2">
            {theme === 'dark' ? <Sun className="size-5" /> : <Moon className="size-5" />}
          </button>
          <button onClick={logout} aria-label="Log out" className="p-2">
            <LogOut className="size-5" />
          </button>
        </div>
      </header>

      <main className="pb-20 md:pb-0">
        <Outlet />
      </main>

      <nav className="fixed inset-x-0 bottom-0 z-20 flex items-center justify-around border-t border-line bg-bg pb-[env(safe-area-inset-bottom)] md:hidden">
        <NavLink to="/" end aria-label="Home" className={({ isActive }) => `p-3 ${isActive ? 'text-accent' : ''}`}>
          <Home className="size-6" />
        </NavLink>
        <NavLink to="/explore" aria-label="Explore" className={({ isActive }) => `p-3 ${isActive ? 'text-accent' : ''}`}>
          <Compass className="size-6" />
        </NavLink>
        <button onClick={() => setCreating(true)} aria-label="Create" className="p-3">
          <PlusSquare className="size-6" />
        </button>
        <NavLink to={profilePath} aria-label="Profile" className="p-3">
          <Avatar name={me.username} url={me.avatarUrl} size={26} />
        </NavLink>
      </nav>

      {creating && <CreatePostDialog onClose={() => setCreating(false)} />}
    </div>
  )
}
