import React from 'react'
import { QBox, QFlex, QHeading, QText } from '../../../core/components'
import { useSelector } from 'react-redux'
import { State } from '../state/store'

export function RoomHeader() {
  const room = useSelector((state: State) => state.game.room)
  if (!room) {
    throw new Error('Room header needs a room!')
  }

  return (
    <QBox
      background={'white'}
      paddingY={8}
      paddingX={16}
      borderRadius={36}
      boxShadow="xl"
    >
      <QFlex
        direction={'row'}
        justifyContent={'space-between'}
        alignItems={'center'}
        wrap={'wrap'}
      >
        <QBox>
          <QHeading marginBottom={2}>Room</QHeading>
        </QBox>

        <QFlex alignItems={'center'}>
          <QBox mr={12}>
            <QText fontWeight="bold">{room.code}</QText>
            <QText fontSize="sm">Room</QText>
          </QBox>
          <QBox mr={12}>
            <QText fontWeight="bold">{room.moderator}</QText>
            <QText fontSize="sm">Moderator</QText>
          </QBox>
          <QBox mr={12}>
            <QText fontWeight="bold">{room.players.length}</QText>
            <QText fontSize="sm">Player(s)</QText>
          </QBox>
        </QFlex>
      </QFlex>
    </QBox>
  )
}