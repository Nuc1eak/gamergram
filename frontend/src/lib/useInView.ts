import { useEffect, useRef, useState } from 'react'

export function useInView<T extends Element>(rootMargin = '0px', threshold = 0) {
  const ref = useRef<T>(null)
  const [inView, setInView] = useState(false)

  useEffect(() => {
    const element = ref.current
    if (!element) return
    const observer = new IntersectionObserver(([entry]) => setInView(entry.isIntersecting), {
      rootMargin,
      threshold,
    })
    observer.observe(element)
    return () => observer.disconnect()
  }, [rootMargin, threshold])

  return { ref, inView }
}
