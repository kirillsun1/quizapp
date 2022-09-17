import React from 'react'
import { useSelector } from 'react-redux'
import { State } from '../state/store'
import { QContainer, QStack } from '../../../core/components'
import { OngoingQuizStatus } from '../../../domain/Room'
import { SelectRoom } from './SelectRoom'
import { RoomHeader } from './RoomHeader'
import { Activity } from './Activities'

export function PlayerMode() {
  const room = useSelector((state: State) => state.game.room)
  const roomStatus = useSelector((state: State) => state.game.room?.ongoingQuiz?.status ?? OngoingQuizStatus.NOT_STARTED)

  if (!room) {
    return <SelectRoom/>
  }
  return (
    <QContainer maxW={'container.lg'}>
      <QStack paddingTop={12} gap={6}>
        <RoomHeader/>
        <Activity quizStatus={roomStatus}/>
      </QStack>
    </QContainer>
  )
}
