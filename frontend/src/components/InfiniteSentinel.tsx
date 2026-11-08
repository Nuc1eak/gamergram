import { useEffect } from 'react'
import { useInView } from '../lib/useInView'
import { Spinner } from './States'

interface InfiniteSentinelProps {
  hasNextPage: boolean
  isFetching: boolean
  fetchNextPage: () => void
}

export function InfiniteSentinel({ hasNextPage, isFetching, fetchNextPage }: InfiniteSentinelProps) {
  const { ref, inView } = useInView<HTMLDivElement>('600px')

  useEffect(() => {
    if (inView && hasNextPage && !isFetching) fetchNextPage()
  }, [inView, hasNextPage, isFetching, fetchNextPage])

  return <div ref={ref}>{hasNextPage && <Spinner />}</div>
}
