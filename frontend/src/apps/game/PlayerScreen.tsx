import React from 'react'
import { useSelector } from 'react-redux'
import { State } from './state/store'
import { QText } from '../../core/components'

export function PlayerScreen() {
  const mode = useSelector((state: State) => state.game.mode)
  const room = useSelector((state: State) => JSON.stringify(state.game.room))
  if (mode !== 'player') {
    return null
  }

  return (
    <QText>
      You are in room {room} as player
    </QText>
  )
}