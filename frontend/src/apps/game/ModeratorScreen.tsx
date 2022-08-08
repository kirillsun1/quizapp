import React from 'react'
import { useSelector } from 'react-redux'
import { State } from './state/store'
import { QText } from '../../core/components'

export function ModeratorScreen() {
  const mode = useSelector((state: State) => state.game.mode)
  const room = useSelector((state: State) => JSON.stringify(state.game.room))
  if (mode !== 'moderator') {
    return null
  }

  return (
    <QText>
      {room}

      Assign quiz:
    </QText>
  )
}