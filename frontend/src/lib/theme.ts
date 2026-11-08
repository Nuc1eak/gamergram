import { useState } from 'react'

type Theme = 'dark' | 'light'
const KEY = 'gamergram.theme'

function readTheme(): Theme {
  try {
    return localStorage.getItem(KEY) === 'light' ? 'light' : 'dark'
  } catch {
    return 'dark'
  }
}

export function applyStoredTheme() {
  document.documentElement.dataset.theme = readTheme()
}

export function useTheme() {
  const [theme, setTheme] = useState<Theme>(readTheme)

  const toggle = () => {
    const next = theme === 'dark' ? 'light' : 'dark'
    setTheme(next)
    document.documentElement.dataset.theme = next
    try {
      localStorage.setItem(KEY, next)
    } catch {
      // The theme still applies for this session.
    }
  }

  return { theme, toggle }
}
