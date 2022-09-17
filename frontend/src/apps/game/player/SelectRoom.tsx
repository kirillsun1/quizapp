import React, { useState } from 'react'
import { QBox, QCenter, QHeading, QHStack, QPinInput, QPinInputField, QStack, useToast } from '../../../core/components'
import { actions } from '../state/gameSlice'
import { useDispatch, useSelector } from 'react-redux'
import { Action, ThunkDispatch } from '@reduxjs/toolkit'
import { State } from '../state/store'

export function SelectRoom() {
  const name = useSelector((state: State) => state.game.playerName)
  const joiningRoom = useSelector((state: State) => state.game.loadings.joinRoom)
  const [roomCode, setRoomCode] = useState('')
  const dispatch = useDispatch<ThunkDispatch<State, {}, Action>>()
  const toasts = useToast()

  const onJoiningFailed = () => {
    setRoomCode('')
    toasts({
      status: 'error',
      title: 'Couldn\'t join room',
      description: 'Please check PIN and try again',
    })
  }
  const joinRoom = (roomCode: string) => {
    toasts.closeAll()
    dispatch(actions.joinRoom({roomCode, onFailure: onJoiningFailed}))
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

          <QHStack>
            <QPinInput
              autoFocus
              value={roomCode}
              onChange={setRoomCode}
              size={'lg'}
              isDisabled={joiningRoom}
              onComplete={joinRoom}
            >
              <QPinInputField/>
              <QPinInputField/>
              <QPinInputField/>
              <QPinInputField/>
              <QPinInputField/>
              <QPinInputField/>
            </QPinInput>
          </QHStack>
        </QStack>
      </QBox>
    </QCenter>
  )
}