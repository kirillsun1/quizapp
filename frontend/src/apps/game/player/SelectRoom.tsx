import React, { useState } from 'react'
import { QButton, QCenter, QHeading, QInput, QStack } from '../../../core/components'
import { actions } from '../state/gameSlice'
import { useDispatch } from 'react-redux'
import { Action, ThunkDispatch } from '@reduxjs/toolkit'
import { State } from '../state/store'

export function SelectRoom() {
  const [roomCode, setRoomCode] = useState('')
  const dispatch = useDispatch<ThunkDispatch<State, {}, Action>>()
  const joinRoom = () => {
    dispatch(actions.joinRoom(roomCode))
  }

  return (
    <QCenter minH={'100vh'}>
      <QStack w={'xl'} px={'2'}>
        <QHeading marginBottom={6}>Join a room</QHeading>

        <QStack maxW={'2xl'} direction={'row'}>
          <QInput
            placeholder={'Enter room PIN here'}
            value={roomCode}
            maxLength={30}
            onChange={event => setRoomCode(event.target.value)}
          />
          <QButton onClick={joinRoom} disabled={!roomCode}>Go!</QButton>
        </QStack>
      </QStack>
    </QCenter>
  )
}