import { Route, Routes, useLocation, type Location } from 'react-router-dom'
import { RequireAuth } from './auth/RequireAuth'
import { AppShell } from './components/AppShell'
import { EmptyState } from './components/States'
import { ExplorePage } from './pages/ExplorePage'
import { FeedPage } from './pages/FeedPage'
import { GamePage } from './pages/GamePage'
import { LoginPage } from './pages/LoginPage'
import { PostModal, PostPage } from './pages/PostRoutes'
import { ProfilePage } from './pages/ProfilePage'
import { RegisterPage } from './pages/RegisterPage'
import { Ghost } from 'lucide-react'

export function App() {
  const location = useLocation()
  const background = (location.state as { background?: Location } | null)?.background

  return (
    <>
      <Routes location={background ?? location}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route element={<RequireAuth />}>
          <Route element={<AppShell />}>
            <Route index element={<FeedPage />} />
            <Route path="/explore" element={<ExplorePage />} />
            <Route path="/p/:id" element={<PostPage />} />
            <Route path="/u/:username" element={<ProfilePage />} />
            <Route path="/games/:id" element={<GamePage />} />
            <Route path="*" element={<EmptyState icon={Ghost} title="Page not found" />} />
          </Route>
        </Route>
      </Routes>
      {background && (
        <Routes>
          <Route element={<RequireAuth />}>
            <Route path="/p/:id" element={<PostModal />} />
          </Route>
        </Routes>
      )}
    </>
  )
}
