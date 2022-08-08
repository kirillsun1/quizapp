import { useSelector } from 'react-redux'
import { State } from './state/store'
import { QText, QVStack } from '../../core/components'
import React from 'react'

export function PlayerWelcome() {
  const currentName = useSelector((state: State) => state.game.playerName)
  const roomCode = useSelector((state: State) => state.game.room?.code)

  if (!currentName) {
    return null
  }
  if (roomCode) {
    return null
  }
  return (
    <QVStack>
      <QText>Hi, {currentName}!</QText>
    </QVStack>
  )
}
