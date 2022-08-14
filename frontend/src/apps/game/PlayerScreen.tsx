import React from 'react'
import { useSelector } from 'react-redux'
import { State } from './state/store'
import { QHeading, QSimpleGrid, QStack, QText } from '../../core/components'
import { OngoingQuizStatus } from '../../domain/Room'

export function PlayerScreen() {
  const playerName = useSelector((state: State) => state.game.playerName)
  const mode = useSelector((state: State) => state.game.mode)
  const room = useSelector((state: State) => state.game.room)
  const roomStatus = useSelector((state: State) => state.game.room?.ongoingQuiz?.status ?? OngoingQuizStatus.NOT_STARTED)
  if (mode !== 'player' || !room) {
    return null
  }

  const playersToShow = room.players.filter(p => p !== playerName)

  return (
    <QStack>
      <QHeading marginTop={12} marginBottom={6}>
        Welcome to the room!
      </QHeading>
      <QHeading size={'xs'}>Moderator: {room.moderator}</QHeading>
      <QHeading size={'xs'}>Players:</QHeading>
      <QSimpleGrid columns={[1, 2, 3]} spacing={1}>
        {playersToShow.map(p => <QText key={p}>{p}</QText>)}
      </QSimpleGrid>
      {
        roomStatus === OngoingQuizStatus.NOT_STARTED
          ? <QHeading size={'xs'}>Waiting for the moderator to start...</QHeading>
          : null
      }
    </QStack>
  )
}