import React from 'react'
import { useSelector } from 'react-redux'
import type { State } from './state/store'

type Props = {
  children?: React.ReactNode;
}

export function GameServerStatus({children}: Props) {
  const status = useSelector<State, State['gameServer']>(state => state.gameServer)

  if (status.connecting) {
    return <p>Connecting...</p>
  }
  if (status.error) {
    return <p>Oops. We are broken.</p>
  }

  return (
    <>
      {children}
    </>
  )
}