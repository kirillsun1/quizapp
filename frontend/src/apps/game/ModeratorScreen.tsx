import React from 'react'
import { useSelector } from 'react-redux'
import { State } from './state/store'
import { QButton, QHeading, QSimpleGrid, QStack, QText } from '../../core/components'

export function ModeratorScreen() {
  const mode = useSelector((state: State) => state.game.mode)
  const room = useSelector((state: State) => state.game.room)
  if (mode !== 'moderator' || !room) {
    return null
  }

  return (
    <QStack>
      <QHeading marginTop={12} marginBottom={6}>
        Room ready! Invite players to have fun!
      </QHeading>
      <QHeading size={'xs'}>Code: {room.code}</QHeading>
      <QHeading size={'xs'}>Players:</QHeading>
      <QSimpleGrid columns={[1, 2, 3]} spacing={1}>
        {room.players.map(p => <QText key={p}>{p}</QText>)}
      </QSimpleGrid>
      {
        !room.ongoingQuiz
          ? (
            <QHeading size={'xs'}>You need to select a quiz:</QHeading>
          )
          : null

      }
      <QButton disabled={true}>
        Start
      </QButton>
    </QStack>
  )
}