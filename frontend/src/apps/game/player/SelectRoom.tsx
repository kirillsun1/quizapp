import React, { useState } from 'react'
import { QBox, QButton, QCenter, QHeading, QInput, QStack } from '../../../core/components'
import { actions } from '../state/gameSlice'
import { useDispatch, useSelector } from 'react-redux'
import { Action, ThunkDispatch } from '@reduxjs/toolkit'
import { State } from '../state/store'

export function SelectRoom() {
  const name = useSelector((state: State) => state.game.playerName)
  const joiningRoom = useSelector((state: State) => state.game.loadings.joinRoom)
  const [roomCode, setRoomCode] = useState('')
  const dispatch = useDispatch<ThunkDispatch<State, {}, Action>>()
  const joinRoom = () => {
    dispatch(actions.joinRoom(roomCode))
  }

  return (
    <QCenter minH={'100vh'}>
      <QBox
        background={'white'}
        padding={16}
        borderRadius={36}
        boxShadow="xl"
      >
        <QStack w={'xl'} px={'2'}>
          <QBox marginBottom={6}>
            <QHeading marginBottom={2}>Nice to meet you, {name}!</QHeading>
            <QHeading size={'md'}>Enter Room PIN below to join a room.</QHeading>
          </QBox>

          <QStack maxW={'2xl'} direction={'row'}>
            <QInput
              disabled={joiningRoom}
              placeholder={'Room PIN'}
              value={roomCode}
              maxLength={30}
              onChange={event => setRoomCode(event.target.value)}
            />
            <QButton
              isLoading={joiningRoom}
              disabled={!roomCode}
              onClick={joinRoom}
            >
              Enter
            </QButton>
          </QStack>
        </QStack>
      </QBox>
    </QCenter>
  )
}